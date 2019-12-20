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
package com.qlangtech.tis.solrj.extend;

import java.io.File;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient.SimpleQueryResult;

/* *
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
