/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.data.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SqlUtils {

    public static final Pattern PARTTERN = Pattern.compile("\\$([A-Za-z0-9]*?)\\$", Pattern.DOTALL);

    public static final String PLACE_HOLDER_CHAR = "$";

    /**
     * 提取Sql中的占位符，每个占位符对应到一个具体的Function的实现
     *
     * @param sql
     * @return
     */
    public static Iterator<String> parseFunctions(String sql) {
        Set<String> res = null;
        Matcher matcher = PARTTERN.matcher(sql);
        while (matcher.find()) {
            // 如果存在Pattern的情况
            if (res == null) {
                res = new HashSet<String>();
            }
            // $tableName$
            res.add(matcher.group(1));
        }
        return res != null ? res.iterator() : null;
    }

    public static String parseDate(Date date) {
        final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return DF.format(date);
    }
}
