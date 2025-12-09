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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.core.TestRealTISPlanAndExecuteAgent;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.DescriptorsJSONForAIPromote;
import com.qlangtech.tis.util.DescriptorsJSONResult;

import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.impl.PluginEqualResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.util.impl.AttrVals;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

import static com.qlangtech.tis.util.AttrValMap.parseDescribableMap;
import static org.easymock.EasyMock.*;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/11
 */
public class PluginInstanceCreateExecutorTest extends TestCase {

  @Test
  public void testExtractUserInput2Json() {
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
        = instanceCreateExecutor.extractUserInput2Json(IAgentContext.createNull(),
        new UserPrompt("解析数据源配置", userInput), endType, Objects.requireNonNull(entry.getValue()), llmProvider);

      Objects.requireNonNull(jsonObject);

      Descriptor targetDesc = impl.getImplDesc();

      PartialSettedPluginContext msgHandler = IPluginContext.namedContext("test");
//      DefaultMessageHandler messageHandler = new DefaultMessageHandler();
//      msgHandler.setMessageAndFieldErrorHandler(messageHandler, messageHandler);
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
      FormVaildateType verify = FormVaildateType.create(true);
      Descriptor.PluginValidateResult.setValidateItemPos(context, 0, 0);

      Descriptor.PluginValidateResult validateResult = attrValMap.validate(msgHandler, context, verify, Optional.empty());
      Assert.assertFalse(validateResult.isValid());

      validateResult = attrValMap.validate(msgHandler, context, FormVaildateType.create(false), Optional.empty());

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

  /**
   * 测试 isPluginEqual 方法 - 当两个插件的普通属性相等时
   */
  @Test
  public void testIsPluginEqual_WhenPropertiesAreEqual() throws Exception {
    // 创建测试对象
    PluginInstanceCreateExecutor executor = new PluginInstanceCreateExecutor();

    // 创建 Mock 对象
    Describable plugin = createMock(Describable.class);
    Descriptor descriptor = createMock(Descriptor.class);
    PluginFormProperties formProperties = createMock(PluginFormProperties.class);
    RootFormProperties rootProperties = createMock(RootFormProperties.class);

    // 构建属性值映射
    Map<String, JSON> attrMap = new LinkedHashMap<>();
    JSONObject field1 = new JSONObject();
    field1.put(Descriptor.KEY_primaryVal, "value1");
    attrMap.put("field1", field1);

    JSONObject field2 = new JSONObject();
    field2.put(Descriptor.KEY_primaryVal, "value2");
    attrMap.put("field2", field2);

    AttrVals pluginVals = new AttrVals(attrMap);

    // 构建属性类型映射
    LinkedHashMap<String, PropertyType> properties = new LinkedHashMap<>();

    // 创建第一个属性类型
    PropertyType propType1 = createMock(PropertyType.class);
    expect(propType1.isIdentity()).andReturn(false).anyTimes();
    expect(propType1.isDescribable()).andReturn(false).anyTimes();
    expect(propType1.getFrontendOutput(plugin)).andReturn("value1").anyTimes();
    properties.put("field1", propType1);

    // 创建第二个属性类型
    PropertyType propType2 = createMock(PropertyType.class);
    expect(propType2.isIdentity()).andReturn(false).anyTimes();
    expect(propType2.isDescribable()).andReturn(false).anyTimes();
    expect(propType2.getFrontendOutput(plugin)).andReturn("value2").anyTimes();
    properties.put("field2", propType2);

    // 创建属性列表
    List<Map.Entry<String, PropertyType>> propertyList = new ArrayList<>(properties.entrySet());

    // 设置期望行为
    expect(plugin.getDescriptor()).andReturn(descriptor).anyTimes();
    expect(descriptor.getPluginFormPropertyTypes()).andReturn(formProperties).anyTimes();

    // 设置 formProperties 的 accept 方法行为
    expect(formProperties.accept(anyObject(PluginFormProperties.IVisitor.class))).andAnswer(() -> {
      PluginFormProperties.IVisitor visitor = (PluginFormProperties.IVisitor) getCurrentArguments()[0];
      // 模拟 RootFormProperties 的行为
      expect(rootProperties.getSortedUseableProperties()).andReturn(propertyList).anyTimes();
      replay(rootProperties);
      return visitor.visit(rootProperties);
    }).anyTimes();

    // 回放所有 mock 对象
    replay(plugin, descriptor, formProperties, propType1, propType2);

    // 执行测试
    PluginEqualResult result = pluginVals.isPluginEqual(plugin);

    // 验证结果
    assertTrue("插件属性相等时应该返回true", result.isEqual());

    // 验证 mock 对象调用
    verify(plugin, descriptor, formProperties, propType1, propType2);
  }

  /**
   * 测试 isPluginEqual 方法 - 当普通属性不相等时
   */
  @Test
  public void testIsPluginEqual_WhenPropertiesAreDifferent() throws Exception {
    // 创建测试对象
    PluginInstanceCreateExecutor executor = new PluginInstanceCreateExecutor();

    // 创建 Mock 对象
    Describable plugin = createMock(Describable.class);
    Descriptor descriptor = createMock(Descriptor.class);
    PluginFormProperties formProperties = createMock(PluginFormProperties.class);
    RootFormProperties rootProperties = createMock(RootFormProperties.class);

    // 构建属性值映射
    Map<String, JSON> attrMap = new LinkedHashMap<>();
    JSONObject field1 = new JSONObject();
    field1.put(Descriptor.KEY_primaryVal, "value1");
    attrMap.put("field1", field1);

    JSONObject field2 = new JSONObject();
    field2.put(Descriptor.KEY_primaryVal, "differentValue"); // 不同的值
    attrMap.put("field2", field2);

    AttrVals pluginVals = new AttrVals(attrMap);

    // 构建属性类型映射
    LinkedHashMap<String, PropertyType> properties = new LinkedHashMap<>();

    // 创建第一个属性类型
    PropertyType propType1 = createMock(PropertyType.class);
    expect(propType1.isIdentity()).andReturn(false).anyTimes();
    expect(propType1.isDescribable()).andReturn(false).anyTimes();
    expect(propType1.getFrontendOutput(plugin)).andReturn("value1").anyTimes();
    properties.put("field1", propType1);

    // 创建第二个属性类型（返回不同的值）
    PropertyType propType2 = createMock(PropertyType.class);
    expect(propType2.isIdentity()).andReturn(false).anyTimes();
    expect(propType2.isDescribable()).andReturn(false).anyTimes();
    expect(propType2.getFrontendOutput(plugin)).andReturn("value2").anyTimes(); // 实际值与期望值不同
    properties.put("field2", propType2);

    // 创建属性列表
    List<Map.Entry<String, PropertyType>> propertyList = new ArrayList<>(properties.entrySet());

    // 设置期望行为
    expect(plugin.getDescriptor()).andReturn(descriptor).anyTimes();
    expect(descriptor.getPluginFormPropertyTypes()).andReturn(formProperties).anyTimes();

    // 设置 formProperties 的 accept 方法行为
    expect(formProperties.accept(anyObject(PluginFormProperties.IVisitor.class))).andAnswer(() -> {
      PluginFormProperties.IVisitor visitor = (PluginFormProperties.IVisitor) getCurrentArguments()[0];
      // 模拟 RootFormProperties 的行为
      expect(rootProperties.getSortedUseableProperties()).andReturn(propertyList).anyTimes();
      replay(rootProperties);
      return visitor.visit(rootProperties);
    }).anyTimes();

    // 回放所有 mock 对象
    replay(plugin, descriptor, formProperties, propType1, propType2);

    // 执行测试
    boolean result = pluginVals.isPluginEqual(plugin).isEqual();

    // 验证结果
    assertFalse("插件属性不相等时应该返回false", result);

    // 验证 mock 对象调用
    verify(plugin, descriptor, formProperties, propType1, propType2);
  }

  /**
   * 测试 isPluginEqual 方法 - 当插件为null时应该抛出异常
   */
  @Test
  public void testIsPluginEqual_WhenPluginIsNull() {
    PluginInstanceCreateExecutor executor = new PluginInstanceCreateExecutor();
    AttrVals pluginVals = new AttrVals(new LinkedHashMap<>());

    try {
      pluginVals.isPluginEqual(null);
      fail("当插件为null时应该抛出NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("plugin can not be null", e.getMessage());
    } catch (Exception e) {
      fail("应该抛出NullPointerException，但实际抛出了: " + e.getClass().getName());
    }
  }


  @Test
  public void testIsPluginEqual_WhenUsingMySQLPlugin() throws Exception {

    DataSourceFactory orderDb = DataSourceFactory.load("order2");

    DescribableJSON toJson = new DescribableJSON(orderDb);

    AttrValMap valMap = toJson.getPostAttribute();
    PluginInstanceCreateExecutor executor = new PluginInstanceCreateExecutor();

    Assert.assertTrue("dataSource instance must be equal "
      , valMap.getAttrVals().isPluginEqual(orderDb).isEqual());
  }
}
