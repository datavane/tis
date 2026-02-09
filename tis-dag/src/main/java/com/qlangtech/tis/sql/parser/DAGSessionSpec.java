package com.qlangtech.tis.sql.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.DBDataXChildTask;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataXBatchPost;
import com.qlangtech.tis.datax.IDataXGenerateCfgs;
import com.qlangtech.tis.datax.IDataXJobInfo;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.LifeCycleHook;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator.GenerateCfgs;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteDumpTaskTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPostTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RemoteTaskTriggers;
import com.qlangtech.tis.fullbuild.taskflow.TaskAndMilestone;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.powerjob.IDAGSessionSpec;
import com.qlangtech.tis.powerjob.SelectedTabTriggers;
import com.qlangtech.tis.powerjob.algorithm.WorkflowDAGUtils;
import com.qlangtech.tis.powerjob.model.InstanceStatus;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import com.qlangtech.tis.powerjob.model.WorkflowNodeType;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * DAG 会话规范
 * 重构后使用 PowerJob 的 PEWorkflowDAG 数据模型，同时保持向后兼容
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-02-14 10:08
 **/
public class DAGSessionSpec implements IDAGSessionSpec {
    // ========== 旧字段（保留用于向后兼容） ==========
    Map<String, DAGSessionSpec> dptNodes = Maps.newHashMap();
    private static final String KEY_ROOT = "root";
    private final String id;

    List<DAGSessionSpec> attains = Lists.newArrayList();

    boolean milestone = false;

    private final Map<String /** taskid*/, TaskAndMilestone> taskMap;

    // ========== 新增字段（PowerJob DAG 数据模型） ==========
    /**
     * PowerJob Workflow DAG 数据模型
     */
    private final PEWorkflowDAG dag;

    /**
     * 任务名 -> 节点ID 映射
     */
    private final Map<String, Long> peNodeIdMap;

