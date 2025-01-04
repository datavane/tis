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
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.datax.common.element.DataXResultPreviewOrderByCols.OffsetColVal;
import com.alibaba.datax.common.element.QueryCriteria;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.coredefine.module.action.IncrUtils.IncrSpecResult;
import com.qlangtech.tis.datax.DataXCfgFile;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXJobSubmit.InstanceType;
import com.qlangtech.tis.datax.IDataXPowerJobSubmit;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxReaderContext;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.ISearchEngineTypeTransfer;
import com.qlangtech.tis.datax.SourceColMetaGetter;
import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.datax.TableAliasMapper;
import com.qlangtech.tis.datax.impl.DataXBasicProcessMeta;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.datax.impl.ESTableAlias;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.datax.job.DataXJobWorker.K8SWorkerCptType;
import com.qlangtech.tis.datax.job.DefaultSSERunnable;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteSteps;
import com.qlangtech.tis.datax.job.ServerLaunchToken;
import com.qlangtech.tis.datax.job.ServerLaunchToken.FlinkClusterType;
import com.qlangtech.tis.datax.job.SubJobResName;
import com.qlangtech.tis.datax.preview.PreviewHeaderCol;
import com.qlangtech.tis.datax.preview.PreviewRowsData;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.util.MultiItemsViewType;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.apps.IDepartmentGetter;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.plugin.IPluginTaggable;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.SelectedTabExtend;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataTypeMeta;
import com.qlangtech.tis.plugin.ds.DataTypeMeta.IMultiItemsView;
import com.qlangtech.tis.plugin.ds.DefaultTab;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.IdlistElementCreatorFactory;
import com.qlangtech.tis.plugin.trigger.JobTrigger;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.CreateIndexConfirmModel;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DelegateControl4JsonPostMsgHandler;
import com.qlangtech.tis.solrdao.ISchema;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.HeteroList;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * manage DataX pipe process logic
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-08 15:04
 */
@InterceptorRefs({@InterceptorRef("tisStack")})
public class DataxAction extends BasicModule {
  private static final Logger logger = LoggerFactory.getLogger(DataxAction.class);
  private static final String PARAM_KEY_DATAX_NAME = DataxUtils.DATAX_NAME;

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doDeletePowerJobWorkflow(Context context) throws Exception {
    Long pjWorkflowId = this.getLong("id");
    Optional<IDataXPowerJobSubmit> powerJobSubmit = DataXJobSubmit.getPowerJobSubmit();
    IDataXPowerJobSubmit jobSubmit = powerJobSubmit.orElseThrow(() -> new IllegalStateException("dataXJobSubmit must be present"));
    jobSubmit.deleteWorkflow(this, context, pjWorkflowId);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doGetAllPowerjobWorkflowRecord(Context context) throws Exception {

    // Optional<DataXJobSubmit> dataXJobSubmit = DataXJobSubmit.getDataXJobSubmit(false, DataXJobSubmit.getDataXTriggerType());

    Optional<IDataXPowerJobSubmit> powerJobSubmit = DataXJobSubmit.getPowerJobSubmit();

    IDataXPowerJobSubmit jobSubmit = powerJobSubmit.orElseThrow(() -> new IllegalStateException("dataXJobSubmit must be present"));
    Map<String, Object> criteria = Maps.newHashMap();

    Pager pager = createPager();
    Pair<Integer, List<Object>> workflows = jobSubmit.fetchAllInstance(criteria, pager.getCurPage(), pager.getRowsPerPage());
    pager.setTotalCount(workflows.getKey());
    this.setBizResult(context, new PaginationResult(pager, workflows.getRight()));
  }

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doTriggerFullbuildTask(Context context) throws Exception {

    // 在powerjob 系统中 定时任务触发，已经生成wfInstanceId
    Optional<Long> powerJobWorkflowInstanceId
      = Optional.ofNullable(this.getLong(DataxUtils.POWERJOB_WORKFLOW_INSTANCE_ID, null));

    DataXJobSubmit.InstanceType triggerType
      = Optional.ofNullable(this.getString(InstanceType.KEY_TYPE))
      .map((type) -> InstanceType.parse(type))
      .orElseGet(() -> DataXJobSubmit.getDataXTriggerType());

    IDataxProcessor dataXProcessor = DataxProcessor.load(null, this.getCollectionName());

    if (!dataXProcessor.isSupportBatch(this)) {
      this.addErrorMessage(context, "该数据通道不支持批量数据同步，请使用实时同步");
      return;
    }


    Optional<JobTrigger> partialTrigger = JobTrigger.getPartialTriggerFromContext(context);
    DataXCfgGenerator.GenerateCfgs cfgFileNames = dataXProcessor.getDataxCfgFileNames(null, partialTrigger);
    if (!triggerType.validate(this, context, cfgFileNames.getDataXCfgFiles())) {
      return;
    }

    Optional<DataXJobSubmit> dataXJobSubmit = DataXJobSubmit.getDataXJobSubmit(false, triggerType);
    if (!dataXJobSubmit.isPresent()) {
      this.setBizResult(context, Collections.singletonMap("installLocal", true));
      this.addErrorMessage(context, "还没有安装本地触发类型的执行器:" + triggerType + ",请先安装");
      return;
    }
    DataXJobSubmit jobSubmit = dataXJobSubmit.get();
    logger.info("jobSubmit " + jobSubmit.getType() + " the submit instance type of :" + jobSubmit.getClass().getName());
//    List<HttpUtils.PostParam> params = Lists.newArrayList();
//    params.add(new HttpUtils.PostParam(TriggerBuildResult.KEY_APPNAME, this.getCollectionName()));
    //    params.add(new HttpUtils.PostParam(IParamContext.COMPONENT_START, FullbuildPhase.FullDump.getName()));
    //    params.add(new HttpUtils.PostParam(IParamContext.COMPONENT_END, FullbuildPhase.JOIN.getName()));

    // this.setBizResult(context, TriggerBuildResult.triggerBuild(this, context, params));
    this.setBizResult(context, jobSubmit.triggerJob(
      this, context, this.getCollectionName(), powerJobWorkflowInstanceId));
  }


  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doSaveTableCreateDdl(Context context) throws Exception {
    JSONObject post = this.parseJsonPost();
    String dataXName = post.getString(DataxUtils.DATAX_NAME);
    String createTableDDL = post.getString("content");
    if (StringUtils.isEmpty(createTableDDL)) {
      throw new IllegalArgumentException("create table ddl can not be null");
    }
    if (StringUtils.isEmpty(dataXName)) {
      throw new IllegalArgumentException("param dataXName can not be null");
    }
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    IDataxProcessor dataxProcessor = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataXName);

    // DataxProcessor dataxProcessor = DataxProcessor.load(this,); IAppSource.load(this, dataXName);
    String createFileName = post.getString("fileName");
    dataxProcessor.saveCreateTableDDL(this, new StringBuffer(createTableDDL), createFileName, true);
    this.addActionMessage(context, "已经成功更新建表DDL脚本 " + createFileName);
  }

  /**
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doDataxProcessorDesc(Context context) throws Exception {

    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));

    UploadPluginMeta pluginMeta = UploadPluginMeta.parse(HeteroEnum.APP_SOURCE.identity);
    HeteroList<IAppSource> hlist = new HeteroList<>(pluginMeta);
    hlist.setDescriptors(Collections.singletonList(pmodel.getPluginDescMeta()));
    hlist.setExtensionPoint(IAppSource.class);
    hlist.setSelectable(Selectable.Single);
    hlist.setCaption(StringUtils.EMPTY);

    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    if (StringUtils.isNotEmpty(dataxName)) {
      hlist.setItems(Collections.singletonList(pmodel.loadDataXProcessor(this, dataxName)));
    }

    this.setBizResult(context, hlist.toJSON());
  }

  /**
   * 取得写插件配置
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetWriterPluginInfo(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    JSONObject writerDesc = this.parseJsonPost();
    if (StringUtils.isEmpty(dataxName)) {
      throw new IllegalStateException("param " + PARAM_KEY_DATAX_NAME + " can not be null");
    }
    /**
     * 确保会执行执行：DataxReader.dataxReaderThreadLocal.set(reader);
     */
    pmodel.getDataXReader(this, dataxName);

