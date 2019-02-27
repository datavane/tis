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
package com.qlangtech.tis.wangjubao.jingwei.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.wangjubao.jingwei.IAliasProcess;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AliasProcessImpl implements IAliasProcess {

    // 2012-03-26 00:00:00
    private static final Pattern datetimeFormat = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2})");

    private static final Pattern datetimeYYYYMMddFormat = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

    private static final Logger log = LoggerFactory.getLogger(AliasProcessImpl.class);

    protected String fmtStrTime(String value) {
        return formatStrTime(value);
    }

    public static void main(String[] args) {
        AliasProcessImpl impl = new AliasProcessImpl() {

            @Override
            public Object process(Map<String, String> valuesStore) {
                return null;
            }
        };
        System.out.println(impl.dateYYYYMMddHHmmss("1414673054933"));
    }

    private static final ThreadLocal<SimpleDateFormat> formatYYYYmmdd = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    public static String formatYYYYmmdd(Date date) {
        return formatYYYYmmdd.get().format(date);
    }

    private static final ThreadLocal<SimpleDateFormat> formatYYYYMMddHHmmss = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    protected String dateYYYYmmdd(long unixtime) {
        return formatYYYYmmdd.get().format(new Date(unixtime));
    }

    protected String dateYYYYmmdd(String time) {
        return yyyymmdd(time);
    }

    public static String yyyymmdd(String time) {
        if (StringUtils.isBlank(time)) {
            return DEFAULT_START_TIME_yyyyMMdd;
        }
        Matcher m = datetimeYYYYMMddFormat.matcher(time);
        if (m.matches()) {
            return m.group(1) + m.group(2) + m.group(3);
        }
        return time;
    }

    protected String dateYYYYMMddHHmmss(long unixtime) {
        return formatYYYYMMddHHmmss.get().format(new Date(unixtime));
    }

    private static final String DEFAULT_START_TIME = "19700000000000";

    private static final String DEFAULT_START_TIME_yyyyMMdd = "19700000";

    protected String dateYYYYMMddHHmmss(String unixtime) {
        if (StringUtils.isBlank(unixtime)) {
            return DEFAULT_START_TIME;
        }
        try {
            return formatYYYYMMddHHmmss.get().format(new Date(Long.parseLong(unixtime)));
        } catch (NumberFormatException e) {
            log.error("unixtime:" + unixtime, e);
            return DEFAULT_START_TIME;
        }
    }

    protected String trimLeft0(Serializable val) {
        try {
            return String.valueOf(Integer.valueOf(String.valueOf(val)));
        } catch (Throwable e) {
        }
        return "0";
    }

    /**
     * @param val
     */
    public static String formatStrTime(Serializable val) {
        Matcher m = datetimeFormat.matcher(String.valueOf(val));
        if (m.matches()) {
            return m.group(1) + m.group(2) + m.group(3) + m.group(4) + m.group(5) + m.group(6);
        }
        // "19700000000000"; // String.valueOf(val);
        return String.valueOf(val);
    }
}
