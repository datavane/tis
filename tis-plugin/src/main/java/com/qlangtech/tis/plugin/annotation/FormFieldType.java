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
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.IEnumTypeSer;
import com.qlangtech.tis.extension.impl.PropertyType.PropVal;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.MemorySize;
import com.qlangtech.tis.plugin.MemorySize.MemoryUnit;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    , DATE(3, TISJsonSchema.FieldType.String) //
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
    INT_NUMBER(4, TISJsonSchema.FieldType.Integer) //
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
            final LocalDateTime dateTime = LocalDateTime.parse((String) val.rawVal(), StringUtils.isEmpty(datetimeFormat) ?
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

    FormFieldType(int val, TISJsonSchema.FieldType schemaFieldType) {
        this(val, new IPropValProcessor() {
        }, (fieldProps) -> {
        }, schemaFieldType);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, TISJsonSchema.FieldType schemaFieldType) {
        this(val, valProcessor, (fieldProps) -> {
        }, schemaFieldType);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, Consumer<JSONObject> appendExternalProps,
                  TISJsonSchema.FieldType schemaFieldType) {
        this.identity = val;
        this.valProcessor = valProcessor;
        this.appendExternalProps = appendExternalProps;
        this.schemaFieldType = schemaFieldType;
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
