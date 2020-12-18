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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.control.SelectableServer;
import com.qlangtech.tis.extension.Descriptor;
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
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.meta.Position;
import com.qlangtech.tis.util.*;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
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

  static {
    DEFAULT_SINGLE_TABLE_POSITION = new Position();
    DEFAULT_SINGLE_TABLE_POSITION.setX(141);
    DEFAULT_SINGLE_TABLE_POSITION.setY(121);

    DEFAULT_SINGLE_JOINER_POSITION = new Position();
    DEFAULT_SINGLE_JOINER_POSITION.setX(237);
    DEFAULT_SINGLE_JOINER_POSITION.setY(296);
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
    if (datasource == null) {
      throw new IllegalStateException("prop 'datasource' can not be null");
    }
    final String targetTable = post.getString("table");
    if (StringUtils.isEmpty(targetTable)) {
      throw new IllegalStateException("param 'table' can not be null");
    }
    String indexName = StringUtils.defaultIfEmpty(post.getString("indexName"), targetTable);


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
    //TISTable table, BasicModule action, Context context, boolean updateMode
    TISTable table = new TISTable();
    table.setTableName(targetTable);
    table.setDbId(dsDb.getId());

    OfflineManager.ProcessedTable dsTable = offlineManager.addDatasourceTable(table, this, context, false);
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
    //List<ColumnMetaData> targetColMetas = Lists.newArrayList();
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

    coreNode.setReplicaCount(1);
    coreNode.setShardCount(1);

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
    String topologyName, OfflineManager.ProcessedTable dsTable, TargetColumnMeta targetColMetas) {
    SqlTaskNodeMeta.SqlDataFlowTopology topology = new SqlTaskNodeMeta.SqlDataFlowTopology();
    SqlTaskNodeMeta.TopologyProfile profile = new SqlTaskNodeMeta.TopologyProfile();
    profile.setName(topologyName);
    profile.setTimestamp(System.currentTimeMillis());
    topology.setProfile(profile);

    DependencyNode dNode = new DependencyNode();
    dNode.setId(String.valueOf(UUID.randomUUID()));
    dNode.setName(dsTable.getName());
    dNode.setDbid(String.valueOf(dsTable.getDbId()));
    dNode.setTabid(String.valueOf(dsTable.getId()));
    dNode.setExtraSql(dsTable.getExtraSql());
    dNode.setPosition(DEFAULT_SINGLE_TABLE_POSITION);
    dNode.setType(NodeType.DUMP.getType());
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

    return topology;
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


  private PluginItems getDataSourceItems(JSONObject datasource) {
    Map<String, String> dsParams = Maps.newHashMap();
    for (String dsKey : datasource.keySet()) {
      dsParams.put(dsKey, datasource.getString(dsKey));
    }
    List<Descriptor<DataSourceFactory>> descriptorList = TIS.get().getDescriptorList(DataSourceFactory.class);
    final String plugin = dsParams.remove("plugin");
    if (StringUtils.isEmpty(plugin)) {
      throw new IllegalStateException("datasource/plugin can not be null");
    }
    Optional<Descriptor<DataSourceFactory>> dsPluginDesc
      = descriptorList.stream().filter((des) -> plugin.equals(des.getDisplayName())).findFirst();
    Descriptor<DataSourceFactory> dsDescriptpr = null;
    if (!dsPluginDesc.isPresent()) {
      throw new IllegalStateException("plugin:'" + plugin + "' relevant plugin descriper can not be null");
    }
    dsDescriptpr = dsPluginDesc.get();


    UploadPluginMeta pluginMeta = UploadPluginMeta.parse(HeteroEnum.DATASOURCE.identity
      + ":" + UploadPluginMeta.KEY_REQUIRE + "," + PostedDSProp.KEY_TYPE + "_detailed,update_false");

    PluginItems items = new PluginItems(new DftPluginContext(), pluginMeta);
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
    // items.save(context);
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
    @Override
    public boolean isCollectionAware() {
      return false;
    }

    @Override
    public boolean isDataSourceAware() {
      return true;
    }

    @Override
    public void addDb(String dbName, Context context) {
      CollectionAction.this.addDb(dbName, context);
    }

    @Override
    public String getCollectionName() {
      return null;
    }
  }

}
