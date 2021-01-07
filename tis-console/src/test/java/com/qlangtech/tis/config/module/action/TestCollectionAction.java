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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.coredefine.module.action.ExtendWorkFlowBuildHistory;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.LoadSolrCoreConfigByAppNameServlet;
import com.qlangtech.tis.manage.spring.MockClusterStateReader;
import com.qlangtech.tis.manage.spring.MockZooKeeperGetter;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.action.AddAppAction;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.runtime.module.action.TestSysInitializeAction;
import com.qlangtech.tis.solrdao.ISchemaField;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.DatasourceTableCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.struts2.StrutsSpringTestCase;
import org.apache.zookeeper.data.Stat;
import org.easymock.EasyMock;
import org.shai.xmodifier.util.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-16 10:39
 */
public class TestCollectionAction extends BasicActionTestCase {

  static {
    CenterResource.setNotFetchFromCenterRepository();
    HttpUtils.addMockGlobalParametersConfig();
    AbstractTisCloudSolrClient.initHashcodeRouter();

    // stub create collection
    HttpUtils.addMockApply(CoreAction.CREATE_COLLECTION_PATH, () -> {
      return TestCollectionAction.class.getResourceAsStream("s4employees_create_success.json");
    });

    // stub trigger collection indexbuild
    HttpUtils.addMockApply(CoreAction.TRIGGER_FULL_BUILD_COLLECTION_PATH, () -> {
      return TestCollectionAction.class.getResourceAsStream("s4employees_trigger_index_build_success.json");
    });

  }

  private static final String TEST_TABLE_EMPLOYEES_NAME = "employees";
  private static final String TEST_TABLE_SALARIES_NAME = "salaries";
  private static final String TEST_TABLE_DEPARTMENT_NAME = "departments";
  private static final String TEST_DS_NAME = "employees";

  private static final String FIELD_EMPLOYEES_FIRST_NAME = "first_name";
  private static final String FIELD_EMPLOYEES_LAST_NAME = "last_name";


  private static final Map<String, JSONArray> tabCols = Maps.newHashMap();

  static {

    tabCols.put(TEST_TABLE_EMPLOYEES_NAME, getBuildEmployeesCols());
    tabCols.put(TEST_TABLE_SALARIES_NAME, getBuildSaLariesCols());
    tabCols.put(TEST_TABLE_DEPARTMENT_NAME, getBuildDepartmentCols());
  }

  private static final String COLLECTION_NAME = TISCollectionUtils.NAME_PREFIX + TEST_TABLE_EMPLOYEES_NAME;

