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
package com.qlangtech.tis.common.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorDeleteRequest {

    /**
     * @uml.property  name="id"
     */
    private String id = null;

    /**
     * @uml.property  name="query"
     */
    private String query = null;

    /**
     * @uml.property  name="routeValues"
     */
    private List<Map<String, Object>> routeValues = null;

    /**
     * 添加用来路由的值，由于可能有由多个字段才能路由到具体的某一个shard
     * 故此处路由的值用Map对象，此处的路由字段会有GroupRouter类进行具体的路由 操作
     *
     * @param value
     * @return
     */
    public TerminatorDeleteRequest addRouteValue(Map<String, Object> value) {
        if (routeValues == null) {
            routeValues = new ArrayList<Map<String, Object>>();
        }
        routeValues.add(value);
        return this;
    }

    /**
     * 判断是否包含有路由字段
     *
     * @return
     */
    public boolean containsRouteValues() {
        return routeValues != null && !routeValues.isEmpty();
    }

    /**
     * @return
     * @uml.property  name="routeValues"
     */
    public List<Map<String, Object>> getRouteValues() {
        return this.routeValues;
    }

    /**
     * @return
     * @uml.property  name="id"
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     * @uml.property  name="id"
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     * @uml.property  name="query"
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     * @uml.property  name="query"
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @param routeValues
     * @uml.property  name="routeValues"
     */
    public void setRouteValues(List<Map<String, Object>> routeValues) {
        this.routeValues = routeValues;
    }
}
