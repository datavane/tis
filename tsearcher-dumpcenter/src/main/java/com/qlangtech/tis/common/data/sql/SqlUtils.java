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
package com.qlangtech.tis.common.data.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SqlUtils {

    public static final Pattern PARTTERN = Pattern.compile("\\$([A-Za-z0-9]*?)\\$", Pattern.DOTALL);

    public static final String PLACE_HOLDER_CHAR = "$";

    /**
     * 提取Sql中的站位符，每个占位符对应到一个具体的Function的实现
     *
     * @param sql
     * @return
     */
    public static Iterator<String> parseFunctions(String sql) {
        Set<String> res = null;
        Matcher matcher = PARTTERN.matcher(sql);
        while (matcher.find()) {
            if (res == null) {
                res = new HashSet<String>();
            }
            res.add(matcher.group(1));
        }
        return res != null ? res.iterator() : null;
    }

    public static String parseDate(Date date) {
        final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return DF.format(date);
    }
}
