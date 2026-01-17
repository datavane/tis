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
import com.qlangtech.tis.aiagent.llm.JsonSchema;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.EnumFieldMode;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.PropertyType.PropVal;
import com.qlangtech.tis.manage.common.Option;
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
public enum FormFieldType {


    /**
     * 多选字段,目标属性样例：'List<String> cols'
     */
    MULTI_SELECTABLE(8, JsonSchema.FieldType.Array),

    INPUTTEXT(1, JsonSchema.FieldType.String),
    /**
     * 有多个选项可以选择
     */
    SELECTABLE(6, JsonSchema.FieldType.String),
    /**
     * 密码
     */
    PASSWORD(7, JsonSchema.FieldType.String), // 支持文件上传
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
    }, JsonSchema.FieldType.String) //
    , TEXTAREA(2, JsonSchema.FieldType.String) //
    , DATE(3, JsonSchema.FieldType.String) //
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
    }, JsonSchema.FieldType.String),
    /**
     * 输入一个数字
     */
    INT_NUMBER(4, JsonSchema.FieldType.Integer) //
    , ENUM(5, new IPropValProcessor() {
        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            return IPropValProcessor.super.processInput(instance, val);
        }
    }, JsonSchema.FieldType.String) //
    , DateTime(10, new IPropValProcessor() {
        final DateTimeFormatter isoFormat = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {
            LocalDateTime dateTime = LocalDateTime.parse((String) val.rawVal(), isoFormat);
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
    }, JsonSchema.FieldType.String) //
    , DECIMAL_NUMBER(4, JsonSchema.FieldType.Number)
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
    }, JsonSchema.FieldType.Integer) //
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
    }, JsonSchema.FieldType.Integer) //
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
    }, JsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_BYTE(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.BYTES), JsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_KIBI(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofKibiBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getKibiBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.KILO_BYTES), JsonSchema.FieldType.Integer) //
    , MEMORY_SIZE_OF_MEGA(CONST_UNIT_INTEGER_FIELD, new MemorySizeValProcessor() {
        @Override
        protected MemorySize deserialize(long val) {
            return MemorySize.ofMebiBytes(val);
        }

        @Override
        protected long serialize(MemorySize val) {
            return val.getMebiBytes();
        }
    }, new MemoryAppendExternalProps(MemoryUnit.MEGA_BYTES), JsonSchema.FieldType.Integer);

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
    public final JsonSchema.FieldType schemaFieldType;

    FormFieldType(int val, JsonSchema.FieldType schemaFieldType) {
        this(val, new IPropValProcessor() {
        }, (fieldProps) -> {
        }, schemaFieldType);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, JsonSchema.FieldType schemaFieldType) {
        this(val, valProcessor, (fieldProps) -> {
        }, schemaFieldType);
    }

    FormFieldType(int val, IPropValProcessor valProcessor, Consumer<JSONObject> appendExternalProps,
                  JsonSchema.FieldType schemaFieldType) {
        this.identity = val;
        this.valProcessor = valProcessor;
        this.appendExternalProps = appendExternalProps;
        this.schemaFieldType = schemaFieldType;
    }

    public int getIdentity() {
        return this.identity;
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
            this(cmeta.getName(), cmeta.getName(), true // !cmeta.isDisable()
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
