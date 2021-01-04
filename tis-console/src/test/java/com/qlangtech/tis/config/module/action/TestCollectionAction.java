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
import com.google.common.collect.Maps;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.LoadSolrCoreConfigByAppNameServlet;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.action.AddAppAction;
import com.qlangtech.tis.runtime.module.action.SchemaAction;
import com.qlangtech.tis.solrdao.ISchemaField;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.DatasourceTableCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.StrutsSpringTestCase;
import org.shai.xmodifier.util.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-16 10:39
 */
public class TestCollectionAction extends StrutsSpringTestCase {

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

  private static final Map<String, JSONArray> tabCols = Maps.newHashMap();

  static {

    tabCols.put(TEST_TABLE_EMPLOYEES_NAME, getBuildEmployeesCols());
    tabCols.put(TEST_TABLE_SALARIES_NAME, getBuildSaLariesCols());
    tabCols.put(TEST_TABLE_DEPARTMENT_NAME, getBuildDepartmentCols());
  }

  private static final String COLLECTION_NAME = TISCollectionUtils.NAME_PREFIX + TEST_TABLE_EMPLOYEES_NAME;

  public void testSend2RemoteServer() throws Exception {
    this.clearUpDB();
   // URL url = new URL("http://192.168.28.200:8080/tjs/config/config.ajax?emethod=create&action=collection_action");
    URL url = new URL("http://localhost:8080/tjs/config/config.ajax?emethod=create&action=collection_action");
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

  public void testDoGetTaskStatus() throws Exception {
    request.setParameter("emethod", "getTaskStatus");
    request.setParameter("action", "collection_action");
    JSONObject content = new JSONObject();
    content.put(IParamContext.KEY_TASK_ID, 644);
    content.put(CollectionAction.KEY_SHOW_LOG, true);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy();
    String result = proxy.execute();
    assertEquals("CollectionAction_ajax", result);
    showBizResult();
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

      String first_name = "first_name";
      field = fields.get(first_name);
      assertNotNull(field);
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.STRING.literia, field.getTisFieldTypeName());
      assertEquals(ColumnMetaData.ReflectSchemaFieldType.LIKE.literia, field.getTokenizerType());

      String last_name = "last_name";
      field = fields.get(last_name);
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
    }else{
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
    IComDfireTisWorkflowDAOFacade wfDaoFacade
      = applicationContext.getBean("wfDaoFacade", IComDfireTisWorkflowDAOFacade.class);

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


  @Override
  protected String[] getContextLocations() {
    //classpath:/tis.application.context.xml
    return new String[]{"classpath:/tis.application.context.xml"};
  }
}
