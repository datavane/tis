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

import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.utils.InputColor;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * {@link FormFieldType#Color} 的值往返：前端只认颜色字符串，服务端只认 {@link InputColor}。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/9
 */
public class TestFormFieldTypeColor extends TestCase {

    public static class ColorHolder {
        @FormField(ordinal = 0, type = FormFieldType.Color, validate = {})
        public InputColor color;
    }

    /** POST 侧：JSON 里的 {@code _primaryVal} 是字符串，必须包成 InputColor 才能 field.set */
    public void testProcessInputConvertsString2InputColor() throws Exception {
        ColorHolder holder = new ColorHolder();
        PropertyType pt = getColorPropertyType();

        pt.setVal(holder, "#1890ff");

        assertNotNull(holder.color);
        assertEquals("#1890ff", holder.color.getValue());
    }

    /** 已经是实例时保持幂等（dftVal / valueChangePipe 回放会走这条） */
    public void testProcessInputIsIdempotentForInstance() throws Exception {
        ColorHolder holder = new ColorHolder();
        InputColor given = new InputColor("#f5222d");

        getColorPropertyType().setVal(holder, given);

        assertSame(given, holder.color);
    }

    /** 空串 / null 一律落成 null，避免出现 value="" 的脏对象 */
    public void testProcessInputEmptyBecomesNull() throws Exception {
        ColorHolder holder = new ColorHolder();
        getColorPropertyType().setVal(holder, "  ");
        assertNull(holder.color);

        holder.color = new InputColor("#fff");
        getColorPropertyType().setVal(holder, null);
        assertNull(holder.color);
    }

    /** GET 侧：下发给前端的是裸字符串，不是 {"value": "..."} */
    public void testSerialize2OutputFlattens2String() throws Exception {
        ColorHolder holder = new ColorHolder();
        holder.color = new InputColor("#00b96b");
        PropertyType pt = getColorPropertyType();

        assertEquals("#00b96b", pt.getFrontendOutput(holder));
        assertNull(pt.getFrontendOutput(new ColorHolder()));
    }

    /** 线格式契约：前端按 *ngSwitchCase="14" 分派，identity 被改就静默不渲染 */
    public void testWireIdentityIs14() {
        assertEquals(14, FormFieldType.Color.getIdentity());
    }

    public void testShortCommentAndEndType() {
        assertEquals("颜色取色器", FormFieldType.Color.shortComment());
        assertEquals(IEndTypeGetter.EndType.BgColors, FormFieldType.Color.getEndType());
    }

    /**
     * 插件实例走 XStream 落盘，反射反序列化不走带参构造器 —— 没有无参构造器就会在读存量配置时炸。
     */
    public void testXStreamRoundTrip() {
        ColorHolder holder = new ColorHolder();
        holder.color = new InputColor("#722ed1");

        String xml = XmlFile.DEFAULT_XSTREAM.toXML(holder);
        Object back = XmlFile.DEFAULT_XSTREAM.fromXML(xml);

        assertTrue(xml, back instanceof ColorHolder);
        InputColor restored = ((ColorHolder) back).color;
        assertNotNull(xml, restored);
        assertEquals("#722ed1", restored.getValue());
    }

    /**
     * 不走 descriptor 扫描（那需要 IPluginContext 线程上下文 + JDK17 的 --add-opens），
     * 直接用 PropertyType 的构造器 —— 与 {@code Descriptor.getPropertyTypes()} 内部造的是同一种对象。
     */
    private static PropertyType getColorPropertyType() throws Exception {
        Field field = ColorHolder.class.getField("color");
        return new PropertyType(ColorHolder.class, field, field.getAnnotation(FormField.class));
    }
}
