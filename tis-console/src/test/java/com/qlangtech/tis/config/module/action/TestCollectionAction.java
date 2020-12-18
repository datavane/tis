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
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import com.qlangtech.tis.workflow.pojo.DatasourceTableCriteria;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import org.apache.struts2.StrutsSpringTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-16 10:39
 */
public class TestCollectionAction extends StrutsSpringTestCase {

  static {
    CenterResource.setNotFetchFromCenterRepository();
    HttpUtils.addMockGlobalParametersConfig();
    AbstractTisCloudSolrClient.initHashcodeRouter();
  }

  private static final String TEST_TABLE_NAME = "employees";
  private static final String TEST_DS_NAME = "employees";


  public void testDoCreate() throws Exception {

    this.clearUpDB();

    request.setParameter("emethod", "create");
    request.setParameter("action", "collection_action");

    JSONObject content = new JSONObject();
    JSONObject datasource = new JSONObject();

    datasource.put("plugin", "TiKV");
    datasource.put("pdAddrs", "192.168.28.202:2379");
    datasource.put("dbName", TEST_DS_NAME);
    content.put("datasource", datasource);
    content.put("indexName", TEST_TABLE_NAME);
    content.put("table", TEST_TABLE_NAME);

    JSONArray columns = getBuildTargetCols();

    content.put("columns", columns);
    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));
    ActionProxy proxy = getActionProxy("/config/config.ajax");
    assertNotNull(proxy);
    CollectionAction collectionAction = (CollectionAction) proxy.getAction();
    assertNotNull(collectionAction);
    String result = proxy.execute();
    // assertEquals(Action.NONE, result);
    AjaxValve.ActionExecResult actionExecResult = MockContext.getActionExecResult();
    if (!actionExecResult.isSuccess()) {
      System.out.println(AjaxValve.buildResultStruct(MockContext.instance));
      // actionExecResult.getErrorPageShow()
    }
    assertTrue(actionExecResult.isSuccess());
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
