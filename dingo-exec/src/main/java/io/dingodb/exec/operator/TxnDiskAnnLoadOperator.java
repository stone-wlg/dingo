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

package io.dingodb.exec.operator;

import io.dingodb.common.profile.OperatorProfile;
import io.dingodb.exec.Services;
import io.dingodb.exec.dag.Vertex;
import io.dingodb.exec.operator.params.TxnDiskAnnLoadParam;
import io.dingodb.store.api.StoreInstance;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class TxnDiskAnnLoadOperator extends FilterProjectSourceOperator {

    public static final TxnDiskAnnLoadOperator INSTANCE = new TxnDiskAnnLoadOperator();

    @Override
    protected @NonNull Iterator<Object[]> createSourceIterator(Vertex vertex) {
        TxnDiskAnnLoadParam param = vertex.getParam();
        OperatorProfile profile = param.getProfile("diskAnnLoad");
        long start = System.currentTimeMillis();
        StoreInstance instance = Services.KV_STORE.getInstance(param.getTableId(), param.getPartId());
        String diskAnnResetStatus = instance.diskAnnLoad(param.getScanTs(), param.getIndexId(), param.getNodesCacheNum(), param.isWarmup());
        List<Object[]> results = new ArrayList<>();
        Object[] priTuples = new Object[2];
        priTuples[0] = param.getPartId().seq;
        priTuples[1] = diskAnnResetStatus;
        results.add(priTuples);
        profile.incrTime(start);
        return results.iterator();
    }

}
