/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.plugin.utils;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link HexColorValidator} 的格式判定与错误上报。
 *
 * <p>本规则由 {@code FormFieldType.Color} 的内建 {@code InputValueValidator} 统一调用，
 * 即所有声明为 {@code type = FormFieldType.Color} 的字段共用一份口径；放宽/收紧都会
 * 影响全部颜色输入项，因此边界值在这里钉死。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/22
 */
public class HexColorValidatorTest {

    @Test
    public void testAcceptCssHexColors() {
        String[] valid = {
                "#fff", "#FFF",       // 三位
                "#abcd", "#ABCD",     // 四位（带 alpha）
                "#f5222d", "#AABBCC", // 六位
                "#f5222dcc", "#00FF00FF" // 八位（带 alpha）
        };
        for (String value : valid) {
            Assert.assertTrue("应接受合法色值: " + value, HexColorValidator.isHexColor(value));
        }
    }

    @Test
    public void testRejectInvalidColors() {
        String[] invalid = {
                null, "",
                "#", "#ff", "#fffff", "#fffffff", // 位数不对
                "fff", "f5222d",                   // 缺 #
                "#ggg", "#12345g",                 // 非十六进制字符
                "red", "rgb(255,0,0)",             // 非十六进制写法
                " #fff", "#fff ",                  // 带空白不 trim
                "##fff"
        };
        for (String value : invalid) {
            Assert.assertFalse("应拒绝非法色值: " + value, HexColorValidator.isHexColor(value));
        }
    }

    @Test
    public void testValidateReportsErrorForInvalidValue() {
        RecordingHandler handler = new RecordingHandler();

        Assert.assertFalse(HexColorValidator.validate(handler, null, "backgroundColor", "red"));
        Assert.assertEquals("非法值必须回报一次字段错误", 1, handler.count);
        Assert.assertEquals("backgroundColor", handler.fieldName);
        Assert.assertTrue("错误文案须点明十六进制要求，实际: " + handler.message,
                handler.message.contains("十六进制"));
    }

    @Test
    public void testValidatePassesSilentlyForValidValue() {
        RecordingHandler handler = new RecordingHandler();

        Assert.assertTrue(HexColorValidator.validate(handler, null, "backgroundColor", "#f5222d"));
        Assert.assertEquals("合法值不应产生任何字段错误", 0, handler.count);
    }

    /** 记录 addFieldError 的调用；接口其余方法是桩，本用例不会走到 */
    private static class RecordingHandler implements IFieldErrorHandler {
        int count;
        String fieldName;
        String message;

        @Override
        public void addFieldError(Context context, String fieldName, String msg, Object... params) {
            this.count++;
            this.fieldName = fieldName;
            this.message = msg;
        }

        @Override
        public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BasicPipelineValidator getPipelineValidator(BizLogic logicType) {
            throw new UnsupportedOperationException();
        }
    }
}
