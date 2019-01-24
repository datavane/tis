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

// import com.koubei.persistence.BaseIbatisDAO;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResourceCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UploadResourceDAOImpl extends BasicDAO<UploadResource, UploadResourceCriteria> implements IUploadResourceDAO {

    @Override
    public String getEntityName() {
        return "upload_resource";
    }

    public UploadResourceDAOImpl() {
        super();
    }

    public int countByExample(UploadResourceCriteria example) {
        Integer count = (Integer) this.count("upload_resource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(UploadResourceCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("upload_resource.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(UploadResourceCriteria criteria) {
        return this.deleteRecords("upload_resource.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long urId) {
        UploadResource key = new UploadResource();
        key.setUrId(urId);
        return this.deleteRecords("upload_resource.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(UploadResource record) {
        return (Integer) this.insert("upload_resource.ibatorgenerated_insert", record);
    }

    public Integer insertSelective(UploadResource record) {
        return (Integer) this.insert("upload_resource.ibatorgenerated_insertSelective", record);
    }

    // @SuppressWarnings("unchecked")
    public List<UploadResource> selectByExampleWithBLOBs(UploadResourceCriteria example) {
        List<UploadResource> list = this.list("upload_resource.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<UploadResource> selectByExample(UploadResourceCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    // @SuppressWarnings("unchecked")
    public List<UploadResource> selectByExampleWithoutBLOBs(UploadResourceCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<UploadResource> list = this.list("upload_resource.ibatorgenerated_selectByExample", example);
        return list;
    }

    public UploadResource selectByPrimaryKey(Long urId) {
        UploadResource key = new UploadResource();
        key.setUrId(urId);
        UploadResource record = (UploadResource) this.load("upload_resource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(UploadResource record, UploadResourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("upload_resource.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(UploadResource record, UploadResourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("upload_resource.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(UploadResource record, UploadResourceCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("upload_resource.ibatorgenerated_updateByExample", parms);
    }

    public UploadResource loadFromWriteDB(Long urId) {
        UploadResource key = new UploadResource();
        key.setUrId(urId);
        UploadResource record = (UploadResource) this.loadFromWriterDB("upload_resource.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends UploadResourceCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, UploadResourceCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
