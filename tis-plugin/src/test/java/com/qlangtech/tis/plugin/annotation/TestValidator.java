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
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestValidator {

    static final String field1Name = "testField";

    static final String field2Name = "test2Field";

    @Test
    public void testRelativePath() {
        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));
        Context context = new DefaultContext();
        final String fieldPath = "path";
        Mockito.doNothing().when(msgHandler).addFieldError(context, fieldPath, ValidatorCommons.MSG_RELATIVE_PATH_ERROR);
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "user/home"
                + "/admin"));
        assertFalse("shall not be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "/user"
                + "/home/admin"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx"
                + "/instancedetail*"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx"
                + "/instancedetail*.text"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx"
                + "/instancedetail*.csv"));
        assertTrue("shall be valid", Validator.relative_path.validate(msgHandler, context, fieldPath, "xxx/*.json"));

        Mockito.verify(msgHandler, Mockito.times(1)).addFieldError(context, fieldPath, ValidatorCommons.MSG_RELATIVE_PATH_ERROR);
    }

    @Test
    public void testAbsolutePath() {

        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));
        Context context = new DefaultContext();
        final String fieldPath = "path";
        Mockito.doNothing().when(msgHandler).addFieldError(context, fieldPath, ValidatorCommons.MSG_ABSOLUTE_PATH_ERROR);
        assertTrue("shall be valid", Validator.absolute_path
                .validate(msgHandler, context, fieldPath, "/opt/misc_/apache-dolphinscheduler-3.2.2-bin_"));

        assertTrue("shall be valid", Validator.absolute_path
                .validate(msgHandler, context, fieldPath, "/opt/misc_/apache-dolphinscheduler-3.2.2-bin_*"));

        assertFalse("shall be invalid", Validator.absolute_path
                .validate(msgHandler, context, fieldPath, "opt/misc_/apache-dolphinscheduler-3.2.2-bin_*"));

        assertFalse("shall be invalid", Validator.absolute_path
                .validate(msgHandler, context, fieldPath, "/opt/misc&/apache-dolphinscheduler-3.2.2-bin_*"));
        Mockito.verify(msgHandler, Mockito.times(2)).addFieldError(context, fieldPath, ValidatorCommons.MSG_ABSOLUTE_PATH_ERROR);
    }

    /**
     * 非数字开头
     */
    @Test
    public void testForbidStartWithNumber() {
        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));
        Context context = new DefaultContext();
        final String processName = "processName";
        Mockito.doNothing().when(msgHandler).addFieldError(context, processName, ValidatorCommons.MSG_FORBID_START_WITH_NUMBER);
        assertTrue("shall be valid", Validator.forbid_start_with_number.validate(msgHandler, context, processName,
                "username123"));
        assertFalse("shall be invalid", Validator.forbid_start_with_number.validate(msgHandler, context, processName,
                "123dddd"));
        Mockito.verify(msgHandler).addFieldError(context, processName, ValidatorCommons.MSG_FORBID_START_WITH_NUMBER);
    }


    @Test
    public void testHost() {
        final String fieldHost = "host";
        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));
        Context context = new DefaultContext();
        Mockito.doNothing().when(msgHandler).addFieldError(context, fieldHost, ValidatorCommons.MSG_HOST_IP_ERROR);
        Assert.assertFalse("must be faild", Validator.host.validate(msgHandler, context, fieldHost, "http://192.168"
                + ".64.3:31135"));
        Assert.assertTrue("must be valid", Validator.host.validate(msgHandler, context, fieldHost, "192.168.64"
                + ".3:31135"));
        Assert.assertTrue("must be valid", Validator.host.validate(msgHandler, context, fieldHost, "baidu.com:31135"));
        Assert.assertFalse("must be faild", Validator.host.validate(msgHandler, context, fieldHost, "baidu.com"));

        Mockito.verify(msgHandler, Mockito.times(2)).addFieldError(context, fieldHost, ValidatorCommons.MSG_HOST_IP_ERROR);
    }


    /**
     * 一个校验项，的值需要依赖另外一个校验项运行
     */
    @Test
    public void testValidateIdentityWithDependencyRule() {

        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));

        String field1NameVal = "123";

        Mockito.when(msgHandler.getString(field1Name)).thenReturn(field1NameVal);
        Mockito.when(msgHandler.getString(field2Name)).thenReturn("field2NameValue");

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
                        , (IFieldValidator) ((msgHdr, ctx, fieldKey, fieldData) -> {

                            assertEquals(field1NameVal, field1Val[0]);

                            return true;
                        })//
                );

        Mockito.doNothing().when(msgHandler).errorsPageShow(context);

        assertTrue(Validator.validate(msgHandler, context, validatorsRules));
        Mockito.verify(msgHandler).getString(field1Name);
        Mockito.verify(msgHandler).getString(field2Name);
        Mockito.verify(msgHandler, Mockito.times(1)).errorsPageShow(context);
    }

    @Test
    public void testNone_blank() {
        Validator noneBlankValidator = Validator.none_blank;
        DefaultFieldErrorHandler fEHandler = new DefaultFieldErrorHandler();
        DefaultContext context = new DefaultContext();
        assertTrue("error shall none error", noneBlankValidator.validate(fEHandler, context, field1Name, ""));
        Assert.assertFalse(context.hasErrors());

        assertFalse("error shall error", noneBlankValidator.validate(fEHandler, context, field1Name, "        "));
        Assert.assertTrue(context.hasErrors());

         context = new DefaultContext();

        assertTrue("error shall none error", noneBlankValidator.validate(fEHandler, context, field1Name, "   aa a  a  "));
        Assert.assertFalse(context.hasErrors());

        assertTrue("error shall none error", noneBlankValidator.validate(fEHandler, context, field1Name, "a   b"));
        Assert.assertFalse(context.hasErrors());
    }

    @Test
    public void testValidateIdentity() {
        Validator identityValidator = Validator.identity;
        DefaultFieldErrorHandler fEHandler = new DefaultFieldErrorHandler();
        DefaultContext context = new DefaultContext();
        context.put(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX, 2);
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

    @Test
    public void testCreateValidateField() {
        try {
            Validator.fieldsValidator(field1Name, field2Name);
            fail("must throw  an param rules can not be empty excpetion");
        } catch (Exception e) {
        }
        try {
            Validator.fieldsValidator(new Validator.FieldValidators(Validator.require) {
            }, (IFieldValidator) ((msgHandler, context, fieldKey, fieldData) -> {
                return true;
            }));
            fail("rule must start with type of string ");
        } catch (Exception e) {
        }
    }

    @Test
    public void testValidateFieldByMultiRules() {
        final String numbericValidateFaild = "必须是数字";
        //
        Map<String, Validator.FieldValidators> validatorsRules = //
                Validator.fieldsValidator(//
                        field1Name //
                        , new Validator.FieldValidators(Validator.require) {
                        } //
                        , (IFieldValidator) ((msgHandler, context, fieldKey, fieldData) -> {
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
        IControlMsgHandler msgHandler = Mockito.mock(IControlMsgHandler.class, Mockito.withSettings().name("msgHandler"));
        Mockito.when(msgHandler.getString(field1Name)).thenReturn("123").thenReturn("123aaa");
        Mockito.doNothing().when(msgHandler).errorsPageShow(context);
        Mockito.doNothing().when(msgHandler).addFieldError(context, field1Name, numbericValidateFaild);
        Mockito.doNothing().when(msgHandler).addErrorMessage(context, Validator.FORM_ERROR_SUMMARY);
        // Mockito.when(msgHandler.getString(field1Name)).thenReturn("123aaa");
        assertTrue(Validator.validate(msgHandler, context, validatorsRules));
        // 第二次应该校验失败了
        assertFalse(Validator.validate(msgHandler, context, validatorsRules));
        Mockito.verify(msgHandler, Mockito.times(2)).getString(field1Name);
        Mockito.verify(msgHandler, Mockito.times(2)).errorsPageShow(context);
        Mockito.verify(msgHandler).addFieldError(context, field1Name, numbericValidateFaild);
        Mockito.verify(msgHandler).addErrorMessage(context, Validator.FORM_ERROR_SUMMARY);
    }
}
