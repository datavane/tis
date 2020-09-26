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
