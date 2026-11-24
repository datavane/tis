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
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.PropertyType.PropVal;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.IEnumTypeSer;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.MemorySize;
import com.qlangtech.tis.plugin.MemorySize.MemoryUnit;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.plugin.utils.HexColorValidator;
import com.qlangtech.tis.plugin.utils.InputColor;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.qlangtech.tis.extension.IPropertyType.CONST_UNIT_INTEGER_FIELD;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月11日
 */
@SuppressWarnings("all")
public enum FormFieldType implements DescriptorUseableShortComment, IEndTypeGetter {
    /**
     * 颜色取色器，目标属性类型为 {@link InputColor}，前端使用 Coloris 组件渲染。
     * <p>
     * 值在前后端之间以颜色字符串（{@code #rrggbb}）往返：入站把字符串包成 {@link InputColor}，
     * 出站再把 {@link InputColor#getValue()} 摊平回字符串。不能依赖默认处理器——
     * 默认的 {@code PropVal#convertedVal()} 走 commons-beanutils，没有 String→{@link InputColor}
     * 的转换器，POST 时会因目标类型不匹配失败。
     */
    Color(14, new IPropValProcessor() {
        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            if (val.rawVal() instanceof InputColor color) {
                // 幂等：默认值 / valueChangePipe 回放可能直接给的是实例
                return color;
            }
            if (val.rawVal() == null) {
                return null;
            }
            // 注意不能用 val.convertedVal()：目标类型是 InputColor，commons-beanutils 没有对应转换器
            String colorVal = StringUtils.trimToNull(String.valueOf(val.rawVal()));
            // 必填由 Validator 负责，这里对空值保留 null 语义（清空即置空）
            return StringUtils.isEmpty(colorVal) ? null : new InputColor(colorVal);
        }

        @Override
        public Object serialize2Output(PropertyType pt, Object val) throws Exception {
            return val == null ? null : ((InputColor) val).getValue();
        }
    }, TISJsonSchema.FieldType.String
            , Lists.newArrayList(new InputValueValidator() {
        @Override
        public boolean validate(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {
            if (StringUtils.isEmpty(fieldData)) {
                return true;
            }
            return HexColorValidator.validate(msgHandler, context, fieldKey, fieldData);
        }
    })),
    /**
     * 对应插件中如下类型
     * <pre>
     *      @FormField(ordinal = 3, desClazz = TestChild.class, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN,validate = {Validator.require})
     *      public List<TestChild> children;
     * </pre>
     *
     */
    MULTI_DESCRIBLE_PLUGIN(13, TISJsonSchema.FieldType.Array),
    /**
     * 多选字段,目标属性样例：'List<String> cols'
     */
    MULTI_SELECTABLE(8, TISJsonSchema.FieldType.Array),

