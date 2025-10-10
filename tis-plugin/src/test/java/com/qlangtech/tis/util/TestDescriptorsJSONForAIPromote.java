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
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;

import java.util.*;

/**
 * DescriptorsJSONForAIPromote 单元测试类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2025/01/16
 */
public class TestDescriptorsJSONForAIPromote extends TestCase {

    private DefaultPlugin.DefaultDescriptor defaultDescriptor;
    private DescribableImpl describableImpl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // 初始化TIS实例（如果需要）
        try {
            TIS.get();
        } catch (Exception e) {
            // 如果TIS未初始化，可以忽略或进行初始化
        }

        // 创建DefaultDescriptor实例
        defaultDescriptor = new DefaultPlugin.DefaultDescriptor();
        describableImpl = new DescribableImpl(DefaultPlugin.class, Optional.empty());
    }

    /**
     * 测试构造方法 - 单个描述符
     */
    public void testConstructorWithSingleDescriptor() {

        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);
        assertNotNull(descriptorsJSON);
    }

    /**
     * 测试构造方法 - 描述符集合
     */
    public void testConstructorWithDescriptorCollection() {
        List<Descriptor<DefaultPlugin>> descriptors = Arrays.asList((Descriptor<DefaultPlugin>) defaultDescriptor);

        // 测试 rootDesc = true
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(descriptors, true);
        assertNotNull(descriptorsJSON);

        // 测试 rootDesc = false
        descriptorsJSON = new DescriptorsJSONForAIPromote<>(descriptors, false);
        assertNotNull(descriptorsJSON);
    }

    /**
     * 测试 getDescriptorsJSON 基本功能
     * 验证AI模式下不包含文档URL
     */
    public void testGetDescriptorsJSON() {
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        assertNotNull(result);
        assertNotNull(result.getDescriptorsResult());

        // 获取第一个描述符的JSON
        String descriptorId = defaultDescriptor.getId();
        assertTrue("Result should contain descriptor ID: " + descriptorId,
                result.getDescriptorsResult().containsKey(descriptorId));

        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);
        assertNotNull(descJson);

        // 验证基本信息
        assertEquals("default Plugin", descJson.getString(DescriptorsJSON.KEY_DISPLAY_NAME));
        assertEquals(descriptorId, descJson.getString(DescriptorsJSON.KEY_IMPL));

        // 关键验证：AI模式下不应包含文档URL
        assertFalse("Should NOT contain KEY_IMPL_URL in AI promote mode",
                descJson.containsKey(DescriptorsJSON.KEY_IMPL_URL));

        // 验证包含属性
        assertTrue("Should contain attrs", descJson.containsKey("attrs"));
        JSONArray attrs = descJson.getJSONArray("attrs");
        assertNotNull(attrs);
        assertTrue("Should have attributes", attrs.size() > 0);

        String serializeJson = JsonUtil.toString(result, true);
        System.out.println(serializeJson);
    }


    /**
     * 测试字段属性解析
     * DefaultPlugin 包含 name, password, cols, nestProp 字段
     */
    public void testFieldParsing() {
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();
        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);
        JSONArray attrs = descJson.getJSONArray("attrs");

        // 查找各个字段
        Map<String, JSONObject> fieldMap = new HashMap<>();
        for (int i = 0; i < attrs.size(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            fieldMap.put(attr.getString("key"), attr);
        }

        // 验证name字段
        if (fieldMap.containsKey("name")) {
            JSONObject nameField = fieldMap.get("name");
            assertEquals("Should be INPUTTEXT type",
                    FormFieldType.INPUTTEXT.getIdentity(),
                    nameField.getIntValue("type"));
            assertFalse("name should not be identity", nameField.getBoolean("pk"));
        }

        // 验证password字段
        if (fieldMap.containsKey("password")) {
            JSONObject passwordField = fieldMap.get("password");
            assertEquals("Should be PASSWORD type",
                    FormFieldType.PASSWORD.getIdentity(),
                    passwordField.getIntValue("type"));
            assertEquals("password should have ordinal 7", 7, passwordField.getIntValue("ord"));
            assertTrue("password should be required", passwordField.getBoolean("required"));
        }

        // 验证cols字段
        if (fieldMap.containsKey("cols")) {
            JSONObject colsField = fieldMap.get("cols");
            assertEquals("Should be TEXTAREA type",
                    FormFieldType.TEXTAREA.getIdentity(),
                    colsField.getIntValue("type"));
        }

        // 验证nestProp字段（嵌套属性）
        if (fieldMap.containsKey("nestProp")) {
            JSONObject nestField = fieldMap.get("nestProp");
            assertTrue("nestProp should be describable", nestField.getBoolean("describable"));
            assertTrue("Should have descriptors", nestField.containsKey("descriptors"));
        }
    }

