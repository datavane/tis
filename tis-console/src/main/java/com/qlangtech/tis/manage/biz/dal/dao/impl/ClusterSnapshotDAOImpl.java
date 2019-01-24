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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotQuery;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ClusterSnapshotDAOImpl extends BasicDAO<ClusterSnapshot, ClusterSnapshotCriteria> implements IClusterSnapshotDAO {

    @Override
    public String getEntityName() {
        return "cluster_snapshot_dao";
    }

    public ClusterSnapshotDAOImpl() {
        super();
    }

    public int countByExample(ClusterSnapshotCriteria example) {
        Integer count = (Integer) this.count("cluster_snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ClusterSnapshotCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("cluster_snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ClusterSnapshotCriteria criteria) {
        return this.deleteRecords("cluster_snapshot.ibatorgenerated_deleteByExample", criteria);
    }

    public Long insert(ClusterSnapshot record) {
        Object newKey = this.insert("cluster_snapshot.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(ClusterSnapshot record) {
        Object newKey = this.insert("cluster_snapshot.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    /**
     * @param query
     * @return
     */
    public List<ClusterSnapshot> reportClusterStatus(ClusterSnapshotQuery query) {
        return this.listAnonymity("cluster_snapshot.clusterReport" + query.getSqlmapSuffix(), query);
    }

    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ClusterSnapshot> list = this.list("cluster_snapshot.ibatorgenerated_selectByExample", example);
        return list;
    }

    // public ClusterSnapshot selectByPrimaryKey(Long id) {
    // ClusterSnapshot key = new ClusterSnapshot();
    // key.setId(id);
    // ClusterSnapshot record = (ClusterSnapshot) this.load("cluster_snapshot.ibatorgenerated_selectByPrimaryKey",
    // key);
    // return record;
    // }
    public int updateByExampleSelective(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("cluster_snapshot.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("cluster_snapshot.ibatorgenerated_updateByExample", parms);
    }

    private static class UpdateByExampleParms extends ClusterSnapshotCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ClusterSnapshotCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
