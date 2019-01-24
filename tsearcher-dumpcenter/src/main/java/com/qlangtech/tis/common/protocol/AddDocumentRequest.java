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

import java.io.Serializable;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AddDocumentRequest implements RouteValueSupport, Serializable {

    private static final long serialVersionUID = -3948440619130257371L;

    /**
     * @uml.property  name="solrDoc"
     * @uml.associationEnd
     */
    public SolrInputDocument solrDoc;

    /**
     * @uml.property  name="routeValue"
     */
    public Map<String, String> routeValue;

    public AddDocumentRequest() {
    }

    public AddDocumentRequest(SolrInputDocument solrDoc, Map<String, String> routeValue) {
        super();
        this.solrDoc = solrDoc;
        this.routeValue = routeValue;
    }

    /**
     * @return
     * @uml.property  name="routeValue"
     */
    @Override
    public Map<String, String> getRouteValue() {
        return routeValue;
    }

    /**
     * @return
     * @uml.property  name="solrDoc"
     */
    public SolrInputDocument getSolrDoc() {
        return solrDoc;
    }

    /**
     * @param solrDoc
     * @uml.property  name="solrDoc"
     */
    public void setSolrDoc(SolrInputDocument solrDoc) {
        this.solrDoc = solrDoc;
    }

    /**
     * @param routeValue
     * @uml.property  name="routeValue"
     */
    public void setRouteValue(Map<String, String> routeValue) {
        this.routeValue = routeValue;
    }
}