//    /**
//     * 测试嵌套描述符的处理
//     * 验证嵌套的描述符也应该使用 DescriptorsJSONForAIPromote 处理
//     */
//    public void testNestedDescribable() {
//        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
//            new DescriptorsJSONForAIPromote<>(defaultDescriptor);
//
//        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();
//
//        String descriptorId = defaultDescriptor.getId();
//        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);
//        JSONArray attrs = descJson.getJSONArray("attrs");
//
//        // 查找nestProp字段
//        JSONObject nestPropField = null;
//        for (int i = 0; i < attrs.size(); i++) {
//            JSONObject attr = attrs.getJSONObject(i);
//            if ("nestProp".equals(attr.getString("key"))) {
//                nestPropField = attr;
//                break;
//            }
//        }
//
//        if (nestPropField != null) {
//            assertTrue("nestProp should be describable", nestPropField.getBoolean("describable"));
//
//            // 验证嵌套描述符
//            if (nestPropField.containsKey("descriptors")) {
//                JSONObject nestedDescriptors = nestPropField.getJSONObject("descriptors");
//                assertNotNull("Nested descriptors should not be null", nestedDescriptors);
//
//                // 嵌套的描述符也不应该包含文档URL（AI模式）
//                for (String key : nestedDescriptors.keySet()) {
//                    JSONObject nestedDesc = nestedDescriptors.getJSONObject(key);
//                    assertFalse("Nested descriptor should NOT contain URL in AI mode",
//                               nestedDesc.containsKey(DescriptorsJSON.KEY_IMPL_URL));
//                }
//            }
//        }
//    }

    /**
     * 测试多个描述符的情况
     */
    public void testMultipleDescriptors() {
        // 创建第二个描述符实例
        DefaultPlugin.DefaultDescriptor descriptor2 = new DefaultPlugin.DefaultDescriptor();

        List<Descriptor<DefaultPlugin>> descriptors = Arrays.asList(
                (Descriptor<DefaultPlugin>) defaultDescriptor,
                (Descriptor<DefaultPlugin>) descriptor2
        );

        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(descriptors, true);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        assertNotNull(result);
        // 由于两个描述符ID相同，实际上只会有一个
        assertTrue(result.getDescriptorsResult().size() >= 1);

        // 验证所有描述符都不包含文档URL
        for (Object key : result.getDescriptorsResult().keySet()) {
            JSONObject desc = (JSONObject) result.getDescriptorsResult().get(key);
            assertFalse("Descriptor should NOT contain URL in AI mode",
                    desc.containsKey(DescriptorsJSON.KEY_IMPL_URL));
        }
    }

    /**
     * 测试继承关系
     * 验证 DescriptorsJSONForAIPromote 正确继承了 DescriptorsJSON
     */
    public void testInheritance() {
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);

        // 验证是 DescriptorsJSON 的子类
        assertTrue("Should be instance of DescriptorsJSON",
                descriptorsJSON instanceof DescriptorsJSON);

        // 验证类型参数
        assertTrue("Should be parameterized with DefaultPlugin",
                descriptorsJSON instanceof DescriptorsJSONForAIPromote);
    }

    /**
     * 对比测试：DescriptorsJSON vs DescriptorsJSONForAIPromote
     * 验证两者的主要区别（文档URL的存在与否）
     */
    public void testCompareWithRegularDescriptorsJSON() {
        // 创建普通的 DescriptorsJSON
        DescriptorsJSON<DefaultPlugin> regularJSON = new DescriptorsJSON<>(defaultDescriptor);
        DescriptorsJSONResult regularResult = regularJSON.getDescriptorsJSON();

        // 创建 AI 模式的 DescriptorsJSONForAIPromote
        DescriptorsJSONForAIPromote<DefaultPlugin> aiJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);
        DescriptorsJSONResult aiResult = aiJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();

        // 获取两个结果的JSON对象
        JSONObject regularDesc = (JSONObject) regularResult.getDescriptorsResult().get(descriptorId);
        JSONObject aiDesc = (JSONObject) aiResult.getDescriptorsResult().get(descriptorId);

        // 验证普通模式包含URL，AI模式不包含URL
        assertTrue("Regular mode SHOULD contain KEY_IMPL_URL",
                regularDesc.containsKey(DescriptorsJSON.KEY_IMPL_URL));
        assertFalse("AI mode should NOT contain KEY_IMPL_URL",
                aiDesc.containsKey(DescriptorsJSON.KEY_IMPL_URL));

        // 其他字段应该相同
        assertEquals("Display names should be same",
                regularDesc.getString(DescriptorsJSON.KEY_DISPLAY_NAME),
                aiDesc.getString(DescriptorsJSON.KEY_DISPLAY_NAME));
        assertEquals("Impl IDs should be same",
                regularDesc.getString(DescriptorsJSON.KEY_IMPL),
                aiDesc.getString(DescriptorsJSON.KEY_IMPL));
    }

    /**
     * 测试空描述符列表的处理
     */
    public void testEmptyDescriptorList() {
        List<Descriptor<DefaultPlugin>> emptyList = Collections.emptyList();

        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(emptyList, true);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        assertNotNull(result);
        assertNotNull(result.getDescriptorsResult());
        assertEquals("Should have no descriptors", 0, result.getDescriptorsResult().size());
    }

    /**
     * 测试默认值处理
     * DefaultPlugin.DefaultDescriptor 设置了 name 字段的默认值
     */
    public void testDefaultValues() {
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();
        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);

        // 验证是否包含extractProps（可能包含默认值信息）
        if (descJson.containsKey("extractProps")) {
            Map<String, Object> extractProps = (Map<String, Object>) descJson.get("extractProps");
            assertNotNull("Extract props should not be null", extractProps);
        }
    }

    /**
     * 测试扩展点信息
     */
    public void testExtendPoint() {
        DescriptorsJSONForAIPromote<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPromote<>(defaultDescriptor, describableImpl);

        DescriptorsJSONResult result = descriptorsJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();
        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);

        // 验证扩展点
        assertTrue("Should contain extend point",
                descJson.containsKey(DescriptorsJSON.KEY_EXTEND_POINT));
        assertEquals("Extend point should be DefaultPlugin class",
                DefaultPlugin.class.getName(),
                descJson.getString(DescriptorsJSON.KEY_EXTEND_POINT));
    }
}