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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SqlFunctionCollectors {

    private Map<String, SqlFunction> functions = null;

    public SqlFunction register(SqlFunction function) {
        if (functions == null) {
            functions = new HashMap<String, SqlFunction>();
        }
        return functions.put(function.getPlaceHolderName(), function);
    }

    public String parseSql(String sql) {
        Iterator<String> i = SqlUtils.parseFunctions(sql);
        if (i == null) {
            return sql;
        }
        while (i.hasNext()) {
            String funcName = i.next();
            SqlFunction function = functions.get(funcName);
            if (function == null) {
                throw new RuntimeException("没有定义的SQL的Function  ==> " + funcName);
            }
            String placeHolderName = function.getPlaceHolderName();
            String value = function.getValue();
            sql = sql.replace(SqlUtils.PLACE_HOLDER_CHAR + placeHolderName + SqlUtils.PLACE_HOLDER_CHAR, value);
        }
        return sql;
    }
}
