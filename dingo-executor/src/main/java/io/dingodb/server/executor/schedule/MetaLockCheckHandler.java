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

package io.dingodb.server.executor.schedule;

import io.dingodb.common.ddl.DdlJobEventSource;
import io.dingodb.common.ddl.DdlUtil;
import io.dingodb.common.ddl.MdlCheckMdlVerListenerImpl;
import io.dingodb.common.ddl.MdlCheckTableInfo;
import io.dingodb.common.environment.ExecutionEnvironment;
import io.dingodb.common.log.LogUtils;
import io.dingodb.common.metrics.DingoMetrics;
import io.dingodb.common.util.Utils;
import io.dingodb.server.executor.ddl.DdlContext;
import io.dingodb.server.executor.session.SessionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class MetaLockCheckHandler {
    static volatile long saveMaxSchemaVersion = 0;
    static volatile boolean jobNeedToSync = false;
    static volatile Map<Long, Long> jobCache = new ConcurrentHashMap<>();

    private MetaLockCheckHandler() {
    }

    public static void watchCheckMdlVer() {
        MdlCheckMdlVerListenerImpl checkListener
            = new MdlCheckMdlVerListenerImpl(MetaLockCheckHandler::checkMdlVersionByLocal);
        DdlJobEventSource ddlJobEventSource = DdlJobEventSource.ddlJobEventSource;
        ddlJobEventSource.addMdlCheckVerListener(checkListener);
    }

    private static Boolean checkMdlVersionByLocal(EventObject event) {
        checkMdlVersion();
        return true;
    }

    public static void mdlCheckLoop() {
        watchCheckMdlVer();
        while (!Thread.interrupted()) {
            if (!DdlUtil.mdlEnable) {
                continue;
            }
            Utils.sleep(50);
            checkMdlVersion();
        }
    }

    private static synchronized void checkMdlVersion() {
        try {
            MdlCheckTableInfo mdlCheckTableInfo = ExecutionEnvironment.INSTANCE.mdlCheckTableInfo;
            mdlCheckTableInfo.wLock();
            long maxVer = mdlCheckTableInfo.getNewestVer();
            if (maxVer > saveMaxSchemaVersion) {
                saveMaxSchemaVersion = maxVer;
            } else if (!jobNeedToSync) {
                if (DdlUtil.timeOutError.get()) {
                    LogUtils.info(log, "[ddl] mdl check not need to sync,max ver:{} saveMaxSchema ver:{}",
                        maxVer, saveMaxSchemaVersion);
                }
                DingoMetrics.counter("jobNeedToSync").inc();
            }
            int jobNeedToCheckCnt = mdlCheckTableInfo.getJobsVerMap().size();
            if (jobNeedToCheckCnt == 0) {
                jobNeedToSync = false;
                //LogUtils.info(log, "[ddl] mdl check job need to check cnt is 0,max ver:{} saveMaxSchema ver:{}",
                //    maxVer, saveMaxSchemaVersion);
                mdlCheckTableInfo.wUnlock();
                return;
            }
            Map<Long, String> jobsIdsMap = new HashMap<>(mdlCheckTableInfo.getJobsIdsMap());
            Map<Long, Long> jobsVerMap = new HashMap<>(mdlCheckTableInfo.getJobsVerMap());
            long maxMdlInfoVer = jobsVerMap.values().stream().mapToLong(k -> k).max().orElse(0);
            mdlCheckTableInfo.wUnlock();
            jobNeedToSync = true;
            SessionManager.checkOldRunningTxn(jobsVerMap, jobsIdsMap);
            if (jobsVerMap.size() == jobNeedToCheckCnt && (maxMdlInfoVer >= maxVer)) {
                jobNeedToSync = false;
            }
            if (maxVer > maxMdlInfoVer) {
                LogUtils.info(log, "maxVer > maxMdlVer, maxVer:{}, maxMdlVer:{}, jobNeedToSync:{}",
                    maxVer, maxMdlInfoVer, jobNeedToSync);
            }
            if (jobCache.size() > 1000) {
                jobCache = new HashMap<>();
            }
            if (DdlUtil.timeOutError.get()) {
                LogUtils.info(log, "[ddl] mdl check jobs id map size:{}, "
                    + "jobs ver map size:{}, jobNeedToSync:{}", jobsIdsMap.size(),
                    jobsVerMap.size(), jobNeedToSync);
            }

            if (jobsVerMap.isEmpty()) {
                return;
            }
            for (Map.Entry<Long, Long> entry : jobsVerMap.entrySet()) {
                if (jobCache.containsKey(entry.getKey())
                    && jobCache.get(entry.getKey()) >= entry.getValue()
                ) {
                    continue;
                }
                LogUtils.info(log, "mdl gets lock, update to owner, jobId:{}, version:{}, save ver:{}, jobNeedSync:{}",
                    entry.getKey(), entry.getValue(), saveMaxSchemaVersion, jobNeedToSync);
                try {
                    DdlContext.INSTANCE.getSchemaSyncer().updateSelfVersion(System.identityHashCode(entry),
                        entry.getKey(), entry.getValue());
                    jobCache.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    LogUtils.warn(log, "update self version failed, reason:{}", e.getMessage());
                }
            }
        } catch (Exception e) {
            LogUtils.error(log, "mdlCheckLoop error, error:" + e.getMessage(), e);
        }
    }
}
