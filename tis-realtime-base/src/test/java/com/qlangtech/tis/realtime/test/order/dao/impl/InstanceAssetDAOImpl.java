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
import com.qlangtech.tis.realtime.test.order.dao.IInstanceAssetDAO;
import com.qlangtech.tis.realtime.test.order.pojo.InstanceAsset;
import com.qlangtech.tis.realtime.test.order.pojo.InstanceAssetCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class InstanceAssetDAOImpl extends BasicDAO<InstanceAsset, InstanceAssetCriteria> implements IInstanceAssetDAO {

    public InstanceAssetDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "instance_asset";
    }

    public int countByExample(InstanceAssetCriteria example) {
        Integer count = (Integer) this.count("instance_asset.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(InstanceAssetCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("instance_asset.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(InstanceAssetCriteria criteria) {
        return this.deleteRecords("instance_asset.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        InstanceAsset key = new InstanceAsset();
        key.setId(id);
        return this.deleteRecords("instance_asset.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(InstanceAsset record) {
        this.insert("instance_asset.ibatorgenerated_insert", record);
    }

    public void insertSelective(InstanceAsset record) {
        this.insert("instance_asset.ibatorgenerated_insertSelective", record);
    }

    public List<InstanceAsset> selectByExample(InstanceAssetCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(InstanceAssetCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.order.pojo.InstanceAssetCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("instance_asset.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<InstanceAsset> selectByExample(InstanceAssetCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<InstanceAsset> list = this.list("instance_asset.ibatorgenerated_selectByExample", example);
        return list;
    }

    public InstanceAsset selectByPrimaryKey(String id) {
        InstanceAsset key = new InstanceAsset();
        key.setId(id);
        InstanceAsset record = (InstanceAsset) this.load("instance_asset.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(InstanceAsset record, InstanceAssetCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("instance_asset.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(InstanceAsset record, InstanceAssetCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("instance_asset.ibatorgenerated_updateByExample", parms);
    }

    public InstanceAsset loadFromWriteDB(String id) {
        InstanceAsset key = new InstanceAsset();
        key.setId(id);
        InstanceAsset record = (InstanceAsset) this.loadFromWriterDB("instance_asset.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends InstanceAssetCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, InstanceAssetCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
