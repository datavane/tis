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
package com.qlangtech.tis.solrj.extend.router;

import java.util.Map;
import org.apache.solr.common.cloud.DocCollection;

/**
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
