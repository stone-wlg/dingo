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
import io.dingodb.common.CommonId;
import io.dingodb.common.type.DingoType;
import io.dingodb.common.type.TupleMapping;
import io.dingodb.meta.entity.Table;
import lombok.Getter;

@Getter
@JsonTypeName("pessimistic_lock")
@JsonPropertyOrder({"isolationLevel", "startTs", "forUpdateTs", "lockTimeOut",
    "pessimisticTxn", "isInsert", "table", "schema", "keyMapping"})
public class PessimisticLockParam extends TxnPartModifyParam {

    @JsonProperty("isInsert")
    protected final boolean isInsert;
    @JsonProperty("isScan")
    private final boolean isScan;
    @JsonProperty("opType")
    private final String opType;
    @JsonProperty("isDuplicateKeyUpdate")
    private final boolean isDuplicateUpdate;
    public PessimisticLockParam(
        @JsonProperty("table") CommonId tableId,
        @JsonProperty("schema") DingoType schema,
        @JsonProperty("keyMapping") TupleMapping keyMapping,
        @JsonProperty("isolationLevel") int isolationLevel,
        @JsonProperty("startTs") long startTs,
        @JsonProperty("forUpdateTs") long forUpdateTs,
        @JsonProperty("pessimisticTxn") boolean pessimisticTxn,
        @JsonProperty("primaryLockKey") byte[] primaryLockKey,
        @JsonProperty("lockTimeOut") long lockTimeOut,
        @JsonProperty("isInsert") boolean isInsert,
        @JsonProperty("isScan") boolean isScan,
        @JsonProperty("opType") String opType,
        Table table,
        boolean isDuplicateUpdate
    ) {
        super(tableId, schema, keyMapping, table, pessimisticTxn,
            isolationLevel, primaryLockKey, startTs, forUpdateTs, lockTimeOut);
        this.isInsert = isInsert;
        this.isScan = isScan;
        this.opType = opType;
        this.isDuplicateUpdate = isDuplicateUpdate;
    }
    public void inc() {
        count++;
    }
}
