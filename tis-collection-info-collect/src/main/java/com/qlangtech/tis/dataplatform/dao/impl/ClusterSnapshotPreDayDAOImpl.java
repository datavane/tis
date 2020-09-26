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
package com.qlangtech.tis.dataplatform.dao.impl;

import com.qlangtech.tis.dataplatform.dao.IClusterSnapshotPreDayDAO;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDay;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDayCriteria;
import com.taobao.ibatis.extend.BasicDAO;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ClusterSnapshotPreDayDAOImpl extends BasicDAO<ClusterSnapshotPreDay, ClusterSnapshotPreDayCriteria> implements IClusterSnapshotPreDayDAO {

    public ClusterSnapshotPreDayDAOImpl() {
        super();
    }

    public int countByExample(ClusterSnapshotPreDayCriteria example) {
        Integer count = (Integer) this.count("cluster_snapshot_pre_day.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ClusterSnapshotPreDayCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("cluster_snapshot_pre_day.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ClusterSnapshotPreDayCriteria criteria) {
        return this.deleteRecords("cluster_snapshot_pre_day.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        ClusterSnapshotPreDay key = new ClusterSnapshotPreDay();
        key.setId(id);
        return this.deleteRecords("cluster_snapshot_pre_day.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(ClusterSnapshotPreDay record) {
        Object newKey = this.insert("cluster_snapshot_pre_day.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(ClusterSnapshotPreDay record) {
        Object newKey = this.insert("cluster_snapshot_pre_day.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<ClusterSnapshotPreDay> selectByExample(ClusterSnapshotPreDayCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ClusterSnapshotPreDay> selectByExample(ClusterSnapshotPreDayCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ClusterSnapshotPreDay> list = this.list("cluster_snapshot_pre_day.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ClusterSnapshotPreDay selectByPrimaryKey(Integer id) {
        ClusterSnapshotPreDay key = new ClusterSnapshotPreDay();
        key.setId(id);
        ClusterSnapshotPreDay record = (ClusterSnapshotPreDay) this.load("cluster_snapshot_pre_day.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ClusterSnapshotPreDay record, ClusterSnapshotPreDayCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("cluster_snapshot_pre_day.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ClusterSnapshotPreDay record, ClusterSnapshotPreDayCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("cluster_snapshot_pre_day.ibatorgenerated_updateByExample", parms);
    }

    public ClusterSnapshotPreDay loadFromWriteDB(Integer id) {
        ClusterSnapshotPreDay key = new ClusterSnapshotPreDay();
        key.setId(id);
        ClusterSnapshotPreDay record = (ClusterSnapshotPreDay) this.loadFromWriterDB("cluster_snapshot_pre_day.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ClusterSnapshotPreDayCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ClusterSnapshotPreDayCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