    /**
     * 节点ID生成器（线程安全）
     */
    private final AtomicLong nodeIdGenerator;

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
            , DataXJobSubmit submit, RpcServiceReference statusRpc //
            , ISelectedTab entry, String dumpTaskId, IDAGSessionSpec dagSessionSpec, IDataXGenerateCfgs cfgFileNames) {


        SelectedTabTriggers tabTriggers = new SelectedTabTriggers(entry, appSource);
        RemoteTaskTriggers triggers = Objects.requireNonNull(execChainContext.getTskTriggers(), "triggers can not be "
                + "null");
        if (org.apache.commons.lang3.StringUtils.isEmpty(dumpTaskId)) {
            throw new IllegalArgumentException("param dumpTaskId can not be null");
        }

        IDataxWriter writer = appSource.getWriter(null, true);

        if (CollectionUtils.isEmpty(cfgFileNames.getDataXCfgFiles())) {
            throw new IllegalStateException("dataX cfgFileNames can not be empty");
        }

        IDAGSessionSpec dumpSpec = dagSessionSpec.getDpt(dumpTaskId, WorkflowNodeType.CONTROL, null).setMilestone();
        final Consumer<PEWorkflowDAG.Node> newAddedNodeConsumer = (node) -> {
            JSONObject params = new JSONObject();
            params.put(SelectedTabTriggers.KEY_TABLE, entry.getName());
            node.setNodeParams((params));
        };
        final IDAGSessionSpec[] postSpec = new IDAGSessionSpec[1];
        Pair<IRemoteTaskPreviousTrigger, IRemoteTaskPostTrigger> preAndPost = IDataXBatchPost.process(appSource,
                entry, (batchPostTask, entryName) -> {

            IRemoteTaskPreviousTrigger preExec = null;
            IRemoteTaskPostTrigger postTaskTrigger = batchPostTask.createPostTask(execChainContext, entryName, entry,
                    cfgFileNames);
            if (postTaskTrigger != null) {
                postSpec[0] = dumpSpec.getDpt(postTaskTrigger.getTaskName(), LifeCycleHook.Post, newAddedNodeConsumer);
                triggers.addJoinPhaseTask(postTaskTrigger);
                tabTriggers.setPostTrigger(postTaskTrigger);
            }

            preExec = batchPostTask.createPreExecuteTask(execChainContext, entryName, entry);
            if (preExec != null) {
                dagSessionSpec.getDpt(preExec.getTaskName(), LifeCycleHook.Prep, newAddedNodeConsumer);
                triggers.addDumpPhaseTask(preExec);
                tabTriggers.setPreTrigger(preExec);
            }

            return Pair.of(preExec, postTaskTrigger);
        });

        List<DBDataXChildTask> dataXCfgsOfTab = cfgFileNames.getDataXTaskDependencies(entry.getName());


        final DataXJobSubmit.IDataXJobContext dataXJobContext = submit.createJobContext(execChainContext);
        Objects.requireNonNull(dataXJobContext, "dataXJobContext can not be null");
        List<IRemoteTaskTrigger> splitTabTriggers = Lists.newArrayList();
        for (DBDataXChildTask fileName : dataXCfgsOfTab) {

            IRemoteDumpTaskTrigger jobTrigger = createDataXJob(dataXJobContext, submit, statusRpc, appSource,
                    new DataXJobSubmit.TableDataXEntity(fileName, entry));

            IDAGSessionSpec childDumpSpec = getDumpSpec(postSpec[0], dumpSpec).getDpt( //
                    Objects.requireNonNull(jobTrigger, "jobTrigger can not be null").getTaskName(),
                    LifeCycleHook.Dump,
                    (node) -> node.setNodeParams(IDataXJobInfo.serialize(jobTrigger.getDataXTaskMessage())));

            if (preAndPost.getKey() != null) {
                childDumpSpec.getDpt(preAndPost.getKey().getTaskName(), LifeCycleHook.Post);
            }

            triggers.addDumpPhaseTask(jobTrigger);
            splitTabTriggers.add(jobTrigger);
        }
        tabTriggers.setSplitTabTriggers(splitTabTriggers);
        return tabTriggers;
    }

    public static Pair<DAGSessionSpec, List<Pair<ISelectedTab, SelectedTabTriggers>>> //
    createDAGSessionSpec( //
                          IExecChainContext execChainContext, RpcServiceReference statusRpc //
            , IDataxProcessor appSource, GenerateCfgs cfgFileNames, DataXJobSubmit submit) {
        IDataxReader reader = appSource.getReader(null);
        DAGSessionSpec sessionSpec = new DAGSessionSpec();
        List<ISelectedTab> selectedTabs = reader.getSelectedTabs();
        List<Pair<ISelectedTab, SelectedTabTriggers>> tabTrigger = Lists.newArrayList();
        int selectedTabCount = 0;
        for (ISelectedTab entry : selectedTabs) {
            if (!cfgFileNames.getTargetTabs().contains(entry.getName())) {
                continue;
            }
            selectedTabCount++;
            tabTrigger.add(Pair.of(entry, buildTaskTriggers(execChainContext, appSource, submit, statusRpc, entry,
                    entry.getName(), sessionSpec, cfgFileNames)));
        }
        if (selectedTabCount < 1) {
            throw new IllegalStateException(selectedTabs.stream().map(ISelectedTab::getName).collect(Collectors.joining(",")) //
                    + " relevant selectedTabCount can not small than 1");
        }
        return Pair.of(sessionSpec, tabTrigger);
    }

    protected static IRemoteDumpTaskTrigger createDataXJob(DataXJobSubmit.IDataXJobContext execChainContext,
                                                           DataXJobSubmit submit, RpcServiceReference statusRpc,
                                                           IDataxProcessor appSource,
                                                           DataXJobSubmit.TableDataXEntity fileName) {

        //        if (submit.getType() == DataXJobSubmit.InstanceType.DISTRIBUTE) {
        //            // TODO： 先注释掉 ，看起来没有这段代码 也能正常执行的 2023/11/09
        //            //            IncrStatusUmbilicalProtocolImpl statCollect = IncrStatusUmbilicalProtocolImpl
        //            .getInstance();
        //            //            // 将指标纬度统计向注册到内存中，下一步可提供给DataX终止功能使用
        //            //            statCollect.getAppSubExecNodeMetrixStatus(execChainContext.getTaskContext()
        //            .getIndexName(),
        //            //            fileName.getFileName());
        //        }
        return submit.createDataXJob(execChainContext, statusRpc, appSource, fileName);
    }

    private static IDAGSessionSpec getDumpSpec(IDAGSessionSpec postSpec, IDAGSessionSpec dumpSpec) {
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

    public IDAGSessionSpec setMilestone() {
        this.milestone = true;
        this.taskMap.put(this.id, TaskAndMilestone.createMilestone(this.id));

        // 新增：如果节点还未添加到 DAG 中，则添加
        if (!peNodeIdMap.containsKey(this.id)) {
            //Long nodeId = nodeIdGenerator.incrementAndGet();
            addNode(this.id, WorkflowNodeType.CONTROL, false, null, (node) -> {
            });
        }

        return this;
    }

    @Override
    public void addDpt(IDAGSessionSpec dpt) {
        throw new UnsupportedOperationException();
    }

    public DAGSessionSpec(String id, PEWorkflowDAG dag, Map<String, TaskAndMilestone> taskMap,
                          AtomicLong nodeIdGenerator, Map<String, Long> peNodeIdMap) {
        this.id = id;
        this.taskMap = taskMap;
        this.dag = dag;
        this.peNodeIdMap = peNodeIdMap;
        this.nodeIdGenerator = nodeIdGenerator;// new AtomicLong(0);
    }

    public DAGSessionSpec() {
        this(new PEWorkflowDAG());
    }

    public DAGSessionSpec(PEWorkflowDAG dag) {
        this(KEY_ROOT, dag, Maps.newHashMap(), new AtomicLong(0), Maps.newHashMap());
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

    @Override
    public IDAGSessionSpec getDpt(String id, WorkflowNodeType nodeType, LifeCycleHook execRole,
                                  Consumer<PEWorkflowDAG.Node> nodeConsumer) {
        DAGSessionSpec spec = null;
        if ((spec = dptNodes.get(id)) == null) {
            spec = this.addDpt(id);

            // 新增：同时在 PEWorkflowDAG 中添加节点

            Long nodeId = addNode(id, nodeType, false, execRole, nodeConsumer);
            if (!KEY_ROOT.equals(this.id)) {
                // 添加边：当前节点 -> 子节点
                Long parentNodeId = Objects.requireNonNull(peNodeIdMap.get(this.id //
                ), "id:" + this.id + " relevant parent node can not be null");
                // if (parentNodeId != null) {
                addEdge(nodeId, parentNodeId);
                // }
            }
            // 新增：如果有父节点，添加边
            if (this.milestone) {
                spec.attains.add(this);
            }

            return spec;
        } else {
            return spec;
        }
    }

    private DAGSessionSpec addDpt(String id) {
        DAGSessionSpec spec = new DAGSessionSpec(id, this.dag, this.taskMap, this.nodeIdGenerator, this.peNodeIdMap);
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

    // ========== 新增方法（PowerJob DAG 操作） ==========

    /**
     * 添加节点
     *
     * @param nodeName       节点名称
     * @param skipWhenFailed 失败时是否跳过
     */

    public Long addNode(String nodeName, WorkflowNodeType nodeType, boolean skipWhenFailed, LifeCycleHook execRole,
                        Consumer<PEWorkflowDAG.Node> newAddedNodeConsumer) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new IllegalArgumentException("nodeName cannot be empty");
        }
        Long nodeId = null;
        if ((nodeId = peNodeIdMap.get(nodeName)) != null) {
            return nodeId;
        }
        nodeId = nodeIdGenerator.incrementAndGet();

        PEWorkflowDAG.Node node = new PEWorkflowDAG.Node();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setNodeType(Objects.requireNonNull(nodeType, "nodeType can not be null"));
        node.setEnable(true);
        node.setSkipWhenFailed(skipWhenFailed);
        node.setStatus(InstanceStatus.WAITING);
        node.setExecRole(execRole);
        newAddedNodeConsumer.accept(node);
        dag.getNodes().add(node);
        peNodeIdMap.put(nodeName, nodeId);
        return nodeId;
    }

    /**
     * 添加边
     *
     * @param fromNodeId 起始节点ID
     * @param toNodeId   目标节点ID
     */
    public void addEdge(Long fromNodeId, Long toNodeId) {
        if (fromNodeId == null || toNodeId == null) {
            throw new IllegalArgumentException("fromNodeId and toNodeId cannot be null");
        }

        PEWorkflowDAG.Edge edge = new PEWorkflowDAG.Edge();
        edge.setFrom(fromNodeId);
        edge.setTo(toNodeId);
        edge.setEnable(true);

        dag.getEdges().add(edge);
    }

    /**
     * 校验DAG
     *
     * @return 是否有效
     */
    public boolean validate() {
        return WorkflowDAGUtils.valid(dag);
    }

    /**
     * 序列化为JSON
     *
     * @return JSON字符串
     */
    public String toJson() {
        return JSON.toJSONString(dag);
    }

    /**
     * 从JSON反序列化
     *
     * @param json JSON字符串
     * @return DAGSessionSpec实例
     */
    //    public static DAGSessionSpec fromJson(String json) {
    //        if (StringUtils.isEmpty(json)) {
    //            throw new IllegalArgumentException("json cannot be empty");
    //        }
    //
    //        PEWorkflowDAG dag = JSON.parseObject(json, PEWorkflowDAG.class);
    //        DAGSessionSpec spec = new DAGSessionSpec(dag);
    //        //spec.dag = dag;
    //
    //        // 重建 nodeIdMap
    //        spec.nodeIdMap = Maps.newHashMap();
    //        //long maxNodeId = 0;
    //        if (dag.getNodes() != null) {
    //            for (PEWorkflowDAG.Node node : dag.getNodes()) {
    //                spec.nodeIdMap.put(node.getNodeName(), node.getNodeId());
    //                if (node.getNodeId() > maxNodeId) {
    //                    maxNodeId = node.getNodeId();
    //                }
    //            }
    //        }
    //        spec.nodeIdGenerator = new AtomicLong(maxNodeId);
    //
    //        return spec;
    //    }

    /**
     * 获取 PEWorkflowDAG
     *
     * @return DAG对象
     */
    public PEWorkflowDAG getDAG() {
        return this.dag;
    }

    /**
     * 根据任务名获取节点ID
     *
     * @param nodeName 节点名称
     * @return 节点ID，如果不存在返回null
     */
    public Long getNodeId(String nodeName) {
        return peNodeIdMap.get(nodeName);
    }

    /**
     * 获取所有就绪节点
     *
     * @return 就绪节点列表
     */
    public List<PEWorkflowDAG.Node> getReadyNodes() {
        return WorkflowDAGUtils.listReadyNodes(dag);
    }

    /**
     * 获取所有根节点
     *
     * @return 根节点列表
     */
    public List<PEWorkflowDAG.Node> getRootNodes() {
        return WorkflowDAGUtils.listRoots(dag);
    }
}
