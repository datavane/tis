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

import org.apache.solr.client.solrj.SolrQuery;
import junit.framework.TestCase;

/* *
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
