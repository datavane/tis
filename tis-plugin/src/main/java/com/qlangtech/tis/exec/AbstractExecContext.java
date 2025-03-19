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
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.powerjob.SelectedTabTriggersConfig;
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
    private PhaseStatusCollection latestPhaseStatusCollection;
    // private StoreResourceType resType;
    private File specifiedLocalLoggerPath;
    /**
     * 执行DataX 任务时候，是否不需要连接远端GRPC服务
     */
    private boolean disableGrpcRemoteServerConnect;

    private final long ps;

    @Override
    public long getPartitionTimestampWithMillis() {
        return ps;
    }

    public AbstractExecContext(long triggerTimestamp) {
        this.ps = Objects.requireNonNull(triggerTimestamp, "param triggerTimestamp can not be null");
        ExecChainContextUtils.setDependencyTablesPartitions(this, new TabPartitions(Maps.newHashMap()));
    }

    public static PluginAndCfgsSnapshot resolveCfgsSnapshotConsumer(JSONObject instanceParams) {
        String pluginCfgsMetas = instanceParams.getString(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS);
        String appName = instanceParams.getString(JobParams.KEY_COLLECTION);
        if (StringUtils.isEmpty(pluginCfgsMetas)) {
            throw new IllegalStateException("property:"
                    + PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS + " of instanceParams can not be null");
        }

        final Base64 base64 = new Base64();
        try (InputStream manifestJar = new ByteArrayInputStream(base64.decode(pluginCfgsMetas))) {
            return PluginAndCfgsSnapshot.getRepositoryCfgsSnapshot(appName, manifestJar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    /**
     * 反序列化
     *
     * @param instanceParams
     * @return
     */
    static AbstractExecContext deserializeInstanceParams(TriggersConfig triggerCfg, JSONObject instanceParams, boolean resolveCfgsSnapshotConsumer //
            , Consumer<AbstractExecContext> execChainContextConsumer, Consumer<PluginAndCfgsSnapshot> cfgsSnapshotConsumer) {
        Integer taskId = Objects.requireNonNull(instanceParams.getInteger(JobParams.KEY_TASK_ID),
                JobParams.KEY_TASK_ID + " can not be null," + JsonUtil.toString(instanceParams));
        boolean dryRun = instanceParams.getBooleanValue(IFullBuildContext.DRY_RUN);

        final String javaMemSpec = instanceParams.getString(JobParams.KEY_JAVA_MEMORY_SPEC);

        Long triggerTimestamp = instanceParams.getLong(DataxUtils.EXEC_TIMESTAMP);


        AbstractExecContext execChainContext = null;
        switch (triggerCfg.getResType()) {
            case DataFlow:
                WorkflowExecContext wfContext = new WorkflowExecContext(0, triggerTimestamp);
                wfContext.setWorkflowName(triggerCfg.getDataXName());
                break;
            case DataApp:
                String appName = instanceParams.getString(JobParams.KEY_COLLECTION);
                execChainContext = new DataXPipelineExecContext(appName, triggerTimestamp);
                break;
            default:
                throw new IllegalStateException("illegal resType:" + triggerCfg.getResType());
        }


        execChainContext.setJavaMemSpec(javaMemSpec);
        execChainContext.setCoordinator(ITISCoordinator.create());
        execChainContext.setDryRun(dryRun);
        execChainContext.setAttribute(JobCommon.KEY_TASK_ID, taskId);

        execChainContextConsumer.accept(execChainContext);


        // execChainContext.setLatestPhaseStatusCollection( );

        if (resolveCfgsSnapshotConsumer) {

//            String pluginCfgsMetas = instanceParams.getString(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS);
//
//            if (StringUtils.isEmpty(pluginCfgsMetas)) {
//                throw new IllegalStateException("property:"
//                        + PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS + " of instanceParams can not be null");
//            }
//
//            final Base64 base64 = new Base64();
//            try (InputStream manifestJar = new ByteArrayInputStream(base64.decode(pluginCfgsMetas))) {
//                cfgsSnapshotConsumer.accept(PluginAndCfgsSnapshot.getRepositoryCfgsSnapshot(appName, manifestJar));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }

            cfgsSnapshotConsumer.accept(resolveCfgsSnapshotConsumer(instanceParams));
        }

        return execChainContext;
    }

    @Override
    public ExecutePhaseRange getExecutePhaseRange() {
        return this.executePhaseRange;
    }

    @Override
    public ITISFileSystem getIndexBuildFileSystem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rebindLoggingMDCParams() {
        throw new UnsupportedOperationException();
    }


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
    public int getTaskId() {
        Integer taskId = Objects.requireNonNull(getAttribute(JobCommon.KEY_TASK_ID),
                JobCommon.KEY_TASK_ID + " can " + "not be null");
        return taskId;
    }

    public void setLatestPhaseStatusCollection(PhaseStatusCollection latestPhaseStatusCollection) {
        this.latestPhaseStatusCollection = latestPhaseStatusCollection;
    }

    @Override
    public PhaseStatusCollection loadPhaseStatusFromLatest() {
        return this.latestPhaseStatusCollection;
    }

    @Override
    public void addAsynSubJob(AsynSubJob jobName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AsynSubJob> getAsynSubJobs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containAsynJob() {
        throw new UnsupportedOperationException();
    }


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
