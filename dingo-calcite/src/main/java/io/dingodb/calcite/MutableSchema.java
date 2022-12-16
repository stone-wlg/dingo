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

package io.dingodb.calcite;

import io.dingodb.common.table.TableDefinition;
import io.dingodb.meta.MetaService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
public abstract class MutableSchema extends AbstractSchema {
    @Getter
    protected final MetaService metaService;

    protected MutableSchema(MetaService metaService) {
        this.metaService = metaService;
    }

    public void createTable(@NonNull String tableName, @NonNull TableDefinition tableDefinition) {
        log.info(" create metaservice: " + metaService);
        metaService.createTable(tableName, tableDefinition);
    }

    public boolean dropTable(@NonNull String tableName) {
        return metaService.dropTable(tableName);
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}
