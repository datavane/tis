/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.coredefine.module.action;

import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.datax.impl.DataxReader;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-20 10:59
 **/
public class BasicPluginAction extends BasicActionTestCase {

  protected MockHttpServletRequest initializeRequest() {
    String dataXName = "baisuitestTestcase";
    request.addHeader(DataxReader.HEAD_KEY_REFERER, "/x/" + dataXName + "/config");
    request.setParameter("event_submit_do_get_plugin_config_info", "y");
    request.setParameter("action", "plugin_action");
    request.setParameter("plugin", "dataxReader:require,targetDescriptorName_MySQL,subFormFieldName_selectedTabs,dataxName_" + dataXName);
    return request;
  }

  protected ActionProxy getActionProxy() {
    ActionProxy proxy = getActionProxy("/coredefine/corenodemanage.ajax");
    assertNotNull(proxy);
    PluginAction pluginAction = (PluginAction) proxy.getAction();
    assertNotNull(pluginAction);
    return proxy;
  }
}
