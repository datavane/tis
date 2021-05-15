/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.ISelectedTab;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.apps.IDepartmentGetter;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.impl.DataFlowAppSource;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.HeteroEnum;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;

import java.io.File;
import java.util.*;

/**
 * manage DataX pipe process logic
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-08 15:04
 */
@InterceptorRefs({@InterceptorRef("tisStack")})
public class DataxAction extends BasicModule {

  @Func(value = PermissionConstant.DATAX_MANAGE)
  public void doTriggerFullbuildTask(Context context) throws Exception {

    List<HttpUtils.PostParam> params = Lists.newArrayList();
    params.add(new HttpUtils.PostParam(CoreAction.KEY_APPNAME, this.getCollectionName()));
    params.add(new HttpUtils.PostParam(IParamContext.COMPONENT_START, FullbuildPhase.FullDump.getName()));
    params.add(new HttpUtils.PostParam(IParamContext.COMPONENT_END, FullbuildPhase.FullDump.getName()));

    CoreAction.triggerBuild(this, context, params);


  }

  /**
   * @param context
   */
  public void doDataxProcessorDesc(Context context) {
    this.setBizResult(context, new PluginDescMeta(Collections.singletonList(DataxProcessor.getPluginDescMeta())));
  }

  /**
   * 启动DataX执行器
   *
   * @param context
   */
  public void doLaunchDataxWorker(Context context) {
    PluginStore<DataXJobWorker> dataxJobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
    DataXJobWorker dataxJobWorker = dataxJobWorkerStore.getPlugin();

    if (dataxJobWorker.inService()) {
      throw new IllegalStateException("dataxJobWorker is in serivce ,can not launch repeat");
    }

    dataxJobWorker.launchService();
    this.doGetDataxWorkerMeta(context);
    AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
    DataXJobWorkerStatus jobWorkerStatus = (DataXJobWorkerStatus) actionExecResult.getBizResult();
    if (jobWorkerStatus == null || !jobWorkerStatus.isK8sReplicationControllerCreated()) {
      throw new IllegalStateException("DataX Controller launch faild please contract administer");
    }
    this.addActionMessage(context, "已经成功启动DataX执行器");
  }

  /**
   * 删除dataX实例
   *
   * @param context
   */
  public void doRemoveDataxWorker(Context context) {
    PluginStore<DataXJobWorker> dataxJobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
    DataXJobWorker dataxJobWorker = dataxJobWorkerStore.getPlugin();
    if (!dataxJobWorker.inService()) {
      throw new IllegalStateException("dataxJobWorker is not in serivce ,can not remove");
    }
    dataxJobWorker.remove();
    this.addActionMessage(context, "DataX Worker 已经被删除");
  }

  /**
   * 取得K8S dataX worker
   *
   * @param context
   */
  public void doGetDataxWorkerMeta(Context context) {
    PluginStore<DataXJobWorker> dataxJobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
    DataXJobWorker dataxJobWorker = dataxJobWorkerStore.getPlugin();
    DataXJobWorkerStatus jobWorkerStatus = new DataXJobWorkerStatus();
    if (dataxJobWorker == null) {
      jobWorkerStatus.setK8sReplicationControllerCreated(false);
      this.setBizResult(context, jobWorkerStatus);
      return;
    }

    jobWorkerStatus.setK8sReplicationControllerCreated(dataxJobWorker.inService());
    jobWorkerStatus.setRcDeployment(dataxJobWorker.getRCDeployment());
    this.setBizResult(context, jobWorkerStatus);
  }

  /**
   * 保存K8S dataX worker
   *
   * @param context
   */
  public void doSaveDataxWorker(Context context) {
    JSONObject postContent = this.parseJsonPost();
    JSONObject k8sSpec = postContent.getJSONObject("k8sSpec");
    //JSONObject dataxWorker = postContent.getJSONObject("dataxWorker");

    // UploadPluginMeta pluginMeta = UploadPluginMeta.parse(HeteroEnum.DATAX_WORKER.identity);

//    JSONArray itemsArray = new JSONArray();
//    itemsArray.add(dataxWorker);
//    PluginAction.PluginItemsParser pluginItemsParser
//      = PluginAction.parsePluginItems(this, pluginMeta
//      , context, 0, dataxWorker.getJSONArray("items"), false);

//    if (pluginItemsParser.faild) {
//
//      return;
//    }

    IncrUtils.IncrSpecResult incrSpecResult = IncrUtils.parseIncrSpec(context, k8sSpec, this);
    if (!incrSpecResult.isSuccess()) {
      return;
    }
    PluginStore<DataXJobWorker> jobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
    Descriptor.ParseDescribable<DataXJobWorker> describablesWithMeta = PluginStore.getDescribablesWithMeta(jobWorkerStore);
    DataXJobWorker dataxJobWorker = describablesWithMeta.instance;
    Objects.requireNonNull(dataxJobWorker, "dataxJobWorker can not be null");
    dataxJobWorker.setReplicasSpec(incrSpecResult.getSpec());
    if (incrSpecResult.hpa != null) {
      dataxJobWorker.setHpa(incrSpecResult.hpa);
    }
    //IPluginContext pluginContext, Optional<Context> context, List<Descriptor.ParseDescribable<T>> dlist, boolean update
    List<Descriptor.ParseDescribable<DataXJobWorker>> dlist = Collections.singletonList(describablesWithMeta);
    jobWorkerStore.setPlugins(this, Optional.empty(), dlist, true);
  }

