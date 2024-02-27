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
package com.qlangtech.tis.plugin.annotation;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.ItemsErrors;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;

import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestValidator extends TestCase {

    static final String field1Name = "testField";

    static final String field2Name = "test2Field";

    public void testRelativePath() {
        IControlMsgHandler msgHandler = EasyMock.createMock("msgHandler", IControlMsgHandler.class);
        Context context = new DefaultContext();
        final String fieldPath = "path";
        msgHandler.addFieldError(context, fieldPath, ValidatorCommons.MSG_RELATIVE_PATH_ERROR);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(msgHandler);
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "user/home/admin"));
        assertFalse("shall not be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "/user/home/admin"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx/instancedetail*"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx/instancedetail*.text"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx/instancedetail*.csv"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx/*.json"));

        EasyMock.verify(msgHandler);
    }


    public void testHost() {
        final String fieldHost = "host";
        IControlMsgHandler msgHandler = EasyMock.createMock("msgHandler", IControlMsgHandler.class);
        Context context = new DefaultContext();
        msgHandler.addFieldError(context, fieldHost, ValidatorCommons.MSG_HOST_IP_ERROR);
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(msgHandler);
        Assert.assertFalse("must be faild", Validator.host.validate(msgHandler, context, fieldHost, "http://192.168.64.3:31135"));
        Assert.assertTrue("must be valid", Validator.host.validate(msgHandler, context, fieldHost, "192.168.64.3:31135"));
        Assert.assertTrue("must be valid", Validator.host.validate(msgHandler, context, fieldHost, "baidu.com:31135"));
        Assert.assertFalse("must be faild", Validator.host.validate(msgHandler, context, fieldHost, "baidu.com"));

        EasyMock.verify(msgHandler);
    }


    /**
     * 一个校验项，的值需要依赖另外一个校验项运行
     */
    public void testValidateIdentityWithDependencyRule() {

        IControlMsgHandler msgHandler = EasyMock.createMock("msgHandler", IControlMsgHandler.class);

        String field1NameVal = "123";

        EasyMock.expect(msgHandler.getString(field1Name)).andReturn(field1NameVal);
        EasyMock.expect(msgHandler.getString(field2Name)).andReturn("field2NameValue");

        Context context = new DefaultContext();

        final String[] field1Val = new String[1];
        // field2Name 字段依赖 fieldName 的值
        Map<String, Validator.FieldValidators> validatorsRules = //
                Validator.fieldsValidator(//
                        field1Name //
                        , new Validator.FieldValidators(Validator.require) {
                            @Override
                            public void setFieldVal(String val) {
                                field1Val[0] = val;
                            }
                        } //
                        , field2Name
                        , new Validator.FieldValidators(Validator.require) {
                        }.addDependency(field1Name)
                        , (Validator.IFieldValidator) ((msgHdr, ctx, fieldKey, fieldData) -> {

                            assertEquals(field1NameVal, field1Val[0]);

                            return true;
                        })//
                );

        msgHandler.errorsPageShow(context);
        EasyMock.expectLastCall().andVoid().times(1);

        EasyMock.replay(msgHandler);
        assertTrue(Validator.validate(msgHandler, context, validatorsRules));
        EasyMock.verify(msgHandler);
    }


    public void testValidateIdentity() {
        Validator identityValidator = Validator.identity;
        DefaultFieldErrorHandler fEHandler = new DefaultFieldErrorHandler();
        DefaultContext context = new DefaultContext();
        context.put(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX, new Integer(2));
        assertTrue("error shall none error", identityValidator.validate(fEHandler, context, field1Name, "base123"));
        assertFalse(identityValidator.validate(fEHandler, context, field1Name, "_bas&e123"));
        List<List<ItemsErrors>> pluginErrorList
                = (List<List<ItemsErrors>>) context.get(IFieldErrorHandler.ACTION_ERROR_FIELDS);
        assertEquals(3, pluginErrorList.size());
        ItemsErrors itemError = pluginErrorList.get(2).get(0);
        assertNotNull("fError can not be null", itemError);

        //System.out.println();

        assertEquals("[{\"name\":\"testField\",\"content\":\"必须由小写字母，大写字母，数字、下划线、减号组成\"}]"
                , JsonUtil.toString(itemError.serial2JSON(), false));

//        assertEquals(field1Name, fError.getFieldName());
//        assertEquals(ValidatorCommons.MSG_IDENTITY_ERROR, fError.getMsg());
//        assertNull(fError.itemsErrorList);
    }

    public void testCreateValidateField() {
        try {
            Validator.fieldsValidator(field1Name, field2Name);
            fail("must throw  an param rules can not be empty excpetion");
        } catch (Exception e) {
        }
        try {
            Validator.fieldsValidator(new Validator.FieldValidators(Validator.require) {
            }, (Validator.IFieldValidator) ((msgHandler, context, fieldKey, fieldData) -> {
                return true;
            }));
            fail("rule must start with type of string ");
        } catch (Exception e) {
        }
    }

    public void testValidateFieldByMultiRules() {
        final String numbericValidateFaild = "必须是数字";
        //
        Map<String, Validator.FieldValidators> validatorsRules = //
                Validator.fieldsValidator(//
                        field1Name //
                        , new Validator.FieldValidators(Validator.require) {
                        } //
                        , (Validator.IFieldValidator) ((msgHandler, context, fieldKey, fieldData) -> {
                            if (!StringUtils.isNumeric(fieldData)) {
                                msgHandler.addFieldError(context, field1Name, numbericValidateFaild);
                                // 校验是否是数字
                                return false;
                            }
                            return true;
                        }));
        assertEquals("validatorsRules size", 1, validatorsRules.size());
        Validator.FieldValidators fieldValidators = validatorsRules.get(field1Name);
        assertNotNull(fieldValidators);
        assertEquals(2, fieldValidators.validators.size());
        Context context = new DefaultContext();
        IControlMsgHandler msgHandler = EasyMock.createMock("msgHandler", IControlMsgHandler.class);
        EasyMock.expect(msgHandler.getString(field1Name)).andReturn("123");
        msgHandler.errorsPageShow(context);
        EasyMock.expectLastCall().andVoid().times(2);
        EasyMock.expect(msgHandler.getString(field1Name)).andReturn("123aaa");
        msgHandler.addFieldError(context, field1Name, numbericValidateFaild);
        msgHandler.addErrorMessage(context, Validator.FORM_ERROR_SUMMARY);
        // EasyMock.expect().andReturn("123aaa");
        EasyMock.replay(msgHandler);
        assertTrue(Validator.validate(msgHandler, context, validatorsRules));
        // 第二次应该校验失败了
        assertFalse(Validator.validate(msgHandler, context, validatorsRules));
        EasyMock.verify(msgHandler);
    }
}
