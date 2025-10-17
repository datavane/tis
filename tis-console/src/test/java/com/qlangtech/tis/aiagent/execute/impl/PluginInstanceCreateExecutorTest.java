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

package com.qlangtech.tis.aiagent.execute.impl;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.TestRealTISPlanAndExecuteAgent;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.coredefine.module.action.PluginItemsParser;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;

import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.util.AttrValMap.parseDescribableMap;
import static org.junit.Assert.*;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/11
 */
public class PluginInstanceCreateExecutorTest {

  @Test
  public void extractUserInput2Json() {
  //  HeteroEnum.DATASOURCE.getPlugins();
    PluginInstanceCreateExecutor instanceCreateExecutor = new PluginInstanceCreateExecutor();
    String userInput = "host=192.168.1.10, port=3306, user=admin, password为‘pass123’, database=orders,所在区域是冰岛";
    Optional<IEndTypeGetter.EndType> endType = Optional.empty();

    DescribableImpl impl = new DescribableImpl(DataSourceFactory.class, Optional.empty());
    impl.addImpl("com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory");
    Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc = DescriptorsJSONForAIPromote.desc(impl);
    LLMProvider llmProvider = TestRealTISPlanAndExecuteAgent.getLlmProvider();
    for (Map.Entry<String, JSONObject> entry : desc.getLeft().getDescriptorsResult().entrySet()) {
      // 需要遍历他的所有属性如果有需要创建的属性插件需要先创建
      JSONObject jsonObject
        = instanceCreateExecutor.extractUserInput2Json(
        userInput, endType, Objects.requireNonNull(entry.getValue()), llmProvider);

      Objects.requireNonNull(jsonObject);

      Descriptor targetDesc = impl.getImplDesc();

      PartialSettedPluginContext msgHandler = IPluginContext.namedContext("test");
      DefaultMessageHandler messageHandler = new DefaultMessageHandler();
      msgHandler.setMessageAndFieldErrorHandler(messageHandler, messageHandler);
      Context context = new DefaultContext(); //
      //  IControlMsgHandler msgHandler;

      AttrValMap attrValMap = parseDescribableMap(
        Optional.empty(), jsonObject, ((propType, val) -> {

          // 需要判断 是否有可用的已经存在的插件实例可用，
          // 如果没有：则需要创建
          // 如果有：需要便利已经存在的插件确认是否是相同的
          if (propType.isIdentity()) {

          }

          return val;
        }));
      Descriptor.FormVaildateType verify = Descriptor.FormVaildateType.create(true);
      Descriptor.PluginValidateResult.setValidateItemPos(context, 0, 0);

      Descriptor.PluginValidateResult validateResult = attrValMap.validate(msgHandler, context, verify, Optional.empty());
      Assert.assertFalse(validateResult.isValid());

      validateResult = attrValMap.validate(msgHandler, context, Descriptor.FormVaildateType.create(false), Optional.empty());

      //  attrValMap.createDescribable()
      //
//      AttrVals formData = null; //
//      Optional<PluginFormProperties> pTypes = Optional.empty(); //
//      Optional<SubFormFilter> subFormFilter = Optional.empty();
//
//      Optional<Descriptor.PostFormVals> parentFormVals = Optional.empty();
//      targetDesc.verify(msgHandler, context, verify, );
//
//
//      UploadPluginMeta pluginMeta = UploadPluginMeta.parse("test");
//
//      int pluginIndex = 0;
//      JSONArray itemsArray = new JSONArray();
//      itemsArray.add(jsonObject);
//      boolean verify = true;
//      PropValRewrite propValRewrite = PropValRewrite.dftRewrite();//
//
//      PluginItemsParser.parsePluginItems(msgHandler, msgHandler, pluginMeta, context, pluginIndex, itemsArray, verify, propValRewrite);


    }


  }
}
