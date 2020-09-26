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
package com.qlangtech.tis.realtime.test.shop.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.shop.dao.IMallShopDAO;
import com.qlangtech.tis.realtime.test.shop.pojo.MallShop;
import com.qlangtech.tis.realtime.test.shop.pojo.MallShopCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MallShopDAOImpl extends BasicDAO<MallShop, MallShopCriteria> implements IMallShopDAO {

    public MallShopDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "mall_shop";
    }

    public int countByExample(MallShopCriteria example) {
        Integer count = (Integer) this.count("mall_shop.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(MallShopCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("mall_shop.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(MallShopCriteria criteria) {
        return this.deleteRecords("mall_shop.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        MallShop key = new MallShop();
        key.setId(id);
        return this.deleteRecords("mall_shop.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(MallShop record) {
        this.insert("mall_shop.ibatorgenerated_insert", record);
    }

    public void insertSelective(MallShop record) {
        this.insert("mall_shop.ibatorgenerated_insertSelective", record);
    }

    public List<MallShop> selectByExample(MallShopCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(MallShopCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.shop.pojo.MallShopCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("mall_shop.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<MallShop> selectByExample(MallShopCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<MallShop> list = this.list("mall_shop.ibatorgenerated_selectByExample", example);
        return list;
    }

    public MallShop selectByPrimaryKey(String id) {
        MallShop key = new MallShop();
        key.setId(id);
        MallShop record = (MallShop) this.load("mall_shop.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(MallShop record, MallShopCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("mall_shop.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(MallShop record, MallShopCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("mall_shop.ibatorgenerated_updateByExample", parms);
    }

    public MallShop loadFromWriteDB(String id) {
        MallShop key = new MallShop();
        key.setId(id);
        MallShop record = (MallShop) this.loadFromWriterDB("mall_shop.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends MallShopCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, MallShopCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
