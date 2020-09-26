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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.solr.common.SolrDocument;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestGet extends TestCase {

    private TisCloudSolrClient client = null;

    @Override
    protected void setUp() throws Exception {
        client = new TisCloudSolrClient("10.1.6.65,2181,10.1.6.67,2181,10.1.6.80,2181/tis/cloud");
    }

    public void testGet() throws Exception {
        SolrDocument document = client.getById("search4OrderInfo", "kkkk25", "00000241");
        Assert.assertNotNull(document);
        for (String name : document.getFieldNames()) {
            System.out.println("key:" + name + ",value:" + document.getFieldValue(name));
        }
    }
}
