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
package com.qlangtech.tis.realtime.transfer.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.IPk;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultPk implements IPk {

    private final String uuid;

    private static final Pattern PK_PATTERN = Pattern.compile("[\\w+|\\d]+");

    public static void main(String[] args) {
        Matcher m = PK_PATTERN.matcher("12345Wwkolp");
        System.out.println(m.matches());
        m = PK_PATTERN.matcher("12345Wwk,olp");
        System.out.println(m.matches());
    }

    public DefaultPk(String uuid) {
        super();
        if (StringUtils.isEmpty(uuid)) {
            throw new IllegalArgumentException("pk can not be null");
        }
        Matcher m = PK_PATTERN.matcher(uuid);
        if (!m.matches()) {
            throw new IllegalArgumentException(uuid + " is not match pattern:" + PK_PATTERN);
        }
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public String getValue() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object obj) {
        return this.uuid.equals(((DefaultPk) obj).uuid);
    }

    @Override
    public String toString() {
        return this.uuid;
    }
}
