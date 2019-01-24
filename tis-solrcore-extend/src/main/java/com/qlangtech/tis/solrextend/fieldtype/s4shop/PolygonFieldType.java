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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 通过用户传过来的点集合构建WKT可识别的字符串内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PolygonFieldType extends BaseShapeFieldType {

    static final Logger logger = LoggerFactory.getLogger(PolygonFieldType.class);

    static final String KEY_LAT = "latitude";

    static final String KEY_LONGITUDE = "longitude";

    static final String POLYGON_PREFIX = "POLYGON((";

    @Override
    protected StringBuffer buildShapLiteria(Object val) {
        JSONObject point;
        JSONArray array = null;
        try {
            point = null;
            JSONTokener t = new JSONTokener(String.valueOf(val));
            array = new JSONArray(t);
        } catch (JSONException e) {
            logger.warn("field{}", val, e);
            return null;
        }
        int len = array.length();
        if (len < 3) {
            // 多边形至少要三个点以上
            return null;
        }
        // POLYGON (( 120.1596 30.2447, 120.1595 30.3447, 120.1695 30.2447,120.1596 //
        // 30.2447))
        StringBuffer buffer = new StringBuffer(POLYGON_PREFIX);
        for (int i = 0; i < len; i++) {
            point = array.getJSONObject(i);
            buffer.append(point.get(KEY_LONGITUDE)).append(" ").append(point.get(KEY_LAT));
            buffer.append(",");
        }
        // 形成一个环
        point = array.getJSONObject(0);
        buffer.append(point.get(KEY_LONGITUDE)).append(" ").append(point.get(KEY_LAT));
        buffer.append(" ))");
        return buffer;
    }

    @Override
    protected boolean isShapeLiteria(Object val) {
        return String.valueOf(val).startsWith(POLYGON_PREFIX);
    }
}