  public void doDataxWorkerDesc(Context context) {

    List<Descriptor<DataXJobWorker>> descriptors = HeteroEnum.DATAX_WORKER.descriptors();
    this.setBizResult(context, new PluginDescMeta(descriptors));
  }

  public void doGetSupportedReaderWriterTypes(Context context) {

    DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes = TIS.get().getDescriptorList(DataxReader.class);
    DescriptorExtensionList<DataxWriter, Descriptor<DataxWriter>> writerTypes = TIS.get().getDescriptorList(DataxWriter.class);

    this.setBizResult(context, new DataxPluginDescMeta(readerTypes, writerTypes));
  }

  /**
   * 取得生成的配置文件的内容
   *
   * @param context
   */
  public void doGetGenCfgFile(Context context) throws Exception {
    String dataxName = this.getString("dataxName");
    String fileName = this.getString("fileName");
    DataxProcessor dataxProcessor = DataFlowAppSource.load(dataxName);
    File dataxCfgDir = dataxProcessor.getDataxCfgDir();
    File cfgFile = new File(dataxCfgDir, fileName);
    if (!cfgFile.exists()) {
      throw new IllegalStateException("target file:" + cfgFile.getAbsolutePath());
    }
    Map<String, Object> fileMeta = Maps.newHashMap();
    fileMeta.put("content", FileUtils.readFileToString(cfgFile, TisUTF8.get()));
    this.setBizResult(context, fileMeta);
  }

  /**
   * @param context
   * @throws Exception
   */
  public void doValidateDataxProfile(Context context) throws Exception {
    Application app = this.parseJsonPost(Application.class);
    SchemaAction.CreateAppResult validateResult = this.createNewApp(context, app
      , true, (newAppId) -> {
        throw new UnsupportedOperationException();
      });
  }

  /**
   * DataX中显示已有部门
   *
   * @param
   * @return
   */
  public static List<Option> getDepartments() {
    RunContext runContext = BasicServlet.getBeanByType(ServletActionContext.getServletContext(), RunContext.class);
    Objects.requireNonNull(runContext, "runContext can not be null");
    return BasicModule.getDptList(runContext, new IDepartmentGetter() {
    });
  }

  /**
   * 重新生成datax配置文件
   *
   * @param context
   * @throws Exception
   */
  public void doGenerateDataxCfgs(Context context) throws Exception {
    String dataxName = this.getString("dataxName");
    DataxProcessor dataxProcessor = DataFlowAppSource.load(dataxName);
    DataXCfgGenerator cfgGenerator = new DataXCfgGenerator(dataxProcessor);
    File dataxCfgDir = dataxProcessor.getDataxCfgDir();
    FileUtils.forceMkdir(dataxCfgDir);
    // 先清空文件
    FileUtils.cleanDirectory(dataxCfgDir);
    this.setBizResult(context, cfgGenerator.startGenerateCfg(dataxCfgDir));
  }


  /**
   * 创建DataX实例
   *
   * @param context
   */
  public void doCreateDatax(Context context) throws Exception {
    String dataxName = this.getString("dataxName");
    DataxProcessor dataxProcessor = DataFlowAppSource.load(dataxName);
    Application app = dataxProcessor.buildApp(); //this.parseJsonPost(Application.class);

    SchemaAction.CreateAppResult createAppResult = this.createNewApp(context, app
      , false, (newAppId) -> {
        SchemaAction.CreateAppResult appResult = new SchemaAction.CreateAppResult();
        appResult.setSuccess(true);
        appResult.setNewAppId(newAppId);
        return appResult;
      });

  }

  /**
   * 保存表映射
   *
   * @param context
   */
  public void doSaveTableMapper(Context context) {
    String dataxName = this.getString("dataxName");
    // 表别名列表
    JSONArray tabAliasList = this.parseJsonArrayPost();
    Objects.requireNonNull(tabAliasList, "tabAliasList can not be null");

    DataxProcessor dataxProcessor = null;
    JSONObject alias = null;
    IDataxProcessor.TableAlias tabAlias = null;
    List<IDataxProcessor.TableAlias> tableMaps = Lists.newArrayList();
    for (int i = 0; i < tabAliasList.size(); i++) {
      alias = tabAliasList.getJSONObject(i);
      tabAlias = new IDataxProcessor.TableAlias();
      tabAlias.setFrom(alias.getString("from"));
      tabAlias.setTo(alias.getString("to"));
      tableMaps.add(tabAlias);
    }

    //Descriptor<IAppSource> pluginDescMeta = DataxProcessor.getPluginDescMeta();
    //Descriptor.ParseDescribable<IAppSource> appSourceParseDescribable = pluginDescMeta.newInstance(this, Collections.emptyMap(), Optional.empty());
    dataxProcessor = DataxProcessor.load(this, dataxName);//  appSource.isPresent() ? appSource.get() : (DataxProcessor) appSourceParseDescribable.instance;
    dataxProcessor.setTableMaps(tableMaps);
    DataFlowAppSource.save(dataxName, dataxProcessor);
  }

