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

import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent.SortField;
import com.qlangtech.tis.solrj.extend.search4supplyUnionTabs.UnionTab;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/*
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
        zkHost = "zk1.2dfire-daily.com:2181,zk2.2dfire-daily.com:2181,zk3.2dfire-daily.com:2181/tis/cloud";
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