    INPUTTEXT(1, TISJsonSchema.FieldType.String),
    /**
     * 有多个选项可以选择
     */
    SELECTABLE(6, TISJsonSchema.FieldType.String),
    /**
     * 密码
     */
    PASSWORD(7, TISJsonSchema.FieldType.String), // 支持文件上传
    FILE(9, new IPropValProcessor() {
        @Override
        public Object processInput(Object instance, PropVal pval) throws Exception {
            String val = pval.convertedVal();
            if (!(instance instanceof ITmpFileStore)) {
                throw new IllegalStateException("instance of " + instance.getClass() + " must be type of " + ITmpFileStore.class.getName());
            }
            String[] filePath = StringUtils.split(val, ";");
            if (filePath.length == 2) {
                // 创建/更新
                File tmpPath = new File(filePath[0]);
                if (!tmpPath.exists()) {
                    throw new IllegalStateException("tmp path:" + tmpPath.getAbsolutePath() + " is not exist");
                }


                ((ITmpFileStore) instance).setTmpeFile(new ITmpFileStore.TmpFile(tmpPath));
                // org.apache.commons.io.FileUtils.copyFile(tmpPath, new File(xmlStoreFile.getParentFile(),
                // filePath[1]));
                return filePath[1];
            } else if (filePath.length == 1) {
                // 保持不变
                String fileName = filePath[0];
                return fileName;
            } else {
                throw new IllegalArgumentException("filePath.length must be 2: " + val);
            }


        }
    }, TISJsonSchema.FieldType.String) //
    , TEXTAREA(2, TISJsonSchema.FieldType.String) //
    /**
     * 日期选择，线格式 {@code yyyy-MM-dd}（ontology {@code DateWidget} 生成的
     * {@code rejectInvalidDate("yyyy-MM-dd")} 强校验的正是此格式，displayFormat 仅供前端展示）。
     * <p>
     * 不能依赖默认处理器——默认的 {@code PropVal#convertedVal()} 走 commons-beanutils，
     * 没有 String→{@link Date} 的转换器，字段声明为 {@code java.util.Date} 时 POST 会直接抛
     * {@code DateConverter does not support default String to 'Date' conversion}。
     * <p>
     * 日期值以 UTC 零点锚定，且 {@link #processInput}/{@code serialize2Output} 两侧都用 UTC：
     * {@code DateTime} 的「入站 UTC / 出站 sysZoneId」组合对纯日期不适用——UTC 以西时区的服务器
     * 回显时会倒退一天。两侧同锚点保证往返不随部署时区漂移。
     * <p>
     * 入站解析兼容未补零的月/日（如 {@code 1981-11-9}）：前端校验用的 {@code SimpleDateFormat}
     * （非宽松）对数字宽度本就放行，服务端解析不得比它更严；但 STRICT 解析会拒绝不存在的日期
     * （如 {@code 02-30}），同样与前端非宽松语义一致，不静默挪成 02-29。出站一律规范化成补零格式。
     * <p>
     * 目标属性类型支持两种：{@link Date}（解析字符串）、{@link String}
     * （存量用法如 {@code DateWidget.defaultValue} 以字符串承载 yyyy-MM-dd，逐字透传保持原语义）。
     */
    , DATE(3, new IPropValProcessor() {
        // 出站：规范化补零的 yyyy-MM-dd
        final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
        // 入站：月/日兼容 1~2 位数字（前端 rejectInvalidDate 用的 SimpleDateFormat("yyyy-MM-dd")
        // 非宽松校验即此语义，"1981-11-9" 能过校验，入站解析必须同样放行）；STRICT 解析拒绝
        // 不存在的日期（如 02-30），与 SimpleDateFormat 非宽松语义一致，不得静默挪成 02-29
        final DateTimeFormatter inputFormat = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            Object rawVal = val.rawVal();
            if (rawVal == null) {
                return null;
            }
            // 幂等：dftVal / valueChangePipe 回放可能直接给的就是 Date 实例
            if (rawVal instanceof Date) {
                return rawVal;
            }
            Class targetClazz = val.getTargetClazz();
            if (targetClazz == String.class) {
                // 存量用法（DateWidget.defaultValue 等）：String 承载 yyyy-MM-dd，逐字透传不改写
                return String.valueOf(rawVal);
            }
            String dateStr = StringUtils.trimToEmpty(String.valueOf(rawVal));
            if (StringUtils.isEmpty(dateStr)) {
                return null;
            }
            if (targetClazz == Date.class) {
                return Date.from(LocalDate.parse(dateStr, inputFormat).atStartOfDay(ZoneOffset.UTC).toInstant());
            }
            throw new IllegalStateException("DATE field target class must be:" + Date.class.getName()
                    + " or " + String.class.getName() + " but now is:" + targetClazz.getName());
        }

        @Override
        public Object serialize2Output(PropertyType pt, Object val) throws Exception {
            if (val instanceof String) {
                return val; // String 承载的字段，存储值即线格式
            }
            if (val instanceof Date) {
                // 走 getTime() 而非 toInstant()：java.sql.Date#toInstant 恒抛 UnsupportedOperationException
                return LocalDate.ofInstant(Instant.ofEpochMilli(((Date) val).getTime()), ZoneOffset.UTC)
                        .format(dateFormat);
            }
            return val;
        }
    }, TISJsonSchema.FieldType.String) //
    , JDBCColumn(11, new IPropValProcessor() {
        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            // return IPropValProcessor.super.processInput(instance, val);
            return null;
        }

        @Override
        public Object serialize2Output(PropertyType pt, Object val) throws Exception {
            // return IPropValProcessor.super.serialize2Output(pt, val);
            return null;
        }
    }, TISJsonSchema.FieldType.String),
    /**
     * 输入一个数字
     */
    INT_NUMBER(4, TISJsonSchema.FieldType.Integer, Collections.singletonList(Validator.integer)) //
    , ENUM(5, new IPropValProcessor() {
        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {

            if (val.propertyType.fieldClazz.isEnum()) {
                //需要将 val.rawVal() 转成 val.propertyType.fieldClazz对应的枚举实例 作为返回值
                Object rawVal = val.rawVal();
                // 属性默认值可能直接就是枚举实例，例如由插件json资源中的脚本指定
                if (rawVal instanceof Enum) {
                    return rawVal;
                }
                String enumName = StringUtils.trimToEmpty(rawVal == null ? null : String.valueOf(rawVal));
                final Class<?> fieldClazz = val.propertyType.fieldClazz;
                if (IEnumTypeSer.class.isAssignableFrom(fieldClazz)) {
                    // 实现了自定义序列化的枚举：选项 value 由 serialize() 产出，反查走同一套映射；空 token 返回 null
                    return IEnumTypeSer.deserialize(fieldClazz, enumName);
                }
                // 选项为空则属性置空
                return StringUtils.isEmpty(enumName) ? null
                        : Enum.valueOf((Class) fieldClazz, enumName);
            }


            return IPropValProcessor.super.processInput(instance, val);
        }

        @Override
        public Object serialize2Output(PropertyType pt, Object val) throws Exception {
            // 与 createEnumOptions() 下发的选项 value 保持一致，否则表单回填时匹配不上选项
            if (val instanceof IEnumTypeSer serDe) {
                return serDe.valueToken();
            }
            return val instanceof Enum<?> e ? e.name() : val;
        }
    }, TISJsonSchema.FieldType.String) //
    , DateTime(10, new IPropValProcessor() {
        final DateTimeFormatter isoFormat = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            String datetimeFormat = val.propertyType.getExtraProps().getString(PluginExtraProps.KEY_DATETIME_FORMAT);
            final LocalDateTime dateTime = LocalDateTime.parse((String) val.rawVal(),
                    StringUtils.isEmpty(datetimeFormat) ?
                            isoFormat : DateTimeFormatter.ofPattern(datetimeFormat));
            // ng-zorro 组件上传使用格林尼治时间
            Instant ist = dateTime.atZone(ZoneOffset.UTC).toInstant();
            //  Date dateTime = dateTimeFormat.parse();
            Class targetClazz = val.getTargetClazz();
            if (targetClazz == Long.class) {
                return ist.toEpochMilli();
            } else if (targetClazz == java.util.Date.class) {
                return Date.from(ist);
            } else {
                throw new IllegalStateException("invalid target class:" + targetClazz);
            }
        }

        @Override
        public Object serialize2Output(final PropertyType pt, Object val) throws Exception {

            Class targetClazz = pt.fieldClazz;
            Instant ist = null;
            if (targetClazz == Long.class) {
                ist = Instant.ofEpochMilli((Long) val);
            } else if (targetClazz == java.util.Date.class) {
                ist = ((Date) val).toInstant();
                //  return LocalDateTime.ofInstant(, TimeFormat.sysZoneId).format(isoFormat)
            } else {
                throw new IllegalStateException("invalid target class:" + targetClazz);
            }
            return LocalDateTime.ofInstant(ist, TimeFormat.sysZoneId).format(isoFormat);
        }
    }, TISJsonSchema.FieldType.String) //
    , DECIMAL_NUMBER(4, TISJsonSchema.FieldType.Number)
    /**
     * 时间长度duration
     */
    , DURATION_OF_SECOND(CONST_UNIT_INTEGER_FIELD, new DurationValProcessor() {
        @Override
        protected Duration deserialize(long val) {
            return Duration.ofSeconds(val);
        }

        @Override
        protected long serialize(Duration val) {
            return val.toSeconds();
        }
    }, (fieldProps) -> {
        fieldProps.put(IPropertyType.KEY_UNIT, "Second");
    }, TISJsonSchema.FieldType.Integer) //
    , DURATION_OF_MINUTE(CONST_UNIT_INTEGER_FIELD, new DurationValProcessor() {
        @Override
        protected Duration deserialize(long val) {
            return Duration.ofMinutes(val);
        }

        @Override
        protected long serialize(Duration val) {
            return val.toMinutes();
        }
    }, (fieldProps) -> {
        fieldProps.put(IPropertyType.KEY_UNIT, "Minute");
    }, TISJsonSchema.FieldType.Integer) //
    , DURATION_OF_HOUR(CONST_UNIT_INTEGER_FIELD, new DurationValProcessor() {
        @Override
        protected Duration deserialize(long val) {
            return Duration.ofHours(val);
        }

        @Override
        protected long serialize(Duration val) {
            return val.toHours();
        }
    }, (fieldProps) -> {
        fieldProps.put(IPropertyType.KEY_UNIT, "Hour");
    }, TISJsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_BYTE(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.BYTES), TISJsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_KIBI(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofKibiBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getKibiBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.KILO_BYTES), TISJsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_MEGA(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofMebiBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getMebiBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.MEGA_BYTES), TISJsonSchema.FieldType.Integer);

    public List<InputValueValidator> getAppendValueValidator() {
        return this.customizeValidators;
    }

    private static class MemoryAppendExternalProps implements Consumer<JSONObject> {
        private final MemoryUnit memoryUnit;

        public MemoryAppendExternalProps(MemoryUnit memoryUnit) {
            this.memoryUnit = Objects.requireNonNull(memoryUnit, "memoryUnit can not be null");
        }

        @Override
        public void accept(JSONObject fieldProps) {
            fieldProps.put(IPropertyType.KEY_UNIT, StringUtils.upperCase(memoryUnit.getUnits()[1]));
        }
    }

    private static abstract class MemorySizeValProcessor implements IPropValProcessor {
        @Override
        public final Object processInput(Object instance, PropVal val) throws Exception {
            if (MemorySize.class != val.getTargetClazz()) {
                throw new IllegalStateException("field class must be type:" + Duration.class + " but now is " + val.getTargetClazz().getSimpleName());
            }
            Number rawVal = (Number) val.rawVal();
            return deserialize(rawVal.longValue());
        }

        @Override
        public final Object serialize2Output(PropertyType pt, Object val) throws Exception {
            Class targetClazz = pt.fieldClazz;
            if (MemorySize.class != targetClazz) {
                throw new IllegalStateException("field class must be type:" + Duration.class + " but now is " + targetClazz.getSimpleName());
            }
            return serialize((MemorySize) val);
        }

        protected abstract MemorySize deserialize(long val);

        protected abstract long serialize(MemorySize val);
    }


    private static abstract class DurationValProcessor implements IPropValProcessor {
        @Override
        public final Object processInput(Object instance, PropVal val) throws Exception {
            if (Duration.class != val.getTargetClazz()) {
                throw new IllegalStateException("field class must be type:" + Duration.class + " but now is " + val.getTargetClazz().getSimpleName());
            }
            Number rawVal = (Number) val.rawVal();
            // return Duration.ofSeconds(rawVal.longValue());
            return deserialize(rawVal.longValue());
        }

        protected abstract Duration deserialize(long val);

        protected abstract long serialize(Duration val);

        @Override
        public final Object serialize2Output(PropertyType pt, Object val) throws Exception {
            Class targetClazz = pt.fieldClazz;
            if (Duration.class != targetClazz) {
                throw new IllegalStateException("field class must be type:" + Duration.class + " but now is " + targetClazz.getSimpleName());
            }
            return serialize((Duration) val);
            // return ((Duration) val).toSeconds();
        }
    }

    public static void main(String[] args) throws Exception {
        int index = 1;
        for (FormFieldType type : FormFieldType.values()) {
            System.out.println((index++) + ". " + String.valueOf(type));
        }
    }

    private final int identity;
    public final IPropValProcessor valProcessor;
    public final Consumer<JSONObject> appendExternalProps;
    public final TISJsonSchema.FieldType schemaFieldType;
    private final List<InputValueValidator> customizeValidators;

    FormFieldType(int val, TISJsonSchema.FieldType schemaFieldType) {
        this(val, new IPropValProcessor() {
        }, (fieldProps) -> {
        }, schemaFieldType, Collections.emptyList());
    }

    FormFieldType(int val, TISJsonSchema.FieldType schemaFieldType, List<InputValueValidator> customizeValidators) {
        this(val, new IPropValProcessor() {
        }, (fieldProps) -> {
        }, schemaFieldType, customizeValidators);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, TISJsonSchema.FieldType schemaFieldType) {
        this(val, valProcessor, (fieldProps) -> {
        }, schemaFieldType, Collections.emptyList());
    }

    FormFieldType(int val, IPropValProcessor valProcessor, TISJsonSchema.FieldType schemaFieldType,
                  List<InputValueValidator> customizeValidators) {
        this(val, valProcessor, (fieldProps) -> {
        }, schemaFieldType, customizeValidators);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, Consumer<JSONObject> appendExternalProps,
                  TISJsonSchema.FieldType schemaFieldType) {
        this(val, valProcessor, appendExternalProps, schemaFieldType, Collections.emptyList());
    }

    FormFieldType(int val, IPropValProcessor valProcessor, Consumer<JSONObject> appendExternalProps,
                  TISJsonSchema.FieldType schemaFieldType, List<InputValueValidator> customizeValidators) {
        this.identity = val;
        this.valProcessor = valProcessor;
        this.appendExternalProps = appendExternalProps;
        this.schemaFieldType = schemaFieldType;
        this.customizeValidators = customizeValidators;
    }

    public int getIdentity() {
        return this.identity;
    }

    /**
     * 每种表单控件的一句话功能说明。最终由 {@link com.qlangtech.tis.manage.common.OptionWithEndType#createEnumOptions(Class)}
     * 以选项的 {@code description} 随选项下发前端，落在选项 JSON 的 {@code help} 上。
     * <p>
     * 表述刻意保持简短：{@code PropertyType#resolveEnumLabel} 的 javadoc 里 shortComment()
     * 还是 label 的备选来源（实现目前只取常量名），文案太长就不适合当选项名用了。
     */
    @Override
    public String shortComment() {
        return switch (this) {
            case MULTI_DESCRIBLE_PLUGIN -> "嵌套的子插件列表";
            case MULTI_SELECTABLE -> "多选，可勾选多项";
            case INPUTTEXT -> "单行文本";
            case SELECTABLE -> "下拉单选";
            case PASSWORD -> "密码输入";
            case FILE -> "上传文件";
            case TEXTAREA -> "多行文本";
            case DATE -> "日期";
            case JDBCColumn -> "数据源表字段";
            case INT_NUMBER -> "整数";
            case ENUM -> "枚举单选";
            case DateTime -> "日期时间";
            case DECIMAL_NUMBER -> "小数";
            case DURATION_OF_SECOND -> "时长（秒）";
            case DURATION_OF_MINUTE -> "时长（分钟）";
            case DURATION_OF_HOUR -> "时长（小时）";
            case MEMORY_SIZE_OF_BYTE -> "内存大小（字节）";
            case MEMORY_SIZE_OF_KIBI -> "内存大小（KiB）";
            case MEMORY_SIZE_OF_MEGA -> "内存大小（MiB）";
            case Color -> "颜色取色器";
            // 显式给 default：switch 表达式对枚举不写 default 时，编译器会合成一个抛
            // IncompatibleClassChangeError 的分支，日后新增常量会变成运行时炸点
            default -> this.name();
        };
    }

    /**
     * 取值必须能作为前端 nzType 渲染（见 {@link EndType} 里那段 Ant Design 图标全集的说明）：
     * 这里选的都是已收录的常量，val 要么是合法的 Ant 图标名（containICON=false，前端 assets 里加载），
     * 要么在 classpath 的 endtype/icon/ 下有自绘 svg（containICON=true，如 file / table）。
     */
    @Override
    public EndType getEndType() {
        return switch (this) {
            case MULTI_DESCRIBLE_PLUGIN -> EndType.DeploymentUnit;
            case MULTI_SELECTABLE -> EndType.CheckSquare;
            case INPUTTEXT -> EndType.Edit;
            case SELECTABLE -> EndType.DownSquare;
            case PASSWORD -> EndType.Lock;
            case FILE -> EndType.File;
            case TEXTAREA -> EndType.AlignLeft;
            case DATE -> EndType.Calendar;
            case JDBCColumn -> EndType.Table;
            case INT_NUMBER -> EndType.FieldNumber;
            case ENUM -> EndType.UnorderedList;
            case DateTime -> EndType.FieldTime;
            case DECIMAL_NUMBER -> EndType.Percentage;
            case DURATION_OF_SECOND -> EndType.ClockCircle;
            case DURATION_OF_MINUTE -> EndType.ClockCircle;
            case DURATION_OF_HOUR -> EndType.Hourglass;
            case MEMORY_SIZE_OF_BYTE -> EndType.Hdd;
            case MEMORY_SIZE_OF_KIBI -> EndType.Hdd;
            case MEMORY_SIZE_OF_MEGA -> EndType.Hdd;
            case Color -> EndType.BgColors;
            // 与 {@link IEndTypeGetter#getEndType()} 接口默认值保持一致：无图标
            default -> EndType.Blank;
        };
    }

    /**
     * 可对多选控件进行校验
     */
    public interface IMultiSelectValidator {
        /**
         * @param msgHandler
         * @param subFormFilter
         * @param context
         * @param fieldName
         * @param items         多选条目列表
         * @return
         */
        public default boolean validate(IFieldErrorHandler msgHandler, Optional<SubFormFilter> subFormFilter,
                                        Context context, String fieldName, List<SelectedItem> items) {

            int selectCount = 0;
            for (FormFieldType.SelectedItem item : items) {
                if (item.isChecked()) {
                    selectCount++;
                }
            }
            if (selectCount < 1) {
                msgHandler.addFieldError(context, fieldName, "至少选择一项");
                return false;
            }
            return true;
        }
    }

    public static class SelectedItem extends Option {
        // 是否选中了
        // private boolean checked;

        private IMultiElement cmeta;

        public SelectedItem(String name, String value, boolean checked) {
            super(name, value);
            this.setChecked(checked);
        }

        public SelectedItem(IMultiElement cmeta) {
            this(Objects.requireNonNull(cmeta, "cmeta can not be null").getName()
                    , cmeta.getName(), true // !cmeta
                    // .isDisable()
            );
            this.cmeta = cmeta;
        }

        public IMultiElement getCmeta() {
            return cmeta;
        }

        //        public boolean isChecked() {
        //            return checked;
        //        }
    }

    public interface IPropValProcessor {
        /**
         * @param val 从json中取出来的值
         * @return
         */
        public default Object processInput(Object instance, PropVal val) throws Exception {
            return val.convertedVal();
        }

        public default Object serialize2Output(final PropertyType pt, Object val) throws Exception {
            return val;
        }
    }
}
