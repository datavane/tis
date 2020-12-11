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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ResourceParametersDAOImpl extends BasicDAO<ResourceParameters, ResourceParametersCriteria> implements IResourceParametersDAO {

    @Override
    public String getEntityName() {
        return "config_resource_parameters";
    }

    public ResourceParametersDAOImpl() {
        super();
    }

    public int countByExample(ResourceParametersCriteria example) {
        Integer count = this.count("resource_parameters.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ResourceParametersCriteria example) {
        Integer count = this.countFromWriterDB("resource_parameters.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(ResourceParametersCriteria criteria) {
        return this.deleteRecords("resource_parameters.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Long rpId) {
        ResourceParameters key = new ResourceParameters();
        key.setRpId(rpId);
        return this.deleteRecords("resource_parameters.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Long insert(ResourceParameters record) {
        Object newKey = this.insert("resource_parameters.ibatorgenerated_insert", record);
        return (Long) newKey;
    }

    public Long insertSelective(ResourceParameters record) {
        Object newKey = this.insert("resource_parameters.ibatorgenerated_insertSelective", record);
        return (Long) newKey;
    }

    public List<ResourceParameters> selectByExample(ResourceParametersCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<ResourceParameters> selectByExample(ResourceParametersCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<ResourceParameters> list = this.list("resource_parameters.ibatorgenerated_selectByExample", example);
        return list;
    }

    public ResourceParameters selectByPrimaryKey(Long rpId) {
        ResourceParameters key = new ResourceParameters();
        key.setRpId(rpId);
        ResourceParameters record = this.load("resource_parameters.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(ResourceParameters record, ResourceParametersCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("resource_parameters.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(ResourceParameters record, ResourceParametersCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("resource_parameters.ibatorgenerated_updateByExample", parms);
    }

    public ResourceParameters loadFromWriteDB(Long rpId) {
        ResourceParameters key = new ResourceParameters();
        key.setRpId(rpId);
        ResourceParameters record = this.loadFromWriterDB("resource_parameters.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends ResourceParametersCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, ResourceParametersCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
