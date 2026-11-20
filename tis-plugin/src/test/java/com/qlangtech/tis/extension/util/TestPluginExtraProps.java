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
package com.qlangtech.tis.extension.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.util.AbstractPropAssist.MarkdownHelperContent;
import com.qlangtech.tis.extension.util.PluginExtraProps.RouterAssistType;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_CREATOR_ASSIST_TYPE;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
/**
 *
 */
public class TestPluginExtraProps {

    @Before public void setUp() throws Exception {
        PartialSettedPluginContext context = new PartialSettedPluginContext();
        context.setLoginUser(() -> Config.ADMIN_NAME);
        IPluginContext.setPluginContext(context);
    }

    /**
     * 反向 -> 正向 一致性：编程式 {@link PluginExtraProps.FieldRefCreator} 经
     * {@link PluginExtraProps.Props#setRefCreateor(Optional)} 写回 props 后，
     * 再经 {@link PluginExtraProps.Props#createFieldRefCreateor(JSONObject)} 正向解析，
     * 字段应与原 creator 一致
     */
    @Test
    public void testSetRefCreateorRoundtrip() {
        final String hetero = HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.getIdentity();

        PluginExtraProps.FieldRefCreator creator = new PluginExtraProps.FieldRefCreator();
        creator.setLabel("配置");
        creator.addCandidatePlugin(new PluginExtraProps.CandidatePlugin("DeepSeek",
                Optional.of("LLM"), hetero));
        creator.addCandidatePlugin(new PluginExtraProps.CandidatePlugin("QWen",
                Optional.empty(), hetero));
        creator.setAssistType(RouterAssistType.paramCfg);
        creator.setDftValGetter(() -> "DeepSeek-1");

        PluginExtraProps.Props props = new PluginExtraProps.Props(new JSONObject());
        props.setRefCreateor(Optional.of(creator));

        // 反写后的 props 结构
        JSONObject creatorJ = props.getProps().getJSONObject(PluginExtraProps.KEY_CREATOR);
        assertNotNull(creatorJ);
        assertEquals("配置", creatorJ.getString(Option.KEY_LABEL));
        assertEquals("paramCfg", creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
        assertEquals("DeepSeek-1", props.getProps().getString(PluginExtraProps.KEY_DFTVAL_PROP));

        JSONArray plugins = creatorJ.getJSONArray(PluginExtraProps.KEY_PLUGIN);
        assertEquals(2, plugins.size());
        JSONObject p0 = plugins.getJSONObject(0);
        assertEquals("DeepSeek", p0.getString(PluginExtraProps.KEY_DESC_NAME));
        assertEquals("LLM", p0.getString(UploadPluginMeta.KEY_TARGET_PLUGIN_DESC));
        assertEquals(hetero, p0.getString(PluginExtraProps.KEY_CREATOR_HETERO));
        assertEquals(Boolean.TRUE, p0.getBoolean(UploadPluginMeta.KEY_APPEND));

        // 正向解析回读，逐字段一致
        PluginExtraProps.FieldRefCreator parsed
                = PluginExtraProps.Props.createFieldRefCreateor(props.getProps());
        assertNotNull(parsed);
        assertEquals(creator.getLabel(), parsed.getLabel());
        assertEquals(creator.getAssistType(), parsed.getAssistType());
        assertEquals(creator.getCandidatePlugins().size(), parsed.getCandidatePlugins().size());
        assertEquals("DeepSeek-1", parsed.getDftValue());
        assertCandidateEquals(creator.getCandidatePlugins(), parsed.getCandidatePlugins());
    }

    /**
     * 正向 -> 反向 一致性：json 声明的 creator 正向解析为 {@link PluginExtraProps.FieldRefCreator} 后，
     * 经 {@link PluginExtraProps.Props#setRefCreateor(Optional)} 反写回 props，再次正向解析语义应保持一致。
     * <p>
     * 一致性以有效值衡量：原 json 缺省的 targetItemDesc（解析为 Optional.empty）经反写会落
     * {@code getTargetItemDesc()}（回退 displayName），append 缺省经解析归一为 true 后反写显式 true，
     * 两者二次解析后均与首趟解析收敛一致。
     */
    @Test
    public void testCreatorForwardBackwardConsistency() {
        JSONObject propsJson = JSON.parseObject("{"
                + "\"creator\":{\"label\":\"管理\","
                + "\"plugin\":[{\"hetero\":\"fs\",\"descName\":\"HDFS\"},"
                + "{\"hetero\":\"fs\",\"descName\":\"Aliyun-Jindo-HDFS\"}],"
                + "\"assistType\":\"paramCfg\"}}");

        PluginExtraProps.FieldRefCreator origin
                = PluginExtraProps.Props.createFieldRefCreateor(propsJson);
        assertNotNull(origin);
        assertEquals(2, origin.getCandidatePlugins().size());

        PluginExtraProps.Props props = new PluginExtraProps.Props(new JSONObject());
        props.setRefCreateor(Optional.of(origin));

        // 二次正向解析，语义一致
        PluginExtraProps.FieldRefCreator reparsed
                = PluginExtraProps.Props.createFieldRefCreateor(props.getProps());
        assertNotNull(reparsed);
        assertEquals(origin.getLabel(), reparsed.getLabel());
        assertEquals(origin.getAssistType(), reparsed.getAssistType());
        assertEquals(origin.getCandidatePlugins().size(), reparsed.getCandidatePlugins().size());
        assertCandidateEquals(origin.getCandidatePlugins(), reparsed.getCandidatePlugins());
    }

    private static void assertCandidateEquals(List<PluginExtraProps.CandidatePlugin> expected,
                                              List<PluginExtraProps.CandidatePlugin> actual) {
        for (int i = 0; i < expected.size(); i++) {
            PluginExtraProps.CandidatePlugin e = expected.get(i);
            PluginExtraProps.CandidatePlugin a = actual.get(i);
            assertEquals("candidate[" + i + "].displayName", e.getDisplayName(), a.getDisplayName());
            assertEquals("candidate[" + i + "].targetItemDesc", e.getTargetItemDesc(), a.getTargetItemDesc());
            assertEquals("candidate[" + i + "].hetero", e.getHetero().getIdentity(), a.getHetero().getIdentity());
            assertEquals("candidate[" + i + "].append", e.isAppend(), a.isAppend());
        }
    }

    //    public void testParsePostMCols() {
    //
    //        ElementCreatorFactory elementCreator = new TestElementCreatorFactory();
    //        IControlMsgHandler msgHandler = null;
    //        Context context = null;
    //        String keyColsMeta = null;
    //        JSONArray targetCols = null;
    //        IPropertyType propertyType = null;
    //        elementCreator.parsePostMCols(propertyType, msgHandler, context, keyColsMeta, targetCols);
    //    }

    private static class TestElementCreatorFactory implements ElementCreatorFactory<CMeta> {
        @Override
        public CMeta createDefault(JSONObject jo) {
            return new CMeta();
        }

        @Override
        public CMeta create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
            return new CMeta();
        }

        @Override
        public ParsePostMCols<CMeta> parsePostMCols(IPropertyType propertyType, IControlMsgHandler msgHandler,
                                                    Context context, String keyColsMeta, JSONArray targetCols) {
            throw new UnsupportedOperationException();
        }
        //        @Override
        //        public CMeta create(JSONObject targetCol) {
        //            return new CMeta();
        //        }
    }


