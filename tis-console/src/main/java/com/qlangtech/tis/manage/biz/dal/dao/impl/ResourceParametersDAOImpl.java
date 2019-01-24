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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
        Integer count = (Integer) this.count("resource_parameters.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(ResourceParametersCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("resource_parameters.ibatorgenerated_countByExample", example);
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
        ResourceParameters record = (ResourceParameters) this.load("resource_parameters.ibatorgenerated_selectByPrimaryKey", key);
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
        ResourceParameters record = (ResourceParameters) this.loadFromWriterDB("resource_parameters.ibatorgenerated_selectByPrimaryKey", key);
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
