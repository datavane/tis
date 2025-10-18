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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.plugin.TestPluginImpl;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Optional;

import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-22 10:27
 **/
public class TestAttrValMap extends TestCase {
    public void testCreateDescribable() {

        IControlMsgHandler fieldErrorHandler = EasyMock.createMock("fieldErrorHandler", IControlMsgHandler.class);
        IControlMsgHandler pluginContext = EasyMock.createMock("pluginContext", IControlMsgHandler.class);
        Context context = EasyMock.createMock("context", Context.class);

        JSONObject jsonObject = IOUtils.loadResourceFromClasspath(TestPluginImpl.class
                , "testPluginImpl-post-content.json", true, (input) -> {
                    return JSON.parseObject(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
                });

        EasyMock.replay(fieldErrorHandler, pluginContext, context);
        AttrValMap attrValMap = AttrValMap.parseDescribableMap(Optional.empty(), jsonObject);

        Descriptor.ParseDescribable describable = attrValMap.createDescribable(pluginContext, context);
        assertNotNull(describable);
        TestPluginImpl testPlugin = (TestPluginImpl) describable.getInstance();
        assertNotNull(testPlugin);
        // 没有设置值，所以值对象应该为空，不能为0
        assertTrue("testPlugin.connectionsPerHost must be null", testPlugin.connectionsPerHost == null);
        assertEquals(12, (int) testPlugin.maxPendingPerConnection);

        EasyMock.verify(fieldErrorHandler, pluginContext, context);
    }

    public void testGetPostJsonBody() {
        JSONObject jsonObject = IOUtils.loadResourceFromClasspath(TestPluginImpl.class
                , "testPluginImpl-post-content.json", true, (input) -> {
                    return JSON.parseObject(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
                });

        AttrValMap attrValMap = AttrValMap.parseDescribableMap(Optional.empty(), jsonObject);
        assertNotNull("attrValMap should not be null", attrValMap);

        JSONObject postJsonBody = attrValMap.getPostJsonBody();
        assertNotNull("postJsonBody should not be null", postJsonBody);
        
        assertTrue("postJsonBody should contain impl", postJsonBody.containsKey(PLUGIN_EXTENSION_IMPL));
        assertEquals("com.qlangtech.tis.util.plugin.TestPluginImpl", postJsonBody.getString(PLUGIN_EXTENSION_IMPL));
        
        assertTrue("postJsonBody should contain vals", postJsonBody.containsKey(AttrValMap.PLUGIN_EXTENSION_VALS));
        JSONObject vals = postJsonBody.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);
        assertNotNull("vals should not be null", vals);
        
        assertTrue("vals should contain connectionsPerHost", vals.containsKey("connectionsPerHost"));
        Object connectionsPerHost = vals.get("connectionsPerHost");
        assertEquals("connectionsPerHost should be empty string", "", connectionsPerHost);
        
        assertTrue("vals should contain maxPendingPerConnection", vals.containsKey("maxPendingPerConnection"));
        Object maxPendingPerConnection = vals.get("maxPendingPerConnection");
        assertEquals("maxPendingPerConnection should be 12", 12, maxPendingPerConnection);
    }

    public void testGetPostJsonBodyWithNullValues() {
        JSONObject testData = new JSONObject();
        testData.put("impl", "com.qlangtech.tis.util.plugin.TestPluginImpl");
        
        JSONObject vals = new JSONObject();
        
        JSONObject nullField = new JSONObject();
        nullField.put("_primaryVal", null);
        vals.put("testField", nullField);
        
        testData.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);

        AttrValMap attrValMap = AttrValMap.parseDescribableMap(Optional.empty(), testData);
        assertNotNull("attrValMap should not be null", attrValMap);

        JSONObject postJsonBody = attrValMap.getPostJsonBody();
        assertNotNull("postJsonBody should not be null", postJsonBody);
        
        JSONObject resultVals = postJsonBody.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);
        assertNotNull("resultVals should not be null", resultVals);
        
        if (resultVals.containsKey("testField")) {
            Object testFieldValue = resultVals.get("testField");
            assertNull("testField should be null", testFieldValue);
        }
    }

    public void testGetPostJsonBodyWithDescribableField() {
        JSONObject testData = new JSONObject();
        testData.put(PLUGIN_EXTENSION_IMPL, TestPluginImpl.class.getName());
        
        JSONObject vals = new JSONObject();
        
        JSONObject describableField = new JSONObject();
        JSONObject descVal = new JSONObject();
        descVal.put(PLUGIN_EXTENSION_IMPL, TestPluginImpl.class.getName());
        
        JSONObject innerVals = new JSONObject();
        JSONObject innerField = new JSONObject();
        innerField.put(KEY_primaryVal, "innerValue");
        innerVals.put("innerField", innerField);
        
        descVal.put(AttrValMap.PLUGIN_EXTENSION_VALS, innerVals);
        describableField.put(KEY_DESC_VAL, descVal);
        
        vals.put("describableField", describableField);
        testData.put(AttrValMap.PLUGIN_EXTENSION_VALS, vals);

        AttrValMap attrValMap = AttrValMap.parseDescribableMap(Optional.empty(), testData);
        assertNotNull("attrValMap should not be null", attrValMap);

        JSONObject postJsonBody = attrValMap.getPostJsonBody();
        assertNotNull("postJsonBody should not be null", postJsonBody);
        
        JSONObject resultVals = postJsonBody.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);
        assertNotNull("resultVals should not be null", resultVals);
        
        if (resultVals.containsKey("describableField")) {
            Object describableValue = resultVals.get("describableField");
            assertNotNull("describableField should not be null", describableValue);
            assertTrue("describableField should be JSONObject", describableValue instanceof JSONObject);
            
            JSONObject describableObj = (JSONObject) describableValue;
            assertTrue("describableObj should contain impl", describableObj.containsKey(PLUGIN_EXTENSION_IMPL));
            assertTrue("describableObj should contain vals", describableObj.containsKey(AttrValMap.PLUGIN_EXTENSION_VALS));
            
            JSONObject innerResultVals = describableObj.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS);
            if (innerResultVals.containsKey("innerField")) {
                assertEquals("innerField should have correct value", "innerValue", innerResultVals.get("innerField"));
            }
        }
    }
}
