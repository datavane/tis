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
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.PropertyType.PropVal;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月11日
 */
public enum FormFieldType {
    /**
     * 多选字段,目标属性样例：'List<String> cols'
     */
    MULTI_SELECTABLE(8),

    INPUTTEXT(1),
    /**
     * 有多个选项可以选择
     */
    SELECTABLE(6),
    /**
     * 密码
     */
    PASSWORD(7), // 支持文件上传
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
    }) //
    , TEXTAREA(2) //
    , DATE(3) //
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
    }),
    /**
     * 输入一个数字
     */
    INT_NUMBER(4) //
    , ENUM(5), DateTime(10, new IPropValProcessor() {
        final DateTimeFormatter isoFormat = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public Object processInput(Object instance, PropVal val) throws Exception {

            //  SimpleDateFormat dateTimeFormat = val.extraProp.getDateTimeFormat();
            LocalDateTime dateTime = LocalDateTime.parse((String) val.rawVal(), isoFormat);
            Instant ist = dateTime.atZone(TimeFormat.sysZoneId).toInstant();
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
    }

    ), DECIMAL_NUMBER(11);

    public static void main(String[] args) throws Exception {
        //  SimpleDateFormat dateTimeFormat =new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSS");
        //  System.out.println(  dateTimeFormat.parse("));


//        LocalDateTime dateTime = LocalDateTime.parse("2024-02-27T08:32:21.069Z", isoFormat);
//        System.out.println(dateTime);
//        System.out.println(TimeFormat.yyyyMMddHHmmss.format());
    }

    private final int identity;
    public final IPropValProcessor valProcessor;

    FormFieldType(int val) {
        this(val, new IPropValProcessor() {
        });
    }

    FormFieldType(int val, IPropValProcessor valProcessor) {
        this.identity = val;
        this.valProcessor = valProcessor;
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
        private boolean checked;

        private IMultiElement cmeta;

        public SelectedItem(String name, String value, boolean checked) {
            super(name, value);

            this.checked = checked;
        }

        public SelectedItem(IMultiElement cmeta) {
            this(cmeta.getName(), cmeta.getName(), true // !cmeta.isDisable()
            );
            this.cmeta = cmeta;
        }

        public IMultiElement getCmeta() {
            return cmeta;
        }

        public boolean isChecked() {
            return checked;
        }
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
