/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
