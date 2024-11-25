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

package io.dingodb.exec.operator.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dingodb.codec.CodecService;
import io.dingodb.codec.KeyValueCodec;
import io.dingodb.common.CommonId;
import io.dingodb.common.type.TupleMapping;
import io.dingodb.exec.dag.Vertex;
import io.dingodb.exec.expr.SqlExpr;
import io.dingodb.meta.entity.Column;
import io.dingodb.meta.entity.Table;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@JsonTypeName("index")
@JsonPropertyOrder({"indexTableId", "schema", "keyMapping", "filter", "selection"})
public class GetByIndexParam extends FilterProjectParam {

    @JsonProperty("indexTableId")
    @JsonSerialize(using = CommonId.JacksonSerializer.class)
    @JsonDeserialize(using = CommonId.JacksonDeserializer.class)
    private final CommonId indexTableId;
    @JsonProperty("isLookup")
    private final boolean isLookup;
    @JsonProperty("isUnique")
    private final boolean isUnique;
    @JsonProperty("indexDefinition")
    private final Table index;
    private final Table table;
    private final KeyValueCodec codec;
    private transient KeyValueCodec lookupCodec;
    private List<Integer> mapList;

    public GetByIndexParam(
        CommonId indexTableId,
        CommonId tableId,
        TupleMapping keyMapping,
        SqlExpr filter,
        TupleMapping selection,
        boolean isUnique,
        Table index,
        Table table,
        boolean isLookup
    ) {
        super(tableId, table.tupleType(), table.getVersion(), filter, selection, keyMapping);
        this.indexTableId = indexTableId;
        this.isLookup = isLookup;
        this.isUnique = isUnique;
        this.index = index;
        this.table = table;
        this.codec = CodecService.getDefault().createKeyValueCodec(
            index.getVersion(), index.tupleType(), index.keyMapping()
        );
    }

    @Override
    public void init(Vertex vertex) {
        super.init(vertex);
        if (isLookup()) {
            lookupCodec = CodecService.getDefault().createKeyValueCodec(
                table.getVersion(), table.tupleType(), table.keyMapping()
            );
        } else {
            mapList = mapping(selection, table, index);
        }
    }

    private static List<Integer> mapping(TupleMapping selection, Table td, Table index) {
        Integer[] mappings = new Integer[selection.size()];
        for (int i = 0; i < selection.size(); i ++) {
            Column column = td.getColumns().get(selection.get(i));
            mappings[i] = index.getColumns().indexOf(column);
        }
        return Arrays.asList(mappings);
    }
}
