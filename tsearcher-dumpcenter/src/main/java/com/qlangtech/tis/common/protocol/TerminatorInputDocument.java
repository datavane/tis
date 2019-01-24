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

import org.apache.solr.common.SolrInputDocument;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorInputDocument {

    /**
     * @uml.property  name="solrInputDocument"
     * @uml.associationEnd
     */
    private SolrInputDocument solrInputDocument;

    /**
     * @uml.property  name="routeValue"
     */
    private Object routeValue = null;

    public TerminatorInputDocument() {
    }

    public TerminatorInputDocument(SolrInputDocument solrInputDocument) {
        this(solrInputDocument, null);
    }

    public TerminatorInputDocument(SolrInputDocument solrInputDocument, Object routeValue) {
        this.solrInputDocument = solrInputDocument;
        this.routeValue = routeValue;
    }

    /**
     * @return
     * @uml.property  name="solrInputDocument"
     */
    public SolrInputDocument getSolrInputDocument() {
        return solrInputDocument;
    }

    /**
     * @param solrInputDocument
     * @uml.property  name="solrInputDocument"
     */
    public void setSolrInputDocument(SolrInputDocument solrInputDocument) {
        this.solrInputDocument = solrInputDocument;
    }

    /**
     * @return
     * @uml.property  name="routeValue"
     */
    public Object getRouteValue() {
        return routeValue;
    }

    /**
     * @param routeValue
     * @uml.property  name="routeValue"
     */
    public void setRouteValue(Object routeValue) {
        this.routeValue = routeValue;
    }
}
