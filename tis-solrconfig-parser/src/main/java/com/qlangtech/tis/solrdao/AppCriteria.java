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
package com.qlangtech.tis.solrdao;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-7
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
