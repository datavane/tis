/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.common.data.processor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/*
 * 日期格式转化类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DateFormater implements DataProcessor {

    public static final String FROM_TO_SEPERATOR = "->";

    public static ThreadLocal<DateFormat> fromLocal = new ThreadLocal<DateFormat>();

    public static ThreadLocal<DateFormat> toLocal = new ThreadLocal<DateFormat>();

    /**
     * @uml.property  name="formats"
     */
    private Map<String, String> formats;

    /**
     * @uml.property  name="dateFormats"
     */
    private Map<String, DateFormatPair> dateFormats;

    /**
     * @uml.property  name="defaultDate"
     */
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

    @Override
    public String getDesc() {
        return "DateFormaters";
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss.0");
        for (int i = 0; i < 100; i++) {
            Thread.sleep(10);
            System.out.println(f.format(new Date()));
        }
    }

    @Override
    public ResultCode process(Map<String, String> map) throws DataProcessException {
        Set<String> columnNames = dateFormats.keySet();
        for (String columName : columnNames) {
            DateFormatPair pair = dateFormats.get(columName);
            try {
                String value = map.get(columName);
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                Date srcDate = pair.getFromDateFormat().parse(value);
                String destDate = pair.getToDateFormat().format(srcDate);
                map.put(columName, destDate);
            } catch (ParseException e) {
                map.put(columName, defaultDate);
            }
        }
        return ResultCode.SUC;
    }

    /**
     * @author  yingyuan.lyq
     */
    public class DateFormatPair {

        /**
         * @uml.property  name="fromDateStr"
         */
        private String fromDateStr;

        /**
         * @uml.property  name="toDateStr"
         */
        private String toDateStr;

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

        /**
         * @return
         * @uml.property  name="fromDateStr"
         */
        public String getFromDateStr() {
            return fromDateStr;
        }

        /**
         * @return
         * @uml.property  name="toDateStr"
         */
        public String getToDateStr() {
            return toDateStr;
        }
    }

    /**
     * @return
     * @uml.property  name="formats"
     */
    public Map<String, String> getFormats() {
        return formats;
    }

    /**
     * @param formats
     * @uml.property  name="formats"
     */
    public void setFormats(Map<String, String> formats) {
        this.formats = formats;
    }

    /**
     * @return
     * @uml.property  name="dateFormats"
     */
    public Map<String, DateFormatPair> getDateFormats() {
        return dateFormats;
    }

    /**
     * @param dateFormats
     * @uml.property  name="dateFormats"
     */
    public void setDateFormats(Map<String, DateFormatPair> dateFormats) {
        this.dateFormats = dateFormats;
    }

    /**
     * @return
     * @uml.property  name="defaultDate"
     */
    public String getDefaultDate() {
        return defaultDate;
    }

    /**
     * @param defaultDate
     * @uml.property  name="defaultDate"
     */
    public void setDefaultDate(String defaultDate) {
        this.defaultDate = defaultDate;
    }
}
