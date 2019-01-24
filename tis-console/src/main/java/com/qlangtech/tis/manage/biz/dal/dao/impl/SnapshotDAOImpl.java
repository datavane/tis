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
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SnapshotDAOImpl extends BasicDAO<Snapshot, SnapshotCriteria> implements ISnapshotDAO {

    public SnapshotDAOImpl() {
        super();
    }

    @Override
    public String getEntityName() {
        return "snapshot";
    }

    @Override
    public List<Snapshot> findPassTestSnapshot(SnapshotCriteria example) {
        example.setPage(1);
        example.setPageSize(100);
        List<Snapshot> list = this.list("snapshot.ibatorgenerated_select_pass_test_ByExample", example);
        return list;
    }

    @Override
    public Integer getMaxSnapshotId(SnapshotCriteria criteria) {
        return (Integer) this.count("snapshot.ibatorgenerated_selectMaxSnapshotId", criteria);
    }

    public int countByExample(SnapshotCriteria example) {
        Integer count = (Integer) this.count("snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(SnapshotCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("snapshot.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(SnapshotCriteria criteria) {
        return this.deleteRecords("snapshot.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer snId) {
        Snapshot key = new Snapshot();
        key.setSnId(snId);
        return this.deleteRecords("snapshot.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(Snapshot record) {
        return (Integer) this.insert("snapshot.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(Snapshot record) {
        return (Integer) this.insert("snapshot.ibatorgenerated_insertSelective", record);
    }

    public List<Snapshot> selectByExample(SnapshotCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<Snapshot> selectByExample(SnapshotCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Snapshot> list = this.list("snapshot.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Snapshot selectByPrimaryKey(Integer snId) {
        Snapshot key = new Snapshot();
        key.setSnId(snId);
        Snapshot record = (Snapshot) this.load("snapshot.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Snapshot record, SnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("snapshot.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Snapshot record, SnapshotCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("snapshot.ibatorgenerated_updateByExample", parms);
    }

    public Snapshot loadFromWriteDB(Integer snId) {
        Snapshot key = new Snapshot();
        key.setSnId(snId);
        Snapshot record = (Snapshot) this.loadFromWriterDB("snapshot.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends SnapshotCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, SnapshotCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
