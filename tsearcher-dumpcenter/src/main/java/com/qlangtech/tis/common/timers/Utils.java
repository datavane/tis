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
package com.qlangtech.tis.common.timers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 时间表达式的解析工具类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Utils {

    public static final Pattern HHMMSS_P = Pattern.compile("(\\d*?):(\\d*?):(\\d*)");

    public static final Pattern DDHHMMSS_P = Pattern.compile("(\\d*?) (\\d*?):(\\d*?):(\\d*)");

    /*
	 * {name expression}
	 */
    public static TimerExp parseToExp(String str) throws TimerExpressionException {
        if (str.startsWith("{") && str.endsWith("}")) {
            String[] parts = str.substring(1, str.length() - 1).split(" ");
            if (parts == null || parts.length < 2) {
                throw new TimerExpressionException("Error Expression Format. {" + str + "}");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                sb.append(parts[i]).append(" ");
            }
            return new TimerExp(parts[0], sb.toString().trim());
        } else {
            throw new TimerExpressionException("Error Expression Format. {" + str + "}");
        }
    }

    public static void main(String[] args) throws TimerExpressionException {
        Utils.parseToExp("{permonth 2 12:00:00}");
    }

    /*
	 * dayofweek hh:mm:ss
	 */
    public static Dhms parseToDhms(String expression) throws TimerExpressionException {
        Matcher m = DDHHMMSS_P.matcher(expression.trim());
        int day = -1;
        int hr = -1;
        int min = -1;
        int sec = -1;
        if (m.find()) {
            try {
                day = Integer.parseInt(m.group(1));
                hr = Integer.parseInt(m.group(2));
                min = Integer.parseInt(m.group(3));
                sec = Integer.parseInt(m.group(4));
            } catch (Exception e) {
            }
            if ((sec < 0 || sec > 59) || (min < 0 || min > 59) || (hr < 0 || hr > 23)) {
                throw new TimerExpressionException("Error Expression Format. {" + expression + "}");
            }
            return new Dhms(day, hr, min, sec);
        } else {
            throw new TimerExpressionException("Error Expression Format. {" + expression + "}");
        }
    }

    /*
	 * hh:mm:ss
	 */
    public static Hms parseToHMS(String expression) throws TimerExpressionException {
        Matcher m = HHMMSS_P.matcher(expression.trim());
        int hr = -1;
        int min = -1;
        int sec = -1;
        if (m.find()) {
            try {
                hr = Integer.parseInt(m.group(1));
                min = Integer.parseInt(m.group(2));
                sec = Integer.parseInt(m.group(3));
            } catch (Exception e) {
            }
            if ((sec < 0 || sec > 59) || (min < 0 || min > 59) || (hr < 0 || hr > 23)) {
                throw new TimerExpressionException("Error Expression Format. {" + expression + "}");
            }
            return new Hms(hr, min, sec);
        } else {
            throw new TimerExpressionException("Error Expression Format. {" + expression + "}");
        }
    }

    public static class TimerExp {

        public String name;

        public String expression;

        public TimerExp(String name, String expression) {
            super();
            this.name = name;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "TimerExp [expression=" + expression + ", name=" + name + "]";
        }
    }

    public static class Hms {

        public int hr;

        public int min;

        public int sec;

        public Hms() {
        }

        public Hms(int hr, int min, int sec) {
            super();
            this.hr = hr;
            this.min = min;
            this.sec = sec;
        }

        @Override
        public String toString() {
            return "Hms [hr=" + hr + ", min=" + min + ", sec=" + sec + "]";
        }
    }

    public static class Dhms extends Hms {

        public int day;

        public Dhms() {
        }

        public Dhms(int day, int hr, int min, int sec) {
            super(hr, min, sec);
            this.day = day;
        }

        @Override
        public String toString() {
            return "Dhms [day=" + day + ", hr=" + hr + ", min=" + min + ", sec=" + sec + ", toString()=" + super.toString() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
        }
    }
}
