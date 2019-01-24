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
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthority;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthorityCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BizFuncAuthorityDAOImpl extends BasicDAO<BizFuncAuthority, BizFuncAuthorityCriteria> implements IBizFuncAuthorityDAO {

    public BizFuncAuthorityDAOImpl() {
        super();
    }

    @Override
    public List<BizFuncAuthority> selectAppDumpJob(BizFuncAuthorityCriteria criteria) {
        // 百岁 查找用户的 dump设置条目
        criteria.setPage(1);
        criteria.setPageSize(200);
        List<BizFuncAuthority> list = this.list("biz_func_authority.ibatorgenerated_select_out_join_app_trigger_job_relation_ByExample", criteria);
        return list;
    }

    @Override
    public String getEntityName() {
        return "biz_func_authority";
    }

    public int countByExample(BizFuncAuthorityCriteria example) {
        Integer count = (Integer) this.count("biz_func_authority.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(BizFuncAuthorityCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("biz_func_authority.ibatorgenerated_countByExample", example);
        return count;
    }

    public List<BizFuncAuthority> selectWithGroupByFuncidAppid(BizFuncAuthorityCriteria criteria) {
        criteria.setPage(1);
        criteria.setPageSize(100);
        return this.list("biz_func_authority.ibatorgenerated_selectwithgroup_by_funcid_appid", criteria);
    }

    public int deleteByExample(BizFuncAuthorityCriteria criteria) {
        return this.deleteRecords("biz_func_authority.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer bfId) {
        BizFuncAuthority key = new BizFuncAuthority();
        key.setBfId(bfId);
        return this.deleteRecords("biz_func_authority.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(BizFuncAuthority record) {
        Object newKey = this.insert("biz_func_authority.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(BizFuncAuthority record) {
        Object newKey = this.insert("biz_func_authority.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<BizFuncAuthority> selectByExample(BizFuncAuthorityCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    public List<BizFuncAuthority> selectByExample(BizFuncAuthorityCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<BizFuncAuthority> list = this.list("biz_func_authority.ibatorgenerated_selectByExample", example);
        return list;
    }

    public BizFuncAuthority selectByPrimaryKey(Integer bfId) {
        BizFuncAuthority key = new BizFuncAuthority();
        key.setBfId(bfId);
        BizFuncAuthority record = (BizFuncAuthority) this.load("biz_func_authority.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(BizFuncAuthority record, BizFuncAuthorityCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("biz_func_authority.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(BizFuncAuthority record, BizFuncAuthorityCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("biz_func_authority.ibatorgenerated_updateByExample", parms);
    }

    public BizFuncAuthority loadFromWriteDB(Integer bfId) {
        BizFuncAuthority key = new BizFuncAuthority();
        key.setBfId(bfId);
        BizFuncAuthority record = (BizFuncAuthority) this.loadFromWriterDB("biz_func_authority.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends BizFuncAuthorityCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, BizFuncAuthorityCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
