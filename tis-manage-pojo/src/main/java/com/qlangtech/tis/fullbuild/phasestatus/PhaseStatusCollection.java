/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.fullbuild.phasestatus;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class PhaseStatusCollection implements IPhaseStatusCollection {
    private static final LoadingCache<Integer, PhaseStatusCollection> taskPhaseReference =
            CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES) //
                    .build(new CacheLoader<>() {
                        @Override
                        public PhaseStatusCollection load(Integer taskId) throws Exception {
                            return loadPhaseStatusFromLocal(taskId);
                        }
                    });
    //    private static final Map<Integer, PhaseStatusCollection> /*** taskid*/
    //            taskPhaseReference = new HashMap<>(); //new WeakHashMap<>();

    public static PhaseStatusCollection initialTaskPhase(Integer taskid) {
        PhaseStatusCollection statusCollection = new PhaseStatusCollection(taskid, ExecutePhaseRange.fullRange());
        return initialTaskPhase(statusCollection);
    }

    public static PhaseStatusCollection initialTaskPhase(PhaseStatusCollection statusCollection) {
        taskPhaseReference.put(statusCollection.getTaskid(), statusCollection);
        return statusCollection;
    }

    public static PhaseStatusCollection getTaskPhaseReference(Integer taskId) {
        try {
            return taskPhaseReference.get(taskId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param taskid
     * @return
     * @throws Exception
     */
    private static PhaseStatusCollection loadPhaseStatusFromLocal(int taskid) {
        PhaseStatusCollection result = null;
        FullbuildPhase[] phases = FullbuildPhase.values();
        try {
            File localFile = null;
            BasicPhaseStatus phaseStatus;
            for (FullbuildPhase phase : phases) {
                localFile = BasicPhaseStatus.getFullBuildPhaseLocalFile(taskid, phase);
                if (!localFile.exists()) {
                    return result;
                }
                if (result == null) {
                    result = new PhaseStatusCollection(taskid, ExecutePhaseRange.fullRange());
                }
                IFlush2Local flush2Local =
                        IFlush2LocalFactory.createNew(PhaseStatusCollection.class.getClassLoader(), localFile).orElseThrow(() -> new IllegalStateException("flush2Local must be present"));
                phaseStatus = flush2Local.loadPhase(); // BasicPhaseStatus.statusWriter.loadPhase(localFile);
                switch (phase) {
                    case FullDump:
                        result.setDumpPhase((DumpPhaseStatus) phaseStatus);
                        break;
                    case JOIN:
                        result.setJoinPhase((JoinPhaseStatus) phaseStatus);
                        break;
                    //                    case BUILD:
                    //                        result.setBuildPhase((BuildPhaseStatus) phaseStatus);
                    //                        break;
                    //                    case IndexBackFlow:
                    //                        result.setIndexBackFlowPhaseStatus((IndexBackFlowPhaseStatus) phaseStatus);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("taskid:" + taskid, e);
        }
        return result;
    }

    private DumpPhaseStatus dumpPhase;

    private JoinPhaseStatus joinPhase;

    private BuildPhaseStatus buildPhase;

    private IndexBackFlowPhaseStatus indexBackFlowPhaseStatus;
    private ExecutePhaseRange executePhaseRange;
    private final Integer taskid;

    public PhaseStatusCollection(Integer taskid, ExecutePhaseRange executePhaseRange) {
        super();
        this.executePhaseRange = executePhaseRange;
        this.taskid = Objects.requireNonNull(taskid, "taskId can not be null");
        this.dumpPhase = new DumpPhaseStatus(taskid);
        this.joinPhase = new JoinPhaseStatus(taskid);
        this.buildPhase = new BuildPhaseStatus(taskid);
        this.indexBackFlowPhaseStatus = new IndexBackFlowPhaseStatus(taskid);
    }
    /**
     * @return
     */
    public boolean isComplete() {
        return (!executePhaseRange.contains(FullbuildPhase.FullDump) || dumpPhase.isComplete()) //
                && (!executePhaseRange.contains(FullbuildPhase.JOIN) || joinPhase.isComplete()); //
                // && (!executePhaseRange.contains(FullbuildPhase.BUILD) || buildPhase.isComplete()) //
               // && (!executePhaseRange.contains(FullbuildPhase.IndexBackFlow) || indexBackFlowPhaseStatus.isComplete());
    }

    public boolean isFaild() {
        return (executePhaseRange.contains(FullbuildPhase.FullDump) && dumpPhase.isFaild()) //
                || (executePhaseRange.contains(FullbuildPhase.JOIN) && joinPhase.isFaild()); //
                // || (executePhaseRange.contains(FullbuildPhase.BUILD) && buildPhase.isFaild()) //
                //|| (executePhaseRange.contains(FullbuildPhase.IndexBackFlow) && indexBackFlowPhaseStatus.isFaild());
    }

    public void flushStatus2Local() {
        if (executePhaseRange.contains(FullbuildPhase.FullDump)) {
            dumpPhase.intervalWriteStatus2Local();
        }
        if (executePhaseRange.contains(FullbuildPhase.JOIN)) {
            joinPhase.intervalWriteStatus2Local();
        }
//        if (executePhaseRange.contains(FullbuildPhase.BUILD)) {
//            buildPhase.intervalWriteStatus2Local();
//        }
//        if (executePhaseRange.contains(FullbuildPhase.IndexBackFlow)) {
//            indexBackFlowPhaseStatus.intervalWriteStatus2Local();
//        }
    }



    public Integer getTaskid() {
        return this.taskid;
    }

    public DumpPhaseStatus getDumpPhase() {
        return this.dumpPhase;
    }

    public void setDumpPhase(DumpPhaseStatus dumpPhase) {
        this.dumpPhase = dumpPhase;
    }

    public JoinPhaseStatus getJoinPhase() {
        return this.joinPhase;
    }

    public void setJoinPhase(JoinPhaseStatus joinPhase) {
        this.joinPhase = joinPhase;
    }

    @JSONField(serialize = false)
    public BuildPhaseStatus getBuildPhase() {
        return this.buildPhase;
    }

    @JSONField(serialize = false)
    public IndexBackFlowPhaseStatus getIndexBackFlowPhaseStatus() {
        return this.indexBackFlowPhaseStatus;
    }

    public void setBuildPhase(BuildPhaseStatus buildPhase) {
        this.buildPhase = buildPhase;
    }

    public void setIndexBackFlowPhaseStatus(IndexBackFlowPhaseStatus indexBackFlowPhaseStatus) {
        this.indexBackFlowPhaseStatus = indexBackFlowPhaseStatus;
    }
}
