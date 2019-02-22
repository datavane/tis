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
package com.qlangtech.tis.dataplatform.dao.impl;

import com.qlangtech.ibatis.extend.BasicDAO;
import com.qlangtech.tis.dataplatform.dao.IClusterSnapshotPreDayDAO;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDay;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDayCriteria;

import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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

    @SuppressWarnings("all")
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
