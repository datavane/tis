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
package com.qlangtech.tis.realtime.test.order.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.order.dao.IDiscountDetailDAO;
import com.qlangtech.tis.realtime.test.order.pojo.DiscountDetail;
import com.qlangtech.tis.realtime.test.order.pojo.DiscountDetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DiscountDetailDAOImpl extends BasicDAO<DiscountDetail, DiscountDetailCriteria> implements IDiscountDetailDAO {

    public DiscountDetailDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "discount_detail";
    }

    public int countByExample(DiscountDetailCriteria example) {
        Integer count = (Integer) this.count("discount_detail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(DiscountDetailCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("discount_detail.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(DiscountDetailCriteria criteria) {
        return this.deleteRecords("discount_detail.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        DiscountDetail key = new DiscountDetail();
        key.setId(id);
        return this.deleteRecords("discount_detail.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(DiscountDetail record) {
        this.insert("discount_detail.ibatorgenerated_insert", record);
    }

    public void insertSelective(DiscountDetail record) {
        this.insert("discount_detail.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<DiscountDetail> selectByExampleWithBLOBs(DiscountDetailCriteria example) {
        List<DiscountDetail> list = this.list("discount_detail.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<DiscountDetail> selectByExampleWithoutBLOBs(DiscountDetailCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(DiscountDetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.DiscountDetailCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("discount_detail.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<DiscountDetail> selectByExampleWithoutBLOBs(DiscountDetailCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DiscountDetail> list = this.list("discount_detail.ibatorgenerated_selectByExample", example);
        return list;
    }

    public DiscountDetail selectByPrimaryKey(String id) {
        DiscountDetail key = new DiscountDetail();
        key.setId(id);
        DiscountDetail record = (DiscountDetail) this.load("discount_detail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(DiscountDetail record, DiscountDetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("discount_detail.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(DiscountDetail record, DiscountDetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("discount_detail.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(DiscountDetail record, DiscountDetailCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("discount_detail.ibatorgenerated_updateByExample", parms);
    }

    public DiscountDetail loadFromWriteDB(String id) {
        DiscountDetail key = new DiscountDetail();
        key.setId(id);
        DiscountDetail record = (DiscountDetail) this.loadFromWriterDB("discount_detail.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends DiscountDetailCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, DiscountDetailCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
