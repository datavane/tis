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

import org.apache.solr.client.solrj.SolrQuery;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTestCase extends TestCase {

    protected TisCloudSolrClient client = null;

    protected SolrQuery query = null;

    protected long start;

    protected long end;

    protected String zkHost = "zk1.qlangtech-daily.com:2181,zk2.qlangtech-daily.com:2181,zk3.qlangtech-daily.com:2181/tis/cloud";

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
