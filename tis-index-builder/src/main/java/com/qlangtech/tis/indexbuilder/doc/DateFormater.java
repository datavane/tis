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
package com.qlangtech.tis.indexbuilder.doc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 日期格式转化类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DateFormater {

    public static final String FROM_TO_SEPERATOR = "->";

    private Map<String, String> formats;

    private Map<String, DateFormatPair> dateFormats;

    private String defaultDate;

    /**
     * 使用前一定要调用该初始化方法，请配置为Spring的init-method属性
     */
    public void init() {
        Set<String> columnNames = formats.keySet();
        if (dateFormats == null) {
            dateFormats = new HashMap<String, DateFormatPair>(formats.size());
        }
        for (String columnName : columnNames) {
            String formatStr = formats.get(columnName);
            String[] fromToStr = formatStr.split(FROM_TO_SEPERATOR);
            String from = fromToStr[0].trim();
            String to = fromToStr[1].trim();
            dateFormats.put(columnName, new DateFormatPair(from, to));
        }
    }

    // @Override
    // public String getDesc() {
    // return "DateFormaters";
    // }
    public static void main(String[] args) throws InterruptedException {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss.0");
        for (int i = 0; i < 100; i++) {
            Thread.sleep(10);
            System.out.println(f.format(new Date()));
        }
    }

    public class DateFormatPair {

        private String fromDateStr, toDateStr;

        public ThreadLocal<DateFormat> fromLocal = new ThreadLocal<DateFormat>();

        public ThreadLocal<DateFormat> toLocal = new ThreadLocal<DateFormat>();

        public DateFormatPair(String fromDateStr, String toDateStr) {
            this.fromDateStr = fromDateStr;
            this.toDateStr = toDateStr;
        }

        public DateFormat getFromDateFormat() {
            DateFormat format = (DateFormat) fromLocal.get();
            try {
                if (format == null || (!((SimpleDateFormat) format).toPattern().equals(this.getFromDateStr()))) {
                    format = new SimpleDateFormat(this.getFromDateStr());
                    fromLocal.set(format);
                }
            } catch (Exception ex) {
                throw new RuntimeException("获取fromDateFormat失败", ex);
            }
            return format;
        }

        public DateFormat getToDateFormat() {
            DateFormat format = (DateFormat) toLocal.get();
            try {
                if (format == null || (!((SimpleDateFormat) format).toPattern().equals(this.getToDateStr()))) {
                    format = new SimpleDateFormat(this.getToDateStr());
                    toLocal.set(format);
                }
            } catch (Exception ex) {
                throw new RuntimeException("获取toDateFormat失败", ex);
            }
            return format;
        }

        public String getFromDateStr() {
            return fromDateStr;
        }

        public String getToDateStr() {
            return toDateStr;
        }
    }

    public Map<String, String> getFormats() {
        return formats;
    }

    public void setFormats(Map<String, String> formats) {
        this.formats = formats;
    }

    public Map<String, DateFormatPair> getDateFormats() {
        return dateFormats;
    }

    public void setDateFormats(Map<String, DateFormatPair> dateFormats) {
        this.dateFormats = dateFormats;
    }

    public String getDefaultDate() {
        return defaultDate;
    }

    public void setDefaultDate(String defaultDate) {
        this.defaultDate = defaultDate;
    }
}
