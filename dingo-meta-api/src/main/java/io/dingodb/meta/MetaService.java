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

package io.dingodb.meta;

import io.dingodb.common.CommonId;
import io.dingodb.common.Location;
import io.dingodb.common.config.DingoConfiguration;
import io.dingodb.common.meta.Tenant;
import io.dingodb.common.partition.PartitionDetailDefinition;
import io.dingodb.common.partition.RangeDistribution;
import io.dingodb.common.table.Index;
import io.dingodb.common.table.IndexDefinition;
import io.dingodb.common.table.TableDefinition;
import io.dingodb.common.util.ByteArrayUtils.ComparableByteArray;
import io.dingodb.meta.entity.Table;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public interface MetaService {

    static MetaService root() {
        return MetaServiceProvider.getDefault().root();
    }

    static MetaService root(long ts) {
        if (ts > 0) {
            return snapshot(ts);
        } else {
            return root();
        }
    }

    static MetaService snapshot(long ts) {
        return MetaServiceProvider.getDefault().snapshot(ts);
    }

    String DINGO_NAME = "DINGO";

    /**
     * Returns this meta service id.
     *
     * @return this id
     */
    CommonId id();

    /**
     * Returns this meta service name.
     *
     * @return this name
     */
    String name();

    default boolean isRoot() {
        return id().equals(root().id());
    }

    /**
     * Create sub meta service.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param name sub meta service name
     */
    void createSubMetaService(String name);

    /**
     * Get all sub meta services.
     *
     * @return all sub meta services
     */
    <M extends MetaService> Map<String, M> getSubMetaServices();

    /**
     * Get sub meta service by name.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param name meta service name
     * @return sub meta service
     */
    MetaService getSubMetaService(String name);

    /**
     * Drop sub meta service by name.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param name meta service name
     * @return true if success
     */
    boolean dropSubMetaService(String name);

    /**
     * Create and save table meta, initialize table storage.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param tableName       table name
     * @param tableDefinition table definition
     */
    @Deprecated
    default void createTable(@NonNull String tableName, @NonNull TableDefinition tableDefinition) {
        createTables(tableDefinition, Collections.emptyList());
    }

    default void createView(long schemaId, String viewName, TableDefinition tableDefinition) {

    }

    default long createReplicaTable(long schemaId, Object tableDefinition, String tableName) {
        return 0L;
    }

    default void createIndexReplicaTable(long schemaId, long tableId, Object indexDefinition, String indexName) {

    }

    long createTables(TableDefinition tableDefinition, List<IndexDefinition> indexTableDefinitions);

    default long createTables(
        long schemaId, TableDefinition tableDefinition, List<IndexDefinition> indexTableDefinitions
    ) {
        return 0;
    }

    default void recoverTable(long schemaId, Object tableWithId, List<Object> indexTableList) {

    }

    default void rollbackCreateTable(
        long schemaId,
        @NonNull TableDefinition tableDefinition,
        @NonNull List<IndexDefinition> indexTableDefinitions
    ) {

    }

    /**
     * Drop table meta and table storage.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param tableName table name
     * @return true if success
     */
    boolean dropTable(long schemaId, String tableName, long jobId);

    boolean dropTable(long tenantId, long schemaId, String tableName, long jobId);

    long truncateTable(@NonNull String tableName, long tableEntityId, long jobId);

    default long truncateTable(long schemaId, @NonNull String tableName, long tableEntityId, long jobId) {
        return 0;
    }


    /**
     * Get table by table name.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param tableName table name
     * @return table definition or null if not found.
     */
    Table getTable(String tableName);

    Table getTable(CommonId tableId);

    /**
     * Returns all table.
     *
     * @return all table
     */
    Set<Table> getTables();

    default long addDistribution(String schemaName, String tableName, PartitionDetailDefinition detail) {
        return 0;
    }

    default Object addPart(
        String schemaName, String tableName, PartitionDetailDefinition detail, long partId, Object objWithId
    ) {
        return null;
    }

    default Map<CommonId, Long> getTableCommitCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get range distributions by table id.
     *
     * @param id table id
     * @return table range distributions
     */
    default NavigableMap<ComparableByteArray, RangeDistribution> getRangeDistribution(CommonId id) {
        // todo
        throw new UnsupportedOperationException();
    }

    /**
     * Returns current process location.
     *
     * @return current process location
     */
    default Location currentLocation() {
        return DingoConfiguration.location();
    }

    default void createIndex(String tableName, List<Index> indexList) {
        throw new UnsupportedOperationException();
    }

    default void createIndex(CommonId tableId, String tableName, IndexDefinition index) {
        throw new UnsupportedOperationException();
    }

    default void dropIndex(CommonId table, CommonId index, long jobId, long startTs) {
        throw new UnsupportedOperationException();
    }

    Map<CommonId, TableDefinition> getTableIndexDefinitions(@NonNull CommonId id);

    /**
     * Returns all table definition.
     *
     * @return all table definition
     */
    default Map<String, TableDefinition> getTableDefinitions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get table definition by table name.
     * Notice: check the table name case, because by default, the table names are converted to uppercase
     *
     * @param name table name
     * @return table definition or null if not found.
     */
    default TableDefinition getTableDefinition(@NonNull String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get table definition by table id.
     *
     * @param id table id
     * @return table definition or null if not found.
     */
    default TableDefinition getTableDefinition(@NonNull CommonId id) {
        throw new UnsupportedOperationException();
    }

    Long getAutoIncrement(CommonId tableId);

    Long getNextAutoIncrement(CommonId tableId);

    void updateAutoIncrement(CommonId tableId, long autoIncrementId);

    long getLastId(CommonId tableId);

    default void rebaseAutoInc(CommonId tableId) {

    }

    default void resetAutoInc() {

    }

    default void invalidateDistribution(CommonId tableId) {

    }

    default void dropRegionByTable(CommonId tableId, long jobId, long startTs) {
        dropRegionByTable(tableId, jobId, startTs, false);
    }

    default void dropRegionByTable(CommonId tableId, long jobId, long startTs, boolean autoInc) {

    }

    default void deleteRegion(
        CommonId tableId, long jobId, long startTs, boolean autoInc, Collection<RangeDistribution> rangeDistributions
    ) {

    }

    default void createTenant(Tenant tenant) {

    }

    default void updateTenant(Tenant tenant) {

    }

    default void deleteTenant(long tenantId) {

    }

    default void delAutoInc(Object id) {

    }

    void close();
}
