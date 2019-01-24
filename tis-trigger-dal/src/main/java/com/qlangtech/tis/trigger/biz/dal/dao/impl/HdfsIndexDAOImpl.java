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
package com.qlangtech.tis.trigger.biz.dal.dao.impl;

import java.util.List;
import com.qlangtech.tis.trigger.biz.dal.dao.IHdfsIndexDAO;
import com.qlangtech.tis.trigger.biz.dal.pojo.HdfsIndex;
import com.qlangtech.tis.trigger.biz.dal.pojo.HdfsIndexCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexDAOImpl extends BasicDAO<HdfsIndex, HdfsIndexCriteria> implements IHdfsIndexDAO {

    public HdfsIndexDAOImpl() {
        super();
    }

    public int countByExample(HdfsIndexCriteria example) {
        Integer count = (Integer) this.count("hdfs_index.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(HdfsIndexCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("hdfs_index.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(HdfsIndexCriteria criteria) {
        return this.deleteRecords("hdfs_index.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long id) {
        HdfsIndex key = new HdfsIndex();
        key.setId(id);
        return this.deleteRecords("hdfs_index.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(HdfsIndex record) {
        Object newKey = this.insert("hdfs_index.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(HdfsIndex record) {
        Object newKey = this.insert("hdfs_index.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<HdfsIndex> selectByExample(HdfsIndexCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<HdfsIndex> selectByExample(HdfsIndexCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<HdfsIndex> list = this.list("hdfs_index.ibatorgenerated_selectByExample", example);
        return list;
    }

    public HdfsIndex selectByPrimaryKey(Long id) {
        HdfsIndex key = new HdfsIndex();
        key.setId(id);
        HdfsIndex record = (HdfsIndex) this.load("hdfs_index.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(HdfsIndex record, HdfsIndexCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("hdfs_index.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(HdfsIndex record, HdfsIndexCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("hdfs_index.ibatorgenerated_updateByExample", parms);
    }

    public HdfsIndex loadFromWriteDB(Long id) {
        HdfsIndex key = new HdfsIndex();
        key.setId(id);
        HdfsIndex record = (HdfsIndex) this.loadFromWriterDB("hdfs_index.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends HdfsIndexCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, HdfsIndexCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.trigger.biz.dal.dao.impl.BasicDAO#getTableName()
	 */
    @Override
    public String getTableName() {
        return "hdfs_index";
    }
}
