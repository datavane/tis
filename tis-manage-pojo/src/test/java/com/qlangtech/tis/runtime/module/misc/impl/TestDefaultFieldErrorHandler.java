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

package com.qlangtech.tis.runtime.module.misc.impl;

import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;




/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/9/5
 */
public class TestDefaultFieldErrorHandler extends TestCase {


    public void testAddFieldErrorMsg() {

        DefaultFieldErrorHandler fieldErrorHandler = new DefaultFieldErrorHandler();

        DefaultContext context = new DefaultContext();
        String fieldName = "cols[3][0].name";
        String msg = "格式不复合规范";

        fieldErrorHandler.addFieldError(context, fieldName, msg);

        fieldName = "cols[3][0].jsonpath";
        msg = "不能为空";
        fieldErrorHandler.addFieldError(context, fieldName, msg);
        // context.getContextMap()

        fieldErrorHandler.addFieldError(context, "name", msg);

        JsonUtil.assertJSONEqual(TestDefaultFieldErrorHandler.class, "context_error_content.json",
                JsonUtil.toString(context.getContextMap()), (message, expected, actual) -> {
            Assert.assertEquals(message, expected, actual);
        });


    }

    public void testGetJsonPrimaryKey1() {
        final String pkName = "testField";
        String complexProp = "testField.xxxx[3].kkk[3].age";

        Assert.assertEquals(pkName, DefaultFieldErrorHandler.getKeyFieldName(complexProp));


    }


    public void testGetJsonPrimaryKey2() {
        final String pkName2 = "testField2";
        String complexProp = "testField2[1].xxxx[3].kkk[3].age";

        Assert.assertEquals(pkName2, DefaultFieldErrorHandler.getKeyFieldName(complexProp));

        //        String[] pk2 = new String[1];
        //        AtomicBoolean hasSetPrimaryKeyName2 = new AtomicBoolean(false);
        //        DefaultFieldErrorHandler.setVal(null, complexProp, "testVal", (primaryKeyName) -> {
        //            Assert.assertEquals(pkName2, primaryKeyName);
        //            pk2[0] = primaryKeyName;
        //            hasSetPrimaryKeyName2.set(true);
        //        });
        //        Assert.assertTrue("hasSetPrimaryKeyName", hasSetPrimaryKeyName2.get());
        //        Assert.assertEquals(pkName2, pk2[0]);
    }

    public void testGetJsonPrimaryKey3() {
        final String pkName = "testField";
        String complexProp = "testField";

        Assert.assertEquals(pkName, DefaultFieldErrorHandler.getKeyFieldName(complexProp));

        //        final String[] pk = new String[1];
        //        final AtomicBoolean hasSetPrimaryKeyName = new AtomicBoolean(false);
        //        DefaultFieldErrorHandler.setVal(null, complexProp, "testVal", (primaryKeyName) -> {
        //            Assert.assertEquals(pkName, primaryKeyName);
        //            pk[0] = primaryKeyName;
        //            hasSetPrimaryKeyName.set(true);
        //        });
        //        Assert.assertTrue("hasSetPrimaryKeyName", hasSetPrimaryKeyName.get());
        //        Assert.assertEquals(pkName, pk[0]);


    }


    public void testCreateJsonByPath() {
        String complexProp = "testField.xxxx[1].name";
        DefaultFieldErrorHandler.IFieldMsg msgContent = null;
        JSONObject json = new JSONObject();
        msgContent = DefaultFieldErrorHandler.setVal(json, complexProp, "testVal");
        // System.out.println("final ============================");
        // System.out.println(JsonUtil.toString(msgContent.getContent()));

        complexProp = "testField.xxxx[2].kkk[3].name";
        msgContent = DefaultFieldErrorHandler.setVal(json, complexProp, "testVal222");

        complexProp = "testField.cccc[2][0].name";
        DefaultFieldErrorHandler.setVal(json, complexProp, "cccName");

        complexProp = "testField.cccc[2][0].age";
        DefaultFieldErrorHandler.setVal(json, complexProp, "88");

        complexProp = "testField.xxxx[2].kkk[3].age";
        msgContent = DefaultFieldErrorHandler.setVal(json, complexProp, "14");
        // System.out.println("final ============================");
        //System.out.println(JsonUtil.toString(msgContent.getContent()));

        complexProp = "testField.xxxx[3].kkk[3].age";
        msgContent = DefaultFieldErrorHandler.setVal(json, complexProp, "12");
        // System.out.println("final ============================");
        // System.out.println(JsonUtil.toString(json));

        JsonUtil.assertJSONEqual(TestDefaultFieldErrorHandler.class, "createJsonByPath.json", json //
                , (message, expected, actual) -> {
                    Assert.assertEquals(message, expected, actual);
                });


        complexProp = "testField";
        msgContent = DefaultFieldErrorHandler.setVal(json, complexProp, "testVal222");
        // 没有匹配到内容
        Assert.assertNotNull(msgContent.getContent());
        Assert.assertTrue(msgContent.getContent() instanceof String);
    }


}
