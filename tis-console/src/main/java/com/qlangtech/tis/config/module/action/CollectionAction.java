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
package com.qlangtech.tis.config.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.xwork2.ActionContext;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.action.*;
import com.qlangtech.tis.coredefine.module.control.SelectableServer;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.runtime.module.action.CreateIndexConfirmModel;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.runtime.module.action.SysInitializeAction;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.solrdao.ISchemaField;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.meta.*;
import com.qlangtech.tis.util.*;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

//import com.qlangtech.tis.coredefine.biz.CoreNode;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-13 16:10
 */
public class CollectionAction extends com.qlangtech.tis.runtime.module.action.AddAppAction {
  private static final Position DEFAULT_SINGLE_TABLE_POSITION;
  private static final Position DEFAULT_SINGLE_JOINER_POSITION;
  private static final Logger logger = LoggerFactory.getLogger(CollectionAction.class);
  private static final int SHARED_COUNT = 1;

  static {
    DEFAULT_SINGLE_TABLE_POSITION = new Position();
    DEFAULT_SINGLE_TABLE_POSITION.setX(141);
    DEFAULT_SINGLE_TABLE_POSITION.setY(121);

    DEFAULT_SINGLE_JOINER_POSITION = new Position();
    DEFAULT_SINGLE_JOINER_POSITION.setX(237);
    DEFAULT_SINGLE_JOINER_POSITION.setY(296);
  }

  private String indexName = null;

  private PlatformTransactionManager transactionManager;

  @Autowired
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  /**
   * 创建索实例
   *
   * @param context
   * @throws Exception
   */
  public void doCreate(Context context) throws Exception {
    JSONObject post = this.parseJsonPost();
    JSONObject datasource = post.getJSONObject("datasource");
    JSONObject incrCfg = post.getJSONObject("incr");
    if (datasource == null) {
      throw new IllegalStateException("prop 'datasource' can not be null");
    }
    final String targetTable = post.getString("table");
    if (StringUtils.isEmpty(targetTable)) {
      throw new IllegalStateException("param 'table' can not be null");
    }
    this.indexName = StringUtils.defaultIfEmpty(post.getString("indexName"), targetTable);
    List<String> existCollection = CoreAction.listCollection(this, context);
    if (existCollection.contains(this.indexName)) {
      //throw new IllegalStateException();
      this.addErrorMessage(context, "index:" + this.indexName + " already exist in cloud");
      return;
    }
    PluginItems dataSourceItems = getDataSourceItems(datasource);
    if (dataSourceItems.items.size() < 1) {
      throw new IllegalStateException("datasource item can not small than 1,now:" + dataSourceItems.items.size());
    }

    TargetColumnMeta targetColMetas = getTargetColumnMeta(context, post, targetTable, dataSourceItems);
    if (!targetColMetas.valid) {
      return;
    }
    dataSourceItems.save(context);
    if (context.hasErrors()) {
      return;
    }
    DatasourceDb dsDb = (DatasourceDb) context.get(IMessageHandler.ACTION_BIZ_RESULT);
    Objects.requireNonNull(dsDb, "can not find dsDb which has insert into DB just now");

    OfflineManager offlineManager = new OfflineManager();
    offlineManager.setComDfireTisWorkflowDAOFacade(this.getWorkflowDAOFacade());
    TISTable table = new TISTable();
    table.setTableName(targetTable);
    table.setDbId(dsDb.getId());

    OfflineManager.ProcessedTable dsTable = offlineManager.addDatasourceTable(table, this, context, false, true);
    if (context.hasErrors()) {
      return;
    }
    this.setBizResult(context, new Object());
    Objects.requireNonNull(dsTable, "dsTable can not be null");
//    DataSourceFactoryPluginStore dsPluginStore = TIS.getDataBasePluginStore(new PostedDSProp(targetTable));
//    // 保存表
//    dsPluginStore.saveTable(targetTable, targetColMetas.targetColMetas);

    // 开始创建DF
    final String topologyName = indexName;
    File parent = new File(SqlTaskNode.parent, topologyName);
    FileUtils.forceMkdir(parent);
    final SqlTaskNodeMeta.SqlDataFlowTopology topology = this.createTopology(topologyName, dsTable, targetColMetas);

    OfflineDatasourceAction.CreateTopologyUpdateCallback dbSaver
      = new OfflineDatasourceAction.CreateTopologyUpdateCallback(this.getUser(), this.getWorkflowDAOFacade());
    WorkFlow df = dbSaver.execute(topologyName, topology);
    // 保存一个时间戳
    SqlTaskNodeMeta.persistence(topology, parent);
    // 在在引擎节点上创建实例节点
    this.createCollection(context, df, indexName, targetColMetas);

    if (incrCfg != null) {
      logger.info("start incr channel create");
      if (!createIncrSyncChannel(context, df, incrCfg)) {
        return;
      }
    }

    // 需要提交一下事务
    TransactionStatus tranStatus
      = (TransactionStatus) ActionContext.getContext().get(TransactionStatus.class.getSimpleName());
    Objects.requireNonNull(tranStatus,"transtatus can not be null");
    transactionManager.commit(tranStatus);

    // 现在需要开始触发全量索引了
    CoreAction.TriggerBuildResult triggerBuildResult
      = CoreAction.triggerFullIndexSwape(this, context, df.getId(), df.getName(), SHARED_COUNT);
    this.setBizResult(context, triggerBuildResult);
  }

