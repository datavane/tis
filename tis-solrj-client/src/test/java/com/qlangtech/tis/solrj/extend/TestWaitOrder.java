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
package com.qlangtech.tis.solrj.extend;

import java.io.File;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient.SimpleQueryResult;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestWaitOrder extends BasicTestCase {

    public void testQuery() throws Exception {
        SolrQuery query = new SolrQuery();
        query.setRows(0);
        StringBuffer q = new StringBuffer();
        q.append("entity_id:00029187");
        q.append(" AND NOT {!terms f=menu_id}00029187504a866c015069a7742f722b,00029187514170f30151427b1fb97a56,000291874dddc80f014dff9064b55ca4");
        q.append(" AND customerregister_id:351bb278d7bf4512886b0811aa628cb0");
        q.append(" AND ((order_kind:2 AND {!terms f=order_status}4,5,6,7) OR ({!terms f=order_kind}3,4 AND order_status:4 ))");
        q.append(" AND NOT name:(纸巾 OR 米饭 OR 一品锅 OR 餐具)");
        query.setQuery(q.toString());
        System.out.println(query.toString());
        query.setFacet(true);
        query.addFacetField("menu_id");
        query.setFacetLimit(10);
        query.setFacetSort("count");
        SimpleQueryResult<Object> result = this.client.query("search4waitInstance", "00029187", query, Object.class);
        System.out.println("found:" + result.getNumberFound());
        FacetField facetfile = result.getResponse().getFacetField("menu_id");
        if (facetfile == null) {
            throw new IllegalStateException("result facetfield can not be null");
        }
        for (Count count : facetfile.getValues()) {
            System.out.println("menuid:" + count.getName() + ",count:" + count.getCount());
        }
    }
}