    @Test
    public void testLode() throws Exception {
        Optional<PluginExtraProps> ep = PluginExtraProps.load(TestPluginExtraProps.class);
        assertNotNull(ep);
        assertTrue(ep.isPresent());
        PluginExtraProps extraProps = ep.get();
        PluginExtraProps.Props prop = extraProps.getProp("dbName");
        assertNotNull(prop);
        assertNotNull("数据库名", prop.getLable());

        prop = extraProps.getProp("userName");
        assertNotNull(prop);
        assertNotNull("用户名", prop.getLable());
        assertTrue("isAsynHelp must be true", prop.isAsynHelp());


        PluginExtraProps.Props encode = extraProps.getProp("encode");
        JSONObject props = encode.getProps();
        JSONObject creator = props.getJSONObject("creator");
        assertNotNull(creator);
        assertEquals("部门管理", creator.getString("label"));
        assertEquals("/base/departmentlist", creator.getString("routerLink"));
        assertEquals(RouterAssistType.hyperlink, RouterAssistType.parse(creator.getString(KEY_CREATOR_ASSIST_TYPE)));
    }

    //    public void testCreatorWithError() throws Exception {
    //
    //        try {
    //            Optional<PluginExtraProps> ep = PluginExtraProps.load(WithCreatorError.class);
    //            fail("must have faild");
    //        } catch (Exception e) {
    //            assertEquals("propKey:dbName,package:com.qlangtech.tis.extension.util,propKey:WithCreatorError
    //            .json", e.getMessage());
    //        }
    //    }

