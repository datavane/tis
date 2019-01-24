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
package com.qlangtech.tis.solrdao;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppCriteria {

    private final SolrQuery solrQuery;

    private List<Criteria> oredCriteria;

    public AppCriteria(SolrQuery solrQuery) {
        super();
        this.solrQuery = solrQuery;
    // this.oredCriteria = new ArrayList<Criteria>();
    }

    public AppCriteria() {
        this(new SolrQuery());
    }

    public Criteria createCriteria() {
        Criteria c = null;
        if (oredCriteria == null) {
            oredCriteria = new ArrayList<Criteria>();
            c = new Criteria();
            oredCriteria.add(c);
        } else {
            c = new Criteria();
        }
        return c;
    }

    public void or(Criteria criteria) {
        if (oredCriteria == null) {
            throw new IllegalStateException("oredCriteria can not be null");
        }
        oredCriteria.add(criteria);
    }

    public static class Criteria {

        private final StringBuffer criteria;

        private boolean addParam = false;

        public Criteria() {
            super();
            this.criteria = new StringBuffer("(");
        }

        public Criteria andIdEqualTo(Integer value) {
            appendAND();
            criteria.append("id:").append(value);
            return this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            appendAND();
            criteria.append("id:[ " + value1 + " TO " + value2 + " ]");
            return this;
        }

        private void appendAND() {
            if (addParam) {
                criteria.append("AND ");
            }
            addParam = true;
        }

        public String toSolrQuery() {
            return criteria.append(")").toString();
        }
    }
}