  @Override
  public String getCollectionName() {
    if (StringUtils.isEmpty(this.indexName)) {
      throw new IllegalStateException("indexName:" + indexName + " can not be null");
    }
    return TISCollectionUtils.NAME_PREFIX + indexName;
  }

  /**
   * 会调获取索引创建的状态
   *
   * @param context
   * @throws Exception
   */
  public void doGetTaskStatus(Context context) throws Exception {

  }

  /**
   * @param context
   * @throws Exception
   */
  public void doQuery(Context context) throws Exception {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
//org.apache.solr.client.solrj.io.sql.DriverImpl
    try {
      con = DriverManager.getConnection("jdbc:solr://zkHost:port?collection=collection&amp;aggregationMode=map_reduce");
      stmt = con.createStatement();
      rs = stmt.executeQuery("select a, sum(b) from tablex group by a");
      while (rs.next()) {
        String a = rs.getString("a");
        rs.getString("sum(b)");
      }
    } finally {
      rs.close();
      stmt.close();
      con.close();
    }
  }

  private TargetColumnMeta getTargetColumnMeta(
    Context context, JSONObject post, String targetTable, PluginItems dataSourceItems) {
    TargetColumnMeta columnMeta = new TargetColumnMeta(targetTable);
    Map<String, ColumnMetaData> colMetas = null;
    for (AttrValMap vals : dataSourceItems.items) {
      if (!vals.validate(context)) {
        return columnMeta.invalid();
      }
      DataSourceFactory dsFactory = (DataSourceFactory) vals.createDescribable().instance;
      List<ColumnMetaData> tableMetadata = dsFactory.getTableMetadata(targetTable);
      colMetas = tableMetadata.stream().collect(Collectors.toMap((m) -> m.getKey(), (m) -> m));
      break;
    }
    Objects.requireNonNull(colMetas, "colMetas can not null");

    Map<String, TargetCol> targetColMap = getTargetCols(post);
    columnMeta.targetColMap = targetColMap;
    ColumnMetaData colMeta = null;

    for (Map.Entry<String, TargetCol> tc : targetColMap.entrySet()) {
      colMeta = colMetas.get(tc.getKey());
      if (colMeta == null) {
        throw new IllegalStateException("target col:" + tc.getKey() + " is not exist in table:" + targetTable + " meta cols"
          + colMetas.values().stream().map((c) -> c.getKey()).collect(Collectors.joining(",")));
      }
      columnMeta.targetColMetas.add(colMeta);
    }
    return columnMeta;
  }

