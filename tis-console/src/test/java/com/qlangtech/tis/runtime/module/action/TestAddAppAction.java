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
package com.qlangtech.tis.runtime.module.action;

import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.cloud.MockZKUtils;
import com.qlangtech.tis.config.module.action.TestCollectionAction;
import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.impl.SingleTableAppSource;
import com.qlangtech.tis.manage.spring.MockZooKeeperGetter;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-01 12:40
 */
public class TestAddAppAction extends BasicActionTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    HttpUtils.mockConnMaker.clearStubs();
    HttpUtils.addMockApply(CoreAction.CREATE_COLLECTION_PATH, () -> {
      return TestCollectionAction.class.getResourceAsStream("s4employees_create_success.json");
    });
  }

  /**
   * 测试创建
   *
   * @throws Exception
   */
  public void testDoCreateCollection() throws Exception {
    request.setParameter("emethod", "create_collection");
    request.setParameter("action", "add_app_action");

    try (InputStream content = this.getClass().getResourceAsStream("create_confirm_index_http_body.json")) {
      assertNotNull(content);
      request.setContent(IOUtils.toByteArray(content));
    }
    ITISCoordinator zkCoordinator = MockZKUtils.createZkMock();
    MockZooKeeperGetter.mockCoordinator = zkCoordinator;

    setCollection(TestSchemaAction.collection);

    ActionProxy proxy = getActionProxy();
    replay();
    String result = proxy.execute();
    assertEquals("AddAppAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    verifyAll();

    IAppSource appSource = IAppSource.load(null, TestSchemaAction.collection);
    assertTrue(appSource instanceof SingleTableAppSource);
  }

  private ActionProxy getActionProxy() {
    ActionProxy proxy = getActionProxy("/runtime/addapp.ajax");
    assertNotNull(proxy);
    AddAppAction schemaAction = (AddAppAction) proxy.getAction();
    assertNotNull(schemaAction);
    return proxy;
  }
}
