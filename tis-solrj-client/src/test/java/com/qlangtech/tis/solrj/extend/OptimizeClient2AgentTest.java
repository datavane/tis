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

import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent.SortField;
import com.qlangtech.tis.solrj.extend.search4supplyUnionTabs.UnionTab;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InOptimizeClientAgent Tester.
 * @version 1.0
 * @since <pre>03/03/2017</pre>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class OptimizeClient2AgentTest extends TestCase {

    String expr;

    private String zkHost;

    private InOptimizeClientAgent agent;

    private TisCloudSolrClient client;

    public OptimizeClient2AgentTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(OptimizeClient2AgentTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        // 10.1.21.202:2181,10.1.21.201:2181,10.1.21.200:2181/tis/cloud
        zkHost = "zk1.qlangtech-daily.com:2181,zk2.qlangtech-daily.com:2181,zk3.qlangtech-daily.com:2181/tis/cloud";
        client = new TisCloudSolrClient(zkHost);
    }

    // public void testCountStream() throws Exception {
    // expr = "count(\n" +
    // "  innerJoin(\n" +
    // "    searchExtend(search4supplyGoods, qt=/export, _route_=99928910, q=\"entity_id:99928910 AND is_valid:1\", fl=\"id\", sort=id asc),\n" +
    // "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928910, q=entity_id:99928910 AND is_valid:1 AND table_name:warehouse_goods AND warehouse_id:9992891057d0c4340157d0c434ab0000, fl=goods_id, sort=goods_id asc), over=goods_id),\n" +
    // "  on=id=goods_id)\n" +
    // ")";
    // System.out.println(expr);
    // System.out.println(agent.getCountStreamValue(expr));
    // }
    public void testQuery() throws Exception {
        expr = "top(n=20,\n" + "innerJoin(\n" + "searchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField sort=create_time,asc,long afterId=11 afterValue=3333}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "on=id=goods_id),\n" + "sort=create_time asc\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(client);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99928370");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields(new String[] { "id", "entity_id", "create_time" });
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20000);
        System.out.println(expr);
        SortField[] sortFields = new SortField[] {};
        System.out.println("start query=================================");
        List<UnionTab> list = agent.query(UnionTab.class, sortFields);
        System.out.println("list.size():" + list.size());
        int i = 1;
    }
}