  private static class TargetColumnMeta {
    private final String tableName;

    public TargetColumnMeta(String tableName) {
      this.tableName = tableName;
    }

    boolean valid = true;
    private Map<String, TargetCol> targetColMap = null;
    final List<ColumnMetaData> targetColMetas = Lists.newArrayList();
    //  ref: com.pingcap.tikv.types.MySQLType
    static final int TypeTimestamp = 7;
    static final int TypeDatetime = 12;
    static final int TypeDate = 10;


    /**
     * 这个是一个暂时的实现，类似设置记录的evenetTime，应该是让用户手工设置
     *
     * @return
     */
    public ColumnMetaData getTimeVerColName() {
      //  ref: com.pingcap.tikv.types.MySQLType
      for (ColumnMetaData cMeta : targetColMetas) {
        if (cMeta.getType() == TypeTimestamp || cMeta.getType() == TypeDatetime) {
          return cMeta;
        }
      }
      for (ColumnMetaData cMeta : targetColMetas) {
        if (cMeta.getType() == TypeDate) {
          return cMeta;
        }
      }

      for (ColumnMetaData cMeta : targetColMetas) {
        if (StringUtils.lowerCase(cMeta.getKey()).indexOf("time") > -1) {
          return cMeta;
        }
      }
      return getPKMeta();
    }

    /**
     * 目前只取得一个
     *
     * @return
     */
    public ColumnMetaData getPKMeta() {
      List<ColumnMetaData> pks = targetColMetas.stream().filter((c) -> c.isPk()).collect(Collectors.toList());
      if (pks.size() > 1) {
        throw new IllegalStateException("table:" + tableName + "'s pk col can not much than 1,now is:"
          + pks.stream().map((r) -> r.getKey()).collect(Collectors.joining(",")));
      }
      if (pks.size() < 1) {
        throw new IllegalStateException("table:" + tableName + " can not find pk");
      }
      for (ColumnMetaData pk : pks) {
        TargetCol targetCol = targetColMap.get(pk.getKey());
        Objects.requireNonNull(targetCol, "pk:" + pk.getKey() + " is not in target cols:"
          + targetColMap.values().stream().map((r) -> r.getName()).collect(Collectors.joining(",")));
        return pk;
      }
      throw new IllegalStateException();
    }

    public Map<String, TargetCol> getTargetColMap() {
      Objects.requireNonNull(targetColMap, "targetColMap can not be bull");
      return this.targetColMap;
    }

    private TargetColumnMeta invalid() {
      this.valid = false;
      return this;
    }


  }