  /**
   * 取得表映射
   *
   * @param context
   */
  public void doGetTableMapper(Context context) {
    String dataxName = this.getString("dataxName");
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
    DataxReader dataxReader = readerStore.getPlugin();
    Objects.requireNonNull(dataxReader, "dataReader:" + dataxName + " relevant instance can not be null");

    IDataxProcessor.TableAlias tableAlias;
    Optional<DataxProcessor> dataXAppSource = DataFlowAppSource.loadNullable(dataxName);
    Map<String, IDataxProcessor.TableAlias> tabMaps = Collections.emptyMap();
    if (dataXAppSource.isPresent()) {
      DataxProcessor dataxSource = dataXAppSource.get();
      tabMaps = dataxSource.getTabAlias();
    }

    if (dataxReader.hasMulitTable()) {
      List<IDataxProcessor.TableAlias> tmapList = Lists.newArrayList();
      for (ISelectedTab selectedTab : dataxReader.getSelectedTabs()) {
        tableAlias = tabMaps.get(selectedTab.getName());
        if (tableAlias == null) {
          tmapList.add(new IDataxProcessor.TableAlias(selectedTab.getName()));
        } else {
          tmapList.add(tableAlias);
        }
      }
      this.setBizResult(context, tmapList);
    }
  }

  /**
   * submit reader type and writer type form for validate
   *
   * @param context
   */
  public void doValidateReaderWriter(Context context) {
    JSONObject post = this.parseJsonPost();

    String dataxPipeName = post.getString("dataxPipeName");

    DataxReader dataxReader = DataxReader.load(dataxPipeName);
    DataxReader.BaseDataxReaderDescriptor descriptor = (DataxReader.BaseDataxReaderDescriptor) dataxReader.getDescriptor();
//    JSONObject readerDescriptor = post.getJSONObject("readerDescriptor");
//    JSONObject writerDescriptor = post.getJSONObject("writerDescriptor");
    DataXCreateProcessMeta processMeta = new DataXCreateProcessMeta();
    // 使用这个属性来控制是否要进入创建流程的第三步
    processMeta.readerMultiTableSelectable = descriptor.isMulitTableSelectable();
    processMeta.explicitTable = descriptor.hasExplicitTable();
    this.setBizResult(context, processMeta);
  }

  public static class DataXCreateProcessMeta {
    boolean readerMultiTableSelectable;
    boolean explicitTable;

    public boolean isExplicitTable() {
      return explicitTable;
    }

    public boolean isReaderMultiTableSelectable() {
      return readerMultiTableSelectable;
    }
  }

  public static List<String> getTablesInDB(String dataxName) {
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
    DataxReader reader = readerStore.getPlugin();
    Objects.requireNonNull(reader, "reader can not be null");
    return reader.getTablesInDB();
  }

  public static List<ColumnMetaData> getReaderTableSelectableCols(String dataxName, String table) {
    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
    DataxReader reader = readerStore.getPlugin();
    Objects.requireNonNull(reader, "reader can not be null");
    List<ColumnMetaData> tableMeta = reader.getTableMetadata(table);
    return tableMeta;
  }

//  /**
//   * get cols meta
//   *
//   * @param context
//   */
//  public void doGetReaderTableSelectableCols(Context context) {
//    String dataxName = this.getString(DataxUtils.DATAX_NAME);
//    String tableName = this.getString("tableName");
//    KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
//    DataxReader reader = readerStore.getPlugin();
//    Objects.requireNonNull(reader, "reader can not be null");
//    List<ColumnMetaData> tableMeta = reader.getTableMetadata(tableName);
//    this.setBizResult(context, tableMeta);
//  }


  public static class DataxPluginDescMeta extends PluginDescMeta<DataxReader> {
    private final DescriptorsJSON writerTypesDesc;

    public DataxPluginDescMeta(DescriptorExtensionList<DataxReader, Descriptor<DataxReader>> readerTypes
      , DescriptorExtensionList<DataxWriter, Descriptor<DataxWriter>> writerTypes) {
      super(readerTypes);
      this.writerTypesDesc = new DescriptorsJSON(writerTypes);
    }

    @JSONField(serialize = false)
    public com.alibaba.fastjson.JSONObject getPluginDesc() {
      throw new UnsupportedOperationException();
    }

    public com.alibaba.fastjson.JSONObject getReaderDesc() {
      return pluginDesc.getDescriptorsJSON();
    }


    public com.alibaba.fastjson.JSONObject getWriterDesc() {
      return writerTypesDesc.getDescriptorsJSON();
    }
  }

}
