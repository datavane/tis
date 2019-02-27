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
package com.qlangtech.tis.solrj.extend.search4supplyUnionTabs;

import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent;
import com.qlangtech.tis.solrj.extend.InOptimizeClientAgent.SortField;
import org.apache.solr.client.solrj.beans.Field;
import java.util.List;

/*
 * 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UnionTab extends UnionTabBase {

    @Field("goods_id")
    private String goodsId;

    @Field("entity_id")
    private String entityId;

    @Field("self_entity_id")
    private String selfEntityId;

    @Field("is_valid")
    private int isValid;

    @Field("last_ver")
    private int lastVersion;

    @Field("warehouse_id")
    private String warehouseId;

    @Field("supplier_id")
    private String supplierId;

    @Field("op_time")
    private long opTime;

    @Field("scd_stock_check_id")
    private String scdStockCheckId;

    @Field("create_time")
    private long createTime;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getSelfEntityId() {
        return selfEntityId;
    }

    public void setSelfEntityId(String selfEntityId) {
        this.selfEntityId = selfEntityId;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public int getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(int lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public long getOpTime() {
        return opTime;
    }

    public void setOpTime(long opTime) {
        this.opTime = opTime;
    }

    public String getScdStockCheckId() {
        return scdStockCheckId;
    }

    public void setScdStockCheckId(String scdStockCheckId) {
        this.scdStockCheckId = scdStockCheckId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public static void main(String[] args) throws Exception {
        String zkHost = "10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud";
        String expr = "top(n=3,\n" + "\tcomplement(\n" + "\t\tsearchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField rowCount=3 sort=create_time order=asc}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" + "\t\tunique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" + "\ton=id=goods_id),\n" + "\tsort=create_time asc\n" + ")";
        // String expr = "top(n=3,\n" +
        // "\tleftOuterJoin(\n" +
        // "\t\tsearchExtend(search4supplyGoods, qt=/export, _route_=99928370, q=_query_:\"{!topNField rowCount=3 sort=create_time order=asc afterId=55984 afterValue=20160830111700948}entity_id:99928370\", fl=\"id, docid:[docid f=docid]\", sort=id asc),\n" +
        // "\t\tunique(searchExtend(search4supplyUnionTabs, qt=/export, _route_=99928370, q=entity_id:99928370, fl=goods_id, sort=goods_id asc), over=goods_id),\n" +
        // "\ton=id=goods_id),\n" +
        // "\tsort=create_time asc\n" +
        // ")";
        InOptimizeClientAgent agent = new InOptimizeClientAgent(zkHost);
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
        agent.setRows(30);
        System.out.println(expr);
        SortField[] sortFields = new SortField[] {};
        List<UnionTab> list = agent.query(UnionTab.class, sortFields);
        int i = 1;
    }
}