  /**
   * 创建索引实例
   *
   * @param context
   * @param df
   * @param indexName
   * @param targetColMetas
   * @throws Exception
   */
  private void createCollection(Context context, WorkFlow df, String indexName, TargetColumnMeta targetColMetas) throws Exception {
    Objects.requireNonNull(df, "param df can not be null");
    CreateIndexConfirmModel confirmModel = new CreateIndexConfirmModel();
    SelectableServer.ServerNodeTopology coreNode = new SelectableServer.ServerNodeTopology();

    SelectableServer.CoreNode[] coreNodeInfo
      = SelectableServer.getCoreNodeInfo(this.getRequest(), this, false, true);

    //FIXME 这一步应该是去掉的最终提交的host内容应该是一个ip格式的，应该是取getNodeName的内容，UI中的内容应该要改一下
    for (SelectableServer.CoreNode n : coreNodeInfo) {
      n.setHostName(n.getNodeName());
    }

    coreNode.setReplicaCount(1);
    coreNode.setShardCount(SHARED_COUNT);
    coreNode.setHosts(coreNodeInfo);

    confirmModel.setCoreNode(coreNode);
    confirmModel.setTplAppId(getTemplateApp(this).getAppId());
    ExtendApp extendApp = new ExtendApp();
    extendApp.setDptId(SysInitializeAction.DEPARTMENT_DEFAULT_ID);
    extendApp.setName(indexName);
    extendApp.setRecept(this.getUser().getName());
    Objects.requireNonNull(df.getId(), "id of dataflow can not be null");
    extendApp.setWorkflow(df.getId() + ":" + df.getName());

    confirmModel.setAppform(extendApp);

    SchemaResult schemaResult = SchemaAction.mergeWfColsWithTplCollection(this
      , context, df, (cols, schemaParseResult) -> {
        ColumnMetaData pkMeta = targetColMetas.getPKMeta();
        PSchemaField field = null;
        TargetCol tcol = null;
        for (ISchemaField f : schemaParseResult.getSchemaFields()) {
          field = (PSchemaField) f;
          if (StringUtils.equals(pkMeta.getKey(), field.getName())) {
            field.setIndexed(true);
          }
          tcol = targetColMetas.targetColMap.get(field.getName());
          if (tcol != null) {
            if (tcol.isIndexable()) {
              field.setIndexed(true);
            }
            if (StringUtils.isNotEmpty(tcol.getToken())) {
              field.setTokenizerType(tcol.getToken());
            }
          }
        }

        schemaParseResult.setUniqueKey(pkMeta.getKey());
        schemaParseResult.setSharedKey(pkMeta.getKey());
      });

    // 创建索引实例
    this.createCollection(context, confirmModel, schemaResult
      , (ctx, app, publishSnapshotId, schemaContent) -> {
        return this.createNewApp(ctx, app, publishSnapshotId, schemaContent);
      });
  }

  private SqlTaskNodeMeta.SqlDataFlowTopology createTopology(
    String topologyName, OfflineManager.ProcessedTable dsTable, TargetColumnMeta targetColMetas) throws Exception {
    SqlTaskNodeMeta.SqlDataFlowTopology topology = new SqlTaskNodeMeta.SqlDataFlowTopology();
    SqlTaskNodeMeta.TopologyProfile profile = new SqlTaskNodeMeta.TopologyProfile();
    profile.setName(topologyName);
    profile.setTimestamp(System.currentTimeMillis());
    topology.setProfile(profile);

    DependencyNode dNode = createDumpNode(dsTable);
    topology.addDumpTab(dNode);

    SqlTaskNodeMeta joinNodeMeta = new SqlTaskNodeMeta();
    joinNodeMeta.setId(String.valueOf(UUID.randomUUID()));
    joinNodeMeta.addDependency(dNode);
    joinNodeMeta.setExportName(topologyName);
    joinNodeMeta.setType(NodeType.JOINER_SQL.getType());
    joinNodeMeta.setPosition(DEFAULT_SINGLE_JOINER_POSITION);

    joinNodeMeta.setSql(ColumnMetaData.buildExtractSQL(
      dsTable.getName(), true, targetColMetas.targetColMetas).toString());

    topology.addNodeMeta(joinNodeMeta);


    /***********************************************************
     * 设置TabExtraMeta
     **********************************************************/
    ERRules erRules = new ERRules();
    DependencyNode node = createDumpNode(dsTable);
    node.setExtraSql(null);
    ColumnMetaData pkMeta = targetColMetas.getPKMeta();
    TabExtraMeta extraMeta = new TabExtraMeta();
    extraMeta.setSharedKey(pkMeta.getKey());
    extraMeta.setMonitorTrigger(true);
    List<PrimaryLinkKey> primaryIndexColumnName = Lists.newArrayList();
    PrimaryLinkKey pk = new PrimaryLinkKey();
    pk.setName(pkMeta.getKey());
    pk.setPk(true);
    primaryIndexColumnName.add(pk);
    extraMeta.setPrimaryIndexColumnNames(primaryIndexColumnName);
    extraMeta.setPrimaryIndexTab(true);

    ColumnMetaData timeVerColName = targetColMetas.getTimeVerColName();
    extraMeta.setTimeVerColName(timeVerColName.getKey());
    //extraMeta.setTimeVerColName();
    node.setExtraMeta(extraMeta);
    erRules.addDumpNode(node);
    // erRules.setRelationList();
    ERRules.write(topologyName, erRules);
    /***********************************************************
     * <<<<<<<<
     **********************************************************/


    // topology

    return topology;
  }

