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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

// import com.koubei.persistence.BaseIbatisDAO;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResourceCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
