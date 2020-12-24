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
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.servlet.LoadSolrCoreConfigByAppNameServlet;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.runtime.module.action.AddAppAction;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.DatasourceTableCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.StrutsSpringTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

  private static final String TEST_TABLE_NAME = "employees";
  private static final String TEST_DS_NAME = "employees";

  private static final String COLLECTION_NAME = TISCollectionUtils.NAME_PREFIX + TEST_TABLE_NAME;

  public void testSend2RemoteServer() throws Exception {
    this.clearUpDB();
    URL url = new URL("http://192.168.28.200:8080/tjs/config/config.ajax?emethod=create&action=collection_action");
    HttpUtils.post(url, getPostJSONContent().toJSONString().getBytes(TisUTF8.get()), new PostFormStreamProcess<Void>() {

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


  public void testDoCreate() throws Exception {

    this.clearUpDB();

    request.setParameter("emethod", "create");
    request.setParameter("action", "collection_action");

    JSONObject content = getPostJSONContent();
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy("/config/config.ajax");
    assertNotNull(proxy);
    CollectionAction collectionAction = (CollectionAction) proxy.getAction();
    assertNotNull(collectionAction);
    AtomicReference<AppKey> appKeyRef = new AtomicReference<>();
    AddAppAction.appKeyProcess = (key) -> {
      appKeyRef.set(key);
    };
    // 执行
    String result = proxy.execute();
    // assertEquals(Action.NONE, result);
    AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
    if (!actionExecResult.isSuccess()) {
      System.out.println(AjaxValve.buildResultStruct(MockContext.instance));
      // actionExecResult.getErrorPageShow()
    }

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
  }

  private JSONObject getPostJSONContent() {
    JSONObject content = new JSONObject();

    JSONObject datasource = new JSONObject();

    datasource.put("plugin", "TiKV");
    datasource.put("pdAddrs", "192.168.28.202:2379");
    datasource.put("dbName", TEST_DS_NAME);
    content.put("datasource", datasource);
    content.put("indexName", TEST_TABLE_NAME);
    content.put("table", TEST_TABLE_NAME);

    JSONObject incrCfg = new JSONObject();
    incrCfg.put("plugin", "TiCDC-Kafka");
    incrCfg.put("mqAddress", "192.168.28.201:9092");
    incrCfg.put("topic", "baisui");
    incrCfg.put("groupId", "consume_test1");
    incrCfg.put("offsetResetStrategy", "earliest");
    content.put("incr", incrCfg);
    JSONArray columns = getBuildTargetCols();

    content.put("columns", columns);
    System.out.println(content.toJSONString());
    return content;
  }

  private void clearUpDB() {
    IComDfireTisWorkflowDAOFacade wfDaoFacade
      = applicationContext.getBean("wfDaoFacade", IComDfireTisWorkflowDAOFacade.class);

    DatasourceTableCriteria tabCriteria = new DatasourceTableCriteria();
    tabCriteria.createCriteria().andNameEqualTo(TEST_TABLE_NAME);
    wfDaoFacade.getDatasourceTableDAO().deleteByExample(tabCriteria);

    DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
    dbCriteria.createCriteria().andNameEqualTo(TEST_DS_NAME);
    wfDaoFacade.getDatasourceDbDAO().deleteByExample(dbCriteria);

    WorkFlowCriteria wfCriteria = new WorkFlowCriteria();
    wfCriteria.createCriteria().andNameEqualTo(TEST_TABLE_NAME);
    wfDaoFacade.getWorkFlowDAO().deleteByExample(wfCriteria);
    // daoContext" class="com.qlangtech.tis.manage.common.RunContextImpl
    RunContext daoContext = applicationContext.getBean("daoContext", RunContext.class);

    ApplicationCriteria appCriteria = new ApplicationCriteria();
    appCriteria.createCriteria().andProjectNameEqualTo(TISCollectionUtils.NAME_PREFIX + TEST_TABLE_NAME);
    for (Application app : daoContext.getApplicationDAO().selectByExample(appCriteria)) {
      ServerGroupCriteria sgCriteria = new ServerGroupCriteria();
      sgCriteria.createCriteria().andAppIdEqualTo(app.getAppId());
      daoContext.getServerGroupDAO().deleteByExample(sgCriteria);
    }
    daoContext.getApplicationDAO().deleteByExample(appCriteria);


  }

  private JSONArray getBuildTargetCols() {
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