    @Test
    public void testCreatorWithMerge() throws Exception {
        Optional<PluginExtraProps> ep = PluginExtraProps.load(WithCreatorErrorOk.class);
        assertTrue(ep.isPresent());
        PluginExtraProps extraProps = ep.get();
        for (Map.Entry<String, PluginExtraProps.Props> e : extraProps.entrySet()) {
            System.out.println("key:" + e.getKey());
            System.out.println("value:" + JsonUtil.toString(e.getValue()));
            System.out.println("==============================================");
        }

        PluginExtraProps.Props dbName = extraProps.getProp("dbName");
        assertNotNull(dbName);

        JSONObject props = dbName.getProps();
        JSONObject creator = props.getJSONObject(PluginExtraProps.KEY_CREATOR);
        assertNotNull(creator);

        assertEquals("/base/departmentlist", creator.getString(PluginExtraProps.KEY_ROUTER_LINK));
        assertEquals("部门管理", creator.getString(Option.KEY_LABEL));

        JSONArray plugins = creator.getJSONArray("plugin");
        assertEquals(1, plugins.size());
        JSONObject pmeta = plugins.getJSONObject(0);
        assertNotNull(pmeta);

        JsonUtil.assertJSONEqual(TestPluginExtraProps.class, "pluginMeta.json", creator, (m, e, a) -> {
            assertEquals(m, e, a);
        });

        //        {
        //            "hetero": "params-cfg",
        //                "descName": "DataX-global",
        //                "extraParam": "append_true"
        //        }

    }

    @Test
    public void testAddFieldDescriptor() {

        DefaultPlugin plugin = new DefaultPlugin();
        Optional<ElementPluginDesc> pluginDesc = ElementPluginDesc.create(plugin.getDescriptor());
        Optional<PluginExtraProps> extraProps = PluginExtraProps.load(pluginDesc, DefaultPlugin.class);

        Assert.assertTrue(extraProps.isPresent());

        PluginExtraProps ep = extraProps.get();
        PluginExtraProps.Props nameProp = ep.getProp("name");
        Assert.assertNotNull(nameProp);

        Assert.assertTrue("isAsynHelp must be true", nameProp.isAsynHelp());
        Assert.assertEquals(DefaultPlugin.FILED_NAME_DESCRIPTION, nameProp.getAsynHelp());
        Assert.assertEquals(DefaultPlugin.DFT_NAME_VALUE, nameProp.getDftVal());

        DefaultDescriptorsJSON descJSON = new DefaultDescriptorsJSON(pluginDesc.get().getElementDesc());
        System.out.println(JsonUtil.toString(descJSON.getDescriptorsJSON()));

    }

    @Test
    public void testAddFieldDescriptorWithNotMatchFieldName() {
        try {
            DefaultPlugin plugin = new DefaultPlugin();
            DefaultPlugin.DefaultDescriptor desc = (DefaultPlugin.DefaultDescriptor) plugin.getDescriptor();
            desc.addFieldDescriptor("xxx", DefaultPlugin.DFT_NAME_VALUE,
                    new MarkdownHelperContent(PluginExtraProps.AsynPropHelp.create(DefaultPlugin.FILED_NAME_DESCRIPTION)));
            PluginExtraProps.load(ElementPluginDesc.create(desc), DefaultPlugin.class);
            Assert.fail("must be faild");
        } catch (Exception e) {
            Assert.assertEquals("prop key:xxx relevant prop must exist , exist props keys:fieldForShortAsynHelp,password,nestProp,name,"
                    + "cols", e.getMessage());
        }
    }

