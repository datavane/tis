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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.manage.common.valve.AjaxValve;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-15 12:13
 **/
public class TestPluginAction extends BasicActionTestCase {

  /**
   * 在DataX实例创建时，使用Mysql类型的Reader时 需要选择导入表的的步骤中需要使用到
   *
   * @throws Exception
   */
  public void testGetPluginConfigInfoWithTargetDescriptorName() throws Exception {
    // this.servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,);
//    event_submit_do_get_plugin_config_info: y
//    action: plugin_action
//    plugin: dataxReader:require,targetDescriptorName_MySQL,subFormFieldName_selectedTabs,dataxName_baisuitest

    //doGetPluginConfigInfo
    String dataXName = "baisuitestTestcase";
    request.addHeader(DataxReader.HEAD_KEY_REFERER, "/x/" + dataXName + "/config");
    request.setParameter("event_submit_do_get_plugin_config_info", "y");
    request.setParameter("action", "plugin_action");
    request.setParameter("plugin", "dataxReader:require,targetDescriptorName_MySQL,subFormFieldName_selectedTabs,dataxName_" + dataXName);
    //JSONObject content = new JSONObject();

    //content.put(CollectionAction.KEY_INDEX_NAME, TEST_TABLE_EMPLOYEES_NAME);
    //request.setContent(content.toJSONString().getBytes(TisUTF8.get()));

    ActionProxy proxy = getActionProxy();
    this.replay();
    String result = proxy.execute();
    assertEquals("PluginAction_ajax", result);
    AjaxValve.ActionExecResult aResult = showBizResult();
    assertNotNull(aResult);
    assertTrue(aResult.isSuccess());
    Object bizResult = aResult.getBizResult();
    assertNotNull(bizResult);
    JSONObject bizJSON = (JSONObject) bizResult;
    JSONArray plugins = bizJSON.getJSONArray("plugins");
    assertEquals(1, plugins.size());
    JSONObject plugin = plugins.getJSONObject(0);
    JSONObject descriptors = plugin.getJSONObject("descriptors");
    JSONObject descriptor = null;
    JSONObject subFormMeta = null;
    int descriptorCount = 0;
    for (String readerImpl : descriptors.keySet()) {
      assertEquals("com.qlangtech.tis.plugin.datax.DataxMySQLReader", readerImpl);
      descriptor = descriptors.getJSONObject(readerImpl);
      assertNotNull(descriptor);
      subFormMeta = descriptor.getJSONObject("subFormMeta");
      assertNotNull(subFormMeta);

      descriptorCount++;
    }
    assertEquals(1, descriptorCount);
    this.verifyAll();

  }

  private ActionProxy getActionProxy() {
    ActionProxy proxy = getActionProxy("/coredefine/corenodemanage.ajax");
    assertNotNull(proxy);
    PluginAction pluginAction = (PluginAction) proxy.getAction();
    assertNotNull(pluginAction);
    return proxy;
  }
}
