package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataXBatchPost;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPostTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.taskflow.TaskAndMilestone;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.powerjob.SelectedTabTriggers;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-02-14 10:08
 **/
public class DAGSessionSpec {
    Map<String, DAGSessionSpec> dptNodes = Maps.newHashMap();
    private static final String KEY_ROOT = "root";
    private final String id;

    List<DAGSessionSpec> attains = Lists.newArrayList();

    boolean milestone = false;

    private final Map<String /** taskid*/, TaskAndMilestone>
            taskMap;

    public Set<String> getDptNodeNames() {
        return this.dptNodes.keySet();
    }

    /**
     * 触发一个逻辑表相关子任务
     *
     * @param execChainContext
     * @param appSource
     * @param submit
     * @param statusRpc
     * @param entry
     * @return
     */
    public static SelectedTabTriggers buildTaskTriggers(IExecChainContext execChainContext, IDataxProcessor appSource
            , DataXJobSubmit submit
            , RpcServiceReference statusRpc //
            , ISelectedTab entry, String dumpTaskId, DAGSessionSpec dagSessionSpec) {
        SelectedTabTriggers tabTriggers = new SelectedTabTriggers(entry, appSource);
        RemoteTaskTriggers triggers = Objects.requireNonNull(execChainContext.getTskTriggers(), "triggers can not be null");
        if (org.apache.commons.lang3.StringUtils.isEmpty(dumpTaskId)) {
            throw new IllegalArgumentException("param dumpTaskId can not be null");
        }
        //  RemoteTaskTriggers triggers = new RemoteTaskTriggers();
        IRemoteTaskTrigger jobTrigger = null;
        IDataxWriter writer = appSource.getWriter(null, true);
        DataXCfgGenerator.GenerateCfgs cfgFileNames = appSource.getDataxCfgFileNames(null);
        if (CollectionUtils.isEmpty(cfgFileNames.getDataXCfgFiles())) {
            throw new IllegalStateException("dataX cfgFileNames can not be empty");
        }
        DAGSessionSpec postSpec = null;
        DAGSessionSpec dumpSpec = dagSessionSpec.getDpt(dumpTaskId).setMilestone();
        IRemoteTaskPreviousTrigger preExec = null;
        if (writer instanceof IDataXBatchPost) {

            IDataXBatchPost batchPostTask = (IDataXBatchPost) writer;

            IRemoteTaskPostTrigger postTaskTrigger = batchPostTask.createPostTask(execChainContext, entry, cfgFileNames);
            if (postTaskTrigger != null) {
                postSpec = dumpSpec.getDpt(postTaskTrigger.getTaskName());
                triggers.addJoinPhaseTask(postTaskTrigger);
                tabTriggers.setPostTrigger(postTaskTrigger);
            }
            // Objects.requireNonNull(postTaskTrigger, "postTaskTrigger can not be null");


            preExec = batchPostTask.createPreExecuteTask(execChainContext, entry);
            if (preExec != null) {
                dagSessionSpec.getDpt(preExec.getTaskName());
                triggers.addDumpPhaseTask(preExec);
                tabTriggers.setPreTrigger(preExec);
            }
        }

        List<DataXCfgGenerator.DBDataXChildTask> dataXCfgsOfTab = cfgFileNames.getDataXTaskDependencies(entry.getName());


        final DataXJobSubmit.IDataXJobContext dataXJobContext = submit.createJobContext(execChainContext);
        Objects.requireNonNull(dataXJobContext, "dataXJobContext can not be null");
        List<IRemoteTaskTrigger> splitTabTriggers = Lists.newArrayList();
        for (DataXCfgGenerator.DBDataXChildTask fileName : dataXCfgsOfTab) {

            jobTrigger = createDataXJob(dataXJobContext, submit
                    , statusRpc, appSource
                    , new DataXJobSubmit.TableDataXEntity(fileName, entry));

            DAGSessionSpec childDumpSpec = getDumpSpec(postSpec, dumpSpec)
                    .getDpt(Objects.requireNonNull(jobTrigger, "jobTrigger can not be null").getTaskName());

            if (preExec != null) {
                childDumpSpec.getDpt(preExec.getTaskName());
            }

            triggers.addDumpPhaseTask(jobTrigger);
            splitTabTriggers.add(jobTrigger);
        }
        tabTriggers.setSplitTabTriggers(splitTabTriggers);
        return tabTriggers;
    }

