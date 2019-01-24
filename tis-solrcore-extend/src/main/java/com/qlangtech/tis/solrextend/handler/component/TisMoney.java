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
package com.qlangtech.tis.solrextend.handler.component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisMoney {

    // 分
    private int fen = 0;

    private static final Logger logger = LoggerFactory.getLogger(TisMoney.class);

    private static final String[] ZERO_ARRAY = new String[] { "00", "0", StringUtils.EMPTY };

    public static TisMoney create() {
        return new TisMoney();
    }

    public static TisMoney create(String doubleValue) {
        return new TisMoney(doubleValue);
    }

    private TisMoney() {
        this.fen = 0;
    }

    private static final Pattern DECI_PATTERN = Pattern.compile("(\\-?)(\\d+)(\\.(\\d{1,2}))?");

    private TisMoney(String doubleValue) {
        Matcher m = DECI_PATTERN.matcher(doubleValue);
        if (m.find()) {
            String decimal = m.group(4);
            this.fen = Integer.parseInt(m.group(2) + StringUtils.trimToEmpty(m.group(4)) + ZERO_ARRAY[StringUtils.length(decimal)]);
            if ("-".equals(m.group(1))) {
                this.fen *= -1;
            }
            return;
        }
        logger.error("doubleValue:" + doubleValue + " is not match pattern:" + DECI_PATTERN);
    }

    public static void main(String[] args) {
        Matcher m = DECI_PATTERN.matcher("69.24");
        if (m.matches()) {
            // this.fen = Integer.parseInt(m.group(1) + m.group(2));
            System.out.println("groupsize:" + m.groupCount());
            System.out.println(m.group(1));
            System.out.println(m.group(2));
            System.out.println(m.group(3));
        }
        System.out.println("================================");
        m = DECI_PATTERN.matcher("69");
        if (m.matches()) {
            // this.fen = Integer.parseInt(m.group(1) + m.group(2));
            System.out.println("groupsize:" + m.groupCount());
            System.out.println(m.group(1));
            System.out.println(m.group(2));
            System.out.println(m.group(3));
        }
    // TisMoney v = TisMoney.create("122.03");
    // v.addCoefficient(new BigDecimal("0.03"));
    // System.out.println(v.format());
    }

    public void addCoefficient(BigDecimal value) {
        this.fen *= value.doubleValue();
    }

    public void addCoefficient(TisMoney value) {
        this.fen *= value.fen;
        this.fen /= 100;
    }

    public int getFen() {
        return this.fen;
    }

    public int intValue() {
        return (fen / 100);
    }

    public void add(TisMoney fen) {
        this.fen += fen.fen;
    }

    public String format() {
        int decim = Math.abs(fen % 100);
        return ((fen < 0) ? "-" : "") + (Math.abs(fen) / 100) + "." + ((decim < 10 && decim > 0) ? "0" : "") + decim;
    }

    @Override
    public String toString() {
        return format();
    }
}