  private DependencyNode createDumpNode(OfflineManager.ProcessedTable dsTable) {
    DependencyNode dNode = new DependencyNode();
    dNode.setId(String.valueOf(UUID.randomUUID()));
    dNode.setDbName(dsTable.getDBName());
    dNode.setName(dsTable.getName());
    dNode.setDbid(String.valueOf(dsTable.getDbId()));
    dNode.setTabid(String.valueOf(dsTable.getId()));
    dNode.setExtraSql(dsTable.getExtraSql());
    dNode.setPosition(DEFAULT_SINGLE_TABLE_POSITION);
    dNode.setType(NodeType.DUMP.getType());
    return dNode;
  }

  private Map<String, TargetCol> getTargetCols(JSONObject post) {
    JSONArray targetCols = post.getJSONArray("columns");
    Map<String, TargetCol> targetColMap = targetCols.stream().map((c) -> {
      JSONObject o = (JSONObject) c;
      TargetCol targetCol = new TargetCol(o.getString("name"));
      Boolean indexable = o.getBoolean("search");
      targetCol.setIndexable(indexable == null ? true : indexable);
      targetCol.setToken(o.getString("token"));
      return targetCol;
    }).collect(Collectors.toMap((c) -> c.getName(), (c) -> c));
    return targetColMap;
  }

  /**
   * 创建增量同步通道
   *
   * @param incrCfg
   */
  private boolean createIncrSyncChannel(Context context, WorkFlow df, JSONObject incrCfg) throws Exception {

    // 生成DAO脚本
    HeteroEnum pluginType = HeteroEnum.MQ;
    UploadPluginMeta pluginMeta = UploadPluginMeta.parse(pluginType.identity + ":" + UploadPluginMeta.KEY_REQUIRE);
    PluginItems incrPluginItems = getPluginItems(incrCfg, pluginType, pluginMeta);
    if (incrPluginItems.items.size() < 1) {
      throw new IllegalStateException("incr plugin item size can not small than 1");
    }

    for (AttrValMap vals : incrPluginItems.items) {
      if (!vals.validate(context)) {
        // return columnMeta.invalid();
        return false;
      }
      // MQListenerFactory mqListenerFactory = (MQListenerFactory) vals.createDescribable().instance;
      break;
    }
    incrPluginItems.save(context);

    /**=======================================
     *开始生成脚本并且编译打包
     *=======================================*/
    IndexIncrStatus incrStatus = CoreAction.generateDAOAndIncrScript(
      this, context, df.getId(), true, true, true);

    if (context.hasErrors()) {
      return false;
    }

    IncrSpec incrPodSpec = new IncrSpec();
    //FIXME 目前先写死
    incrPodSpec.setReplicaCount(1);
    incrPodSpec.setMemoryRequest(Specification.parse("1G"));
    incrPodSpec.setMemoryLimit(Specification.parse("2G"));
    incrPodSpec.setCpuRequest(Specification.parse("500m"));
    incrPodSpec.setCpuLimit(Specification.parse("1"));

//    IncrUtils.IncrSpecResult applySpec = IncrUtils.parseIncrSpec(context, this.parseJsonPost(), this);
//    if (!applySpec.isSuccess()) {
//      return;
//    }
    // 将打包好的构建，发布到k8s集群中去
    // https://github.com/kubernetes-client/java
    TISK8sDelegate k8sClient = TISK8sDelegate.getK8SDelegate(this.getCollectionName());
    // 通过k8s发布
    k8sClient.deploy(incrPodSpec, incrStatus.getIncrScriptTimestamp());
    return true;
  }