    final String requestDescId = writerDesc.getString("impl");
    DataxWriter writer = pmodel.loadWriter(this, writerDesc, dataxName);//  (DataxWriter)dataxProcessor.getWriter(this);

    // DataxReader.load(this, dataxName);
    // KeyedPluginStore<DataxWriter> writerStore = DataxWriter.getPluginStore(this, dataxName);
    //    DataxWriter writer = writerStore.getPlugin();
    Map<String, Object> pluginInfo = Maps.newHashMap();
    DescriptorsJSONResult writeDesc = null;
    if (writer != null) {
      pluginInfo.put("item", (new DescribableJSON(writer)).getItemJson());
      writeDesc = DescriptorsJSON.desc(writer.getDescriptor());
    } else {
      writeDesc = DescriptorsJSON.desc(requestDescId);
    }
    pluginInfo.put("desc", writeDesc);
    //    final String requestDescId = writerDesc.getString("impl");
    //    if (writer != null && StringUtils.equals(writer.getDescriptor().getId(), requestDescId)) {
    //      DataxReader readerPlugin = DataxReader.load(this, dataxName);
    //      DataxWriter.BaseDataxWriterDescriptor writerDescriptor = (DataxWriter.BaseDataxWriterDescriptor) writer
    //      .getDescriptor();
    //      if (!writerDescriptor.isSupportMultiTable() && readerPlugin.getSelectedTabs().size() > 1) {
    //        // 这种情况是不允许的，例如：elastic这样的writer中对于column的设置比较复杂，需要在writer plugin页面中完成，所以就不能支持在reader中选择多个表了
    //        throw new IllegalStateException("status is not allowed:!writerDescriptor.isSupportMultiTable() &&
    //        readerPlugin.hasMulitTable()");
    //      }
    //      pluginInfo.put("item", (new DescribableJSON(writer)).getItemJson());
    //    }
    // pluginInfo.put("desc", new DescriptorsJSON(TIS.get().getDescriptor(requestDescId)).getDescriptorsJSON
    // (DescriptorsJSON.FORM_START_LEVEL));


