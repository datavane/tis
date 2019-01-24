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
package com.qlangtech.tis.solrj.extend.router;

import java.util.Map;
import org.apache.solr.common.cloud.DocCollection;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RouterUtils {

    /**
     * 取得路由字段值
     *
     * @param acmd
     * @return AddUpdateCommand acmd
     */
    public static String getRouterValue(DocCollection col, IRouterValueGetter routerGetter) {
        return getRouterValue(col, getRouterFieldName(col), routerGetter);
    }

    public static String getRouterValue(DocCollection col, final String routerFieldName, IRouterValueGetter routerGetter) {
        String routerValue = routerGetter.get(routerFieldName);
        if (routerValue == null) {
            throw new IllegalStateException("routerFieldName can not get value from doc" + routerGetter.docDesc());
        }
        return String.valueOf(routerValue);
    }

    // baisui add 20150928 start
    /**
     * @param col
     * @return
     */
    @SuppressWarnings("all")
    private static String getRouterFieldName(DocCollection col) {
        Map routeMap = (Map) col.get("router");
        if (routeMap == null) {
            throw new IllegalStateException("prop router can not be null in doc Collection:" + col.getName());
        }
        String routeField = (String) routeMap.get("field");
        if (routeField == null) {
            throw new IllegalStateException("prop routeField can not be null in router prop :" + col.getName());
        }
        return routeField;
    }
}
