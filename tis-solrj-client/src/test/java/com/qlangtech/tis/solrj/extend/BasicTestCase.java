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

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTestCase extends TestCase {
    static {
       // AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    protected TisCloudSolrClient client = null;

    protected SolrQuery query = null;

    protected long start;

    protected long end;

    protected String zkHost = "192.168.28.200:2181/tis/cloud";

    /**
     */
    public BasicTestCase() {
        super();
    }

    /**
     * @param name
     */
    public BasicTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        // public TisCloudSolrClient(String zkHost //
        // , int socketTimeout, int connTimeout, int maxConnectionsPerHost, int
        // maxConnections) {
        client = new TisCloudSolrClient(zkHost, 5000, /* socketTimeout */
                5000, /* connTimeout */
                20, /* maxConnectionsPerHost */
                100);
        query = new SolrQuery();
        start = System.currentTimeMillis();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        end = System.currentTimeMillis();
        System.out.println("-------------cost time: " + (end - start) + "ms");
    }
}
