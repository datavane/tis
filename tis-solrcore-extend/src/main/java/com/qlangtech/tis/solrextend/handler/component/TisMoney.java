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
package com.qlangtech.tis.solrextend.handler.component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
