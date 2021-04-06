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

import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.manage.common.valve.AjaxValve;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 16:19
 */
public class TestFullbuildWorkflowAction extends BasicActionTestCase {

  public void testDoCreateNewTaskWithSingleTableCollectionFullBuild() throws Exception {
    // createMockCollection(COLLECTION_NAME);
    String collectionName = "search4employee4local";
    request.setParameter("emethod", "createNewTask");
    request.setParameter("action", "fullbuild_workflow_action");
    request.setParameter(IFullBuildContext.KEY_TRIGGER_TYPE, String.valueOf(TriggerType.MANUAL.getValue()));
    request.setParameter(IFullBuildContext.KEY_APP_NAME, collectionName);
    // JSONObject content = new JSONObject();

//    content.put(CollectionAction.KEY_INDEX_NAME, TEST_TABLE_EMPLOYEES_NAME);
//    request.setContent(content.toJSONString().getBytes(TisUTF8.get()));

    ActionProxy proxy = getActionProxy();
    this.replay();
    String result = proxy.execute();
    assertEquals("FullbuildWorkflowAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    this.verifyAll();
  }

  private ActionProxy getActionProxy() {
    ActionProxy proxy = getActionProxy("/config/config.ajax");
    assertNotNull(proxy);
    FullbuildWorkflowAction fullbuildWorkflowAction = (FullbuildWorkflowAction) proxy.getAction();
    assertNotNull(fullbuildWorkflowAction);
    return proxy;
  }

}
