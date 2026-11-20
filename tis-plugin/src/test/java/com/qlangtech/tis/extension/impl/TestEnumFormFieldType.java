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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.IEnumTypeSer;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.UserProfile;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * {@link EnumFormFieldType} 注解驱动的枚举显示模式解析（{@code PropertyType.getEnumFieldMode()}）：
 * <ul>
 * <li>枚举类上标了 {@code @EnumFormFieldType} → 以注解为准（如 {@link IEndTypeGetter.EndType} 标了
 * MODEL_ASSIST，于是 {@code UserProfile.rightIcon} 无需再在 UserProfile.json 里写
 * {@code "enumMode":"model_assist"}）；</li>
 * <li>未标注 → 回退到 .json 的 {@code enumMode} 键（存量插件 json 不受影响），都没有则 {@link EnumFieldMode#DEFAULT}；</li>
 * <li>非 ENUM/SELECTABLE 类型字段一律返回 {@code null}。</li>
 * </ul>
 * 同类覆盖选项过滤解析（{@code PropertyType.resolveEnumProp()}）：注解的
 * {@code enumItemPredicate()}（缺省 {@code ALL}）在枚举选项自动生成时过滤枚举常量。
 * 注意 {@code getEnumFieldMode()} 的结果在首次调用后被缓存在实例上，因此每个用例都须用独立实例。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/14
 */
public class TestEnumFormFieldType {

    /** EndType 类上标了 MODEL_ASSIST，UserProfile.rightIcon（ENUM）随之生效，json 无需配置 enumMode；且 mode 回写进 extraProp 随 descriptor 下发 */
    @Test
    public void testUserProfileRightIconModeFromAnnotation() throws Exception {
        PropertyType pt = createPropertyType(UserProfile.class, "rightIcon");
        assertSame(EnumFieldMode.MODEL_ASSIST, pt.getEnumFieldMode());
        assertEquals("annotation mode shall write back into extraProp for frontend descriptor",
                EnumFieldMode.MODEL_ASSIST.val, pt.extraProp.getProps().getString(EnumFieldMode.KEY_ENUM_MODE));
    }

    /** SELECTABLE 类型同样吃注解：EndType 作 SELECTABLE 字段类型时也解析出 MODEL_ASSIST */
    @Test
    public void testAnnotatedEnumOnSelectableField() throws Exception {
        EnumFieldMode mode = createPropertyType(SelectableHolder.class, "endType").getEnumFieldMode();
        assertSame(EnumFieldMode.MODEL_ASSIST, mode);
    }

    /** 注解优先于 .json：字段类型标了注解后，json 里再写 enumMode 也不生效 */
    @Test
    public void testAnnotationTakesPrecedenceOverJsonEnumMode() throws Exception {
        PropertyType pt = createPropertyType(UserProfile.class, "rightIcon", EnumFieldMode.MULTIPLE.val);
        assertSame(EnumFieldMode.MODEL_ASSIST, pt.getEnumFieldMode());
    }

    /** 未标注的枚举（TestChild.Sex）仍走 .json 回退路径，存量插件配置不受影响 */
    @Test
    public void testUnAnnotatedEnumFallback2JsonEnumMode() throws Exception {
        PropertyType pt = createPropertyType(UserProfile.class, "sex", EnumFieldMode.MODEL_ASSIST.val);
        assertSame(EnumFieldMode.MODEL_ASSIST, pt.getEnumFieldMode());
    }

    /** 未标注且 json 未配置 → DEFAULT（前端按单选下拉框渲染） */
    @Test
    public void testUnAnnotatedEnumWithoutConfigDefaultMode() throws Exception {
        assertSame(EnumFieldMode.DEFAULT, createPropertyType(UserProfile.class, "sex").getEnumFieldMode());
    }

    /** SELECTABLE 的 String 字段（UserProfile.llm）未标注、json 无 enumMode → DEFAULT */
    @Test
    public void testSelectableStringWithoutConfigDefaultMode() throws Exception {
        assertSame(EnumFieldMode.DEFAULT, createPropertyType(UserProfile.class, "llm").getEnumFieldMode());
    }

    /** 非 ENUM/SELECTABLE 类型字段与显示模式无关，恒为 null */
    @Test
    public void testNonEnumFormFieldTypeReturnNull() throws Exception {
        assertNull(createPropertyType(UserProfile.class, "name").getEnumFieldMode());
    }

    /**
     * resolveEnumProp 过滤路径：EndType 标了 {@code ExcludeDataEndType}，
     * 自动生成的选项须排除 Data/DataType 类别常量（图标选择场景不出现数据库端类型）
     */
    @Test
    public void testResolveEnumPropExcludeDataEndType() throws Exception {
        JSONObject props = new JSONObject();
        PropertyType.resolveEnumProp(SelectableHolder.class.getField("endType"), null, props, Function.identity());

        JSONArray opts = props.getJSONArray(Descriptor.KEY_ENUM_PROP);
        assertNotNull(opts);

        List<String> expected = Arrays.stream(IEndTypeGetter.EndType.values())
                .filter((endType) -> endType.category != IEndTypeGetter.EndTypeCategory.Data
                        && endType.category != IEndTypeGetter.EndTypeCategory.DataType)
                .map(IEnumTypeSer.class::cast).map(IEnumTypeSer::valueToken).toList();
        List<String> actual = new ArrayList<>(opts.size());
        for (int i = 0; i < opts.size(); i++) {
            actual.add(opts.getJSONObject(i).getString(Option.KEY_VALUE));
        }
        assertEquals("options shall exclude Data/DataType end types", expected.size(), actual.size());
        assertTrue("options mismatch,expected:" + expected + ",actual:" + actual //
                , expected.containsAll(actual) && actual.containsAll(expected));
    }

    /** 未标注枚举走缺省 ALL：TestChild.Sex 两个常量全量保留 */
    @Test
    public void testResolveEnumPropUnAnnotatedEnumKeepsAll() throws Exception {
        JSONObject props = new JSONObject();
        PropertyType.resolveEnumProp(UserProfile.class.getField("sex"), null, props, Function.identity());

        JSONArray opts = props.getJSONArray(Descriptor.KEY_ENUM_PROP);
        assertEquals(2, opts.size());
    }

    /**
     * 不走 descriptor 扫描（那需要 IPluginContext 线程上下文 + JDK17 的 --add-opens），
     * 直接用 PropertyType 的构造器 —— 与 {@code Descriptor.getPropertyTypes()} 内部造的是同一种对象。
     */
    private static PropertyType createPropertyType(Class<?> ownerClazz, String fieldName) throws Exception {
        Field field = ownerClazz.getField(fieldName);
        return new PropertyType(ownerClazz, field, field.getAnnotation(FormField.class));
    }

    private static PropertyType createPropertyType(Class<?> ownerClazz, String fieldName, String enumMode) throws
            Exception {
        PropertyType pt = createPropertyType(ownerClazz, fieldName);
        pt.setExtraProp(new PluginExtraProps.Props(propsWith(enumMode)));
        return pt;
    }

    private static JSONObject propsWith(String enumMode) {
        JSONObject props = new JSONObject();
        props.put(EnumFieldMode.KEY_ENUM_MODE, enumMode);
        return props;
    }

    public static class SelectableHolder {
        @FormField(ordinal = 0, type = FormFieldType.SELECTABLE, validate = {})
        public IEndTypeGetter.EndType endType;
    }
}