  private PluginItems getDataSourceItems(JSONObject datasource) {

    HeteroEnum pluginType = HeteroEnum.DATASOURCE;

    UploadPluginMeta pluginMeta = UploadPluginMeta.parse(pluginType.identity
      + ":" + UploadPluginMeta.KEY_REQUIRE + "," + PostedDSProp.KEY_TYPE + "_detailed,update_false");
    return getPluginItems(datasource, pluginType, pluginMeta);
    // items.save(context);
  }


  private PluginItems getPluginItems(JSONObject pluginCfg, HeteroEnum pluginType, UploadPluginMeta pluginMeta) {
    Map<String, String> dsParams = Maps.newHashMap();
    for (String dsKey : pluginCfg.keySet()) {
      dsParams.put(dsKey, pluginCfg.getString(dsKey));
    }
    List<Descriptor<?>> descriptorList = TIS.get().getDescriptorList((Class) pluginType.extensionPoint);
    final String plugin = dsParams.remove("plugin");
    if (StringUtils.isEmpty(plugin)) {
      throw new IllegalStateException("pluginCfg/plugin can not be null");
    }
    Optional<Descriptor<?>> pluginDesc
      = descriptorList.stream().filter((des) -> plugin.equals(des.getDisplayName())).findFirst();
    Descriptor<?> dsDescriptpr = null;
    if (!pluginDesc.isPresent()) {
      throw new IllegalStateException("plugin:'" + plugin + "' relevant plugin descriper can not be null");
    }
    dsDescriptpr = pluginDesc.get();


    PluginItems items = new PluginItems(new DftPluginContext(pluginType), pluginMeta);
    JSONArray itemsArray = new JSONArray();
    JSONObject item = new JSONObject();
    JSONObject vals = new JSONObject();
    JSONObject val = null;
    for (Map.Entry<String, String> p : dsParams.entrySet()) {
      val = new JSONObject();
      val.put(Descriptor.KEY_primaryVal, p.getValue());
      vals.put(p.getKey(), val);
    }
    item.put(AttrValMap.PLUGIN_EXTENSION_IMPL, dsDescriptpr.getId());
    item.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);
    itemsArray.add(item);
    items.items = AttrValMap.describableAttrValMapList(this, itemsArray);
    return items;
  }


  private static class TargetCol {
    private final String name;
    private String token;
    private boolean indexable;

    public TargetCol(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    public boolean isIndexable() {
      return indexable;
    }

    public void setIndexable(boolean indexable) {
      this.indexable = indexable;
    }
  }

  private class DftPluginContext implements IPluginContext {

    private final HeteroEnum pluginType;

    public DftPluginContext(HeteroEnum pluginType) {
      this.pluginType = pluginType;
    }

    @Override
    public boolean isCollectionAware() {
      return this.pluginType == HeteroEnum.MQ;
    }

    @Override
    public String getCollectionName() {
      return CollectionAction.this.getCollectionName();
    }

    @Override
    public boolean isDataSourceAware() {
      return pluginType == HeteroEnum.DATASOURCE;
    }

    @Override
    public void addDb(String dbName, Context context) {
      // CollectionAction.this.
      DatasourceDbCriteria criteria = new DatasourceDbCriteria();
      criteria.createCriteria().andNameEqualTo(dbName);
      int exist = CollectionAction.this.getWorkflowDAOFacade().getDatasourceDbDAO().countByExample(criteria);
      // 如果数据库已经存在则直接跳过
      if (exist > 0) {
        for (DatasourceDb db : CollectionAction.this.getWorkflowDAOFacade().getDatasourceDbDAO().selectByExample(criteria)) {
          CollectionAction.this.setBizResult(context, db);
        }
        return;
      }

      CollectionAction.this.addDb(dbName, context);
    }


  }

}