  public void testSend2RemoteServer() throws Exception {
    this.clearUpDB();
    URL url = new URL("http://192.168.28.200:8080/tjs/config/config.ajax?emethod=create&action=collection_action");
    // URL url = new URL("http://localhost:8080/tjs/config/config.ajax?emethod=create&action=collection_action");
    HttpUtils.post(url, getPostJSONContent(TEST_TABLE_EMPLOYEES_NAME).toJSONString().getBytes(TisUTF8.get()), new PostFormStreamProcess<Void>() {

      @Override
      public ContentType getContentType() {
        return ContentType.Multipart_byteranges;
      }

      @Override
      public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
        try {
          System.out.println(IOUtils.toString(stream, TisUTF8.get()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.clearMocks();
  }

  public void testDeleteCollection() throws Exception {

    ITISCoordinator zkCoordinator = mock("zkCoordinator", ITISCoordinator.class);

    try (InputStream input = this.getClass().getResourceAsStream("/com/qlangtech/tis/overseer_elect_leader.json")) {
      EasyMock.expect(zkCoordinator.getData(CoreAction.ZK_PATH_OVERSEER_ELECT_LEADER, null, new Stat(), true))
        .andReturn(IOUtils.toByteArray(input));
    }

    MockZooKeeperGetter.mockCoordinator = zkCoordinator;

    request.setParameter("emethod", "deleteIndex");
    request.setParameter("action", "collection_action");
    JSONObject content = new JSONObject();
    content.put(CollectionAction.KEY_INDEX_NAME, TEST_TABLE_EMPLOYEES_NAME);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));

    ActionProxy proxy = getActionProxy();
    this.replay();
    String result = proxy.execute();
    assertEquals("CollectionAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    this.verifyAll();
  }


  public void testDoFullbuild() throws Exception {
    ITISCoordinator zkCoordinator = mock("zkCoordinator", ITISCoordinator.class);
    // Watcher watcher = mock("watcher", Watcher.class);
    String incrStatecollect = "/tis/incr-transfer-group/incr-state-collect";
    String childPath = "nodes0000000361";
    String childPathContent = "192.168.28.200:38293";
    List<String> incrStatecollectList = Lists.newArrayList(childPath);
    EasyMock.expect(zkCoordinator.getChildren(incrStatecollect, null, true)).andReturn(incrStatecollectList);
    EasyMock.expect(zkCoordinator.getData(incrStatecollect + "/" + childPath, null, new Stat(), true))
      .andReturn(childPathContent.getBytes(TisUTF8.get()));

    MockZooKeeperGetter.mockCoordinator = zkCoordinator;

    request.setParameter("emethod", "fullbuild");
    request.setParameter("action", "collection_action");
    JSONObject content = new JSONObject();
    content.put(CollectionAction.KEY_INDEX_NAME, TEST_TABLE_EMPLOYEES_NAME);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy();
    this.replay();
    String result = proxy.execute();
    assertEquals("CollectionAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    CoreAction.TriggerBuildResult triggerResult = (CoreAction.TriggerBuildResult) aResult.getBizResult();
    assertNotNull(triggerResult);
    assertEquals(1234, triggerResult.getTaskid());
    this.verifyAll();
  }

  public void testQuery() throws Exception {

//    URL.setURLStreamHandlerFactory(new StubStreamHandlerFactory());
//    URLStreamHandler streamHandler = mock("httpStreamHander", StubStreamHandlerFactory.StubHttpURLStreamHander.class);
//    StubStreamHandlerFactory.streamHander = streamHandler;

    String collectionName = TISCollectionUtils.NAME_PREFIX + TEST_TABLE_EMPLOYEES_NAME;

    TISZkStateReader tisZkStateReader = this.mock("tisZkStateReader", TISZkStateReader.class);
    MockClusterStateReader.mockStateReader = tisZkStateReader;

    DocCollection docCollection = this.mock("docCollection", DocCollection.class);
    Map<String, Slice> sliceMap = Maps.newHashMap();
    Slice slice = this.mock("shard1Slice", Slice.class);
    Replica replica = this.mock("core_node2_replica", Replica.class);
    sliceMap.put("shard1", slice);
    EasyMock.expect(slice.getReplicas()).andReturn(Collections.singleton(replica));
    EasyMock.expect(replica.getBool("leader", false)).andReturn(true);
    EasyMock.expect(replica.getCoreUrl()).andReturn("http://192.168.28.200:8080/solr/search4employees_shard1_replica_n1");
    EasyMock.expect(docCollection.getSlicesMap()).andReturn(sliceMap);
    EasyMock.expect(tisZkStateReader.fetchCollectionState(collectionName, null))
      .andReturn(docCollection);

    request.setParameter("emethod", "query");
    request.setParameter("action", "collection_action");
    int rowsLimit = 3;
    JSONObject content = new JSONObject();
    content.put(CollectionAction.KEY_INDEX_NAME, TEST_TABLE_EMPLOYEES_NAME);
    JSONArray searchFields = new JSONArray();
    JSONObject queryField = new JSONObject();
    queryField.put("field", FIELD_EMPLOYEES_FIRST_NAME);
    queryField.put("word", "Nirm");
    searchFields.add(queryField);
    content.put(CollectionAction.KEY_QUERY_SEARCH_FIELDS, searchFields);
    JSONArray returnFields = new JSONArray();
    returnFields.add(FIELD_EMPLOYEES_FIRST_NAME);
    returnFields.add(FIELD_EMPLOYEES_LAST_NAME);
    content.put(CollectionAction.KEY_QUERY_FIELDS, returnFields);//FIELD_EMPLOYEES_FIRST_NAME + "," + FIELD_EMPLOYEES_LAST_NAME);

    // content.put(CollectionAction.KEY_QUERY_QUERY_FIELDS, FIELD_EMPLOYEES_FIRST_NAME + " " + FIELD_EMPLOYEES_LAST_NAME);
    content.put(CollectionAction.KEY_QUERY_LIMIT, rowsLimit);
    content.put(CollectionAction.KEY_QUERY_ROWS_OFFSET, 2);
    //  content.put(CollectionAction.KEY_QUERY_ORDER_BY, );

    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy();
    this.replay();
    String result = proxy.execute();
    assertEquals("CollectionAction_ajax", result);
    AjaxValve.ActionExecResult actionExecResult = showBizResult();
    Map<String, Object> bizResult = (Map<String, Object>) actionExecResult.getBizResult();
    assertNotNull("bizResult can not be null", bizResult);
    Long rowsCount = (Long) bizResult.get(CollectionAction.RESULT_KEY_ROWS_COUNT);
    assertEquals("", 227l, (long) rowsCount);
    List<Map<String, String>> rows = (List<Map<String, String>>) bizResult.get(CollectionAction.RESULT_KEY_ROWS);
    assertNotNull(rows);
    assertEquals(rowsLimit, rows.size());
    this.verifyAll();
  }

  public void testDoGetTaskStatus() throws Exception {
    request.setParameter("emethod", "getTaskStatus");
    request.setParameter("action", "collection_action");
    JSONObject content = new JSONObject();
    int taskId = 644;
    content.put(IParamContext.KEY_TASK_ID, taskId);
    content.put(CollectionAction.KEY_SHOW_LOG, true);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy();
    String result = proxy.execute();
    assertEquals("CollectionAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    Map<String, Object> bizResult = (Map<String, Object>) aResult.getBizResult();
    assertNotNull(bizResult.get("log"));
    ExtendWorkFlowBuildHistory status = (ExtendWorkFlowBuildHistory) bizResult.get("status");
    assertNotNull(status);
    assertEquals(taskId, status.getId().intValue());

  }

  public void testDoCreate() throws Exception {

    this.clearUpDB();

    request.setParameter("emethod", "create");
    request.setParameter("action", "collection_action");

    JSONObject content = getPostJSONContent(TEST_TABLE_EMPLOYEES_NAME);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy();
    AtomicReference<AppKey> appKeyRef = new AtomicReference<>();
    AddAppAction.appKeyProcess = (key) -> {
      appKeyRef.set(key);
    };
    AtomicBoolean schemaParseResultProcessed = new AtomicBoolean(false);
    SchemaAction.parseResultCallback4test = (cols, schemaParseResult) -> {
      List<ISchemaField> schemaFields = schemaParseResult.getSchemaFields();
      assertNotNull(schemaFields);
      assertEquals(8, schemaFields.size());

      Map<String, ISchemaField> fields = schemaFields.stream().collect(Collectors.toMap((c) -> c.getName(), (c) -> c));
      String emp_no = "emp_no";
      ISchemaField pk = fields.get(emp_no);
      assertNotNull(pk);
      assertTrue(StringUtils.isEmpty(pk.getTokenizerType()));
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.STRING.literia, pk.getTisFieldTypeName());
      assertEquals(emp_no, schemaParseResult.getUniqueKey());
      assertEquals(emp_no, schemaParseResult.getSharedKey());
      String birth_date = "birth_date";
      ISchemaField field = fields.get(birth_date);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.DATE.literia, field.getTisFieldTypeName());
      assertTrue(StringUtils.isEmpty(field.getTokenizerType()));

      // String first_name = "first_name";
      field = fields.get(FIELD_EMPLOYEES_FIRST_NAME);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.STRING.literia, field.getTisFieldTypeName());
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.LIKE.literia, field.getTokenizerType());

      // String last_name = "last_name";
      field = fields.get(FIELD_EMPLOYEES_LAST_NAME);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.STRING.literia, field.getTisFieldTypeName());
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.LIKE.literia, field.getTokenizerType());

      String gender = "gender";
      field = fields.get(gender);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.STRING.literia, field.getTisFieldTypeName());
      assertTrue(StringUtils.isEmpty(field.getTokenizerType()));

      String hire_date = "hire_date";
      field = fields.get(hire_date);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.DATE.literia, field.getTisFieldTypeName());
      assertTrue(StringUtils.isEmpty(field.getTokenizerType()));
      schemaParseResultProcessed.set(true);
    };
    // 执行
    String result = proxy.execute();
    // assertEquals(Action.NONE, result);
    AjaxValve.ActionExecResult actionExecResult = showBizResult();

    CoreAction.TriggerBuildResult triggerResult = (CoreAction.TriggerBuildResult) actionExecResult.getBizResult();
    assertNotNull("triggerResult can not be null", triggerResult);

    assertTrue(triggerResult.success);
    assertEquals("taskId must large than 0", 1234, triggerResult.getTaskid());

    // SnapshotDomain snapshotDomain = HttpConfigFileReader.getResource(COLLECTION_NAME, targetSnapshotid, RunEnvironment.getSysRuntime(), ConfigFileReader.getAry);
    // 判断缓存中应该已经有snapshotDomain了
    assertNotNull("appKeyRef can not be null", appKeyRef.get());
    SnapshotDomain snapshotDomain = LoadSolrCoreConfigByAppNameServlet.getSnapshotDomain(
      ConfigFileReader.getConfigList(), appKeyRef.get().setFromCache(true), null);
    assertNotNull("snapshotDomain can not null", snapshotDomain);
    assertTrue(actionExecResult.isSuccess());
    assertTrue("schemaParseResultProcessed must be processd", schemaParseResultProcessed.get());
  }

  private AjaxValve.ActionExecResult showBizResult() {
    AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
    if (!actionExecResult.isSuccess()) {
      System.err.println(AjaxValve.buildResultStruct(MockContext.instance));
      // actionExecResult.getErrorPageShow()
    } else {
      System.out.println(AjaxValve.buildResultStruct(MockContext.instance));
    }
    return actionExecResult;
  }

  private ActionProxy getActionProxy() {
    ActionProxy proxy = getActionProxy("/config/config.ajax");
    assertNotNull(proxy);
    CollectionAction collectionAction = (CollectionAction) proxy.getAction();
    assertNotNull(collectionAction);
    return proxy;
  }

  private JSONObject getPostJSONContent(String tableName) {
    JSONObject content = new JSONObject();

    JSONObject datasource = new JSONObject();

    datasource.put("plugin", "TiKV");
    datasource.put("pdAddrs", "192.168.28.202:2379");
    datasource.put("dbName", TEST_DS_NAME);
    datasource.put("datetimeFormat", true);
    content.put("datasource", datasource);
    content.put("indexName", tableName);
    content.put("table", tableName);

    JSONObject incrCfg = new JSONObject();
    incrCfg.put("plugin", "TiCDC-Kafka");
    incrCfg.put("mqAddress", "192.168.28.201:9092");
    incrCfg.put("topic", "baisui");
    incrCfg.put("groupId", "consume_test1");
    incrCfg.put("offsetResetStrategy", "earliest");
    content.put("incr", incrCfg);
    JSONArray columns = tabCols.get(tableName);// getBuildEmployeesCols();

    content.put("columns", columns);
    System.out.println(content.toJSONString());
    return content;
  }

  private void clearUpDB() {
    IWorkflowDAOFacade wfDaoFacade
      = applicationContext.getBean("wfDaoFacade", IWorkflowDAOFacade.class);

    DatasourceTableCriteria tabCriteria = new DatasourceTableCriteria();
    tabCriteria.createCriteria().andNameEqualTo(TEST_TABLE_EMPLOYEES_NAME);
    wfDaoFacade.getDatasourceTableDAO().deleteByExample(tabCriteria);

    DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
    dbCriteria.createCriteria().andNameEqualTo(TEST_DS_NAME);
    wfDaoFacade.getDatasourceDbDAO().deleteByExample(dbCriteria);

    WorkFlowCriteria wfCriteria = new WorkFlowCriteria();
    wfCriteria.createCriteria().andNameEqualTo(TEST_TABLE_EMPLOYEES_NAME);
    wfDaoFacade.getWorkFlowDAO().deleteByExample(wfCriteria);
    // daoContext" class="com.qlangtech.tis.manage.common.RunContextImpl
    RunContext daoContext = applicationContext.getBean("daoContext", RunContext.class);

    ApplicationCriteria appCriteria = new ApplicationCriteria();
    appCriteria.createCriteria().andProjectNameEqualTo(TISCollectionUtils.NAME_PREFIX + TEST_TABLE_EMPLOYEES_NAME);
    for (Application app : daoContext.getApplicationDAO().selectByExample(appCriteria)) {
      ServerGroupCriteria sgCriteria = new ServerGroupCriteria();
      sgCriteria.createCriteria().andAppIdEqualTo(app.getAppId());
      daoContext.getServerGroupDAO().deleteByExample(sgCriteria);
    }
    daoContext.getApplicationDAO().deleteByExample(appCriteria);

    for (Long gid : daoContext.getServerGroupDAO().getServergroupWithoutAppReference()) {
      //assertNotNull(g.getGid());
      daoContext.getServerGroupDAO().deleteByPrimaryKey(gid.intValue());
    }

//    select g.gid
//    from server_group g left join application a on g.app_id = a.app_id
//    where a.app_id is null

  }

  private static JSONArray getBuildDepartmentCols() {
    JSONArray columns = new JSONArray();
    JSONObject col = null;
    col = new JSONObject();
    col.put("name", "dept_no");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "dept_name");
    col.put("search", true);
    columns.add(col);

    return columns;
  }

  private static JSONArray getBuildSaLariesCols() {
    JSONArray columns = new JSONArray();
    JSONObject col = null;
    col = new JSONObject();
    col.put("name", "emp_no");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "salary");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "from_date");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "to_date");
    col.put("search", true);
    columns.add(col);

    return columns;
  }

  private static JSONArray getBuildEmployeesCols() {
    JSONArray columns = new JSONArray();
    JSONObject col = null;
    col = new JSONObject();
    col.put("name", "emp_no");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "birth_date");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "first_name");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "last_name");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "gender");
    col.put("search", true);
    columns.add(col);

    col = new JSONObject();
    col.put("name", "hire_date");
    col.put("search", true);
    columns.add(col);

    return columns;
  }

//  @Override
//  protected ActionMapping getActionMapping(HttpServletRequest request) {
//    ActionMapping mapping = super.getActionMapping(request);
//    ActionContext.getContext().put(ServletActionContext.ACTION_MAPPING, mapping);
//    return mapping;
//  }

  @Override
  protected void setupBeforeInitDispatcher() throws Exception {
    // only load beans from spring once
    if (applicationContext == null) {

      //ApplicationContext appContext;
//      appContext.getAutowireCapableBeanFactory().autowireBeanProperties(
//        initAction, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);

      //GenericXmlContextLoader xmlContextLoader = new GenericXmlContextLoader();
      applicationContext = new ClassPathXmlApplicationContext(getContextLocations());
    }

    servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
  }


}