    public static Pair<DAGSessionSpec, List<ISelectedTab>> createDAGSessionSpec(IExecChainContext execChainContext
            , RpcServiceReference statusRpc, IDataxProcessor appSource, DataXJobSubmit submit) {
        IDataxReader reader = appSource.getReader(null);
        DAGSessionSpec sessionSpec = new DAGSessionSpec();
        List<ISelectedTab> selectedTabs = reader.getSelectedTabs();
        for (ISelectedTab entry : selectedTabs) {
            buildTaskTriggers(execChainContext, appSource, submit, statusRpc, entry, entry.getName(), sessionSpec);
        }
        return Pair.of(sessionSpec, selectedTabs);
    }

    protected static IRemoteTaskTrigger createDataXJob(
            DataXJobSubmit.IDataXJobContext execChainContext
            , DataXJobSubmit submit
            , RpcServiceReference statusRpc
            , IDataxProcessor appSource, DataXJobSubmit.TableDataXEntity fileName
    ) {

        if (submit.getType() == DataXJobSubmit.InstanceType.DISTRIBUTE) {
            // TODO： 先注释掉 ，看起来没有这段代码 也能正常执行的 2023/11/09
//            IncrStatusUmbilicalProtocolImpl statCollect = IncrStatusUmbilicalProtocolImpl.getInstance();
//            // 将指标纬度统计向注册到内存中，下一步可提供给DataX终止功能使用
//            statCollect.getAppSubExecNodeMetrixStatus(execChainContext.getTaskContext().getIndexName(), fileName.getFileName());
        }
        return submit.createDataXJob(
                execChainContext, statusRpc, appSource, fileName);
    }

    private static DAGSessionSpec getDumpSpec(DAGSessionSpec postSpec, DAGSessionSpec dumpSpec) {
        if (postSpec != null) {
            return postSpec;
        }
        if (dumpSpec != null) {
            return dumpSpec;
        }
        throw new IllegalStateException("neither postSpec nor dumpSpec can be null");
    }

    public Map<String, TaskAndMilestone> getTaskMap() {
        return taskMap;
    }

    public DAGSessionSpec setMilestone() {
        this.milestone = true;
        this.taskMap.put(this.id, TaskAndMilestone.createMilestone(this.id));
        return this;
    }

    public DAGSessionSpec(String id, Map<String, TaskAndMilestone> taskMap) {
        this.id = id;
        this.taskMap = taskMap;
    }

    public DAGSessionSpec() {
        this(KEY_ROOT, Maps.newHashMap());
    }

    public StringBuffer buildSpec() {
        return buildSpec(Sets.newHashSet(), (dpt) -> {
        });
    }

    public StringBuffer buildSpec(Consumer<Pair<String, String>> dptConsumer) {
        return buildSpec(Sets.newHashSet(), dptConsumer);
    }

    private StringBuffer buildSpec(Set<String> collected, Consumer<Pair<String, String>> dependency) {

        StringBuffer specs = new StringBuffer();
        for (DAGSessionSpec spec : dptNodes.values()) {
            specs.append(spec.buildSpec(collected, dependency)).append(" ");
        }
        if (StringUtils.equals(this.id, KEY_ROOT)) {
            return specs;
        }
        if (!this.milestone && collected.add(this.id)) {
            specs.append(dptNodes.values().stream().map((n) -> {
                dependency.accept(Pair.of(n.id, DAGSessionSpec.this.id));
                return n.id;
            }).collect(Collectors.joining(","))).append("->").append(this.id);

            if (CollectionUtils.isNotEmpty(this.attains)) {
                specs.append("->").append(this.attains.stream().map((a) -> {
                    dependency.accept(Pair.of(DAGSessionSpec.this.id, a.id));
                    return a.id;
                }).collect(Collectors.joining(",")));
            }
        }
        return specs;
    }

    public DAGSessionSpec getDpt(String id) {
        DAGSessionSpec spec = null;
        if ((spec = dptNodes.get(id)) == null) {
            spec = this.addDpt(id);
            if (this.milestone) {
                spec.attains.add(this);
            }
            return spec;
        } else {
            return spec;
        }
    }

    private DAGSessionSpec addDpt(String id) {
        DAGSessionSpec spec = new DAGSessionSpec(id, this.taskMap);
        this.dptNodes.put(id, spec);
        return spec;
    }

    public DAGSessionSpec addDpt(DAGSessionSpec spec) {
        dptNodes.put(spec.id, spec);
        return this;
    }

    public void put(String taskName, TaskAndMilestone taskAndMilestone) {
        this.taskMap.put(taskName, taskAndMilestone);
    }
}
