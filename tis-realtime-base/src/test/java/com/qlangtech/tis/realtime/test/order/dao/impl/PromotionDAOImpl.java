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
import com.qlangtech.tis.realtime.test.order.dao.IPromotionDAO;
import com.qlangtech.tis.realtime.test.order.pojo.Promotion;
import com.qlangtech.tis.realtime.test.order.pojo.PromotionCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PromotionDAOImpl extends BasicDAO<Promotion, PromotionCriteria> implements IPromotionDAO {

    public PromotionDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "promotion";
    }

    public int countByExample(PromotionCriteria example) {
        Integer count = (Integer) this.count("promotion.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(PromotionCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("promotion.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(PromotionCriteria criteria) {
        return this.deleteRecords("promotion.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String promotionId) {
        Promotion key = new Promotion();
        key.setPromotionId(promotionId);
        return this.deleteRecords("promotion.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Promotion record) {
        this.insert("promotion.ibatorgenerated_insert", record);
    }

    public void insertSelective(Promotion record) {
        this.insert("promotion.ibatorgenerated_insertSelective", record);
    }

    @SuppressWarnings("unchecked")
    public List<Promotion> selectByExampleWithBLOBs(PromotionCriteria example) {
        List<Promotion> list = this.list("promotion.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<Promotion> selectByExampleWithoutBLOBs(PromotionCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(PromotionCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.PromotionCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("promotion.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Promotion> selectByExampleWithoutBLOBs(PromotionCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Promotion> list = this.list("promotion.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Promotion selectByPrimaryKey(String promotionId) {
        Promotion key = new Promotion();
        key.setPromotionId(promotionId);
        Promotion record = (Promotion) this.load("promotion.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Promotion record, PromotionCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("promotion.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(Promotion record, PromotionCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("promotion.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(Promotion record, PromotionCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("promotion.ibatorgenerated_updateByExample", parms);
    }

    public Promotion loadFromWriteDB(String promotionId) {
        Promotion key = new Promotion();
        key.setPromotionId(promotionId);
        Promotion record = (Promotion) this.loadFromWriterDB("promotion.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends PromotionCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, PromotionCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
