///**
// *   Licensed to the Apache Software Foundation (ASF) under one
// *   or more contributor license agreements.  See the NOTICE file
// *   distributed with this work for additional information
// *   regarding copyright ownership.  The ASF licenses this file
// *   to you under the Apache License, Version 2.0 (the
// *   "License"); you may not use this file except in compliance
// *   with the License.  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//package com.qlangtech.tis.solrj.extend.search4supplyUnionTabs;
//
//import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent;
//import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent.SortField;
//import org.apache.solr.client.solrj.beans.Field;
//import java.util.List;
//
///**
// * @author 百岁（baisui@qlangtech.com）
// * @date 2019年1月17日
// */
//public class UnionTab extends UnionTabBase {
//
//    @Field("goods_id")
//    private String goodsId;
//
//    @Field("entity_id")
//    private String entityId;
//
//    @Field("self_entity_id")
//    private String selfEntityId;
//
//    @Field("is_valid")
//    private int isValid;
//
//    @Field("last_ver")
//    private int lastVersion;
//
//    @Field("warehouse_id")
//    private String warehouseId;
//
//    @Field("supplier_id")
//    private String supplierId;
//
//    @Field("op_time")
//    private long opTime;
//
//    @Field("scd_stock_check_id")
//    private String scdStockCheckId;
//
//    @Field("create_time")
//    private long createTime;
//
//    public String getGoodsId() {
//        return goodsId;
//    }
//
//    public void setGoodsId(String goodsId) {
//        this.goodsId = goodsId;
//    }
//
//    public String getEntityId() {
//        return entityId;
//    }
//
//    public void setEntityId(String entityId) {
//        this.entityId = entityId;
//    }
//
//    public String getSelfEntityId() {
//        return selfEntityId;
//    }
//
//    public void setSelfEntityId(String selfEntityId) {
//        this.selfEntityId = selfEntityId;
//    }
//
//    public int getIsValid() {
//        return isValid;
//    }
//
//    public void setIsValid(int isValid) {
//        this.isValid = isValid;
//    }
//
//    public int getLastVersion() {
//        return lastVersion;
//    }
//
//    public void setLastVersion(int lastVersion) {
//        this.lastVersion = lastVersion;
//    }
//
//    public String getWarehouseId() {
//        return warehouseId;
//    }
//
//    public void setWarehouseId(String warehouseId) {
//        this.warehouseId = warehouseId;
//    }
//
//    public String getSupplierId() {
//        return supplierId;
//    }
//
//    public void setSupplierId(String supplierId) {
//        this.supplierId = supplierId;
//    }
//
//    public long getOpTime() {
//        return opTime;
//    }
//
//    public void setOpTime(long opTime) {
//        this.opTime = opTime;
//    }
//
//    public String getScdStockCheckId() {
//        return scdStockCheckId;
//    }
//
//    public void setScdStockCheckId(String scdStockCheckId) {
//        this.scdStockCheckId = scdStockCheckId;
//    }
//
//    public long getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(long createTime) {
//        this.createTime = createTime;
//    }
//
//    public static void main(String[] args) throws Exception {
//        String zkHost = "10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud";
//        String expr = "top(n=3,\n" + "\tcomplement(\n" + "\t\tsearchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField rowCount=3 sort=create_time order=asc}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "\t\tunique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "\ton=id=goods_id),\n" + "\tsort=create_time asc\n" + ")";
//        // String expr = "top(n=3,\n" +
//        // "\tleftOuterJoin(\n" +
//        // "\t\tsearchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField rowCount=3 sort=create_time order=asc afterId=55984 afterValue=20160830111700948}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" +
//        // "\t\tunique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" +
//        // "\ton=id=goods_id),\n" +
//        // "\tsort=create_time asc\n" +
//        // ")";
//        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
//        agent.setQuery(expr);
//        // 索引分库键值
//        agent.setSharedKey("99928370");
//        // 索引名称
//        agent.setCollection("search4supplyGoods");
//        // 需要返回的fields
//        agent.setFields(new String[] { "id", "entity_id", "create_time" });
//        // 反查时的主键
//        agent.setPrimaryField("id");
//        // 最后返回结果的条数
//        agent.setRows(30);
//        System.out.println(expr);
//        SortField[] sortFields = new SortField[] {};
//        List<UnionTab> list = agent.query(UnionTab.class, sortFields);
//        int i = 1;
//    }
//}
