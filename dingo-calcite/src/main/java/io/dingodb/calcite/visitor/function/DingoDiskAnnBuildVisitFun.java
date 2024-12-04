/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.calcite.visitor.function;

import io.dingodb.calcite.DingoRelOptTable;
import io.dingodb.calcite.DingoTable;
import io.dingodb.calcite.rel.DingoDiskAnnBuild;
import io.dingodb.calcite.utils.SqlExprUtils;
import io.dingodb.calcite.utils.VisitUtils;
import io.dingodb.calcite.visitor.DingoJobVisitor;
import io.dingodb.common.CommonId;
import io.dingodb.common.Location;
import io.dingodb.common.partition.RangeDistribution;
import io.dingodb.common.type.TupleMapping;
import io.dingodb.common.util.ByteArrayUtils.ComparableByteArray;
import io.dingodb.common.util.Optional;
import io.dingodb.exec.base.IdGenerator;
import io.dingodb.exec.base.Job;
import io.dingodb.exec.base.OutputHint;
import io.dingodb.exec.base.Task;
import io.dingodb.exec.dag.Vertex;
import io.dingodb.exec.expr.SqlExpr;
import io.dingodb.exec.operator.params.TxnDiskAnnBuildParam;
import io.dingodb.exec.transaction.base.ITransaction;
import io.dingodb.meta.MetaService;
import io.dingodb.meta.entity.IndexTable;
import io.dingodb.meta.entity.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNumericLiteral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;

import static io.dingodb.exec.utils.OperatorCodeUtils.TXN_DISK_ANN_BUILD;

@Slf4j
public final class DingoDiskAnnBuildVisitFun {


    private DingoDiskAnnBuildVisitFun() {

    }

    public static Collection<Vertex> visit(
        Job job, IdGenerator idGenerator, Location currentLocation,
        ITransaction transaction, DingoJobVisitor visitor, DingoDiskAnnBuild rel
    ) {
        if (transaction == null) {
            throw new RuntimeException("not support Non-transaction");
        }
        DingoRelOptTable relTable = rel.getTable();
        DingoTable dingoTable = relTable.unwrap(DingoTable.class);

        MetaService metaService = MetaService.root();
        assert dingoTable != null;
        CommonId tableId = dingoTable.getTableId();
        Table td = dingoTable.getTable();

        NavigableMap<ComparableByteArray, RangeDistribution> ranges = metaService.getRangeDistribution(tableId);
        List<Object> operandsList = rel.getOperands();
        String indexName = Objects.requireNonNull((SqlIdentifier) operandsList.get(1)).toString();
        String indexTableName = rel.getIndexTable().getName();
        if(!indexTableName.equalsIgnoreCase(indexName)){
            throw new IllegalArgumentException("Can not find the text index with name: " + indexName);
        }
        long ts = 0L;
        long reginId = 0L;
        if (operandsList.size() == 3) {
            ts = ((Number) Objects.requireNonNull(((SqlNumericLiteral) operandsList.get(2)).getValue())).longValue();
        }
        if (operandsList.size() == 4) {
            ts = ((Number) Objects.requireNonNull(((SqlNumericLiteral) operandsList.get(2)).getValue())).longValue();
            reginId = ((Number) Objects.requireNonNull(((SqlNumericLiteral) operandsList.get(3)).getValue())).longValue();
        }
        List<Vertex> outputs = new ArrayList<>();
        IndexTable indexTable = (IndexTable) rel.getIndexTable();
        RexNode rexFilter = rel.getFilter();
        TupleMapping resultSelection = rel.getSelection();
        long scanTs = VisitUtils.getScanTs(transaction, visitor.getKind());

        SqlExpr filter = null;
        if (rexFilter != null) {
            filter = SqlExprUtils.toSqlExpr(rexFilter);
        }
        // Get all index table distributions
        NavigableMap<ComparableByteArray, RangeDistribution> indexRanges =
            metaService.getRangeDistribution(rel.getIndexTableId());

        // Create tasks based on partitions
        for (RangeDistribution rangeDistribution : indexRanges.values()) {
            if (reginId != 0 && rangeDistribution.id().seq != reginId) {
                continue;
            }
            TxnDiskAnnBuildParam param = new TxnDiskAnnBuildParam(
                rangeDistribution.id(),
                Optional.mapOrNull(filter, SqlExpr::copy),
                resultSelection,
                rel.tupleType(),
                td,
                ranges,
                indexTable,
                scanTs,
                transaction.getIsolationLevel(),
                transaction.getLockTimeOut(),
                ts
                );
            Vertex vertex = new Vertex(TXN_DISK_ANN_BUILD, param);
            Task task = job.getOrCreate(currentLocation, idGenerator);
            OutputHint hint = new OutputHint();
            hint.setPartId(rangeDistribution.id());
            vertex.setHint(hint);
            vertex.setId(idGenerator.getOperatorId(task.getId()));
            task.putVertex(vertex);
            outputs.add(vertex);
        }
        visitor.setScan(true);
        return outputs;
    }

}
