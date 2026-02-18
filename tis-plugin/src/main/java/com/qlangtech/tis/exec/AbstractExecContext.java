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

package com.qlangtech.tis.exec;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.exec.impl.DataXPipelineExecContext;
import com.qlangtech.tis.exec.impl.WorkflowExecContext;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.order.center.IAppSourcePipelineController;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshot;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshotUtils;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.powerjob.TriggersConfig;
import com.qlangtech.tis.sql.parser.TabPartitions;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-03-19 15:03
 **/
public abstract class AbstractExecContext implements IExecChainContext, IdentityName {
    private ITISCoordinator coordinator;
    private ExecutePhaseRange executePhaseRange;
    private boolean dryRun;
   // private PhaseStatusCollection latestPhaseStatusCollection;
    // private StoreResourceType resType;
    private File specifiedLocalLoggerPath;
    /**
     * 执行DataX 任务时候，是否不需要连接远端GRPC服务
     */
    private boolean disableGrpcRemoteServerConnect;

    private final long ps;

    public static void deserializeInstanceParams(TriggersConfig triggerCfg, JSONObject instanceParams, boolean resolveCfgsSnapshotConsumer, Consumer<AbstractExecContext> execChainContextConsumer, Consumer<PluginAndCfgsSnapshot> cfgsSnapshotConsumer) {
    }

    @Override
    public long getPartitionTimestampWithMillis() {
        return ps;
    }

    public AbstractExecContext(long triggerTimestamp) {
        this.ps = Objects.requireNonNull(triggerTimestamp, "param triggerTimestamp can not be null");
        ExecChainContextUtils.setDependencyTablesPartitions(this, new TabPartitions(Maps.newHashMap()));
    }

    /**
     * @param instanceParams
     * @return
     * @see IExecChainContext#createInstanceParams Params set in this method
     */
    public static PluginAndCfgsSnapshot resolveCfgsSnapshotConsumer(StoreResourceType resourceType, JSONObject instanceParams) {
        return null;
    }

    public void putTablePt(IDumpTable table, ITabPartition pt) {
        ExecChainContextUtils.getDependencyTablesPartitions(this).putPt(table, pt);
    }

    @Override
    public boolean isDisableGrpcRemoteServerConnect() {
        return disableGrpcRemoteServerConnect;
    }

    public void setDisableGrpcRemoteServerConnect(boolean val) {
        this.disableGrpcRemoteServerConnect = val;
    }

    private RemoteTaskTriggers tskTriggers;

    @Override
    public void setTskTriggers(RemoteTaskTriggers tskTriggers) {
        this.tskTriggers = tskTriggers;
    }

    @Override
    public RemoteTaskTriggers getTskTriggers() {
        return Objects.requireNonNull(this.tskTriggers, "tskTriggers can not be null");
    }

    @Override
    public File getSpecifiedLocalLoggerPath() {
        return this.specifiedLocalLoggerPath;
    }

    public void setSpecifiedLocalLoggerPath(File specifiedLocalLoggerPath) {
        this.specifiedLocalLoggerPath = specifiedLocalLoggerPath;
    }

    @Override
    public boolean isDryRun() {
        return this.dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * java 启动内存参数ms mx
     */
    private String javaMemSpec;

    public abstract StoreResourceType getResType();

    public abstract IDataxProcessor getProcessor();

    @Override
    public ITISCoordinator getZkClient() {
        return this.coordinator;
    }

    public void setCoordinator(ITISCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void setExecutePhaseRange(ExecutePhaseRange executePhaseRange) {
        this.executePhaseRange = executePhaseRange;
    }

    @Override
    public String getJavaMemSpec() {
        return javaMemSpec;
    }

    public void setJavaMemSpec(String javaMemSpec) {
        this.javaMemSpec = javaMemSpec;
    }



    @Override
    public ExecutePhaseRange getExecutePhaseRange() {
        return this.executePhaseRange;
    }

    @Override
    public ITISFileSystem getIndexBuildFileSystem() {
        throw new UnsupportedOperationException();
    }

    //    @Override
    //    public void rebindLoggingMDCParams() {
    //        throw new UnsupportedOperationException();
    //    }


    @Override
    public int getIndexShardCount() {
        throw new UnsupportedOperationException();
    }

    private final Map<String, Object> attribute = new HashMap<>();

    @Override
    public <T> T getAttribute(String key) {
        return (T) this.attribute.get(key);
    }

    @Override
    public <T> T getAttribute(String key, Supplier<T> creator) {
        synchronized (attribute) {
            T attr = getAttribute(key);
            if (attr == null) {
                attr = creator.get();
                this.setAttribute(key, attr);
            }
            return attr;
        }
    }

    //  private Map<String, Object> attrs = new HashMap<>();

    @Override
    public void setAttribute(String key, Object v) {
        this.attribute.put(key, v);
    }

    @Override
    public IAppSourcePipelineController getPipelineController() {
        return null;
    }

    @Override
    public Integer getTaskId() {
        Integer taskId = Objects.requireNonNull(getAttribute(JobCommon.KEY_TASK_ID),
                JobCommon.KEY_TASK_ID + " can " + "not be null");
        return taskId;
    }

//    public void setLatestPhaseStatusCollection(PhaseStatusCollection latestPhaseStatusCollection) {
//        this.latestPhaseStatusCollection = latestPhaseStatusCollection;
//    }
//
//    @Override
//    public PhaseStatusCollection loadPhaseStatusFromLatest() {
//        return this.latestPhaseStatusCollection;
//    }


    @Override
    public void cancelTask() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Integer getWorkflowId() {
        //  return this.workflowId;
        throw new UnsupportedOperationException();
    }

    @Override
    public String getWorkflowName() {
        // return this.workflowName;
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String key) {
        throw new UnsupportedOperationException();
    }
}