    this.setBizResult(context, pluginInfo);
  }

  /**
   * 取得读插件配置
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetReaderPluginInfo(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    JSONObject readerDesc = this.parseJsonPost();
    if (StringUtils.isEmpty(dataxName)) {
      throw new IllegalStateException("param " + PARAM_KEY_DATAX_NAME + " can not be null");
    }
    final String requestDescId = readerDesc.getString("impl");
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(this, dataxName);
    DataxReader reader = readerStore.getPlugin();
    Map<String, Object> pluginInfo = Maps.newHashMap();
    if (reader != null && StringUtils.equals(reader.getDescriptor().getId(), requestDescId)) {
      pluginInfo.put("item", (new DescribableJSON(reader)).getItemJson());
    }
    // new DescriptorsJSON(TIS.get().getDescriptor(requestDescId)).getDescriptorsJSON()
    pluginInfo.put("desc", DescriptorsJSON.desc(requestDescId));
    this.setBizResult(context, pluginInfo);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doTestLaunchDataxWorker(Context context) throws IOException {
    PrintWriter printWriter = getEventStreamWriter();

    int timeout = 40 * 1000;
    long start = System.currentTimeMillis();
    long end = System.currentTimeMillis();
    // PrintWriter printWriter = response.getWriter();
//    while ((end - start) < timeout) {
//      //https://stackoverflow.com/questions/28673371/eventsource-onmessage-is-not-working-where-onopen-and-onerror-works-proper
//
//
//      printWriter.println("event: message");
//      printWriter.println("data: {\"name\":\"" + new Date().toString() + "\"}");
//      printWriter.println(); // note the additional line being written to the stream..
//
//      printWriter.println("event: other");
//      printWriter.println("data: {name:'baisui'}");
//      printWriter.println(); // note the additional line being written to the stream..
//      printWriter.flush();
//
//      try {
//        Thread.currentThread().sleep(1000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//      end = System.currentTimeMillis();
//    }

    printWriter.println("event: message");
    printWriter.println("data: xxxxxxxxxxxxxxxxx");
    printWriter.println(); // note the additional line being written to the stream..
    printWriter.flush();

    System.out.println("Exiting handleSSE()-suscribe" + Thread.currentThread().getName());

  }

  public static final String KEY_USING_POWERJOB_USE_EXIST_CLUSTER = "usingPowderJobUseExistCluster";

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doApplyPodNumber(Context context) throws Exception {
    PrintWriter writer = getEventStreamWriter();

    Integer podNum = this.getInt("podNumber");
    TargetResName cptType = new TargetResName(this.getString(DataXJobWorker.KEY_CPT_TYPE));
    DataXJobWorker dataxJobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName());

    ExecuteStep execStep = new ExecuteStep(SubJobResName.createSubJob("scalaPods", (dto) -> {
      throw new UnsupportedOperationException();
    }), null);

    List<ExecuteStep> executeSteps = Collections.singletonList(execStep);
    DefaultSSERunnable launchProcess = new DefaultSSERunnable(
      writer, new ExecuteSteps(dataxJobWorker, executeSteps), () -> {
      try {
        Thread.sleep(4000l);
      } catch (InterruptedException e) {
      }
    });

    ServerLaunchToken launchToken = dataxJobWorker.getProcessTokenFile(cptType, true, K8SWorkerCptType.K8SPods);
    launchToken.deleteLaunchToken();

    DefaultSSERunnable.execute(launchProcess, false, launchToken, () -> {
      // dataxJobWorker.executeLaunchService(launchProcess);
      dataxJobWorker.updatePodNumber(launchProcess, cptType, podNum);
    });

  }

  /**
   * 启动过程中出错，需要重启启动
   * remove_datax_worker
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doRelaunchDataxWorker(Context context) throws Exception {
//    this.doRemoveDataxWorker(context);
//    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName());
//    jobWorker.remove();
//    this.doLaunchDataxWorker(context);
    K8SWorkerCptType cptType = getPowerJobCptType();
    relaunchK8SCluster(context, cptType);
  }

  private K8SWorkerCptType getPowerJobCptType() {
    return this.getBoolean(KEY_USING_POWERJOB_USE_EXIST_CLUSTER)
      ? K8SWorkerCptType.UsingExistCluster : K8SWorkerCptType.Server;
  }

  public void relaunchK8SCluster(Context context, K8SWorkerCptType cptType) throws Exception {
    PrintWriter writer = getEventStreamWriter();

    DataXJobWorker dataxJobWorker = getDataXJobWorker(cptType);
    dataxJobWorker.remove();

    Thread.sleep(3000);

    this.launchDataxWorker(context, writer, dataxJobWorker, DataXJobWorker.getOrchestrate(dataxJobWorker));
  }

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doRelaunchFlinkCluster(Context context) throws Exception {
    this.relaunchK8SCluster(context, K8SWorkerCptType.FlinkCluster);
  }

  /**
   * http://localhost:4200/tjs/coredefine/corenodemanage.ajax?action=datax_action&emethod=get_datax_worker_config
   *
   * @param context
   * @throws Exception
   */
  public void doGetDataxWorkerConfig(Context context) throws Exception {
    // DataxWorkerDTO

//    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(DataXJobWorker.K8S_DATAX_INSTANCE_NAME);
//    DataxUtils.DATAX_NAME
//    UploadPluginMeta pluginMeta = UploadPluginMeta.parse(HeteroEnum.DATAX_WORKER.identity + ":" + UploadPluginMeta.KEY_REQUIRE);
//
//    HeteroList<DataXJobWorker> heteroList = pluginMeta.getHeteroList(this);
//
//    ;


    Optional<ServerLaunchToken> launchToken = DataXJobWorker.getLaunchToken(getK8SJobWorkerTargetName());
    ServerLaunchToken lt = launchToken.orElseThrow(() -> new IllegalStateException("launchToken must be present"));
    JSONObject dataXWorker = new JSONObject();

    dataXWorker.put("usingPowderJobUseExistCluster", lt.workerCptType == K8SWorkerCptType.UsingExistCluster);

    if (lt.workerCptType == K8SWorkerCptType.Server) {

      DataXJobWorker pjServer
        = DataXJobWorker.getJobWorker(TargetResName.K8S_DATAX_INSTANCE_NAME, Optional.of(K8SWorkerCptType.Server));
      DataXJobWorker pjWorker
        = DataXJobWorker.getJobWorker(TargetResName.K8S_DATAX_INSTANCE_NAME, Optional.of(K8SWorkerCptType.Worker));
      dataXWorker.put("powderJobServerRCSpec"
        , IncrUtils.serializeSpec(IncrSpecResult.create(pjServer.getReplicasSpec(), pjServer.getHpa())));
      dataXWorker.put("powderJobWorkerRCSpec"
        , IncrUtils.serializeSpec(IncrSpecResult.create(pjWorker.getReplicasSpec(), pjWorker.getHpa())));
    }


//    dataXWorker.put("processMeta", null);
//    dataXWorker.put("powderJobServerHetero", null);
//    dataXWorker.put("powderJobUseExistClusterHetero", null);
//    dataXWorker.put("powderJobWorkerHetero", null);
//    dataXWorker.put("powderjobJobTplHetero", null);

    this.setBizResult(context, dataXWorker);

  }

  /**
   * 启动DataX执行器
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doLaunchDataxWorker(Context context) throws Exception {

    K8SWorkerCptType cptType = getPowerJobCptType();
    this.launchK8SCluster(context, cptType);

  }


  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doLaunchFlinkCluster(Context context) throws Exception {
    this.launchK8SCluster(context, K8SWorkerCptType.FlinkCluster);
  }

  public void launchK8SCluster(Context context, K8SWorkerCptType cptType) throws Exception {
    DataXJobWorker dataxJobWorker = getDataXJobWorker(cptType);

//    if (dataxJobWorker.inService()) {
//      throw new IllegalStateException("dataxJobWorker is in serivce ,can not launch repeat");
//    }
    boolean orchestrate = DataXJobWorker.isOrchestrate(dataxJobWorker);
    if (orchestrate) {
      PrintWriter httpResponseWriter = getEventStreamWriter();
      this.launchDataxWorker(context, httpResponseWriter, dataxJobWorker, DataXJobWorker.getOrchestrate(dataxJobWorker));
    } else {
      throw new NotImplementedException("to do for " + K8SWorkerCptType.UsingExistCluster
        + ",worker:" + dataxJobWorker.getClass().getName());
    }
  }


  private void launchDataxWorker(Context context
    , PrintWriter httpResponseWriter, DataXJobWorker dataxJobWorker, ILaunchingOrchestrate orchestrate) throws Exception {
    DefaultSSERunnable launchProcess = new DefaultSSERunnable(httpResponseWriter, orchestrate.createExecuteSteps(dataxJobWorker), () -> {
      try {
        Thread.sleep(4000l);
      } catch (InterruptedException e) {
      }
    }) {
      @Override
      public void afterLaunched() {
        doGetJobWorkerMeta(context);
        AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
        DataXJobWorkerStatus jobWorkerStatus = (DataXJobWorkerStatus) actionExecResult.getBizResult();
        if (jobWorkerStatus == null || !jobWorkerStatus.isK8sReplicationControllerCreated()) {
          throw new IllegalStateException("Job Controller launch faild please contract administer");
        }

        addActionMessage(context, "已经成功启动DataX执行器");
      }
    };
    ServerLaunchToken launchToken = dataxJobWorker.getProcessTokenFile();
    // k8SLaunching k8SLaunching = launchProcess.hasLaunchingToken(orchestrate.getExecuteSteps(), launchToken);

    DefaultSSERunnable.execute(launchProcess, dataxJobWorker.inService(), launchToken, () -> {
      dataxJobWorker.executeLaunchService(launchProcess);
    });


//    launchProcess.writeExecuteSteps(k8SLaunching.getExecuteSteps());
//    if (dataxJobWorker.inService()) {
//      // throw new IllegalStateException("dataxJobWorker is in serivce ,can not launch repeat");
//      for (SubJobLog subJobLog : k8SLaunching.getLogs()) {
//        //public void writeMessage(InfoType logLevel, long timestamp, String msg)
//        launchProcess.writeHistoryLog(subJobLog);
//      }
//      return;
//    }
//    if (k8SLaunching.isLaunching()) {
//
//      if (k8SLaunching.isFaild()) {
////        k8SLaunching.getExecuteSteps();
////        k8SLaunching.getMilestones();
//
//        // launchProcess.writeExecuteSteps(k8SLaunching.getExecuteSteps());
//
//        for (SubJobLog subJobLog : k8SLaunching.getLogs()) {
//          //public void writeMessage(InfoType logLevel, long timestamp, String msg)
//          launchProcess.writeHistoryLog(subJobLog);
//        }
//        return;
//      } else if (launchToken.hasWriteOwner()) {
//
//        // 说明启动流程正在执行，attach 到执行流程
//        launchToken.addObserver(k8SLaunching);
//        //  k8SLaunching.attach2RunningProcessor();
//        return;
//      }
//
//    }
//
//    try {
//      launchProcess.setLaunchToken(launchToken);
//      launchProcess.startLaunch();
//
//      dataxJobWorker.executeLaunchService(launchProcess);
//
//
//    } finally {
//      launchProcess.terminate();
//    }
  }

//  private DataXJobWorker getDataXJobWorker() {
//    K8SWorkerCptType cptType = this.getBoolean(KEY_USING_POWERJOB_USE_EXIST_CLUSTER)
//      ? K8SWorkerCptType.UsingExistCluster : K8SWorkerCptType.Server;
//    return getDataXJobWorker(cptType);
//  }

  private DataXJobWorker getDataXJobWorker(K8SWorkerCptType cptType) {
    DataXJobWorker dataxJobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName(), Optional.of(cptType));
    if (dataxJobWorker == null) {
      throw new IllegalStateException("dataxJobWorker can not be null,relevant target type:" + this.getK8SJobWorkerTargetName());
    }
    return dataxJobWorker;
  }

  /**
   * 删除dataX实例
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doRemoveDataxWorker(Context context) {

    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName());

    //    PluginStore<DataXJobWorker> dataxJobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
    //    DataXJobWorker dataxJobWorker = dataxJobWorkerStore.getPlugin();
    if (!jobWorker.inService()) {
      throw new IllegalStateException("dataxJobWorker is not in serivce ,can not remove");
    }
    jobWorker.remove();
    this.addActionMessage(context, "DataX Worker 已经被删除");
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doWorkerDesc(Context context) {
    final TargetResName targetName = getK8SJobWorkerTargetName();

//    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(targetName);
//    if (jobWorker != null && jobWorker.inService()) {
//      throw new IllegalStateException("dataX worker is on duty");
//    }

    // String appName = this.getCollectionName();
    PluginDescMeta pluginDescMeta = new PluginDescMeta(DataXJobWorker.getDesc(targetName));

    boolean addJobTplOverwritePlugin = this.getBoolean("addJobTplOverwritePlugin");
    if (addJobTplOverwritePlugin) {
      pluginDescMeta.addTypedPlugins(DataXJobWorker.K8SWorkerCptType.JobTplAppOverwrite
        , HeteroEnum.appJobWorkerTplReWriter.getPlugins(this, null));
    }

    this.setBizResult(context, pluginDescMeta);
  }


  /**
   * 取得K8S dataX worker
   *
   * @param context
   */

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetDataxWorkerMeta(Context context) {
    getJobWoker(context, TargetResName.K8S_DATAX_INSTANCE_NAME);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetJobWorkerMeta(Context context) {
    final TargetResName targetName = getK8SJobWorkerTargetName();
    getJobWoker(context, targetName);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetFlinkSession(Context context) {
    final TargetResName targetName = getK8SJobWorkerTargetName(false);
    Optional<ServerLaunchToken> launchToken
      = Optional.of(ServerLaunchToken.createFlinkClusterToken().token(FlinkClusterType.K8SSession, targetName));
    getJobWoker(context, targetName, launchToken);
  }

  private void getJobWoker(Context context, TargetResName targetName) {
    getJobWoker(context, targetName, DataXJobWorker.getLaunchToken(targetName));
  }

  private void getJobWoker(Context context, TargetResName targetName, Optional<ServerLaunchToken> launchToken) {


    DataXJobWorkerStatus jobWorkerStatus = new DataXJobWorkerStatus();
    if (!launchToken.isPresent()) {
      jobWorkerStatus.setState(IFlinkIncrJobStatus.State.NONE);
      this.setBizResult(context, jobWorkerStatus);
      return;
    }
    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(targetName, launchToken.map((t) -> t.getWorkerCptType()));
    boolean disableRcdeployment = this.getBoolean("disableRcdeployment");
    jobWorkerStatus.setState((jobWorker != null && jobWorker.inService()) ? IFlinkIncrJobStatus.State.RUNNING : IFlinkIncrJobStatus.State.NONE);
    if (jobWorkerStatus.getState() == IFlinkIncrJobStatus.State.RUNNING && !disableRcdeployment) {
      jobWorkerStatus.setPayloads(jobWorker.getPayloadInfo());
      jobWorkerStatus.setRcDeployments(jobWorker.getRCDeployments());
    }
    this.setBizResult(context, jobWorkerStatus);
  }

  private TargetResName getK8SJobWorkerTargetName() {
    return getK8SJobWorkerTargetName(true);
  }

  private TargetResName getK8SJobWorkerTargetName(boolean validate) {
    final String targetName = this.getString(IFullBuildContext.KEY_TARGET_NAME);
    if (validate) {
      DataXJobWorker.validateTargetName(targetName);
    }
    return new TargetResName(targetName);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetDataxWorkerHpa(Context context) {
    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName());
    if (jobWorker.getHpa() != null) {
      RcHpaStatus hpaStatus = jobWorker.getHpaStatus();
      this.setBizResult(context, hpaStatus);
    }
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doRelaunchPodProcess(Context context) throws Exception {
    DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(this.getK8SJobWorkerTargetName());
    String podName = this.getString("podName");
    jobWorker.relaunch(podName);
    //    PluginStore<IncrStreamFactory> incrStreamStore = getIncrStreamFactoryStore(this, true);
    //    IncrStreamFactory incrStream = incrStreamStore.getPlugin();
    //    IRCController incrSync = incrStream.getIncrSync();
    //    incrSync.relaunch(this.getCollectionName());
  }

  /**
   * 保存K8S dataX worker
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doSaveDataxWorker(Context context) {
    if (!this.isCollectionAware()) {
      throw new IllegalStateException("must be collection aware");
    }
    TargetResName resName = new TargetResName(this.getCollectionName());

    List<UploadPluginMeta> metas = this.getPluginMeta();
    DataXJobWorker.K8SWorkerCptType powerjobCptType = null;
    for (UploadPluginMeta meta : metas) {
      powerjobCptType = DataXJobWorker.K8SWorkerCptType.parse(meta.getDataXName());
    }
    // DataXJobWorker.PowerjobCptType powerjobCptType = DataXJobWorker.PowerjobCptType.parse(this.getString("powerjobCptType"));
    saveWorker(context, resName, Optional.of(powerjobCptType));
  }

//  @Func(value = PermissionConstant.DATAX_MANAGE)
//  public void doSaveFlinkWorker(Context context) {
//    saveWorker(context, DataXJobWorker.K8S_FLINK_CLUSTER_NAME, Optional.empty());
//  }

  private void saveWorker(Context context, TargetResName resName, Optional<DataXJobWorker.K8SWorkerCptType> cptType) {
    JSONObject postContent = this.parseJsonPost();
    JSONObject k8sSpec = postContent.getJSONObject("k8sSpec");

    IncrUtils.IncrSpecResult incrSpecResult = IncrUtils.parseIncrSpec(context, k8sSpec, this);
    if (!incrSpecResult.isSuccess()) {
      return;
    }
    // this.getK8SJobWorkerTargetName();
    //TargetResName resName = DataXJobWorker.K8S_DATAX_INSTANCE_NAME;
    DataXJobWorker worker = DataXJobWorker.getJobWorker(resName, cptType);

    worker.setReplicasSpec(incrSpecResult.getSpec());
    if (incrSpecResult.hpa != null) {
      worker.setHpa(incrSpecResult.hpa);
    }
    DataXJobWorker.setJobWorker(resName, cptType, worker);
  }


  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetSupportedReaderWriterTypes(Context context) {

    DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes =
      TIS.get().getDescriptorList(DataxReader.class);
    List<Descriptor<DataxWriter>> writerTypes = TIS.get().getDescriptorList(DataxWriter.class);


    String writerPluginTag = this.getString("writerPluginTag");
    if (StringUtils.isNotEmpty(writerPluginTag)) {
      IPluginTaggable.PluginTag filterTag = IPluginTaggable.PluginTag.parse(writerPluginTag);
      writerTypes = writerTypes.stream().filter((desc) -> {
        if (desc instanceof IPluginTaggable) {
          IPluginTaggable taggable = (IPluginTaggable) desc;
          return taggable.getTags().contains(filterTag);
        }
        return false;
      }).collect(Collectors.toList());
    }


    this.setBizResult(context, new DataxPluginDescMeta(readerTypes, writerTypes));
  }

  enum GenCfgFileType {
    DATAX_CFG("datax"), CREATE_TABLE_DDL("createTableDDL");
    private final String val;

    static GenCfgFileType parse(String val) {
      for (GenCfgFileType t : GenCfgFileType.values()) {
        if (t.val.equalsIgnoreCase(val)) {
          return t;
        }
      }
      throw new IllegalStateException("illegal val:" + val);
    }

    private GenCfgFileType(String val) {
      this.val = val;
    }
  }


  /**
   * 取得生成的配置文件的内容
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetGenCfgFile(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);

    //  String fileName = this.getString("fileName");

    DataXCfgFile cfgFileCriteria = this.parseJsonPost(DataXCfgFile.class);

    GenCfgFileType fileType = GenCfgFileType.parse(this.getString("fileType"));
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    IDataxProcessor dataxProcessor = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataxName);
    Map<String, Object> fileMeta = Maps.newHashMap();
    switch (fileType) {
      case DATAX_CFG:
        File cfgFile = dataxProcessor.getDataXCfgFile(this, cfgFileCriteria);
        //        File cfgFile = new File(dataxCfgDir, fileName);
        if (!cfgFile.exists()) {
          throw new IllegalStateException("target file:" + cfgFile.getAbsolutePath());
        }
        fileMeta.put("content", FileUtils.readFileToString(cfgFile, TisUTF8.get()));
        break;
      case CREATE_TABLE_DDL:
        File ddlDir = dataxProcessor.getDataxCreateDDLDir(this);
        File sqlScript = new File(ddlDir, cfgFileCriteria.getFileName());
        if (!sqlScript.exists()) {
          throw new IllegalStateException("target file:" + sqlScript.getAbsolutePath());
        }
        fileMeta.put("content", FileUtils.readFileToString(sqlScript, TisUTF8.get()));
        break;
      default:
        throw new IllegalStateException("illegal fileType:" + fileType);
    }


    this.setBizResult(context, fileMeta);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetExecStatistics(Context context) throws Exception {
    WorkFlowBuildHistoryCriteria historyCriteria = new WorkFlowBuildHistoryCriteria();
    Date from = ManageUtils.getOffsetDate(-7);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
    Map<String, DataXExecStatus> execStatis = Maps.newTreeMap();
    ExecResult execResult = null;
    DataXExecStatus execStatus = null;
    String timeLab = null;
    for (int i = 0; i < 8; i++) {
      timeLab = dateFormat.format(ManageUtils.getOffsetDate(-i));
      execStatis.put(timeLab, new DataXExecStatus(timeLab));
    }
    int successCount = 0;
    int errCount = 0;
    historyCriteria.createCriteria().andAppIdEqualTo(this.getAppDomain().getAppid()).andCreateTimeGreaterThan(from);
    for (WorkFlowBuildHistory h : this.wfDAOFacade.getWorkFlowBuildHistoryDAO().selectByExample(historyCriteria)) {
      execResult = ExecResult.parse(h.getState());
      execStatus = execStatis.get(dateFormat.format(h.getCreateTime()));
      if (execStatus == null) {
        continue;
      }
      if (execResult == ExecResult.SUCCESS) {
        execStatus.successCount++;
        successCount++;
      } else if (execResult == ExecResult.FAILD) {
        execStatus.errCount++;
        errCount++;
      }
    }
    Map<String, Object> bizResult = Maps.newHashMap();
    bizResult.put("data", execStatis.values());
    Map<String, Integer> allStatis = Maps.newHashMap();
    allStatis.put("errCount", errCount);
    allStatis.put("successCount", successCount);
    bizResult.put("statis", allStatis);
    this.setBizResult(context, bizResult);
  }

  private static class DataXExecStatus {

    private final String timeLab;

    public DataXExecStatus(String timeLab) {
      this.timeLab = timeLab;
    }

    public String getTimeLab() {
      return timeLab;
    }

    int errCount;
    int successCount;

    public int getErrCount() {
      return errCount;
    }

    public int getSuccessCount() {
      return successCount;
    }
  }

  /**
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doValidateDataxProfile(Context context) throws Exception {
    Application app = this.parseJsonPost(Application.class);
    SchemaAction.CreateAppResult validateResult = this.createNewApp(context, app, null, true, (newAppId) -> {
      throw new UnsupportedOperationException();
    });
  }

  private static List<Option> deps;

  public static void cleanDepsCache() {
    deps = null;
  }

  /**
   * DataX中显示已有部门
   *
   * @param
   * @return
   */
  public static List<Option> getDepartments() {
    if (deps != null) {
      return deps;
    }
    RunContext runContext = BasicServlet.getBeanByType(ServletActionContext.getServletContext(), RunContext.class);
    Objects.requireNonNull(runContext, "runContext can not be null");
    return deps = BasicModule.getDptList(runContext, new IDepartmentGetter() {
    });
  }

  /**
   * 重新生成datax配置文件
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doGenerateDataxCfgs(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    boolean getExist = this.getBoolean("getExist");
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    generateDataXCfgs(this, context, pmodel.resType, dataxName, getExist);
  }

  public static void generateDataXCfgs(IPluginContext pluginContext, Context context, StoreResourceType resType,
                                       String dataxName, boolean getExist) {
    if (StringUtils.isEmpty(dataxName)) {
      throw new IllegalArgumentException("param dataXName can not be null");
    }
    try {
      IDataxProcessor dataxProcessor = DataxProcessor.load(pluginContext, resType, dataxName);

      DataXCfgGenerator cfgGenerator = new DataXCfgGenerator(pluginContext, dataxName, dataxProcessor);
      File dataxCfgDir = dataxProcessor.getDataxCfgDir(pluginContext);

      FileUtils.forceMkdir(dataxCfgDir);
      if (!getExist) {
        //  FileUtils.forceMkdir(dataxCfgDir);
        // 先清空文件
        //FileUtils.cleanDirectory(dataxCfgDir);
      }

      DataXCfgGenerator.GenerateCfgs generateCfgs = null;
      pluginContext.setBizResult(context, getExist ? cfgGenerator.getExistCfg(dataxCfgDir) : (generateCfgs =
        cfgGenerator.startGenerateCfg(dataxCfgDir)));

      if (!getExist) {
        Objects.requireNonNull(generateCfgs, "generateCfgs can not be null");
        generateCfgs.write2GenFile(dataxCfgDir);
      }
    } catch (Exception e) {
      throw new RuntimeException("resType:" + resType + ",name:" + dataxName, e);
    }
  }

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doRegenerateSqlDdlCfgs(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    // DataxProcessor dataxProcessor = IAppSource.load(this, dataxName);
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    IDataxProcessor dataxProcessor = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataxName);

    DataXCfgGenerator cfgGenerator = new DataXCfgGenerator(this, dataxName, dataxProcessor);

    IDataxWriter writer = dataxProcessor.getWriter(this, true);
    if (writer.isGenerateCreateDDLSwitchOff()) {
      throw TisException.create("自动生成Create Table DDL已经关闭，请先打开再使用");
    }
    DataxWriter.BaseDataxWriterDescriptor writerDesc = writer.getWriterDescriptor();
    if (!writerDesc.isSupportTabCreate()) {
      throw new IllegalStateException("writerDesc:" + writerDesc.getDisplayName() + " is not support generate Table " + "create DDL");
    }

    this.setBizResult(context, cfgGenerator.startGenerateCfg(new DataXCfgGenerator.IGenerateScriptFile() {
      @Override
      public void generateScriptFile(boolean supportDataXBatch, SourceColMetaGetter colMetaGetter
        , IDataxReader reader, IDataxWriter writer, DataxWriter.BaseDataxWriterDescriptor writerDescriptor, IDataxReaderContext readerContext,
                                     Set<String> createDDLFiles, Optional<IDataxProcessor.TableMap> tableMapper) throws IOException {

        DataXCfgGenerator.generateTabCreateDDL(DataxAction.this, dataxProcessor, colMetaGetter, writer, readerContext,
          createDDLFiles, tableMapper, true);
      }
    }));
  }

  /**
   * 创建DataX实例
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doDeleteDatax(Context context) throws Exception {
    AppDomainInfo appDomain = this.getAppDomain();
    boolean deleteSuccess = false;
    File dataXDir = null;
    File scriptDootDirTrash = null;
    File dataXDirTrash = null;
    File scriptRootDir = null;
    try {
      // 判断增量实例是否存在
      IFlinkIncrJobStatus.State state = null;
      try {
        IndexIncrStatus incrStatus = CoreAction.getIndexIncrStatus(this, true);
        state = incrStatus.getState();
      } catch (Throwable e) {
        logger.error(e.getMessage(), e);
        state = IFlinkIncrJobStatus.State.NONE;
      }
      if (state != IFlinkIncrJobStatus.State.NONE) {
        this.addErrorMessage(context, "增量实例存在，请先将其删除");
        return;
      }
      //增量配置脚本移位置
      scriptRootDir = StreamContextConstant.getStreamScriptRootDir(appDomain.getAppName());
      scriptDootDirTrash = StreamContextConstant.getStreamScriptRootDir(appDomain.getAppName(), true).getFile();
      if (scriptRootDir.exists()) {
        FileUtils.moveDirectory(scriptRootDir, scriptDootDirTrash);
      }

      //DataX配置移动位置
      IRepositoryResource appSource = IAppSource.getPluginStore(this, appDomain.getAppName());
      dataXDir = appSource.getTargetFile().getFile().getParentFile();
      dataXDirTrash = new File(dataXDir.getParentFile(),
        StreamContextConstant.KEY_DIR_TRASH_NAME + "/" + appDomain.getAppName());
      FileUtils.moveDirectory(dataXDir, dataXDirTrash);

      WorkFlowBuildHistoryCriteria historyCriteria = new WorkFlowBuildHistoryCriteria();
      historyCriteria.createCriteria().andAppIdEqualTo(appDomain.getAppid());
      this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO().deleteByExample(historyCriteria);

      this.getApplicationDAO().deleteByPrimaryKey(appDomain.getAppid());
      this.addActionMessage(context, "已经成功将数据通道'" + appDomain.getAppName() + "'删除");
      IAppSource.cleanAppSourcePluginStoreCache(this, appDomain.getAppName());
      deleteSuccess = true;
    } finally {
      if (deleteSuccess) {
        FileUtils.deleteDirectory(scriptDootDirTrash);
        FileUtils.deleteDirectory(dataXDirTrash);
      } else {
        //从垃圾箱恢复 恢复配置文件
        if (dataXDirTrash != null && dataXDirTrash.exists()) {
          FileUtils.moveDirectory(dataXDirTrash, dataXDir);
        }
        if (scriptDootDirTrash != null && scriptDootDirTrash.exists()) {
          FileUtils.moveDirectory(scriptDootDirTrash, scriptRootDir);
        }
      }
    }
  }


  /**
   * 创建DataX实例
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doCreateDatax(Context context) throws Exception {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    this.createPipeline(context, dataxName);
  }

  private static final Pattern PatternEdittingDirSuffix =
    Pattern.compile("\\-[\\da-z]{8}\\-[\\da-z]{4}\\-[\\da-z]{4" + "}\\-[\\da-z]{4}\\-[\\da-z]{12}");

  /**
   * 更新Powerjob worker与应用绑定,更新Crontab
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doUpdatePowerJob(Context context) throws Exception {

    Optional<IDataXPowerJobSubmit> dataXJobSubmit //
      = DataXJobSubmit.getPowerJobSubmit();

    IDataXPowerJobSubmit jobSubmit = dataXJobSubmit.orElseThrow(() -> new IllegalStateException("dataXJobSubmit must be present"));

    DataxProcessor dataxProcessor = (DataxProcessor) DataxProcessor.load(
      null, StoreResourceType.DataApp, this.getAppDomain().getAppName());
    // 这里可以在pwoerjob 中创建workflow任务
    this.setBizResult(context, jobSubmit.saveJob(this, context, dataxProcessor));
  }


  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doUpdateDatax(Context context) throws Exception {
    final String dataxName = this.getCollectionName();

    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    DataxProcessor old = (DataxProcessor) pmodel.loadDataXProcessor(null, dataxName);

    // DataxProcessor old = DataxProcessor.load(null, dataxName);
    IDataxProcessor editting = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataxName);
    File oldWorkDir = old.getDataXWorkDir((IPluginContext) null);
    File edittingDir = editting.getDataXWorkDir((IPluginContext) this);

    String edittingDirSuffix = StringUtils.substringAfter(edittingDir.getName(), oldWorkDir.getName());
    Matcher matcher = PatternEdittingDirSuffix.matcher(edittingDirSuffix);
    if (!matcher.matches()) {
      throw new IllegalStateException("dir name is illegal,oldDir:" + oldWorkDir.getAbsolutePath() + " editting dir:" + edittingDir.getAbsolutePath());
    }

    File backDir = new File(oldWorkDir.getParentFile(), oldWorkDir.getName() + ".bak");
    // 先备份
    try {
      FileUtils.moveDirectory(oldWorkDir, backDir);
      FileUtils.moveDirectory(edittingDir, oldWorkDir);
      FileUtils.forceDelete(backDir);
    } catch (Exception e) {
      try {
        FileUtils.moveDirectory(backDir, oldWorkDir);
      } catch (Throwable ex) {

      }
      throw new IllegalStateException("oldWorkDir update is illegal:" + oldWorkDir.getAbsolutePath(), e);
    }
    // 更新一下时间戳，workflow 会重新创建流程
    Application dataXApp = new Application();
    dataXApp.setUpdateTime(new Date());
    ApplicationCriteria appCriteria = new ApplicationCriteria();
    appCriteria.createCriteria().andProjectNameEqualTo(dataxName);
    this.getApplicationDAO().updateByExampleSelective(dataXApp, appCriteria);

    IAppSource.cleanAppSourcePluginStoreCache(null, dataxName);
    IAppSource.cleanAppSourcePluginStoreCache(this, dataxName);
    SelectedTabExtend.clearTabExtend(null, dataxName);
    SelectedTabExtend.clearTabExtend(this, dataxName);

    DataXJobSubmit.getPowerJobSubmit().ifPresent((submit) -> {
      submit.saveJob(this, context, old);
    });


    this.addActionMessage(context, "已经成功更新");
  }

  /**
   * 创建一个临时执行目录
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doCreateUpdateProcess(Context context) throws Exception {
    String dataXName = this.getCollectionName();
    String execId = this.getString("execId");
    if (StringUtils.isBlank(execId)) {
      throw new IllegalArgumentException("param execId can not be null");
    }
    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    IDataxProcessor dataxProcessor = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataXName);

    // DataxProcessor dataxProcessor = IAppSource.load(null, dataXName);
    dataxProcessor.makeTempDir(execId);
    // 创建临时执行目录
    this.setBizResult(context, execId);
  }

  /**
   * 保存表映射
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doSaveTableMapper(Context context) {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    // 表别名列表
    JSONArray tabAliasList = this.parseJsonArrayPost();
    Objects.requireNonNull(tabAliasList, "tabAliasList can not be null");

    JSONObject alias = null;
    TableAlias tabAlias = null;
    List<TableAlias> tableMaps = Lists.newArrayList();


    String mapperToVal = null;
    for (int i = 0; i < tabAliasList.size(); i++) {
      alias = tabAliasList.getJSONObject(i);
      tabAlias = new TableAlias();
      tabAlias.setFrom(alias.getString("from"));
      mapperToVal = alias.getString("to");
      String mapper2FieldKey = "tabMapperTo[" + i + "]";
      if (Validator.require.validate(this, context, mapper2FieldKey, mapperToVal)) {
        Validator.db_col_name.validate(this, context, mapper2FieldKey, mapperToVal);
      }
      tabAlias.setTo(mapperToVal);
      tableMaps.add(tabAlias);
    }

    if (context.hasErrors()) {
      return;
    }

    this.saveTableMapper(this, dataxName, tableMaps);

  }

  private void saveTableMapper(IPluginContext pluginContext, String dataxName, List<TableAlias> tableMaps) {

    if (StringUtils.isBlank(dataxName)) {
      throw new IllegalArgumentException("param dataxName can not be null");
    }

    DataxProcessor dataxProcessor = (DataxProcessor) DataxProcessor.load(this, dataxName);
    dataxProcessor.setTableMaps(tableMaps);
    IAppSource.save(pluginContext, dataxName, dataxProcessor);
  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doPreviewTableRows(Context context) {
    String targetTab = this.getString("table");
    JSONObject jsonPostContent = this.getJSONPostContent();
    if (StringUtils.isEmpty(targetTab)) {
      throw new IllegalArgumentException("param table can not be null");
    }
    String dataXName = this.getCollectionName();
    Optional<DataXJobSubmit> jobSubmit = DataXJobSubmit.getDataXJobSubmit(false, InstanceType.LOCAL);
    DataXJobSubmit submit = jobSubmit.orElseThrow(() -> new IllegalStateException("dataXJobSubmit must be present"));


    QueryCriteria queryCriteria = QueryCriteria.createCriteria(this.getInt("pageSize"), jsonPostContent);
//    queryCriteria.setNextPakge(true);
//    queryCriteria.setPageSize(this.getInt("pageSize"));
//    if (queryCriteria.getPageSize() < 1) {
//      throw new IllegalStateException("page size can not small than 1");
//    }
//
//    JSONArray offsetPointer = null;
//    if (jsonPostContent != null) {
//      queryCriteria.setNextPakge(jsonPostContent.getBooleanValue("nextPage"));
//      offsetPointer = jsonPostContent.getJSONArray("offsetPointer");
//    }
//
//    if (offsetPointer != null) {
//      List<OffsetColVal> pagerOffsetCursor = OffsetColVal.deserializePreviewCursor(offsetPointer);
//      queryCriteria.setPagerOffsetCursor(pagerOffsetCursor);
//    }


    Map<String, Object> preview = Maps.newHashMap();
    boolean needHeader = true;
    PreviewRowsData records = submit.previewRowsData(dataXName, targetTab, queryCriteria);

    List<OffsetColVal> headerCursor = records.getHeaderCursor();
    List<OffsetColVal> tailerCursor = records.getTailerCursor();
    if (CollectionUtils.isNotEmpty(headerCursor)) {
      preview.put("headerCursor", OffsetColVal.getPreviewCursor(headerCursor));
    }

    if (CollectionUtils.isNotEmpty(tailerCursor)) {
      preview.put("tailerCursor", OffsetColVal.getPreviewCursor(tailerCursor));
    }

    if (needHeader) {
      JSONArray headerCols = new JSONArray();
      JSONObject col = null;
      for (PreviewHeaderCol headerCol : records.getHeader()) {
        col = new JSONObject();
        col.put("key", headerCol.getKey());
        col.put("blob", headerCol.isBlob());
        headerCols.add(col);
      }
      preview.put("header", headerCols);
    }
    preview.put("rows", records.getRows());

    this.setBizResult(context, preview);
  }

  /**
   * 取得表映射
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetTableMapper(Context context) {
    String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(this, dataxName);
    DataxReader dataxReader = readerStore.getPlugin();
    Objects.requireNonNull(dataxReader, "dataReader:" + dataxName + " relevant instance can not be null");

    TableAlias tableAlias;
    Optional<DataxProcessor> dataXAppSource = IAppSource.loadNullable(this, dataxName);
    TableAliasMapper tabMaps = null;//Collections.emptyMap();
    if (dataXAppSource.isPresent()) {
      DataxProcessor dataxSource = dataXAppSource.get();
      tabMaps = dataxSource.getTabAlias(this);
    }
    if (tabMaps == null) {
      throw new IllegalStateException("tableMaps can not be null");
    }

    if (!dataxReader.hasMulitTable()) {
      throw new IllegalStateException("reader (" + dataxReader.getClass().getSimpleName() + ") has not set table at least");
    }
    List<TableAlias> tmapList = Lists.newArrayList();
    for (ISelectedTab selectedTab : dataxReader.getSelectedTabs()) {
      tableAlias = tabMaps.get(selectedTab);
      if (tableAlias == null) {
        tmapList.add(new TableAlias(selectedTab.getName()));
      } else {
        tmapList.add(tableAlias);
      }
    }
    this.setBizResult(context, tmapList);

  }

  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetReaderWriterMeta(Context context) {
    final String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    DataxProcessor.DataXCreateProcessMeta processMeta = DataxProcessor.getDataXCreateProcessMeta(this, dataxName,
      false);
    this.setBizResult(context, processMeta);
  }

  /**
   * 当reader为非RDBMS，writer为RDBMS类型时 需要为writer设置表名称，以及各个列的名称
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetWriterColsMeta(Context context) {
    final String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    DataxProcessor.DataXCreateProcessMeta processMeta = DataxProcessor.getDataXCreateProcessMeta(this, dataxName);

    if (processMeta.isReaderRDBMS()) {
      throw new IllegalStateException("can not process the flow with:" + processMeta.toString());
    }
    IDataxProcessor processor = DataxProcessor.load(this, dataxName);
    TableAliasMapper tabAlias = processor.getTabAlias(this);
    Optional<TableAlias> findMapper = tabAlias.findFirst();
    IDataxProcessor.TableMap tabMapper = null;
    if (findMapper.isPresent()) {
      tabMapper = (IDataxProcessor.TableMap) findMapper.get();
      List<CMeta> sourceCols = tabMapper.getSourceCols();

      IDataxProcessor.TableMap m = new IDataxProcessor.TableMap(sourceCols);
      m.setFrom(tabMapper.getFrom());
      m.setTo(tabMapper.getTo());
      tabMapper = m;
    } else {
      List<ISelectedTab> tabs = processMeta.getReader().getSelectedTabs();
      int selectedTabsSize = tabs.size();
      if (selectedTabsSize != 1) {
        throw new IllegalStateException("dataX reader getSelectedTabs size must be 1 ,but now is :" + selectedTabsSize);
      }

      for (ISelectedTab selectedTab : tabs) {
        tabMapper = new IDataxProcessor.TableMap(selectedTab);
        tabMapper.setFrom(selectedTab.getName());
        tabMapper.setTo(selectedTab.getName());
        break;
      }
    }

    this.setBizResult(context, DataTypeMeta.createViewBiz(IMultiItemsView.unknow(), tabMapper));
  }

  @Func(value = PermissionConstant.APP_ADD)
  public void doGotoEsAppCreateConfirm(Context context) throws Exception {
    this.errorsPageShow(context);
    // 这里只做schema的校验
    CreateIndexConfirmModel confiemModel = parseJsonPost(CreateIndexConfirmModel.class);
    String schemaContent = null;
    ISchema schema = null;
    ISearchEngineTypeTransfer typeTransfer = ISearchEngineTypeTransfer.load(this, confiemModel.getDataxName());
    if (confiemModel.isExpertModel()) {
      CreateIndexConfirmModel.ExpertEditorModel expect = confiemModel.getExpert();
      schemaContent = expect.getXml();
      schema = typeTransfer.projectionFromExpertModel(expect.asJson());
    } else {
      schema = confiemModel.getStupid().getModel();
      schemaContent = typeTransfer.mergeFromStupidModel(schema,
        ISearchEngineTypeTransfer.getOriginExpertSchema(null)).toJSONString();
    }

    if (!schema.isValid()) {
      for (String err : schema.getErrors()) {
        this.addErrorMessage(context, err);
      }
      return;
    }

    DataxProcessor.DataXCreateProcessMeta processMeta = DataxProcessor.getDataXCreateProcessMeta(this,
      confiemModel.getDataxName());
    List<ISelectedTab> selectedTabs = processMeta.getReader().getSelectedTabs();
    ESTableAlias esTableAlias = new ESTableAlias();
    esTableAlias.setFrom(selectedTabs.stream().findFirst().get().getName());
    esTableAlias.setTo(((ISearchEngineTypeTransfer) processMeta.getWriter()).getIndexName());
    esTableAlias.setSchemaContent(schemaContent);

    this.saveTableMapper(this, confiemModel.getDataxName(), Collections.singletonList(esTableAlias));
  }


  /**
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doSaveWriterColsMeta(Context context) {
    final String dataxName = this.getString(PARAM_KEY_DATAX_NAME);
    DataxProcessor.DataXCreateProcessMeta processMeta = DataxProcessor.getDataXCreateProcessMeta(this, dataxName);
    if ((processMeta.isReaderRDBMS())) {
      throw new IllegalStateException("can not process the flow with:" + processMeta.toString());
    }
    List<CMeta> writerCols = Lists.newArrayList();
    IDataxProcessor.TableMap tableMapper = new IDataxProcessor.TableMap(new DefaultTab(dataxName, writerCols));
    // tableMapper.setSourceCols(writerCols);
    ////////////////////
    // final String keyColsMeta = "colsMeta";
    IControlMsgHandler handler = new DelegateControl4JsonPostMsgHandler(this, this.parseJsonPost());
    if (!Validator.validate(handler, context, Validator.fieldsValidator( //
      "writerTargetTabName" //
      , new Validator.FieldValidators(Validator.require, Validator.db_col_name) {
        @Override
        public void setFieldVal(String val) {
          tableMapper.setTo(val);
        }
      }, "writerFromTabName", new Validator.FieldValidators(Validator.require, Validator.db_col_name) {
        @Override
        public void setFieldVal(String val) {
          tableMapper.setFrom(val);
        }
      }, MultiItemsViewType.keyColsMeta //
      , new Validator.FieldValidators(Validator.require) {
        @Override
        public void setFieldVal(String val) {
        }
      }, new Validator.IFieldValidator() {
        @Override
        public boolean validate(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {
          // CMeta colMeta = null;
          JSONArray targetCols = JSON.parseArray(fieldData);
          //          JSONObject targetCol = null;
          //          int index;
          //          String targetColName = null;
          //          DataType dataType = null;

          if (targetCols.size() < 1) {
            msgHandler.addFieldError(context, fieldKey, "Writer目标表列不能为空");
            return false;
          }

          CMeta.ParsePostMCols postMCols = (new IdlistElementCreatorFactory()).parsePostMCols(null, (IControlMsgHandler) msgHandler,
            context, fieldKey /*MultiItemsViewType.keyColsMeta*/, targetCols);

          //          Map<String, Integer> existCols = Maps.newHashMap();
          //          boolean validateFaild = false;
          //          Integer previousColIndex = null;
          //          boolean pk;
          //          boolean pkHasSelected = false;
          //          JSONObject type = null;
          //          for (int i = 0; i < targetCols.size(); i++) {
          //            targetCol = targetCols.getJSONObject(i);
          //            index = targetCol.getInteger("index");
          //            pk = targetCol.getBooleanValue("pk");
          //            targetColName = targetCol.getString("name");
          //            if (StringUtils.isNotBlank(targetColName) && (previousColIndex = existCols.put(targetColName,
          //            index)) != null) {
          //              msgHandler.addFieldError(context, keyColsMeta + "[" + previousColIndex + "]", "内容不能与第" +
          //              index + "行重复");
          //              msgHandler.addFieldError(context, keyColsMeta + "[" + index + "]", "内容不能与第" +
          //              previousColIndex + "行重复");
          //              return false;
          //            }
          //            if (!Validator.require.validate(DataxAction.this, context, keyColsMeta + "[" + index + "]",
          //            targetColName)) {
          //              validateFaild = true;
          //            } else if (!Validator.db_col_name.validate(DataxAction.this, context, keyColsMeta + "[" +
          //            index + "]", targetColName)) {
          //              validateFaild = true;
          //            }
          //            colMeta = new CMeta();
          //            colMeta.setName(targetColName);
          //            colMeta.setPk(pk);
          //            if (pk) {
          //              pkHasSelected = true;
          //            }
          ////{"s":"3,12,2","typeDesc":"decimal(12,2)","columnSize":12,"typeName":"VARCHAR","unsigned":false,
          // "decimalDigits":4,"type":3,"unsignedToken":""}
          //            type = targetCol.getJSONObject("type");
          //            dataType = new DataType(type.getInteger("type"), type.getString("typeName"), type.getInteger
          //            ("columnSize"));
          //            dataType.setDecimalDigits(type.getInteger("decimalDigits"));
          //            // DataType dataType = targetCol.getObject("type", DataType.class);
          //            // colMeta.setType(ISelectedTab.DataXReaderColType.parse(targetCol.getString("type")));
          //            colMeta.setType(dataType);
          //            writerCols.add(colMeta);
          //          }

          if (!postMCols.pkHasSelected) {
            addErrorMessage(context, "请至少选择一个主键列");
            postMCols.validateFaild = true;
          }
          writerCols.addAll(postMCols.writerCols);
          return !postMCols.validateFaild;
        }
      }))) {
      return;
    }


    this.saveTableMapper(this, dataxName, Collections.singletonList(tableMapper));
  }


  private ValdateReaderAndWriter getValdateReaderAndWriter(ProcessModel pmodel) {
    switch (pmodel) {
      case CreateDatax:
        return (reader, writer, module, context) -> {
          if (reader == null || writer == null) {
            module.addErrorMessage(context, "请选择'Reader类型'和'Writer类型'");
            return false;
          }
          return true;
        };
      case CreateWorkFlow:
        return (reader, writer, module, context) -> {
          if (writer == null) {
            module.addErrorMessage(context, "请选择'引擎类型'");
            return false;
          }
          return true;
        };
      default:
        throw new IllegalStateException("illegal pmode:" + pmodel);
    }
  }


  /**
   * submit reader type and writer type form for validate
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doValidateReaderWriter(Context context) throws Exception {
    this.errorsPageShow(context);
    JSONObject post = this.parseJsonPost();
    ProcessModel pmodel = ProcessModel.parse(post.getString(StoreResourceType.KEY_PROCESS_MODEL));

    String dataxPipeName = post.getString("dataxPipeName");

    JSONObject reader = post.getJSONObject("readerDescriptor");
    JSONObject writer = post.getJSONObject("writerDescriptor");

    if (!getValdateReaderAndWriter(pmodel).valdateReaderAndWriter(reader, writer, this, context)) {
      return;
    }
    DataXBasicProcessMeta processMeta = pmodel.createProcessMeta(this, dataxPipeName, reader, writer);

    this.setBizResult(context, processMeta);
  }


  /**
   * dataX创建之后的管理页面使用
   *
   * @param context
   */
  @Func(value = PermissionConstant.DATAX_MANAGE, sideEffect = false)
  public void doGetDataXMeta(Context context) {
    String dataXName = this.getCollectionName();

    ProcessModel pmodel = ProcessModel.parse(this.getString(StoreResourceType.KEY_PROCESS_MODEL));
    IDataxProcessor processor = (IDataxProcessor) pmodel.loadDataXProcessor(this, dataXName);
    Map<String, Object> result = Maps.newHashMap();

    Optional<DataxReader> dataXReader = pmodel.getDataXReader(this, dataXName);
    DataxReader.BaseDataxReaderDescriptor readerDesc = null;
    DataxReader reader = null;
    if (dataXReader.isPresent()) {
      reader = dataXReader.get();
      readerDesc = (DataxReader.BaseDataxReaderDescriptor) reader.getDescriptor();
      result.put("readerDesc", DescriptorsJSON.desc(readerDesc));
    }

    DataxWriter writer = (DataxWriter) processor.getWriter(this, true);

    DataxWriter.BaseDataxWriterDescriptor writerDesc = (DataxWriter.BaseDataxWriterDescriptor) writer.getDescriptor();


    result.put("processMeta", ProcessModel.getDataXBasicProcessMeta(Optional.ofNullable(readerDesc), writerDesc));
    result.put("writerDesc", DescriptorsJSON.desc(writerDesc));

    setBizResult(context, result);
  }


  public static List<String> getTablesInDB(SubFormFilter filter) {
    DataxReader reader = DataxReader.getDataxReader(filter);
    return reader.getTablesInDB().getTabs();
  }

  public static List<ColumnMetaData> getReaderTableSelectableCols(String dataxName, String table) {
    throw new UnsupportedOperationException();
  }


  public static class DataxPluginDescMeta extends PluginDescMeta<DataxReader> {
    private final DescriptorsJSON writerTypesDesc;

    public DataxPluginDescMeta(DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes,
                               List<Descriptor<DataxWriter>> writerTypes) {
      super(readerTypes);
      this.writerTypesDesc = new DescriptorsJSON(writerTypes);
    }

    @JSONField(serialize = false)
    public DescriptorsJSONResult getPluginDesc() {
      throw new UnsupportedOperationException();
    }

    public DescriptorsJSONResult getReaderDesc() {
      return pluginDesc.getDescriptorsJSON();
    }


    public DescriptorsJSONResult getWriterDesc() {
      return writerTypesDesc.getDescriptorsJSON();
    }
  }

}
