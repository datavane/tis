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
import com.qlangtech.tis.aiagent.llm.ITISJsonSchema;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ontology.OntologyGlossary;
import com.qlangtech.tis.plugin.ontology.OntologyValueType;
import com.qlangtech.tis.plugin.ontology.impl.glossary.DefaultOntologyGlossary;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_ONE_OF;
import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_PROPERTIES;
import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_CONST;
import static com.qlangtech.tis.extension.Descriptor.KEY_EPROPS;
import static com.qlangtech.tis.manage.common.Option.KEY_HELP;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;

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
        describableImpl.addImpl(DefaultPlugin.class.getName());
    }

    /**
     * 测试构造方法 - 单个描述符
     */
    public void testConstructorWithSingleDescriptor() {

        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);
        assertNotNull(descriptorsJSON);
    }

    public void testConstructorWithMySQL8() {
        String pluginId = "com.qlangtech.tis.plugin.ds.mysql.MySQLV8DataSourceFactory";
        //String pluginId = "com.qlangtech.tis.plugin.datax.DataxMySQLWriter";
        // String pluginId = "com.qlangtech.tis.plugin.datax.DataxMySQLReader";
        Descriptor descriptor = TIS.get().getDescriptor(pluginId);
        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(descriptor), true);

        DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta result =
                (DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) descriptorsJSON.getDescriptorsJSON();
        TISJsonSchema jsonSchema = result.descSchemaRegister.get(pluginId).getKey();
        //
        StringBuilder prompt = new StringBuilder();
        jsonSchema.appendFieldDescToPrompt(prompt);
        System.out.println(JsonUtil.toString(jsonSchema.root(), true));
        System.out.println("=========================");
        for (Option option : jsonSchema.getFieldsDesc()) {
            System.out.println(option.getName());
            if (option.getValue() instanceof Map) {
                for (Map.Entry<String, List<Option>> entry :
                        ((Map<String, List<Option>>) option.getValue()).entrySet()) {
                    System.out.println(" " + entry.getKey());
                    for (Option child : entry.getValue()) {
                        System.out.println("\t" + child.getName());
                    }

                }
            }
        }
    }

    /**
     * 测试构造方法 - 描述符集合
     */
    public void testConstructorWithDescriptorCollection() {
        List<Descriptor<DefaultPlugin>> descriptors = Arrays.asList((Descriptor<DefaultPlugin>) defaultDescriptor);

        // 测试 rootDesc = true
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON = new DescriptorsJSONForAIPrompt<>(descriptors, true);
        assertNotNull(descriptorsJSON);

        // 测试 rootDesc = false
        descriptorsJSON = new DescriptorsJSONForAIPrompt<>(descriptors, false);
        assertNotNull(descriptorsJSON);
    }

    /**
     * 测试 getDescriptorsJSON 基本功能
     * 验证AI模式下不包含文档URL
     */
    public void testGetDescriptorsJSON() {
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);

        DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta result =
                (DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) descriptorsJSON.getDescriptorsJSON();

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

        TISJsonSchema pluginMetaSchema = result.descSchemaRegister.get(descriptorId).getKey();
        assertNotNull("descriptorId:" + descriptorId + " relevant pluginMetaSchema can not be null", pluginMetaSchema);
        List<Option> fieldsDesc = pluginMetaSchema.getFieldsDesc();
        assertFalse(fieldsDesc.isEmpty());
    }


    /**
     * 测试字段属性解析
     * DefaultPlugin 包含 name, password, cols, nestProp 字段
     */
    public void testFieldParsing() {
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);

        DescriptorsMeta result = descriptorsJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();
        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);
        JSONArray attrs = descJson.getJSONArray("attrs");


        // 查找各个字段
        Map<String, JSONObject> fieldMap = new HashMap<>();
        for (int i = 0; i < attrs.size(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            fieldMap.put(attr.getString("key"), attr);
        }

        String fieldForShortAsynHelp = "fieldForShortAsynHelp";
        Assert.assertTrue(fieldForShortAsynHelp, fieldMap.containsKey(fieldForShortAsynHelp));
        JSONObject shortHelpField = fieldMap.get(fieldForShortAsynHelp);
        JSONObject eprops = shortHelpField.getJSONObject(KEY_EPROPS);
        Assert.assertNotNull(KEY_EPROPS, eprops);
        Assert.assertEquals("short_fieldForShortAsynHelp", eprops.getString(KEY_HELP));

        // 验证name字段
        if (fieldMap.containsKey("name")) {
            JSONObject nameField = fieldMap.get("name");
            assertEquals("Should be INPUTTEXT type", FormFieldType.INPUTTEXT.getIdentity(), nameField.getIntValue(
                    "type"));
            //  assertFalse("name should not be identity", nameField.getBoolean("pk"));
        }

        // 验证password字段
        if (fieldMap.containsKey("password")) {
            JSONObject passwordField = fieldMap.get("password");
            assertEquals("Should be PASSWORD type", FormFieldType.PASSWORD.getIdentity(), passwordField.getIntValue(
                    "type"));
            //  assertEquals("password should have ordinal 7", 7, passwordField.getIntValue("ord"));
            assertTrue("password should be required", passwordField.getBoolean("required"));
        }

        // 验证cols字段
        if (fieldMap.containsKey("cols")) {
            JSONObject colsField = fieldMap.get("cols");
            assertEquals("Should be TEXTAREA type", FormFieldType.TEXTAREA.getIdentity(), colsField.getIntValue("type"
            ));
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

        List<Descriptor<DefaultPlugin>> descriptors = Arrays.asList((Descriptor<DefaultPlugin>) defaultDescriptor,
                (Descriptor<DefaultPlugin>) descriptor2);

        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON = new DescriptorsJSONForAIPrompt<>(descriptors, true);

        DescriptorsMeta result = descriptorsJSON.getDescriptorsJSON();

        assertNotNull(result);
        // 由于两个描述符ID相同，实际上只会有一个
        assertTrue(result.getDescriptorsResult().size() >= 1);

        // 验证所有描述符都不包含文档URL
        for (Object key : result.getDescriptorsResult().keySet()) {
            JSONObject desc = (JSONObject) result.getDescriptorsResult().get(key);
            assertFalse("Descriptor should NOT contain URL in AI mode", desc.containsKey(DescriptorsJSON.KEY_IMPL_URL));
        }
    }

    /**
     * 测试继承关系
     * 验证 DescriptorsJSONForAIPromote 正确继承了 DescriptorsJSON
     */
    public void testInheritance() {
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);

        // 验证是 DescriptorsJSON 的子类
        assertTrue("Should be instance of DescriptorsJSON", descriptorsJSON instanceof DescriptorsJSON);

        // 验证类型参数
        assertTrue("Should be parameterized with DefaultPlugin", descriptorsJSON instanceof DescriptorsJSONForAIPrompt);
    }

    /**
     * 对比测试：DescriptorsJSON vs DescriptorsJSONForAIPromote
     * 验证两者的主要区别（文档URL的存在与否）
     */
    public void testCompareWithRegularDescriptorsJSON() {
        // 创建普通的 DescriptorsJSON
        DefaultDescriptorsJSON<DefaultPlugin> regularJSON = new DefaultDescriptorsJSON(defaultDescriptor);
        DescriptorsMeta regularResult = regularJSON.getDescriptorsJSON();

        // 创建 AI 模式的 DescriptorsJSONForAIPromote
        DescriptorsJSONForAIPrompt<DefaultPlugin> aiJSON = new DescriptorsJSONForAIPrompt<>(defaultDescriptor,
                describableImpl);
        DescriptorsMeta aiResult = aiJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();

        // 获取两个结果的JSON对象
        JSONObject regularDesc = (JSONObject) regularResult.getDescriptorsResult().get(descriptorId);
        JSONObject aiDesc = (JSONObject) aiResult.getDescriptorsResult().get(descriptorId);

        // 验证普通模式包含URL，AI模式不包含URL
        assertTrue("Regular mode SHOULD contain KEY_IMPL_URL", regularDesc.containsKey(DescriptorsJSON.KEY_IMPL_URL));
        assertFalse("AI mode should NOT contain KEY_IMPL_URL", aiDesc.containsKey(DescriptorsJSON.KEY_IMPL_URL));

        // 其他字段应该相同
        assertEquals("Display names should be same", regularDesc.getString(DescriptorsJSON.KEY_DISPLAY_NAME),
                aiDesc.getString(DescriptorsJSON.KEY_DISPLAY_NAME));
        assertEquals("Impl IDs should be same", regularDesc.getString(DescriptorsJSON.KEY_IMPL),
                aiDesc.getString(DescriptorsJSON.KEY_IMPL));
    }

    /**
     * 测试空描述符列表的处理
     */
    public void testEmptyDescriptorList() {
        List<Descriptor<DefaultPlugin>> emptyList = Collections.emptyList();

        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON = new DescriptorsJSONForAIPrompt<>(emptyList, true);

        DescriptorsMeta result = descriptorsJSON.getDescriptorsJSON();

        assertNotNull(result);
        assertNotNull(result.getDescriptorsResult());
        assertEquals("Should have no descriptors", 0, result.getDescriptorsResult().size());
    }

    /**
     * 测试默认值处理
     * DefaultPlugin.DefaultDescriptor 设置了 name 字段的默认值
     */
    public void testDefaultValues() {
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);

        DescriptorsMeta result = descriptorsJSON.getDescriptorsJSON();

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
        DescriptorsJSONForAIPrompt<DefaultPlugin> descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(defaultDescriptor, describableImpl);

        DescriptorsMeta result = descriptorsJSON.getDescriptorsJSON();

        String descriptorId = defaultDescriptor.getId();
        JSONObject descJson = (JSONObject) result.getDescriptorsResult().get(descriptorId);

        // 验证扩展点
        assertTrue("Should contain extend point", descJson.containsKey(DescriptorsJSON.KEY_EXTEND_POINT));
        assertEquals("Extend point should be DefaultPlugin class", DefaultPlugin.class.getName(),
                descJson.getString(DescriptorsJSON.KEY_EXTEND_POINT));

        Pair<DescriptorsMeta, DescriptorsJSONForAIPrompt> desc = DescriptorsJSONForAIPrompt.desc(describableImpl);

        DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta meta =
                (DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) desc.getKey();

        StringBuilder schema = new StringBuilder();
        for (Map.Entry<String, Pair<TISJsonSchema, Descriptor>> entry : meta.descSchemaRegister.entrySet()) {
            entry.getValue().getKey().appendFieldDescToPrompt(schema);

            System.out.println(JsonUtil.toString(entry.getValue().getKey().root(), true));
            break;
        }
        System.out.println(schema);
    }

    /**
     * 验证 MultiStepsSupportHost 类型 host 的 schema 自动展开为
     * <code>vals.multiStepsSavedItems[oneOf{stepImpl, stepVals}]</code>。
     * 与 {@link com.qlangtech.tis.extension.OneStepOfMultiSteps#parseStepsPlugin} 期望的反序列化格式天然对齐。
     */
    public void testMultiStepsSchemaForOntologyValueType() {
        OntologyValueType.DefaultDesc hostDesc = new OntologyValueType.DefaultDesc();
        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(hostDesc), true);
        DescriptorsMeta meta = descriptorsJSON.getDescriptorsJSON();

        ITISJsonSchema hostSchema = meta.getPluginJsonSchema().values().iterator().next();
        System.out.println("=== OntologyValueType Schema ===");
        System.out.println(JsonUtil.toString(hostSchema.root(), true));

        JSONObject hostProps = hostSchema.schema().getJSONObject(SCHEMA_PROPERTIES);
        assertNotNull("host properties must exist", hostProps);

        // impl const = host descriptor id
        JSONObject implProp = hostProps.getJSONObject(PLUGIN_EXTENSION_IMPL);
        assertNotNull(implProp);
        assertEquals("host impl const must equal descriptor id",
                hostDesc.getId(), implProp.getString(SCHEMA_VALUE_CONST));

        // vals.multiStepsSavedItems
        JSONObject valsProp = hostProps.getJSONObject(PLUGIN_EXTENSION_VALS);
        assertNotNull(valsProp);
        JSONObject valsInnerProps = valsProp.getJSONObject(SCHEMA_PROPERTIES);
        assertNotNull(valsInnerProps);
        JSONObject multiSteps = valsInnerProps.getJSONObject(MultiStepsSupportHost.KEY_MULTI_STEPS_SAVED_ITEMS);
        assertNotNull("multiStepsSavedItems must exist", multiSteps);
        assertEquals("array", multiSteps.getString("type"));

        JSONObject items = multiSteps.getJSONObject("items");
        assertNotNull(items);
        JSONArray oneOf = items.getJSONArray(SCHEMA_ONE_OF);
        assertNotNull("items.oneOf must exist", oneOf);
        assertEquals("Should have 2 step variants (Metadata + Constraints)", 2, oneOf.size());

        Set<String> stepImplConsts = new HashSet<>();
        for (int i = 0; i < oneOf.size(); i++) {
            JSONObject stepSchema = oneOf.getJSONObject(i);
            JSONObject stepProps = stepSchema.getJSONObject(SCHEMA_PROPERTIES);
            assertNotNull("step" + i + " must have properties", stepProps);
            JSONObject stepImpl = stepProps.getJSONObject(PLUGIN_EXTENSION_IMPL);
            assertNotNull("step" + i + " must have impl", stepImpl);
            stepImplConsts.add(stepImpl.getString(SCHEMA_VALUE_CONST));
            assertTrue("step" + i + " must have vals", stepProps.containsKey(PLUGIN_EXTENSION_VALS));
        }
        // 两个 step 的 impl 必须不同（独立的 vals 容器，不会互相覆盖）
        assertEquals("Two step variants must have distinct impl ids", 2, stepImplConsts.size());

        // 验证 step1 (MetadataOfValueType) 的 vals 含 name / description / type
        // 而且 type 字段已通过 MetadataOfValueType.json 的 enum 表达式注入了候选值
        JSONObject metadataStep = findStepByImplName(oneOf, "MetadataOfValueType");
        assertNotNull("MetadataOfValueType step not found", metadataStep);
        JSONObject metadataVals = metadataStep.getJSONObject(SCHEMA_PROPERTIES)
                .getJSONObject(PLUGIN_EXTENSION_VALS).getJSONObject(SCHEMA_PROPERTIES);
        assertTrue("vals.name", metadataVals.containsKey("name"));
        assertTrue("vals.description", metadataVals.containsKey("description"));
        assertTrue("vals.type", metadataVals.containsKey("type"));
        JSONObject typeProp = metadataVals.getJSONObject("type");
        assertTrue("type field should carry enum candidates from MetadataOfValueType.json",
                typeProp.containsKey("enum"));

        // 验证 step2 (ConstraintsOfValueType) 的 vals.constraint 是 oneOf 多态
        JSONObject constraintsStep = findStepByImplName(oneOf, "ConstraintsOfValueType");
        assertNotNull("ConstraintsOfValueType step not found", constraintsStep);
        JSONObject constraintsVals = constraintsStep.getJSONObject(SCHEMA_PROPERTIES)
                .getJSONObject(PLUGIN_EXTENSION_VALS).getJSONObject(SCHEMA_PROPERTIES);
        JSONObject constraintField = constraintsVals.getJSONObject("constraint");
        assertNotNull("vals.constraint must exist", constraintField);
        JSONArray constraintOneOf = constraintField.getJSONArray(SCHEMA_ONE_OF);
        assertNotNull("constraint must be oneOf", constraintOneOf);
        assertTrue("constraint oneOf must include Enum / Range / Regex variants, count=" + constraintOneOf.size(),
                constraintOneOf.size() >= 7);
    }

    private static JSONObject findStepByImplName(JSONArray oneOf, String simpleName) {
        for (int i = 0; i < oneOf.size(); i++) {
            JSONObject stepSchema = oneOf.getJSONObject(i);
            String impl = stepSchema.getJSONObject(SCHEMA_PROPERTIES)
                    .getJSONObject(PLUGIN_EXTENSION_IMPL).getString(SCHEMA_VALUE_CONST);
            if (impl != null && impl.endsWith("." + simpleName)) {
                return stepSchema;
            }
        }
        return null;
    }

    /**
     * 回归对照：非 MultiStepsSupportHost 的 host（OntologyGlossary）schema 不受核心库变更影响，
     * 仍然保持 vals 平铺业务字段、不出现 multiStepsSavedItems。
     */
    public void testNonMultiStepsSchemaUnchanged_OntologyGlossary() {
        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new DefaultOntologyGlossary.DefaultDesc()), true);
        DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta meta =
                (DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta) descriptorsJSON.getDescriptorsJSON();

        TISJsonSchema schema = meta.descSchemaRegister.values().iterator().next().getKey();
        System.out.println("=== OntologyGlossary Schema (regression) ===");
        System.out.println(JsonUtil.toString(schema.root(), true));

        JSONObject hostProps = schema.schema().getJSONObject(SCHEMA_PROPERTIES);
        JSONObject valsInner = hostProps.getJSONObject(PLUGIN_EXTENSION_VALS).getJSONObject(SCHEMA_PROPERTIES);

        assertFalse("Glossary should NOT have multiStepsSavedItems",
                valsInner.containsKey(MultiStepsSupportHost.KEY_MULTI_STEPS_SAVED_ITEMS));
        assertTrue("Glossary vals.term", valsInner.containsKey("term"));
        assertTrue("Glossary vals.synonyms", valsInner.containsKey("synonyms"));
        assertTrue("Glossary vals.description", valsInner.containsKey("description"));
        assertTrue("Glossary vals.target", valsInner.containsKey("target"));
    }
}