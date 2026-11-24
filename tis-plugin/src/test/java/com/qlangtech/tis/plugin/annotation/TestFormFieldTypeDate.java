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
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link FormFieldType#DATE} 的值往返：前端只认 {@code yyyy-MM-dd} 字符串（ontology
 * {@code DateWidget} 用 {@code rejectInvalidDate("yyyy-MM-dd")} 强校验的线格式），
 * 服务端字段可以声明成 {@link Date} 或 {@link String}（后者是 {@code DateWidget.defaultValue}
 * 的存量承载形态，必须逐字透传）。
 *
 * <p>修复锚点：ontology Action 表单（如 {@code ActionFormPreview.chartxy_daily_report_report_date}）
 * 以 {@code java.util.Date} 承载日期字段，默认 {@code PropVal#convertedVal()} 走 commons-beanutils
 * 没有 String→Date 转换器，POST 直接抛
 * {@code DateConverter does not support default String to 'Date' conversion}。
 *
 * <p>时区契约：入站按 UTC 零点锚定、出站也按 UTC 取日历日期（两侧同锚点，往返不随部署时区漂移；
 * {@code DateTime} 的「出站 sysZoneId」组合对纯日期会倒退一天——用例 5 以 23:00 UTC 的时刻值
 * 把这条钉死：在本机 UTC+8 下若有人改成系统时区格式化，将得到 {@code 2024-01-16} 而红）。
 *
 * <p>数字宽度契约：入站解析必须放行未补零的月/日（如 {@code 1981-11-9}）——前端校验的
 * {@code SimpleDateFormat}（非宽松）本就放行；出站一律规范化成补零格式。不存在的日期
 * （{@code 02-30}）与前端一致地拒绝，不静默挪一天。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/23
 */
public class TestFormFieldTypeDate {

    /** ontology Action form groovy 脚本的承载形态：java.util.Date */
    public static class DateHolder {
        @FormField(ordinal = 0, type = FormFieldType.DATE, validate = {})
        public java.util.Date reportDate;
    }

    /** {@code DateWidget.defaultValue} 的存量承载形态：String 逐字透传 */
    public static class StringHolder {
        @FormField(ordinal = 0, type = FormFieldType.DATE, validate = {})
        public String defaultValue;
    }

    public static class LongHolder {
        @FormField(ordinal = 0, type = FormFieldType.DATE, validate = {})
        public Long unsupported;
    }

    /** POST 侧：yyyy-MM-dd 字符串必须解析成 UTC 零点锚定的 java.util.Date */
    @Test
    public void testProcessInputConvertsString2Date() throws Exception {
        DateHolder holder = new DateHolder();

        getPropertyType(DateHolder.class, "reportDate").setVal(holder, "2024-01-15");

        assertTrue("必须是确切类型 java.util.Date（不能是子类）", holder.reportDate.getClass() == Date.class);
        assertEquals("必须锚定 UTC 零点（本断言在任何服务器时区下期望值恒定）",
                LocalDate.parse("2024-01-15").atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                holder.reportDate.getTime());
    }

    /**
     * 未补零的月/日必须放行（回归锚点 {@code 1981-11-9} 曾抛
     * {@code DateTimeParseException ... at index 8}）：前端 rejectInvalidDate 用
     * {@code SimpleDateFormat}（非宽松）校验，对数字宽度本就放行，服务端不得更严。
     */
    @Test
    public void testProcessInputAcceptsUnpaddedMonthDay() throws Exception {
        DateHolder holder = new DateHolder();

        getPropertyType(DateHolder.class, "reportDate").setVal(holder, "1981-11-9");

        assertEquals("未补零按补零语义解析：11-9 即 11-09",
                LocalDate.parse("1981-11-09").atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                holder.reportDate.getTime());
    }

    /** 不存在的日期（如 02-30）必须报错，不得静默挪成 02-29（与前端非宽松校验语义一致） */
    @Test
    public void testProcessInputRejectsImpossibleDate() throws Exception {
        try {
            getPropertyType(DateHolder.class, "reportDate").setVal(new DateHolder(), "2024-02-30");
            fail("02-30 是不存在的日期，必须在解析期报错");
        } catch (RuntimeException e) {
            assertTrue("cause 应为 DateTimeParseException，实际：" + e.getCause(),
                    e.getCause() instanceof java.time.format.DateTimeParseException);
        }
    }

    /** 已经是 Date 实例时保持幂等（dftVal / valueChangePipe 回放会走这条） */
    @Test
    public void testProcessInputIsIdempotentForDateInstance() throws Exception {
        DateHolder holder = new DateHolder();
        Date given = Date.from(LocalDate.parse("2024-03-01").atStartOfDay(ZoneOffset.UTC).toInstant());

        getPropertyType(DateHolder.class, "reportDate").setVal(holder, given);

        assertSame(given, holder.reportDate);
    }

    /** Date 字段：空串 / null 一律落成 null（清空即置空，同 Color 语义） */
    @Test
    public void testProcessInputEmptyBecomesNull() throws Exception {
        DateHolder holder = new DateHolder();
        PropertyType pt = getPropertyType(DateHolder.class, "reportDate");

        pt.setVal(holder, "  ");
        assertNull(holder.reportDate);

        holder.reportDate = new Date();
        pt.setVal(holder, null);
        assertNull(holder.reportDate);
    }

    /**
     * {@code DateWidget.defaultValue} 的存量形态：String 承载字段必须逐字透传，
     * 连空白也不得悄悄 trim（beanutils 时代就是这个语义，静默改写会让存量比对失配）。
     */
    @Test
    public void testStringFieldPassThroughVerbatim() throws Exception {
        StringHolder holder = new StringHolder();
        PropertyType pt = getPropertyType(StringHolder.class, "defaultValue");

        pt.setVal(holder, "2024-01-15");
        assertEquals("2024-01-15", holder.defaultValue);

        pt.setVal(holder, "  ");
        assertEquals("String 承载字段逐字透传，不得 trim", "  ", holder.defaultValue);

        pt.setVal(holder, null);
        assertNull(holder.defaultValue);
    }

    /**
     * GET 侧：Date 必须按 UTC 取日历日期格式化回 {@code yyyy-MM-dd}。
     * 用 23:00 UTC 的时刻值钉死「出站不得用系统时区」：本机（UTC+8）下系统时区格式化会得到 01-16。
     */
    @Test
    public void testSerialize2OutputFormatsDateByUTC() throws Exception {
        DateHolder holder = new DateHolder();
        holder.reportDate = Date.from(LocalDate.parse("2024-01-15")
                .atStartOfDay(ZoneOffset.UTC).plusHours(23).toInstant());

        assertEquals("2024-01-15", getPropertyType(DateHolder.class, "reportDate").getFrontendOutput(holder));
        assertNull(getPropertyType(DateHolder.class, "reportDate").getFrontendOutput(new DateHolder()));
    }

    /**
     * POST→GET 往返闭环：同一字符串进、同一字符串出；未补零输入出站时规范化成补零格式
     * （Date 字段有解析-格式化往返，String 字段逐字透传）。
     */
    @Test
    public void testRoundTrip() throws Exception {
        DateHolder holder = new DateHolder();
        PropertyType datePt = getPropertyType(DateHolder.class, "reportDate");
        datePt.setVal(holder, "2024-01-15");
        assertEquals("2024-01-15", datePt.getFrontendOutput(holder));

        datePt.setVal(holder, "1981-11-9");
        assertEquals("出站规范化成补零格式（回填值要能过前端 yyyy-MM-dd 校验）",
                "1981-11-09", datePt.getFrontendOutput(holder));

        StringHolder strHolder = new StringHolder();
        PropertyType strPt = getPropertyType(StringHolder.class, "defaultValue");
        strPt.setVal(strHolder, "2024-01-15");
        assertEquals("2024-01-15", strPt.getFrontendOutput(strHolder));
    }

    /** 线格式契约：前端按 *ngSwitchCase="3" 分派，identity 被改就静默不渲染日期控件 */
    @Test
    public void testWireIdentityIs3() {
        assertEquals(3, FormFieldType.DATE.getIdentity());
    }

    /** 既非 Date 也非 String 的声明类型（如 Long）必须在实例化期显式报错，不得静默落 null */
    @Test
    public void testUnsupportedTargetClassRejected() throws Exception {
        LongHolder holder = new LongHolder();
        try {
            getPropertyType(LongHolder.class, "unsupported").setVal(holder, "2024-01-15");
            fail("Long 承载的 DATE 字段必须在 setVal 期抛错");
        } catch (RuntimeException e) {
            assertTrue("cause 应为 IllegalStateException，实际：" + e.getCause(),
                    e.getCause() instanceof IllegalStateException);
        }
    }

    /**
     * 不走 descriptor 扫描（那需要 IPluginContext 线程上下文 + JDK17 的 --add-opens），
     * 直接用 PropertyType 的构造器 —— 与 {@code TestFormFieldTypeColor} 同一手法。
     */
    private static PropertyType getPropertyType(Class<?> holderClazz, String fieldName) throws Exception {
        Field field = holderClazz.getField(fieldName);
        return new PropertyType(holderClazz, field, field.getAnnotation(FormField.class));
    }
}
