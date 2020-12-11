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

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthority;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthorityCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        Integer count = this.count("biz_func_authority.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(BizFuncAuthorityCriteria example) {
        Integer count = this.countFromWriterDB("biz_func_authority.ibatorgenerated_countByExample", example);
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
        BizFuncAuthority record = this.load("biz_func_authority.ibatorgenerated_selectByPrimaryKey", key);
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
        BizFuncAuthority record = this.loadFromWriterDB("biz_func_authority.ibatorgenerated_selectByPrimaryKey", key);
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