    /**
     * 测试 CandidatePlugin.createNewPrimaryFieldValue 方法
     */
    @Test
    public void testCreateNewPrimaryFieldValue() {
        // 测试用例1：空的选项列表
        PluginExtraProps.CandidatePlugin candidate = new PluginExtraProps.CandidatePlugin("MySQLReader",
                Optional.empty(), "datax-reader");

        List<Option> emptyOpts = Collections.emptyList();
        String result = candidate.createNewPrimaryFieldValue(emptyOpts).identityValue();
        assertEquals("mysqlreader_1", result);

        // 测试用例2：存在部分匹配的选项
        List<Option> existingOpts = Arrays.asList(new Option("第一个MySQL读取器", "mysqlreader-1"), new Option("第三个MySQL"
                + "读取器", "mysqlreader-3"), new Option("其他插件", "other-plugin"));
        result = candidate.createNewPrimaryFieldValue(existingOpts).identityValue();
        assertEquals("mysqlreader_4", result);

        // 测试用例3：存在连续的选项
        List<Option> continuousOpts = Arrays.asList(new Option("第一个MySQL读取器", "mysqlreader1"), new Option("第二个MySQL"
                + "读取器", "mysqlreader2"), new Option("第三个MySQL读取器", "mysqlreader3"));
        result = candidate.createNewPrimaryFieldValue(continuousOpts).identityValue();
        assertEquals("mysqlreader_4", result);

        // 测试用例4：不规则的命名模式
        List<Option> irregularOpts = Arrays.asList(new Option("大写的MySQL读取器", "MYSQLREADER-10"), new Option("小写的MySQL"
                + "读取器", "mysqlreader5"), new Option("不相关的插件", "unrelated"));
        result = candidate.createNewPrimaryFieldValue(irregularOpts).identityValue();
        assertEquals("mysqlreader_11", result);

        // 测试用例5：包含非数字后缀的选项
        List<Option> mixedOpts = Arrays.asList(new Option("非数字后缀", "mysqlreader-abc"), new Option("数字后缀",
                "mysqlreader-2"), new Option("空后缀", "mysqlreader-"));
        result = candidate.createNewPrimaryFieldValue(mixedOpts).identityValue();
        assertEquals("mysqlreader_3", result);
    }

    /**
     * 测试不同插件名称的 createNewPrimaryFieldValue 方法
     */
    @Test
    public void testCreateNewPrimaryFieldValueWithDifferentNames() {
        // 测试复合名称的插件
        PluginExtraProps.CandidatePlugin esCandidate = new PluginExtraProps.CandidatePlugin("ElasticSearchWriter",
                Optional.empty(), "datax-writer");

        List<Option> esOpts = Arrays.asList(new Option("ES写入器1", "elasticsearchwriter-1"), new Option("ES写入器2",
                "elasticsearchwriter-2"));
        String result = esCandidate.createNewPrimaryFieldValue(esOpts).identityValue();
        assertEquals("elasticsearchwriter_3", result);

        // 测试单个字符的插件名
        PluginExtraProps.CandidatePlugin singleCharCandidate = new PluginExtraProps.CandidatePlugin("A",
                Optional.empty(), "test");

        List<Option> singleCharOpts = Arrays.asList(new Option("单字符插件", "a-5"));
        result = singleCharCandidate.createNewPrimaryFieldValue(singleCharOpts).identityValue();
        assertEquals("a_6", result);

        // 测试包含数字的插件名
        PluginExtraProps.CandidatePlugin numericCandidate = new PluginExtraProps.CandidatePlugin("Plugin2Test",
                Optional.empty(), "test");

        List<Option> numericOpts = Arrays.asList(new Option("数字插件1", "plugin2test-1"), new Option("数字插件3",
                "plugin2test-3"));
        result = numericCandidate.createNewPrimaryFieldValue(numericOpts).identityValue();
        assertEquals("plugin2test_4", result);
    }

    /**
     * 测试边界情况
     */
    @Test
    public void testCreateNewPrimaryFieldValueEdgeCases() {
        PluginExtraProps.CandidatePlugin candidate = new PluginExtraProps.CandidatePlugin("TestPlugin",
                Optional.empty(), "test");

        // 测试大数字后缀
        List<Option> largeNumberOpts = Arrays.asList(new Option("大数字插件", "testplugin-999"));
        String result = candidate.createNewPrimaryFieldValue(largeNumberOpts).identityValue();
        assertEquals("testplugin_1000", result);

        // 测试零后缀
        List<Option> zeroOpts = Arrays.asList(new Option("零后缀插件", "testplugin-0"));
        result = candidate.createNewPrimaryFieldValue(zeroOpts).identityValue();
        assertEquals("testplugin_1", result);

        // 测试负数后缀（应该被忽略）
        List<Option> negativeOpts = Arrays.asList(new Option("负数后缀插件", "testplugin--1"), new Option("正常插件",
                "testplugin-1"));
        result = candidate.createNewPrimaryFieldValue(negativeOpts).identityValue();
        assertEquals("testplugin_2", result);
    }
}
