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
package com.qlangtech.tis.workflow.dao.impl;

import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowDAOImpl extends BasicDAO<WorkFlow, WorkFlowCriteria> implements IWorkFlowDAO {

    public WorkFlowDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "work_flow";
    }

    public int countByExample(WorkFlowCriteria example) {
        Integer count = (Integer) this.count("work_flow.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WorkFlowCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("work_flow.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WorkFlowCriteria criteria) {
        return this.deleteRecords("work_flow.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        WorkFlow key = new WorkFlow();
        key.setId(id);
        return this.deleteRecords("work_flow.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(WorkFlow record) {
        Object newKey = this.insert("work_flow.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(WorkFlow record) {
        Object newKey = this.insert("work_flow.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<WorkFlow> selectByExample(WorkFlowCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlow> selectByExample(WorkFlowCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlow> list = this.list("work_flow.ibatorgenerated_selectByExample", example);
        return list;
    }

    public List<WorkFlow> minSelectByExample(WorkFlowCriteria criteria) {
        return this.minSelectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlow> minSelectByExample(WorkFlowCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlow> list = this.list("work_flow.ibatorgenerated_minSelectByExample", example);
        return list;
    }

    public WorkFlow selectByPrimaryKey(Integer id) {
        WorkFlow key = new WorkFlow();
        key.setId(id);
        WorkFlow record = (WorkFlow) this.load("work_flow.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(WorkFlow record, WorkFlowCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(WorkFlow record, WorkFlowCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow.ibatorgenerated_updateByExample", parms);
    }

    public WorkFlow loadFromWriteDB(Integer id) {
        WorkFlow key = new WorkFlow();
        key.setId(id);
        WorkFlow record = (WorkFlow) this.loadFromWriterDB("work_flow.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WorkFlowCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WorkFlowCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
