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

package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.RequiredPasswordPlugin;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.workshop.widget.WidgetColumnConfig;
import com.qlangtech.tis.plugin.workshop.widget.groovy.GroovyWorkshopWidget;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/19
 */
public class TestPropertyType extends TestCase {

    public void testGetValidator() {
        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(Optional.empty(), DefaultPlugin.class);

        PropertyType passwordPropery = (PropertyType) props.get("password");
        Assert.assertNotNull(passwordPropery);

        Validator[] validators = passwordPropery.getValidator();
        // contain 2 validator 'require' and 'none_blank'
        Assert.assertEquals(2, validators.length);
        boolean containRequire = false;
        for (Validator v : validators) {
            if (v == Validator.require) {
                containRequire = true;
            }
        }
        Assert.assertTrue("shall containRequire", containRequire);
    }

    public void testGetValidatorDisableRequireValidatorByJsonConfig() {
        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(Optional.empty(), RequiredPasswordPlugin.class);
        PropertyType passwordPropery = (PropertyType) props.get("password");
        Assert.assertNotNull(passwordPropery);
        Validator[] validators = passwordPropery.getValidator();
        // contain 2 validator 'require' and 'none_blank'
        Assert.assertEquals(1, validators.length);
        boolean containRequire = false;
        for (Validator v : validators) {
            if (v == Validator.require) {
                containRequire = true;
            }
        }
        Assert.assertFalse("shall not containRequire", containRequire);
    }

    /**
     * 属性为java枚举类型，且json资源中没有显式声明enum时，直接反射枚举常量生成选项
     */
    public void testReflectEnumConstantsAsOptions() {
        Descriptor<WidgetColumnConfig> desc = TIS.get().getDescriptor(WidgetColumnConfig.class);
        Assert.assertNotNull(desc);

        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(ElementPluginDesc.create(desc), WidgetColumnConfig.class);

        PropertyType align = (PropertyType) props.get("align");
        Assert.assertNotNull(align);
        List<Option> opts = align.getEnumPropOptions();
        Assert.assertEquals(3, opts.size());
        // val 取枚举常量名，保证能够反序列化回枚举实例
        Assert.assertEquals("left", opts.get(0).getValue());
        Assert.assertEquals("right", opts.get(2).getValue());
        // label 也取枚举常量名
        Assert.assertEquals("left", opts.get(0).getName());
        Assert.assertEquals("right", opts.get(2).getName());

        PropertyType format = (PropertyType) props.get("format");
        Assert.assertNotNull(format);
        List<Option> formatOpts = format.getEnumPropOptions();
        Assert.assertEquals(4, formatOpts.size());
        Assert.assertEquals("text", formatOpts.get(0).getValue());
        Assert.assertEquals("text", formatOpts.get(0).getName());

        // 枚举没有实现DescriptorUseableShortComment，选项不需要附带说明信息，就是普通的Option，既不显示图标也没有help说明
        JSONObject formatOpt = format.getExtraProps().getJSONArray(Descriptor.KEY_ENUM_PROP).getJSONObject(0);
        Assert.assertNull(formatOpt.getString(Option.KEY_END_TYPE));
        Assert.assertNull(formatOpt.getString(Option.KEY_HELP));
    }

    /**
     * 枚举实现了DescriptorUseableShortComment，选项使用OptionWithEndType承载shortComment，最终以help属性传递给前端展示
     */
    public void testEnumImplementsShortCommentAsOptionWithEndType() {
        Descriptor<GroovyWorkshopWidget> desc = TIS.get().getDescriptor(GroovyWorkshopWidget.class);
        Assert.assertNotNull(desc);

        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(ElementPluginDesc.create(desc), GroovyWorkshopWidget.class);

        PropertyType renderHint = (PropertyType) props.get("renderHint");
        Assert.assertNotNull(renderHint);

        // shortComment 需要传递给前端作为选项的说明信息，所以序列化到enum中的选项含help属性
        JSONObject first = renderHint.getExtraProps().getJSONArray(Descriptor.KEY_ENUM_PROP).getJSONObject(0);
        Assert.assertEquals("generic_form", first.getString(Option.KEY_VALUE));
        Assert.assertEquals("generic_form", first.getString(Option.KEY_LABEL));
        // 前端 enum-icon-select 组件中 help 会以 <em> 形式展示在选项右侧
        Assert.assertEquals("通用表单", first.getString(Option.KEY_HELP));
        // 未指定endType，前端不展示图标
        Assert.assertNull(first.getString(Option.KEY_END_TYPE));
    }

    /**
     * 前端提交的是枚举选项的val（即枚举常量名），需要转成对应的枚举实例才能赋值到实例属性上
     */
    public void testEnumFieldSetVal() {
        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(Optional.empty(), WidgetColumnConfig.class);

        PropertyType align = (PropertyType) props.get("align");
        Assert.assertNotNull(align);
        Assert.assertEquals(WidgetColumnConfig.ColumnAlign.class, align.fieldClazz);

        WidgetColumnConfig columnConfig = new WidgetColumnConfig();
        align.setVal(columnConfig, "right");
        Assert.assertEquals(WidgetColumnConfig.ColumnAlign.right, columnConfig.align);

        // 枚举实例直接赋值（例如属性默认值由插件json资源中的脚本指定）
        align.setVal(columnConfig, WidgetColumnConfig.ColumnAlign.left);
        Assert.assertEquals(WidgetColumnConfig.ColumnAlign.left, columnConfig.align);

        // 未选择选项时属性置空
        align.setVal(columnConfig, "");
        Assert.assertNull(columnConfig.align);

        try {
            align.setVal(columnConfig, "CC");
            Assert.fail("illegal enum name CC shall be rejected");
        } catch (RuntimeException e) {
            // expected: No enum constant WidgetColumnConfig.ColumnAlign.CC
        }
    }
}
