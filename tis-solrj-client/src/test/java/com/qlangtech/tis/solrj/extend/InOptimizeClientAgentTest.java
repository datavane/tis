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
public class InOptimizeClientAgentTest extends TestCase {

    String expr;

    private String zkHost;

    private InOptimizeClientAgent agent;

    private TisCloudSolrClient client;

    public InOptimizeClientAgentTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(InOptimizeClientAgentTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        zkHost = "zk1.2dfire-daily.com:2181,zk2.2dfire-daily.com:2181,zk3.2dfire-daily.com:2181/tis/cloud\n";
        client = new TisCloudSolrClient(zkHost);
        agent = new InOptimizeClientAgent(client);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCountStream() throws Exception {
        expr = "count(\n" + "  innerJoin(\n" + "    searchExtend(search4supplyGoods, qt=/export, _route_=99928910, q=\"entity_id:99928910 AND is_valid:1\", fl=\"id\", sort=id asc),\n" + "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928910, q=entity_id:99928910 AND is_valid:1 AND table_name:warehouse_goods AND warehouse_id:9992891057d0c4340157d0c434ab0000, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "  on=id=goods_id)\n" + ")";
        System.out.println(expr);
        System.out.println(agent.getCountStreamValue(expr));
    }

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
        int i = 1;
    }

    public void testDeleteTopNField() throws Exception {
        expr = "top(n=20,\n" + "innerJoin(\n" + "searchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField sort=create_time,asc,long}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "on=id=goods_id),\n" + "sort=create_time asc\n" + ")";
    }

    public void testAnotherSortQuery() throws Exception {
        expr = "top(n=20,\n" + "\tinnerJoin(\n" + "\t\tunique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926227, q=_query_:\"{!topNField sort=create_time,desc,long}entity_id:99926227\", fl=\"goods_id, docid:[docid f=docid], create_time\", sort=goods_id asc), over=goods_id),\n" + "\t\tsearchExtend(search4supplyGoods, qt=/export, _route_=99926227, q=entity_id:99926227, fl=\"id\", sort=id asc),\n" + "\ton=goods_id=id),\n" + "sort=\"create_time desc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99926227");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields(new String[] { "id", "entity_id", "create_time" });
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        int i = 1;
    }

    public void testSearch() throws Exception {
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        // expr = "searchExtend(search4supplyGoods, qt=/export, _route_=99926350, q=entity_id:99926350, fl=id, sort=id asc)";
        expr = "searchExtend(search4supplyGoods, qt=/export, _route_=99928910, q=_query_:\"{!topNField sort=create_time,desc,long}entity_id:99928910 AND is_valid:1\", fl=\"id, create_time, docid:[docid f=docid]\", sort=id asc)";
        TupleStream joinStream = agent.query(expr);
        joinStream.open();
        int cnt = 0;
        while (true) {
            Tuple tuple = joinStream.read();
            if (tuple.EOF) {
                break;
            } else {
                for (Object key : tuple.getMap().keySet()) {
                    System.out.println(String.valueOf(key) + ":" + String.valueOf(tuple.getString(key)));
                }
                System.out.println(cnt++);
            }
        }
        joinStream.close();
    }

    public void testException() throws Exception {
        expr = "\n" + "top(n=20,\n" + "  innerJoin(\n" + "    searchExtend(search4supplyGoods, qt=/export, _route_=99926454, q=_query_:\"{!topNField sort=category_inner_code,asc,string;bar_code,asc,string}entity_id:99926454\", fl=\"id, category_inner_code,bar_code, docid:[docid f=docid]\", sort=id asc),\n" + "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926454, q=entity_id:99926454 AND is_valid:1 AND table_name:stock_change_log AND op_time:[20170308000000000 TO 20170308240000000], fl=\"goods_id\", sort=goods_id asc), over=goods_id),\n" + "  on=id=goods_id),\n" + "  sort=\"category_inner_code asc,bar_code asc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99926454");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields(new String[] { "id", "entity_id" });
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        System.out.println(list);
        int i = 1;
    }

    public void testException1() throws Exception {
        expr = "\n" + "top(n=20,\n" + "  innerJoin(\n" + "    searchExtend(search4supplyGoods, qt=/export, _route_=99929865, q=entity_id:99929865 AND is_valid:1 AND (bar_code:*海天苹果* OR name:海天苹果 OR spell:*海天苹果*), fl=\"id\", sort=id asc),\n" + "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99929865, q=_query_:\"{!topNField sort=create_time,desc,long}entity_id:99929865 AND is_valid:1 AND table_name:supplier_goods AND supplier_id:999298655b66cd3c015b6a288163008c\", fl=\"goods_id, create_time, docid:[docid f=docid]\", sort=goods_id asc), over=goods_id),\n" + "  on=id=goods_id),\n" + "  sort=\"create_time desc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99929865");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("*");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        System.out.println(list);
        int i = 1;
    }

    public void testException2() throws Exception {
        expr = "top(n=200,\n" + "  innerJoin(\n" + "    searchExtend(search4supplyGoods, qt=/export, _route_=99926350, q=entity_id:99926350 AND " + "is_valid:1 AND self_entity_ids:99926478, fl=\"id\", sort=id asc),\n" + "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926350, " + "q=_query_:\"{!topNField sort=supplier_id,desc,string}entity_id:99926350 AND is_valid:1 AND table_name:int_day_consume AND " + "curr_date:[20170523 TO 20170622] AND self_entity_id:99926478\", fl=\"goods_id, supplier_id, " + "docid:[docid f=docid]\", sort=goods_id asc), over=goods_id),\n" + "  on=id=goods_id),\n" + "  sort=\"supplier_id desc, goods_id asc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99926350");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("id", "entity_id");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(200);
        System.out.println(expr);
        AtomicLong l = new AtomicLong(0);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        list.forEach(unionTab -> System.out.println(l.incrementAndGet() + " " + unionTab.getDocId() + " " + unionTab.getId() + " "));
        System.out.println(list);
        int i = 1;
    }

    public void testException3() throws Exception {
        expr = "top(n=20,\n" + "  innerJoin(\n" + "    searchExtend(search4supplyGoods, qt=/export, _route_=99926350, q=entity_id:99926350 AND " + "is_valid:1 AND self_entity_ids:99926478, fl=\"id\", sort=id asc),\n" + "    unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926350, " + "q=_query_:\"{!topNField sort=supplier_id,desc,string;goods_id,asc,string afterId=213024 " + "afterValue=;99926350583d6ed30158710025d6548e}entity_id:99926350 AND is_valid:1 AND table_name:int_day_consume AND " + "curr_date:[20170523 TO 20170622] AND self_entity_id:99926478\", fl=\"goods_id, supplier_id, " + "docid:[docid f=docid]\", sort=goods_id asc), over=goods_id),\n" + "  on=id=goods_id),\n" + "  sort=\"supplier_id desc, goods_id asc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99926350");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("*");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        AtomicLong l = new AtomicLong(0);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        list.forEach(unionTab -> System.out.println(l.incrementAndGet() + " " + unionTab.getDocId() + " " + unionTab.getId() + " "));
        System.out.println(list);
        int i = 1;
    }

    public void testSingle() throws Exception {
        long start, end;
        start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            expr = "top(n=9,\n" + "innerJoin(\n" + "searchExtend(search4supplyGoods, qt=/export, _route_=99926456, q=_query_:\"{!topNField sort=create_time,asc,long}entity_id:99926456\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926456, q=entity_id:99926456, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "on=id=goods_id),\n" + "sort=create_time asc\n" + ")";
            InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
            agent.setQuery(expr);
            // 索引分库键值
            agent.setSharedKey("99926456");
            // 索引名称
            agent.setCollection("search4supplyGoods");
            // 需要返回的fields
            agent.setFields("id", "entity_id", "create_time");
            // 反查时的主键
            agent.setPrimaryField("id");
            // 最后返回结果的条数
            agent.setRows(9);
            SortField[] sortFields = new SortField[] {};
            List<UnionTab> list = agent.query(UnionTab.class, sortFields);
        }
        end = System.currentTimeMillis();
        System.out.println("10 times spend " + (end - start) + "millis");
        start = end;
        expr = "top(n=20,\n" + "innerJoin(\n" + "searchExtend(search4supplyGoods, qt=/export, _route_=99926456, q=_query_:\"{!topNField sort=create_time,asc,long}entity_id:99926456\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99926456, q=entity_id:99926456, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "on=id=goods_id),\n" + "sort=create_time asc\n" + ")";
        expr = InOptimizeClientAgent.topNFieldRowsUnlimited(expr);
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("99926456");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("id", "entity_id", "create_time");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        SortField[] sortFields = new SortField[] {};
        List<UnionTab> list = agent.query(UnionTab.class, sortFields);
        end = System.currentTimeMillis();
        System.out.println("1 time spend " + (end - start) + "millis");
        int i = 1;
    }

    public void testException4() throws Exception {
        expr = "top(n=20,\n" + "        innerJoin(\n" + "                searchExtend(search4supplyGoods, qt=/export, _route_=00087361, q=entity_id:00087361 " + "AND is_valid:1 AND self_entity_ids:00087385, fl=\"id\", sort=id asc),\n" + "                unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=00087361, " + "q=_query_:\"{!topNField sort=supplier_id,desc,string;goods_id,desc,string}entity_id:00087361 AND " + "is_valid:1 AND table_name:int_day_consume AND curr_date:[20170527 TO 20170626] AND " + "self_entity_id:00087385\", fl=\"goods_id, supplier_id,goods_id, docid:[docid f=docid]\", " + "sort=goods_id asc), over=goods_id),\n" + "        on=id=goods_id),\n" + "        sort=\"supplier_id desc,goods_id desc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("00087385");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("*");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        AtomicLong l = new AtomicLong(0);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        String supplyerId = list.getField("supplier_id");
        String goodsId = list.getField("goods_id");
        String docId = list.getField("docid");
        System.out.println("supplyerId:" + supplyerId);
        System.out.println("goodsId:" + goodsId);
        System.out.println("docId:" + docId);
        list.forEach(unionTab -> System.out.println(l.incrementAndGet() + " " + unionTab.getDocId() + " " + unionTab.getId() + " "));
        System.out.println(list);
        int i = 1;
    }

    public void testException5() throws Exception {
        expr = "top(n=20,\n" + "        innerJoin(\n" + "                searchExtend(search4supplyGoods, qt=/export, _route_=00087361, q=entity_id:00087361 " + "AND is_valid:1 AND self_entity_ids:00087385, fl=\"id\", sort=id asc),\n" + "                unique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=00087361, " + "q=_query_:\"{!topNField sort=supplier_id,desc,string;goods_id,desc,string afterId=145 " + "afterValue=;000873615ccaa433015ccec99b0f0610}entity_id:00087361 AND is_valid:1 AND " + "table_name:int_day_consume AND curr_date:[20170527 TO 20170626] AND self_entity_id:00087385\", " + "fl=\"goods_id, supplier_id,goods_id, docid:[docid f=docid]\", sort=goods_id asc), over=goods_id),\n" + "        on=id=goods_id),\n" + "        sort=\"supplier_id desc,goods_id desc\"\n" + ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
        agent.setQuery(expr);
        // 索引分库键值
        agent.setSharedKey("00087385");
        // 索引名称
        agent.setCollection("search4supplyGoods");
        // 需要返回的fields
        agent.setFields("*");
        // 反查时的主键
        agent.setPrimaryField("id");
        // 最后返回结果的条数
        agent.setRows(20);
        System.out.println(expr);
        AtomicLong l = new AtomicLong(0);
        SortField[] sortFields = new SortField[] {};
        InOptimizeClientAgent.StreamResultList<UnionTab> list = agent.query(UnionTab.class, sortFields);
        list.forEach(unionTab -> System.out.println(l.incrementAndGet() + " " + unionTab.getDocId() + " " + unionTab.getId() + " "));
        System.out.println(list);
        int i = 1;
    }
}
