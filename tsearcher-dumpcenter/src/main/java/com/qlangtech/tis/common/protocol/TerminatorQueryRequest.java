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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorQueryRequest extends SolrQuery {

    private static final long serialVersionUID = -5750475678394136675L;

    /**
     * @uml.property  name="routeValues"
     */
    private List<Map<String, String>> routeValues = null;

    private static final String RQ = "rq";

    /**
     * 添加用来路由的值，由于可能有由多个字段才能路由到具体的某一个shard
     * 故此处路由的值用Map对象，此处的路由字段会有GroupRouter类进行具体的路由 操作
     *
     * @param value
     * @return
     */
    public TerminatorQueryRequest addRouteValue(Map<String, String> value) {
        if (routeValues == null) {
            routeValues = new ArrayList<Map<String, String>>();
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
    public List<Map<String, String>> getRouteValues() {
        return this.routeValues;
    }

    /**
     * @param routeValues
     * @uml.property  name="routeValues"
     */
    public void setRouteValues(List<Map<String, String>> routeValues) {
        this.routeValues = routeValues;
    }

    public SolrQuery addRangeQuery(String... rq) {
        this.add(RQ, rq);
        return this;
    }

    public String[] getRangeQuery() {
        return this.getParams(RQ);
    }
}
