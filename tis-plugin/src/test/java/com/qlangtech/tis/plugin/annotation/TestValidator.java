/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.plugin.annotation;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
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

    static final String fieldName = "testField";

    static final String field2Name = "test2Field";

    public void testValidateIdentity() {
        Validator identityValidator = Validator.identity;
        DefaultFieldErrorHandler fEHandler = new DefaultFieldErrorHandler();
        DefaultContext context = new DefaultContext();
        context.put(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX, new Integer(2));
        assertTrue("error shall none error", identityValidator.validate(fEHandler, context, fieldName, "base123"));
        assertFalse(identityValidator.validate(fEHandler, context, fieldName, "_base123"));
        List<List<List<DefaultFieldErrorHandler.FieldError>>> pluginErrorList = (List<List<List<DefaultFieldErrorHandler.FieldError>>>) context.get(IFieldErrorHandler.ACTION_ERROR_FIELDS);
        assertEquals(3, pluginErrorList.size());
        DefaultFieldErrorHandler.FieldError fError = pluginErrorList.get(2).get(0).get(0);
        assertEquals(fieldName, fError.getFieldName());
        assertEquals(ValidatorCommons.MSG_IDENTITY_ERROR, fError.getMsg());
        assertNull(fError.itemsErrorList);
    }

    public void testCreateValidateField() {
        try {
            Validator.fieldsValidator(fieldName, field2Name);
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
        fieldName, new Validator.FieldValidators(Validator.require) {
        }, (Validator.IFieldValidator) ((msgHandler, context, fieldKey, fieldData) -> {
            if (!StringUtils.isNumeric(fieldData)) {
                msgHandler.addFieldError(context, fieldName, numbericValidateFaild);
                // 校验是否是数字
                return false;
            }
            return true;
        }));
        assertEquals("validatorsRules size", 1, validatorsRules.size());
        Validator.FieldValidators fieldValidators = validatorsRules.get(fieldName);
        assertNotNull(fieldValidators);
        assertEquals(2, fieldValidators.validators.size());
        Context context = new DefaultContext();
        IControlMsgHandler msgHandler = EasyMock.createMock("msgHandler", IControlMsgHandler.class);
        EasyMock.expect(msgHandler.getString(fieldName)).andReturn("123");
        EasyMock.expect(msgHandler.getString(fieldName)).andReturn("123aaa");
        msgHandler.addFieldError(context, fieldName, numbericValidateFaild);
        msgHandler.addErrorMessage(context, Validator.FORM_ERROR_SUMMARY);
        // EasyMock.expect().andReturn("123aaa");
        EasyMock.replay(msgHandler);
        assertTrue(Validator.validate(msgHandler, context, validatorsRules));
        // 第二次应该校验失败了
        assertFalse(Validator.validate(msgHandler, context, validatorsRules));
        EasyMock.verify(msgHandler);
    }
}
