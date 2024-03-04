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
import com.qlangtech.tis.datax.impl.DataxProcessor;
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
import com.qlangtech.tis.sql.parser.TabPartitions;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/18
 */
public class DefaultExecContext implements IExecChainContext, IdentityName {

    private final long ps;
    private Integer workflowId;
    private String workflowName;
    private ExecutePhaseRange executePhaseRange;
    private final String dataXName;
    private boolean dryRun;
    private ITISCoordinator coordinator;
    private PhaseStatusCollection latestPhaseStatusCollection;
    private StoreResourceType resType;

    public DefaultExecContext(String dataXName, Long triggerTimestamp) {
        this.ps = Objects.requireNonNull(triggerTimestamp, "param triggerTimestamp can not be null");
        if (StringUtils.isEmpty(dataXName)) {
            throw new IllegalArgumentException("param dataXName can not be null");
        }
        this.dataXName = dataXName;
        ExecChainContextUtils.setDependencyTablesPartitions(this, new TabPartitions(Maps.newHashMap()));
    }

    /**
     * 反序列化
     *
     * @param instanceParams
     * @return
     */
    static DefaultExecContext deserializeInstanceParams(JSONObject instanceParams, boolean resolveCfgsSnapshotConsumer, Consumer<PluginAndCfgsSnapshot> cfgsSnapshotConsumer) {
        Integer taskId = Objects.requireNonNull(instanceParams.getInteger(JobParams.KEY_TASK_ID),
                JobParams.KEY_TASK_ID + " can not be null," + JsonUtil.toString(instanceParams));
        boolean dryRun = instanceParams.getBooleanValue(IFullBuildContext.DRY_RUN);
        String appName = instanceParams.getString(JobParams.KEY_COLLECTION);
        Long triggerTimestamp = instanceParams.getLong(DataxUtils.EXEC_TIMESTAMP);
        DefaultExecContext execChainContext = new DefaultExecContext(appName, triggerTimestamp);
        execChainContext.setCoordinator(ITISCoordinator.create());
        execChainContext.setDryRun(dryRun);
        execChainContext.setAttribute(JobCommon.KEY_TASK_ID, taskId);

        if (resolveCfgsSnapshotConsumer) {

            String pluginCfgsMetas = instanceParams.getString(PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS);

            if (StringUtils.isEmpty(pluginCfgsMetas)) {
                throw new IllegalStateException("property:"
                        + PluginAndCfgsSnapshotUtils.KEY_PLUGIN_CFGS_METAS + " of instanceParams can not be null");
            }

            final Base64 base64 = new Base64();
            try (InputStream manifestJar = new ByteArrayInputStream(base64.decode(pluginCfgsMetas))) {
                cfgsSnapshotConsumer.accept(PluginAndCfgsSnapshot.getRepositoryCfgsSnapshot(appName, manifestJar));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return execChainContext;
    }

    public void putTablePt(IDumpTable table, ITabPartition pt) {
        ExecChainContextUtils.getDependencyTablesPartitions(this).putPt(table, pt);
    }

    public StoreResourceType getResType() {
        return resType;
    }

    public void setResType(StoreResourceType resType) {
        this.resType = resType;
    }

    public void setCoordinator(ITISCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void setExecutePhaseRange(ExecutePhaseRange executePhaseRange) {
        this.executePhaseRange = executePhaseRange;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }


    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public IDataxProcessor getProcessor() {

        StoreResourceType resType = Objects.requireNonNull(getResType(), "resType can not be null");
        switch (resType) {
            case DataApp:
                return DataxProcessor.load(null, resType, this.dataXName);
            case DataFlow:
                if (StringUtils.isEmpty(this.getWorkflowName())) {
                    throw new IllegalStateException("proper workflowName can not be empty");
                }
                return DataxProcessor.load(null, resType, this.getWorkflowName());
            default:
                throw new IllegalStateException("illegal resType:" + resType);
        }
    }


    @Override
    public String identityValue() {
        StoreResourceType resType = Objects.requireNonNull(getResType(), "resType can not be null");
        switch (resType) {
            case DataApp:
                return resType.getType() + "_" + this.getIndexName();
            case DataFlow:
                if (StringUtils.isEmpty(this.getWorkflowName())) {
                    throw new IllegalStateException("proper workflowName can not be empty");
                }
                return resType.getType() + "_" + this.getWorkflowName();
            default:
                throw new IllegalStateException("illegal resType:" + resType);
        }
    }

    @Override
    public void addAsynSubJob(AsynSubJob jobName) {

    }

    @Override
    public List<AsynSubJob> getAsynSubJobs() {
        return null;
    }

    @Override
    public boolean containAsynJob() {
        return false;
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
    public void cancelTask() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITISCoordinator getZkClient() {
        return this.coordinator;
    }

    @Override
    public Integer getWorkflowId() {
        return this.workflowId;
    }

    @Override
    public String getWorkflowName() {
        return this.workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
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
    public boolean isDryRun() {
        return this.dryRun;
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

    @Override
    public String getIndexName() {
        return this.dataXName;
    }

    @Override
    public boolean hasIndexName() {
        return StringUtils.isNotEmpty(this.dataXName);
    }

    @Override
    public ExecutePhaseRange getExecutePhaseRange() {
        return this.executePhaseRange;
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

    @Override
    public long getPartitionTimestampWithMillis() {
        return ps;
    }

    public void setLatestPhaseStatusCollection(PhaseStatusCollection latestPhaseStatusCollection) {
        this.latestPhaseStatusCollection = latestPhaseStatusCollection;
    }

    @Override
    public PhaseStatusCollection loadPhaseStatusFromLatest() {
        return this.latestPhaseStatusCollection;
    }
}
