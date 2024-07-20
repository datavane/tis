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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.valve.AjaxValve.ActionExecResult;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.ItemsErrors;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.IUploadPluginMeta;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.List;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-20 10:46
 **/
public class TestPluginItemsParser extends BasicPluginAction {

  /**
   * 测试clone 一个已有的pipeline通道时，设置新的通道名称存在重复时需要会报告错误信息
   */
  public void testParsePluginItemsWhenCloneAPipelineWithNewDuplicatedName() {

    this.initializeRequest();

    ActionProxy proxy = this.getActionProxy();
    PluginAction pluginAction = (PluginAction) proxy.getAction();

    // 该ID值需要与系统中已经有的实例Id重复名称
    String newIdentityName = "test";
    String meta = "appSource:require,update_true,justGetItemRelevant_true,dataxName_mysql_mysql,processModel_createDatax";
    List<IUploadPluginMeta> pluginMetas = (UploadPluginMeta.parse(pluginAction, new String[]{meta}, false));

    Context context = MockContext.instance;
    JSONObject postJson = JsonUtil.loadJSON(PluginItemsParser.class, "post-manipulate-body.json");
    UploadPluginMeta pluginMeta = null;
    for (IUploadPluginMeta m : pluginMetas) {
      m.putExtraParams(DBIdentity.KEY_UPDATE, Boolean.FALSE.toString());
      pluginMeta = (UploadPluginMeta) m;
      Assert.assertFalse("must be insert process", pluginMeta.isUpdate());
      JSONArray itemsArray = new JSONArray();
      itemsArray.add(Objects.requireNonNull(postJson.getJSONObject(IUploadPluginMeta.KEY_JSON_MANIPULATE_TARGET)
        , IUploadPluginMeta.KEY_JSON_MANIPULATE_TARGET));


      PluginItemsParser.parsePluginItems(pluginAction
        , (UploadPluginMeta) m
        , context, 0, itemsArray, false, (propType, val) -> {
          PropertyType ptype = (PropertyType) propType;
          // 将原先的主键覆盖掉
          return ptype.isIdentity() ? newIdentityName : val;
        });

      Assert.assertTrue("because newIdentityName is duplicate, result itemsProcessor shall be null", context.hasErrors());
      ActionExecResult execResult = MockContext.getActionExecResult();
      List<List<ItemsErrors>> errorMsgs = execResult.getPluginErrorList();
      Assert.assertEquals(1, errorMsgs.size());
      List<ItemsErrors> itemsErrors = null;
      Assert.assertEquals(1, (itemsErrors = errorMsgs.get(0)).size());
      Assert.assertEquals(1, itemsErrors.size());
      ItemsErrors itemErrors = itemsErrors.get(0);
      Assert.assertNotNull("itemErrors can not be null", itemErrors);
      JSONObject result = new JSONObject();
      result.put("content", itemErrors.serial2JSON());
      String assertFileName = "post-manipulate-item-errors.json";
      JsonUtil.assertJSONEqual(TestPluginItemsParser.class, assertFileName, result, (message, expected, actual) -> {
        Assert.assertEquals(message, expected, actual);
      });

      //context.
      return;
    }

    Assert.fail("can not arrive here");
  }
}
