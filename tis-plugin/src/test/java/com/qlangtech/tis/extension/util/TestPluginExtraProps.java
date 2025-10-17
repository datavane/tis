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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.util.AbstractPropAssist.MarkdownHelperContent;
import com.qlangtech.tis.extension.util.PluginExtraProps.RouterAssistType;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSON;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_CREATOR_ASSIST_TYPE;

/**
 *
 */
public class TestPluginExtraProps extends TestCase {

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
        public ParsePostMCols<CMeta> parsePostMCols(IPropertyType propertyType, IControlMsgHandler msgHandler, Context context, String keyColsMeta, JSONArray targetCols) {
            throw new UnsupportedOperationException();
        }
//        @Override
//        public CMeta create(JSONObject targetCol) {
//            return new CMeta();
//        }
    }


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
//            assertEquals("propKey:dbName,package:com.qlangtech.tis.extension.util,propKey:WithCreatorError.json", e.getMessage());
//        }
//    }

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

    public void testAddFieldDescriptor() {

        DefaultPlugin plugin = new DefaultPlugin();
        Optional<ElementPluginDesc> pluginDesc = ElementPluginDesc.create(plugin.getDescriptor());
        Optional<PluginExtraProps> extraProps
                = PluginExtraProps.load(pluginDesc, DefaultPlugin.class);

        Assert.assertTrue(extraProps.isPresent());

        PluginExtraProps ep = extraProps.get();
        PluginExtraProps.Props nameProp = ep.getProp("name");
        Assert.assertNotNull(nameProp);

        Assert.assertTrue("isAsynHelp must be true", nameProp.isAsynHelp());
        Assert.assertEquals(DefaultPlugin.FILED_NAME_DESCRIPTION, nameProp.getAsynHelp());
        Assert.assertEquals(DefaultPlugin.DFT_NAME_VALUE, nameProp.getDftVal());

        DescriptorsJSON descJSON = new DescriptorsJSON(pluginDesc.get().getElementDesc());
        System.out.println(JsonUtil.toString(descJSON.getDescriptorsJSON()));

    }

    public void testAddFieldDescriptorWithNotMatchFieldName() {
        try {
            DefaultPlugin plugin = new DefaultPlugin();
            DefaultPlugin.DefaultDescriptor desc = (DefaultPlugin.DefaultDescriptor) plugin.getDescriptor();
            desc.addFieldDescriptor("xxx", DefaultPlugin.DFT_NAME_VALUE, new MarkdownHelperContent(DefaultPlugin.FILED_NAME_DESCRIPTION));
            PluginExtraProps.load(ElementPluginDesc.create(desc), DefaultPlugin.class);
            Assert.fail("must be faild");
        } catch (Exception e) {
            Assert.assertEquals("prop key:xxx relevant prop must exist , exist props keys:password,nestProp,name,cols", e.getMessage());
        }
    }

    /**
     * 测试 CandidatePlugin.createNewPrimaryFieldValue 方法
     */
    public void testCreateNewPrimaryFieldValue() {
        // 测试用例1：空的选项列表
        PluginExtraProps.CandidatePlugin candidate = new PluginExtraProps.CandidatePlugin(
            "MySQLReader", Optional.empty(), "datax-reader"
        );
        
        List<Option> emptyOpts = Collections.emptyList();
        String result = candidate.createNewPrimaryFieldValue(emptyOpts);
        assertEquals("mysqlreader-1", result);
        
        // 测试用例2：存在部分匹配的选项
        List<Option> existingOpts = Arrays.asList(
            new Option("第一个MySQL读取器", "mysqlreader-1"),
            new Option("第三个MySQL读取器", "mysqlreader-3"),
            new Option("其他插件", "other-plugin")
        );
        result = candidate.createNewPrimaryFieldValue(existingOpts);
        assertEquals("mysqlreader-4", result);
        
        // 测试用例3：存在连续的选项
        List<Option> continuousOpts = Arrays.asList(
            new Option("第一个MySQL读取器", "mysqlreader1"),
            new Option("第二个MySQL读取器", "mysqlreader2"),
            new Option("第三个MySQL读取器", "mysqlreader3")
        );
        result = candidate.createNewPrimaryFieldValue(continuousOpts);
        assertEquals("mysqlreader-4", result);
        
        // 测试用例4：不规则的命名模式
        List<Option> irregularOpts = Arrays.asList(
            new Option("大写的MySQL读取器", "MYSQLREADER-10"),
            new Option("小写的MySQL读取器", "mysqlreader5"),
            new Option("不相关的插件", "unrelated")
        );
        result = candidate.createNewPrimaryFieldValue(irregularOpts);
        assertEquals("mysqlreader-11", result);
        
        // 测试用例5：包含非数字后缀的选项
        List<Option> mixedOpts = Arrays.asList(
            new Option("非数字后缀", "mysqlreader-abc"),
            new Option("数字后缀", "mysqlreader-2"),
            new Option("空后缀", "mysqlreader-")
        );
        result = candidate.createNewPrimaryFieldValue(mixedOpts);
        assertEquals("mysqlreader-3", result);
    }

    /**
     * 测试不同插件名称的 createNewPrimaryFieldValue 方法
     */
    public void testCreateNewPrimaryFieldValueWithDifferentNames() {
        // 测试复合名称的插件
        PluginExtraProps.CandidatePlugin esCandidate = new PluginExtraProps.CandidatePlugin(
            "ElasticSearchWriter", Optional.empty(), "datax-writer"
        );
        
        List<Option> esOpts = Arrays.asList(
            new Option("ES写入器1", "elasticsearchwriter-1"),
            new Option("ES写入器2", "elasticsearchwriter-2")
        );
        String result = esCandidate.createNewPrimaryFieldValue(esOpts);
        assertEquals("elasticsearchwriter-3", result);
        
        // 测试单个字符的插件名
        PluginExtraProps.CandidatePlugin singleCharCandidate = new PluginExtraProps.CandidatePlugin(
            "A", Optional.empty(), "test"
        );
        
        List<Option> singleCharOpts = Arrays.asList(
            new Option("单字符插件", "a-5")
        );
        result = singleCharCandidate.createNewPrimaryFieldValue(singleCharOpts);
        assertEquals("a-6", result);
        
        // 测试包含数字的插件名
        PluginExtraProps.CandidatePlugin numericCandidate = new PluginExtraProps.CandidatePlugin(
            "Plugin2Test", Optional.empty(), "test"
        );
        
        List<Option> numericOpts = Arrays.asList(
            new Option("数字插件1", "plugin2test-1"),
            new Option("数字插件3", "plugin2test-3")
        );
        result = numericCandidate.createNewPrimaryFieldValue(numericOpts);
        assertEquals("plugin2test-4", result);
    }

    /**
     * 测试边界情况
     */
    public void testCreateNewPrimaryFieldValueEdgeCases() {
        PluginExtraProps.CandidatePlugin candidate = new PluginExtraProps.CandidatePlugin(
            "TestPlugin", Optional.empty(), "test"
        );
        
        // 测试大数字后缀
        List<Option> largeNumberOpts = Arrays.asList(
            new Option("大数字插件", "testplugin-999")
        );
        String result = candidate.createNewPrimaryFieldValue(largeNumberOpts);
        assertEquals("testplugin-1000", result);
        
        // 测试零后缀
        List<Option> zeroOpts = Arrays.asList(
            new Option("零后缀插件", "testplugin-0")
        );
        result = candidate.createNewPrimaryFieldValue(zeroOpts);
        assertEquals("testplugin-1", result);
        
        // 测试负数后缀（应该被忽略）
        List<Option> negativeOpts = Arrays.asList(
            new Option("负数后缀插件", "testplugin--1"),
            new Option("正常插件", "testplugin-1")
        );
        result = candidate.createNewPrimaryFieldValue(negativeOpts);
        assertEquals("testplugin-2", result);
    }
}
