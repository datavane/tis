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
package com.qlangtech.tis.solrextend.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.search.QueryParsing;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestEmbeddedSolrServer extends TestCase {

    public static final EmbeddedSolrServer server;

    public static final File solrHome;

    public TestEmbeddedSolrServer() {
        super();
    }

    public TestEmbeddedSolrServer(String name) {
        super(name);
    }

    static {
        // solrHome = new File("D:\\workspace\\solrhome");
        solrHome = new File("D:\\solr\\solrhome");
        // CoreContainer.Initializer initializer = new
        // CoreContainer.Initializer();
        // CoreContainer coreContainer = initializer.initialize();
        // server = new EmbeddedSolrServer(solrHome.toPath(), "pinyin");
        // server = new EmbeddedSolrServer(solrHome.toPath(), "s4personas");
        // server = new EmbeddedSolrServer(solrHome.toPath(),
        // "supplyStatementAudit");
        server = new EmbeddedSolrServer(solrHome.toPath(), "shop");
    }

    private static final String FIELD_COORDINATE = "coordinate";

    // public void testBitwiseQuery() throws Exception {
    // SolrQuery query = new SolrQuery();
    // query.setQuery("{!lucene q.op=AND}service_flag:15");
    //
    // QueryResponse r = server.query("shop", query);
    //
    // for (SolrDocument doc : r.getResults()) {
    // System.out.println("id:" + doc.getFieldValue("id") + ",service_flag:" +
    // doc.getFieldValue("service_flag"));
    // }
    //
    // }
    public void testHight() throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", "98765");
        doc.setField("contact_id", "9527");
        doc.setField("contact_account", "894054764");
        doc.setField("extend_info", "{k_nickname:\"tnk 松哥\",k_phone:\"8618582185643\"}");
        doc.setField("_version_", "1");
        server.add("shop", doc);
        // server.add("shop", doc);
        server.commit();
        Thread.sleep(5000);
        SolrQuery query = new SolrQuery();
        query.setQuery(" (ex_k_name:\"松哥\" OR ex_k_phone:\"松哥\" OR ex_k_nickname:\"松哥\" OR contact_account:\"松哥\" OR ex_k_name:8618582185643 OR ex_k_phone:8618582185643 OR ex_k_nickname:8618582185643 OR contact_account:8618582185643)");
        query.setHighlight(true);
        query.set("hl.fl", "ex_k_nickname,contact_account");
        // query.set("hl.fl", "contact_account");
        query.set("f.ex_k_nickname.hl.mergeContiguous", true);
        query.set("f.contact_account.hl.requireFieldMatch", true);
        query.set("hl.usePhraseHighlighter", true);
        // query.set("f.contact_account.hl.mergeContiguous", true);
        // query.set("f.contact_account.hl.usePhraseHighlighter", false);
        QueryResponse response = server.query("shop", query);
        for (Map.Entry<String, Map<String, List<String>>> e : response.getHighlighting().entrySet()) {
            System.out.println(e.getKey());
            for (Map.Entry<String, List<String>> ee : e.getValue().entrySet()) {
                System.out.println(ee.getKey() + ":" + ee.getValue().stream().findFirst().get());
            }
            System.out.println(">>>>>>>>>>>>>>>>>>>");
        }
    }

    public void testRecord2() throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", "98765");
        doc.setField("contact_id", "9527");
        doc.setField("contact_account", "894054764");
        doc.setField("extend_info", "{k_nickname:\"tnk 松哥\",k_phone:\"8618582185643\"}");
        doc.setField("_version_", "1");
        String content = IOUtils.toString(this.getClass().getResourceAsStream("msg_content.txt"), "utf8");
        System.out.println(content);
        doc.setField("message_content", content);
        server.add("shop", doc);
        // server.add("shop", doc);
        server.commit();
        Thread.sleep(5000);
        DirectoryReader reader = server.getCoreContainer().getCore("shop").getSearcher().get().getIndexReader();
        TestTerms.readTerms(reader);
        SolrQuery query = new SolrQuery();
        query.set(QueryParsing.OP, "and");
        // query.setQuery("message_content:欺壓百姓作威作福的共產黨");
        query.setQuery("message_content:的");
        QueryResponse response = server.query("shop", query);
        System.out.println(response.getResults().getNumFound());
        for (SolrDocument d : response.getResults()) {
            System.out.println(d);
        }
    }

    public void testRecord() throws Exception {
        //
        // // <field name="id" type="string" stored="true" indexed="true"
        // // required="true"/>
        // // <field name="entity_id" type="string" stored="true" indexed="true"
        // // required="true" docValues="true"/>
        // // <field name="name_agile"
        //
        // // 我愛北京天安門
        // // 我爱北京天安门
        //
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", "98765");
        doc.setField("entity_id", "999999");
        doc.setField("name_agile", "黑皮狗黑皮狗黑皮狗");
        doc.setField("comma_split", "abcd");
        server.add("shop", doc);
        doc = new SolrInputDocument();
        doc.setField("id", "98766");
        doc.setField("entity_id", "888888");
        doc.setField("name_agile", "大人的黑皮衣的男人带着一条狗");
        doc.setField("comma_split", "efgh");
        server.add("shop", doc);
        doc = new SolrInputDocument();
        doc.setField("id", "98767");
        doc.setField("entity_id", "888888");
        doc.setField("name_agile", "马尼拉二手交易市场");
        doc.setField("comma_split", "efgh");
        server.add("shop", doc);
        server.commit();
        Thread.sleep(5000);
        SolrQuery query = new SolrQuery();
        // query.setQuery("name_agile:天安門");
        // query.setQuery("*:*");
        query.setShowDebugInfo(true);
        // query.setFacet(true);
        // query.addFacetField("name_agile");
        // query.setFacetLimit(10);
        // query.setQuery("{!complexphrase df=name_agile}黑皮狗");
        query.set(QueryParsing.OP, "and");
        // query.setQuery("name_agile:\"黑皮狗\"");
        query.setQuery("name_agile:黑皮狗");
        QueryResponse response = server.query("shop", query);
        for (FacetField ff : response.getFacetFields()) {
            for (Count c : ff.getValues()) {
                System.out.println(c.getName() + ":" + c.getCount());
            }
        }
    // query.setQuery("*:*");
    // query.addFilterQuery("{!cachedTerms f=entity_id v=1}123456");
    // queryResult(query);
    // System.out.println("start to
    // query=======================================");
    // query.setQuery("name_agile:\"马尼拉二手\"");
    // query.setQuery("*:*");
    // query.addFilterQuery("{!cachedTerms f=entity_id v=1}123456");
    // queryResult(query);
    // SolrIndexSearcher searcher =
    // server.getCoreContainer().getCore("shop").getSearcher().get().getIndexReader().getem
    // query = new SolrQuery();
    // // query.setQuery("name_agile:天安門");
    // // query.setQuery("*:*");
    //
    // query.setQuery("*:*");
    // query.addFilterQuery("{!cachedTerms f=entity_id v=2}123456");
    //
    // queryResult(query);
    }

    private void queryResult(SolrQuery query) throws SolrServerException, IOException {
        query.set(QueryParsing.OP, "and");
        // query.set
        QueryResponse response = server.query("shop", query);
        System.out.println(">>debuginfo>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (Map.Entry<String, Object> entry : response.getDebugMap().entrySet()) {
            System.out.println(entry.getKey() + "==>" + entry.getValue());
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("getNumFound:" + response.getResults().getNumFound());
        for (SolrDocument d : response.getResults()) {
            System.out.println(d.getFieldValue("name_agile"));
        }
    }

    // public void testShopAddRecord() throws Exception {
    // //
    // org.apache.lucene.spatial.prefix.CellDump.dump(Collections.emptyList());
    // // SolrInputDocument doc = new SolrInputDocument();
    // // // https://en.wikipedia.org/wiki/Well-known_text
    // // doc.addField("id", "0024324763bb7e920163c949a9fa6476");
    // // // doc.addField("svc_area", "POLYGON (( 120.1596 30.2447, 120.1595
    // // // 30.3447,120.1695 30.2447,120.1596 30.2447))");
    // //
    // // double degree = DistanceUtils.dist2Degrees(10,
    // // DistanceUtils.EARTH_MEAN_RADIUS_KM);
    // //
    // // doc.addField("delivery_region", "BUFFER(POINT(120.1596 30.2447)," +
    // degree
    // // +
    // // ")");
    // //
    // // doc.addField("entity_id", "123456");
    // // doc.addField("_version_", "1");
    // // Intersects(ENVELOPE(-10,-8,22,20)) distErrPct=0.025
    // // server.add(doc);
    // // String fromPt = "30.2547,120.1596";
    // String fromPt = "40.596679,79.83271";
    // SolrQuery query = new SolrQuery();
    // // query.setQuery(
    // // "{!inservice curr_pos=30.2547_120.1596}{!terms
    // f=entity_id}99927139,99001347,99934038,99933872,99936012,99226462,99934020");
    // query.setQuery("{!inservice curr_pos=40.596679_79.83271}{!terms
    // f=entity_id}99933871,99933872,99933874");
    // query.addFilterQuery("service_flag:[* TO *]");
    // query.setFields("entity_id", "service_flag", "dist:geodist()", "score",
    // "[docid]");
    // // query.setQuery("svc_area:\"Contains(POINT( 120.1596 30.2547))\"");
    // query.set("pt", fromPt);
    // query.set("sfield", FIELD_COORDINATE);
    // // query.setQuery("delivery_region:\"Contains(POINT( 120.2699
    // 30.2547))\"");
    // QueryResponse r = server.query("shop", query);
    // SolrDocumentList doclist = r.getResults();
    // System.out.println("==================doclist.getNumFound():" +
    // doclist.getNumFound());
    // for (SolrDocument doc : doclist) {
    // System.out.println("entity_id" + doc.getFieldValue("entity_id") +
    // ",service_flag:" + doc.getFieldValue("service_flag") + ",dist:" +
    // doc.getFieldValue("dist") + ",score:" + doc.getFieldValue("score") +
    // ",docid:" + doc.getFieldValue("[docid]"));
    // }
    // server.close();
    // }
    // public void testSearch4supplyTransferUnion() throws Exception {
    //
    // SolrCore goodsCore =
    // server.getCoreContainer().getCore("search4supplyGoods");
    //
    // }
    // // CASE 1,2,3
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_TRADE_ID, Lists.newArrayList(STOREAGE_TYPE.TYPE_3,
    // STOREAGE_TYPE.TYPE_4),
    // true /* summaryInfo */, true/* needPaging */);
    //
    // // CASE 5,CASE 4
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_SUPPLY_TYPE_ID,
    // Lists.newArrayList(STOREAGE_TYPE.TYPE_3, STOREAGE_TYPE.TYPE_4),
    // false /* summaryInfo */ , false/* needPaging */);
    //
    // // CASE 6
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_TRADE_ID,
    // Lists.newArrayList(STOREAGE_TYPE.TYPE_3),
    // false /* summaryInfo */,
    // true/* needPaging */);
    //
    // // CASE 7
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_TRADE_ID,
    // Lists.newArrayList(STOREAGE_TYPE.TYPE_4),
    // false /* summaryInfo */,
    // true/* needPaging */);
    //
    // // CASE 8
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_TRADE_ID, //
    // Lists.newArrayList(STOREAGE_TYPE.TYPE_2, STOREAGE_TYPE.TYPE_4) //
    // , true /* summaryInfo */, true/* needPaging */);
    //
    // }
    // public void testSupplyStatementAudit() throws Exception {
    // // CASE 1,2,3
    // getGroupInfoAndList( //
    // FIELD_KEY_GROUP_COLUMN_TRADE_ID,
    // Lists.newArrayList(STOREAGE_TYPE.TYPE_4),
    // true /* summaryInfo */,
    // true/* needPaging */);
    // }
    //
    // enum STOREAGE_TYPE {
    // TYPE_2(TYPE_IN_STORAGE_2), TYPE_3(TYPE_IN_STORAGE_3),
    // TYPE_4(TYPE_REFUND_4);
    // private int type;
    //
    // private STOREAGE_TYPE(int type) {
    // this.type = type;
    // }
    // }
    //
    // @SuppressWarnings("all")
    // protected void getGroupInfoAndList(String groupByKey, List<STOREAGE_TYPE>
    // types, boolean summaryInfo,
    // boolean needPaging) throws SolrServerException, IOException {
    // long numFound = 0;
    // int childCount = 0;
    // int start = 0;
    // int page = 0;
    // int pageSize = 20;
    // long allrows = -1;
    //
    // final String type3And4Summary = "type3And4Summary";
    // // String cursorMark = CursorMarkParams.CURSOR_MARK_START;
    // while (true) {
    //
    // SolrQuery query = new SolrQuery();
    //
    // // 这里的查询条件自己填
    // query.setQuery("is_valid:1 AND date:[ 20181022 TO 20181022 ] AND
    // trade_role:1
    // AND entity_id:99933218 AND self_entity_id:99933218");
    // query.set("rq", "{!amountResort groupby=" + groupByKey + " }");
    //
    // if (needPaging) {
    // query.setFields("*", // 这里自己填要什么字段，避免使用*
    // "[amount]");
    // query.setRows(pageSize);
    // // query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    // // query.setSort(FIELD_KEY_GROUP_COLUMN_TRADE_ID, ORDER.desc);
    // query.setStart(start = (page * pageSize));
    // } else {
    // // 不需要显示列表
    // query.setStart(CommonParams.START_DEFAULT);
    // query.setRows(0);
    // }
    //
    // query.addFilterQuery("{!tradeIdAggregation groupby=" + groupByKey + "
    // storage_type="
    // + Joiner.on(",").join(types.stream().mapToInt((k) -> k.type).iterator())
    // +
    // "}");
    //
    // if (summaryInfo) {
    // query.set("type3And4Summary", true);
    // }
    //
    // QueryResponse r = server.query("supplyStatementAudit", query);
    //
    // if (summaryInfo) {
    // NamedList<Object> summary = (NamedList<Object>)
    // r.getResponse().get(type3And4Summary);
    // // 打印汇总信息
    // System.out.println(RETURN_FIELD_type3SumAmount + ":" +
    // summary.get(RETURN_FIELD_type3SumAmount) //
    // + "," + RETURN_FIELD_type2SumAmount + ":" +
    // summary.get(RETURN_FIELD_type2SumAmount) //
    // + "," + RETURN_FIELD_type4SumAmount + ":" +
    // summary.get(RETURN_FIELD_type4SumAmount) //
    // + ",differ:" + summary.get(RETURN_FIELD_difference) //
    // + "," + RETURN_FIELD_Status3Count + ":" +
    // summary.get(RETURN_FIELD_Status3Count)//
    // + "," + RETURN_FIELD_StatusNot3Count + ":" +
    // summary.get(RETURN_FIELD_StatusNot3Count)//
    // );
    // }
    //
    // if (!needPaging) {
    // return;
    // }
    //
    // SolrDocumentList doclist = r.getResults();
    //
    // if (allrows < 0) {
    // allrows = doclist.getNumFound();
    // }
    //
    // System.out.println("page" + (page++) +
    // ":=========================================================");
    // for (SolrDocument doc : doclist) {
    // System.out.println(groupByKey + ":" + doc.get(groupByKey) //
    // + ",t2sum:" + doc.get(RETURN_FIELD_type2SumAmount) //
    // + ",t2count:" + doc.get(RETURN_FIELD_type2CountAmount)//
    // + ",t3sum:" + doc.get(RETURN_FIELD_type3SumAmount)//
    // + ",t3count:" + doc.get(RETURN_FIELD_type3CountAmount)//
    // + ",t4sum:" + doc.get(RETURN_FIELD_type4SumAmount)//
    // + ",t4count:" + doc.get(RETURN_FIELD_type4CountAmount) //
    // + ",diff:" + doc.get(RETURN_FIELD_difference));
    // }
    //
    // // 是否最后一页？
    // // if (cursorMark.equals(r.getNextCursorMark())) {
    // // // 已经翻到了最后一页
    // // break;
    // // } else {
    // // cursorMark = r.getNextCursorMark();
    // // }
    //
    // if ((allrows - start) <= pageSize) {
    // return;
    // }
    // }
    // }
    // public void testS4personasQuery() throws Exception {
    // long numFound = 0;
    // int childCount = 0;
    // long start = 0;
    // String pk = "962fb7cfda724041848817a5d88cfc50";
    //
    // SolrDocument doc = null;
    // SolrQuery query = new SolrQuery();
    // query.setParam("nestgetWithRootId", true);
    // query.setParam("testParm", "kkkkkkkkkkkkkkk");
    // query.setQuery("id:" + pk);
    // query.set("id", pk);
    // // if (binlogIdsSet.isEmpty()) {
    // // query.setFields("*", "[child parentFilter=\"type:p AND id:"+pk+"\"
    // childFilter=\"type:c\" limit=100]");
    //
    // //query.setFields("*", "[child parentFilter=\"type:p\"
    // childFilter=\"type:c\"
    // limit=100]");
    //
    // // } else {
    // // // 如果在binlog中已经存在了，那就不需要返回了
    // // query.setFields("*", "[child parentFilter=type:p childFilter=\"NOT
    // {!terms
    // // f=self_entity_id}"
    // // + COMMA_JOINER.join(binlogIdsSet) + "\" limit=100]");
    // // }
    // int count = 1;
    // while (true) {
    // if (count++ > 100) {
    // return;
    // }
    // try {
    // childCount = 0;
    // start = System.currentTimeMillis();
    // QueryResponse r = server.query("s4personas", query);
    // SolrDocumentList doclist = r.getResults();
    // numFound = doclist.getNumFound();
    //
    // for (SolrDocument solrDoc : doclist) {
    // doc = solrDoc;
    // if (doc.hasChildDocuments()) {
    // childCount = doc.getChildDocumentCount();
    // // System.out.println("doc.getChildDocumentCount:" +
    // // doc.getChildDocumentCount());
    // }
    // break;
    // }
    // } finally {
    // System.out.println(count + "consume:" + (System.currentTimeMillis() -
    // start)
    // + ",numberFound:"
    // + numFound + ",childCount:" + childCount);
    // }
    // }
    //
    //
    // }
    // public void testS4personasQuery() throws Exception {
    //
    // SolrQuery query = new SolrQuery();
    // query.setQuery("g_name:抽纸37");
    //
    // query.setRows(3);
    // // query.addSort("entity_id",ORDER.asc );
    // // query.setFields("id", "monetary", "card_degree",
    // // "[parent parentFilter=type:p
    // f=nick_name,customer_register_id,sex,city]");
    //
    // QueryResponse response = server.query("s4microgoods", query);//
    // ("search4personas", "0", );
    //
    // SolrDocumentList docList = response.getResults();
    // System.out.println("numFound:" + docList.getNumFound());
    // for (SolrDocument m : docList) {
    // // System.out.println("openid:" +
    // doc.getFieldValue("openid_wx6b0a76e23e5ec2e5")
    // // + ",nick_name:"
    // // + doc.getFieldValue("nick_name") + ",city:" +
    // doc.getFieldValue("city") +
    // // ",customerid:"
    // // + doc.getFieldValue("customer_register_id"));
    // System.out.println(m.get("id") + "," + m.get("g_name") );
    // }
    //
    // }
    // public void testS4personasQuery() throws Exception {
    // String COORDINATE_FILTER_QUERY = "{!geofilt sfield=coordinate}";
    // //
    // http://lucene.apache.org/solr/guide/6_6/query-re-ranking.html#query-re-ranking
    // SolrQuery query = new SolrQuery();
    // query.setQuery("_query_:\"entity_id:99001125\"
    // _val_:\"{!func}distScore()\"");
    // query.setFilterQueries(COORDINATE_FILTER_QUERY);
    // query.set("pt", "30.29592,120.13432");
    // query.set("d", Integer.MAX_VALUE);
    // query.set("sfield", "coordinate");
    // query.setSort("score", ORDER.asc);
    //
    //
    // query.set("rq", "{!shuffle mod=4 multi=100}");
    //
    // // query.set("rq", "{!rerank reRankQuery=$rqq reRankDocs=200
    // reRankWeight=1}");
    // // query.set("rqq", "_val_:\"{!func}shuffleScore()\"");
    //
    //
    // query.setFields("*", "score");
    // query.setRows(800);
    // // query.addSort("entity_id",ORDER.asc );
    // // query.setFields("id", "monetary", "card_degree",
    // // "[parent parentFilter=type:p
    // f=nick_name,customer_register_id,sex,city]");
    //
    // QueryResponse response = server.query("s4presellStock", query);//
    // ("search4personas", "0", );
    //
    // SolrDocumentList docList = response.getResults();
    // System.out.println("numFound:" + docList.getNumFound());
    // for (SolrDocument m : docList) {
    // // System.out.println("openid:" +
    // doc.getFieldValue("openid_wx6b0a76e23e5ec2e5")
    // // + ",nick_name:"
    // // + doc.getFieldValue("nick_name") + ",city:" +
    // doc.getFieldValue("city") +
    // // ",customerid:"
    // // + doc.getFieldValue("customer_register_id"));
    // System.out.println(m.get("presell_stock_id") + "," + m.get("entity_id") +
    // ",score:" + m.get("score"));
    // }
    //
    // }
    // public void testS4personasQuery() throws Exception {
    //
    // SolrQuery query = new SolrQuery();
    // query.setQuery("id:394 AND _query_:\"{!child of=type:p}id:397\"");
    // query.setFields("id", "monetary", "card_degree",
    // "[parent parentFilter=type:p
    // f=nick_name,customer_register_id,sex,city]");
    //
    // QueryResponse response = server.query("s4personas", query);//
    // ("search4personas", "0", );
    //
    // SolrDocumentList docList = response.getResults();
    // System.out.println("numFound:" + docList.getNumFound());
    // for (SolrDocument doc : docList) {
    // System.out.println("openid:" +
    // doc.getFieldValue("openid_wx6b0a76e23e5ec2e5")
    // + ",nick_name:"
    // + doc.getFieldValue("nick_name") + ",city:" + doc.getFieldValue("city") +
    // ",customerid:"
    // + doc.getFieldValue("customer_register_id"));
    // }
    //
    // }
    // public void testCoreInsert() throws Exception {
    // SolrInputDocument doc = new SolrInputDocument();
    //
    // doc.addField("id", "0024324763bb7e920163c949a9fa6476");
    // doc.addField("main_picture", "main_picture");
    // server.add(doc);
    //
    // doc = new SolrInputDocument();
    // doc.addField("id", "999");
    // // doc.addField("main_picture", "main_picture");
    // server.add(doc);
    // server.commit();
    //
    // Thread.sleep(5000);
    //
    // SolrQuery query = new SolrQuery();
    // // query.setQuery("main_picture_is_null:T");
    //
    // query.setQuery("id:0024324763bb7e920163c949a9fa6476");
    // query.setSort("id", ORDER.asc);
    //
    // QueryResponse r = server.query(query);
    //
    // System.out.println("NumFound:" + r.getResults().getNumFound());
    //
    // for (SolrDocument d : r.getResults()) {
    // System.out.println(
    // "id:" + d.getFieldValue("id") + ",main_picture_has_val:" +
    // d.getFieldValue("main_picture_has_val"));
    //
    // }
    //
    // }
    // @SuppressWarnings("all")
    // public void testCoreQuery() throws Exception {
    //
    // List<String> entityIdList = Lists.newArrayList("99926544");
    // SolrQuery solrQuery = new SolrQuery();
    // StringBuilder sb = new StringBuilder("is_valid:1 AND {!terms
    // f=item_module_type}0,1"
    // + " AND status:1 AND mp_isReserve:1 AND -mp_meal_only:1 AND {!terms
    // f=recommend_level}1,2,3");
    // sb.append(" AND {!terms f=entity_id}").append(String.join(",",
    // entityIdList));
    //
    // solrQuery.setQuery(sb.toString());
    // solrQuery.set("group", true);
    // solrQuery.set("group.field", "entity_id");
    // solrQuery.set("group.limit", 20);
    // solrQuery.setParam("group.sort", "shop_category_sort_code asc,sort_code
    // asc,create_time asc");
    //
    // QueryResponse response = server.query(solrQuery);
    //
    // GroupResponse group = response.getGroupResponse();
    //
    // for (GroupCommand c : group.getValues()) {
    //
    // for (Group g : c.getValues()) {
    // System.out.println("GroupValue:" + g.getGroupValue());
    // for (SolrDocument d : g.getResult()) {
    // System.out.println("==" + d.get("id"));
    // }
    // }
    // }
    //
    // }
    // public void testTermVector() throws Exception {
    // SolrInputDocument doc = new SolrInputDocument();
    // doc.setField("id", "123");
    // doc.setField("_version_", "0");
    // doc.setField("third_open_id", "11223344");
    // server.add("s4crm", doc);
    //
    // server.commit();
    //
    // Thread.sleep(5000);
    //
    // SolrQuery q = new SolrQuery();
    // q.setQuery("*:*");
    //
    // SolrCore core = server.getCoreContainer().getCore("s4crm");
    //
    // DirectoryReader reader =
    // core.getSearcher().incref().get().getIndexReader();
    // Terms terms = reader.getTermVector(0, "third_open_id");
    //
    // // final TokenStream tvStream =
    // // TokenSources.getTermVectorTokenStreamOrNull("third_open_id", tvFields,
    // -1);
    // //
    // TermsEnum enums = terms.iterator();
    //
    // BytesRef next = null;
    // PostingsEnum postings = null;
    // while ((next = enums.next()) != null) {
    //
    // System.out.println("term:" + next.utf8ToString() + ",termfreq:" +
    // enums.totalTermFreq()+",ord:"+ enums.ord());
    //
    // postings = enums.postings(null, PostingsEnum.ALL);
    //
    // System.out.println(postings.nextDoc());
    // System.out.println(postings.freq());
    // }
    // }
    // public void testAddSpan() throws Exception {
    //
    // }
    // @SuppressWarnings("all")
    // public void testCoreQuery() throws Exception {
    //
    // String cursorMark = CursorMarkParams.CURSOR_MARK_START;
    // while (true) {
    //
    // SolrQuery query = new SolrQuery();
    // // query.setQuery(
    // // "{!distinctOrder}order_id:[* TO *] AND entity_id:00180079");
    //
    // query.setQuery("{!distinctOrder}waitingorder_id:0007f0eccfba4e0081dc196d9cecda13");
    //
    // query.addSort("op_time", ORDER.desc);
    // query.addSort("waitingorder_id", ORDER.desc);
    // query.setRows(20);
    //
    // query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    // QueryResponse result = server.query(query);
    // // 命中结果数没有用
    // System.out.println(result.getResults().getNumFound());
    //
    // for (SolrDocument doc : result.getResults()) {
    // System.out.println(doc.get("waitingorder_id") + ",order_id:" +
    // doc.get("order_id") + ",create:"
    // + doc.get("op_time"));
    // }
    //
    // if (cursorMark.equals(result.getNextCursorMark())) {
    // // 已经翻到了最后一页
    // break;
    // } else {
    // cursorMark = result.getNextCursorMark();
    // }
    //
    // }
    // }
    // @SuppressWarnings("all")
    // public void testCoreQuery() throws Exception {
    // // 规则设置
    // JSONArray rules = new JSONArray();
    // JSONObject j = new JSONObject();
    // j.put("labelId", 16);
    // j.put("value", "1,10,1,10");
    // j.put("type", "0");
    // rules.put(j);
    //
    // j = new JSONObject();
    // j.put("labelId", 28);
    // j.put("value", "1,10,1,10");
    // j.put("type", "0");
    // rules.put(j);
    // // 规则设置 end
    // SolrQuery query = new SolrQuery();
    // query.setQuery("{!mrecommend entity_id=99149779 rules="
    // + URLEncoder.encode(rules.toString(), "utf8")
    // + "}991497795461cf70015461d103760001:11 AND menu_spec:[* TO *]");
    // query.setStart(0);
    // query.setRows(0);
    // query.set("menus-r", true);
    // query.set(CommonParams.DISTRIB, false);
    //
    // query.addFilterQuery("entity_id:99149779");
    // query.setFields("name", "menu_id", "server", "path", "price");
    // System.out.println(query.toString());
    //
    // QueryResponse result = server.query(query);
    //
    // long allRows = result.getResults().getNumFound();
    //
    // System.out.println("rowfound:" + allRows);
    //
    // List<SimpleOrderedMap> recommend = (List<SimpleOrderedMap>)
    // result.getResponse()
    // .get(RecommendMenuComponent.KEY_RECOMMEND);
    //
    // List<SimpleOrderedMap> menus = null;
    // for (SimpleOrderedMap o : recommend) {
    //
    // System.out.println(o.get("label"));
    //
    // System.out.println(o.get("proposal"));
    // System.out.println(o.get("allfound"));
    //
    // menus = (List<SimpleOrderedMap>) o.get("menus");
    // if (menus == null) {
    // continue;
    // }
    // for (SimpleOrderedMap m : menus) {
    // System.out.println(m.get("name"));
    // System.out.println(m.get("menu_id"));
    // System.out.println(m.get("server"));
    // System.out.println(m.get("path"));
    // System.out.println(m.get("price"));
    // }
    //
    // }
    //
    // // System.out
    // // .println("result.getResults().getNumFound():" +
    // // result.getResults().getNumFound());
    //
    // // SolrDocumentList list = result
    // //
    // // for (SolrDocument o : list) {
    // //
    // // System.out.println("menu_id:" + o.getFirstValue("menu_id"));
    // //
    // // }
    //
    // System.out.println("===========================================");
    // // GroupResponse groupResult = result.getGroupResponse();
    // // for (GroupCommand g : groupResult.getValues()) {
    // // System.out
    // // .println("groupName:" + g.getName() + ",g.getValues():" +
    // // g.getValues().size());
    // // for (Group group : g.getValues()) {
    // // System.out.println(group.getGroupValue());
    // // for (SolrDocument o : group.getResult()) {
    // //
    // // System.out.println(o.toString());
    // //
    // // }
    // // }
    // // }
    //
    // // for (JhsItemPojo item : result.getBeans(JhsItemPojo.class)) {
    // //
    // // System.out.print(item.getActivityPrice() + ",");
    // // System.out.println(item.getJuId());
    // //
    // // }
    //
    // }
    // public void testCore() throws Exception {
    // System.out.println("start==============================================");
    // SolrInputDocument add = new SolrInputDocument();
    // add.addField("id", "1");
    // add.addField("entity_id", "123333");
    // add.addField("name", "y9砵仔糕吕托");
    //
    // System.out.println(add.toString());
    //
    // server.add(add);
    //
    // server.commit();
    // server.close();
    // }
    // public void testCore() throws Exception {
    //
    // SolrInputDocument add = new SolrInputDocument();
    // add.setField("menu_id", "123");
    // add.setField("name", "情人节套餐");
    // add.setField("is_include", "1");
    // //
    // add.setField("all_child_menu_name",
    // "田园沙拉,水果披萨,鸡肉蘑菇焗饭,意式海鲜炒饭,松露汁野菌意粉,蒜辣海鲜意粉,鸡肉洋葱披萨,意式猪肉肠牛肉肠意粉,帕尔马火腿披萨,香肠蘑菇披萨,卡布奇诺,卡布奇诺（大）,卡布奇诺（冰）,拿铁,冰拿铁,摩卡,冰摩卡,焦糖玛奇朵,栗香玛奇朵,焦糖玛奇朵（冰）,栗香玛奇朵（冰）,巧克力布蕾玛奇朵（冰）,玫瑰花露奶油冰咖啡,美式咖啡,黛西女王,意式浓缩咖啡,浓缩玛奇朵,浓缩康宝蓝,特浓咖啡冰沙,焦糖咖啡冰沙,香草奶油冰沙,可可碎片冰沙,抹茶冰沙,芒果白葡萄冰沙,冻柠茶,冰摇红茶,芒果提子冰茶,红梅黑加仑冰茶,伯爵欧式奶茶,玫瑰蜂蜜奶茶,焦糖味伯爵奶茶,日式抹茶奶茶,有机非洲甘露花草茶,有机香草美果花草茶,有机伯爵红茶,可乐,雪碧,橙汁,果蔬汁,纯牛奶,苏打水,胡萝卜汁,苹果汁,黑咖啡8盎司,白咖啡8盎司,柠檬水,梨子汁,盆栽水果酸奶,时令果汁,西瓜汁,土豆浓汤,奶油南瓜汤,蓝色海洋气泡水,青葱岁月汽泡水,橙色记忆汽泡水,蓝色梦幻汽泡水,红粉佳人汽泡水,香煎带骨肋眼牛排");
    // server.add(add);
    //
    // server.commit();
    // server.close();
    //
    // }
    // public void testCoreQuery() throws Exception {
    //
    // SolrQuery query = new SolrQuery();
    // query.setQuery("menu_name:tc");
    // // query.setFields("ju_id");
    // QueryResponse result = server.query(query);
    //
    // System.out.println("result.getResults().getNumFound():"
    // + result.getResults().getNumFound());
    //
    // SolrDocumentList list = result.getResults();
    //
    // for (SolrDocument o : list) {
    //
    // System.out.println("menu_name:" + o.getFirstValue("menu_name"));z
    //
    // }
    //
    // // for (JhsItemPojo item : result.getBeans(JhsItemPojo.class)) {
    // //
    // // System.out.print(item.getActivityPrice() + ",");
    // // System.out.println(item.getJuId());
    // //
    // // }
    //
    // }
    // public void testAddNestDoc() throws Exception {
    //
    // // SolrInputDocument doc = new SolrInputDocument();
    // // doc.setField("id", "1");
    // // doc.setField("mobile", "15868113480");
    // // doc.setField("sex", "11");
    // // doc.setField("type", "p");
    // // doc.setField("_root_", "1");
    // // doc.setField("_version_", 999999);
    // //
    // // SolrInputDocument child = new SolrInputDocument();
    // // child.setField("id", "2");
    // // child.setField("third_open_id", "aaaaaaaaaaaaaaa");
    // // child.setField("_version_", 999999);
    // // child.setField("type", "c");
    // // child.setField("_root_", "1");
    // // doc.addChildDocument(child);
    // // server.add(doc);
    // //
    // // server.commit();
    // // self_entity_id:00201237 AND NOT customer_register_id:\-1 AND
    // monetary:[1
    // TO
    // // *] AND type:c AND _query_:"{!child of=type:p}(type:p AND -{!termPos
    // f=mobile
    // // start_pos=0}138)"
    // SolrQuery query = new SolrQuery();
    // query.setQuery(
    // "type:c AND NOT customer_register_id:\\-1 AND _query_:\"{!child
    // of=type:p}(type:p AND -{!termPos f=mobile start_pos=0}138)\"");
    //
    // // query.setQuery("type:c AND NOT customer_register_id:\\-1 AND
    // // _query_:\"{!child of=type:p}(type:p AND -sex:1)\"");
    //
    // query.setFields("third_open_id");
    //
    // query.setFields("third_open_id", "mobile", "sex", "[parent
    // parentFilter=type:p f=mobile,sex]");
    //
    // QueryResponse r = server.query("s4crm", query);
    // System.out.println("======================NumFound:" +
    // r.getResults().getNumFound());
    // }
    //
    // public void testCrmQuery() throws Exception {
    //
    // }
    private String val(Object o) {
        if (o == null) {
            return "";
        }
        if (!(o instanceof org.apache.lucene.document.Field)) {
            return String.valueOf(o);
        }
        org.apache.lucene.document.Field f = (org.apache.lucene.document.Field) o;
        return f.stringValue();
    }

    // public void testResponseWriter() throws Exception {
    // System.out.println("******************!!!!!!!!! start");
    // // 1.
    // // SolrQuery q = new SolrQuery();
    // // q.setQuery("title:[* TO *]");
    // // q.add("qt", "/select");
    // // q.setFields("id", "title", "content_type", "[child
    // // parentFilter=content_type:*]");
    // // QueryResponse response = server.query(q);
    // // SolrDocumentList docs = response.getResults();
    // // System.out.println("NumFound:" + docs.getNumFound());
    // // for (SolrDocument doc : docs) {
    // // System.out.println("id:" + val(doc.getFirstValue("id")) + ",title:" +
    // // val(doc.getFirstValue("title"))
    // // + ",content_type:" + val(doc.getFirstValue("content_type")));
    // // for (SolrDocument c : doc.getChildDocuments()) {
    // // System.out.println(">id:" + val(c.getFirstValue("id")) + ",title:" +
    // // val(c.getFirstValue("title"))
    // // + ",content_type:" + val(c.getFirstValue("content_type")));
    // // }
    // // }
    //
    // // 2.
    // SolrQuery query = new SolrQuery();
    // query.setQuery("authorizer_app_id:wxa52bcbabd9527822 AND months:4");
    // query.setFields("customer_register_id", "third_open_id",
    // "authorizer_app_id");
    // query.addSort("customer_register_id", SolrQuery.ORDER.asc);
    // query.set(CommonParams.QT, "/export");
    // AtomicInteger count = new AtomicInteger();
    // server.queryAndStreamResponse("personasMember", query, new
    // StreamingResponseCallback() {
    // @Override
    // public void streamSolrDocument(SolrDocument doc) {
    // count.incrementAndGet();
    // System.out.println();
    // System.out.println("---------------");
    // for (String field : doc.getFieldNames()) {
    // System.out.println("field:" + field + ", value:" +
    // val(doc.getFieldValue(field)));
    // }
    // System.out.println("---------------");
    // System.out.println();
    // }
    //
    // @Override
    // public void streamDocListInfo(long numFound, long start, Float maxScore)
    // {
    // }
    // });
    //
    // // 3.
    //
    // // StreamFactory streamFactory = new
    // // StreamFactory().withCollectionZkHost("nest", zkHost)
    // // .withFunctionName("search",
    // // CloudSolrStream.class).withFunctionName("unique", UniqueStream.class)
    // // .withFunctionName("top", RankStream.class).withFunctionName("group",
    // // ReducerStream.class)
    // // .withFunctionName("parallel", ParallelStream.class);
    // // String expr = "search(nest, zkHost=\"" + zkHost
    // // + "\", qt=\"/export\", q=\"type:i\", fl=\"id, i_name, [parent
    // // parentFilter=type:t f=curr_date]\", sort=\"id asc\", rows=\"20\")";
    // // System.out.println(expr);
    // // CloudSolrStream pstream = (CloudSolrStream)
    // // streamFactory.constructStream(expr);
    // // pstream.open();
    // // while (true) {
    // // Tuple tuple = pstream.read();
    // // if (tuple.EOF) {
    // // break;
    // // } else {
    // // // System.out.println(tuple.toString());
    // // System.out.println(tuple.getString("id"));
    // // System.out.println(tuple.getString("i_name"));
    // // }
    // // }
    // // pstream.close();
    // //
    // System.out.println("******************!!!!!!!!! end");
    // }
    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}
