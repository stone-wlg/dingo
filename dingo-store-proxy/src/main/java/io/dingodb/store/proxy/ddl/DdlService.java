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

package io.dingodb.store.proxy.ddl;

import com.google.auto.service.AutoService;
import io.dingodb.common.CommonId;
import io.dingodb.common.ddl.ActionType;
import io.dingodb.common.ddl.DdlJob;
import io.dingodb.common.log.LogUtils;
import io.dingodb.common.meta.SchemaInfo;
import io.dingodb.common.meta.SchemaState;
import io.dingodb.common.partition.PartitionDetailDefinition;
import io.dingodb.common.sequence.SequenceDefinition;
import io.dingodb.meta.DdlServiceProvider;
import io.dingodb.meta.InfoSchemaService;
import io.dingodb.meta.ddl.InfoSchemaBuilder;
import io.dingodb.meta.entity.InfoCache;
import io.dingodb.meta.entity.InfoSchema;
import io.dingodb.meta.entity.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DdlService extends DdlHandler implements io.dingodb.meta.DdlService {

    public static final DdlService ROOT = new DdlService();

    @AutoService(DdlServiceProvider.class)
    public static class Provider implements DdlServiceProvider {
        @Override
        public io.dingodb.meta.DdlService root() {
            return ROOT;
        }
    }

    public void renameTable(long schemaId, String schemaName, long tableId, String tableName, String toName) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .schemaName(schemaName)
            .tableId(tableId)
            .tableName(tableName)
            .actionType(ActionType.ActionRenameTable)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(toName);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    public void renameIndex(long schemaId, String schemaName, long tableId, String tableName,
        String originIndexName, String toIndexName) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .schemaName(schemaName)
            .tableId(tableId)
            .tableName(tableName)
            .actionType(ActionType.ActionRenameIndex)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(originIndexName);
        args.add(toIndexName);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    public void alterModifyComment(long schemaId, String schemaName, long tableId, String tableName, String comment) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .schemaName(schemaName)
            .tableId(tableId)
            .tableName(tableName)
            .actionType(ActionType.ActionModifyTableComment)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(comment);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    @Override
    public void createSequence(SequenceDefinition sequenceDefinition, String connId) {
        DdlHandler.createSequence("MYSQL", "SEQUENCE", sequenceDefinition, connId);
    }

    @Override
    public void dropSequence(String sequenceName, String connId) {
        DdlHandler.dropSequence("MYSQL", "SEQUENCE", sequenceName, connId);
    }

    @Override
    public InfoSchema getIsLatest() {
        return InfoCache.infoCache.getLatest();
    }

    public InfoSchema getPointIs(long pointTs) {
        InfoSchemaService infoSchemaService = new io.dingodb.store.service.InfoSchemaService(pointTs);
        List<SchemaInfo> schemaInfoList = infoSchemaService.listSchema();

        InfoSchemaBuilder builder = new InfoSchemaBuilder();
        builder.initWithSchemaInfos(schemaInfoList, 0, infoSchemaService);
        return builder.build();
    }

    @Override
    public Table getTable(String schemaName, String tableName) {
        InfoSchema[] infoSchemas = InfoCache.infoCache.cache;
        for (InfoSchema is : infoSchemas) {
            if (is == null) {
                continue;
            }
            Table obj = is.getTable(schemaName, tableName);
            if (obj != null) {
                return obj;
            }
        }
        InfoSchemaService service = InfoSchemaService.root();
        Object tableObj = service.getTable(schemaName, tableName);

        LogUtils.info(log, "[ddl] ddlService getTable by name from store kv, schemaName:{},tableName:{}, "
            + "tab is null:{}", schemaName, tableName, (tableObj == null));
        return (Table) tableObj;
    }

    @Override
    public Table getTable(CommonId id) {
        InfoSchema[] infoSchemas = InfoCache.infoCache.cache;
        for (InfoSchema is : infoSchemas) {
            if (is == null) {
                continue;
            }
            Table obj;
            if (id.type == CommonId.CommonType.INDEX) {
                obj = is.getIndex(id.domain, id.seq);
            } else {
                obj = is.getTable(id.seq);
            }
            if (obj != null) {
                return obj;
            }
        }
        LogUtils.error(log, "[ddl] ddlService getTable by id from store kv, id:{}", id);
        InfoSchemaService service = InfoSchemaService.root();
        if (id.type == CommonId.CommonType.INDEX) {
            return service.getTableDef(id.domain, id.seq);
        } else {
            return service.getIndexDef(id.domain, id.seq);
        }
    }

    @Override
    public void rebaseAutoInc(String schemaName, String tableName, long tableId, long autoInc) {
        SchemaInfo schemaInfo = InfoSchemaService.root().getSchema(schemaName);
        long schemaId = schemaInfo.getSchemaId();
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .tableId(tableId)
            .schemaName(schemaName)
            .tableName(tableName)
            .actionType(ActionType.ActionRebaseAuto)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(autoInc);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    @Override
    public void resetAutoInc() {
        DdlJob job = DdlJob.builder()
            .actionType(ActionType.ActionResetAutoInc)
            .build();
        DdlHandler.doDdlJob(job);
    }

    public void alterIndexVisible(
        long schemaId, String schemaName, long tableId, String tableName, String index, boolean invisible
    ) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .tableId(tableId)
            .schemaName(schemaName)
            .tableName(tableName)
            .actionType(ActionType.ActionAlterIndexVisibility)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(index);
        args.add(invisible);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    public void alterTableAddPart(
        long schemaId, String schemaName, long tableId, String tableName, PartitionDetailDefinition part
    ) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .tableId(tableId)
            .schemaName(schemaName)
            .tableName(tableName)
            .actionType(ActionType.ActionAddTablePartition)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(part);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    public void alterTableDropPart(long schemaId, String schemaName, long tableId, String tableName, String part) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .tableId(tableId)
            .schemaName(schemaName)
            .tableName(tableName)
            .schemaState(SchemaState.SCHEMA_PUBLIC)
            .actionType(ActionType.ActionDropTablePartition)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(part);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

    public void alterTableTruncatePart(long schemaId, String schemaName, long tableId, String tableName, String part) {
        DdlJob job = DdlJob.builder()
            .schemaId(schemaId)
            .tableId(tableId)
            .schemaName(schemaName)
            .tableName(tableName)
            .actionType(ActionType.ActionTruncateTablePartition)
            .build();
        List<Object> args = new ArrayList<>();
        args.add(part);
        job.setArgs(args);
        DdlHandler.doDdlJob(job);
    }

}
