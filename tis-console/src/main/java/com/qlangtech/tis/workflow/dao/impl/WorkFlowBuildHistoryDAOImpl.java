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

import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowBuildHistoryDAOImpl extends BasicDAO<WorkFlowBuildHistory, WorkFlowBuildHistoryCriteria> implements IWorkFlowBuildHistoryDAO {

    public WorkFlowBuildHistoryDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "work_flow_build_history";
    }

    public int countByExample(WorkFlowBuildHistoryCriteria example) {
        Integer count = (Integer) this.count("work_flow_build_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WorkFlowBuildHistoryCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("work_flow_build_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WorkFlowBuildHistoryCriteria criteria) {
        return this.deleteRecords("work_flow_build_history.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        return this.deleteRecords("work_flow_build_history.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(WorkFlowBuildHistory record) {
        Object newKey = this.insert("work_flow_build_history.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(WorkFlowBuildHistory record) {
        Object newKey = this.insert("work_flow_build_history.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlowBuildHistory> list = this.list("work_flow_build_history.ibatorgenerated_selectByExample", example);
        return list;
    }

    public WorkFlowBuildHistory selectByPrimaryKey(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        WorkFlowBuildHistory record = (WorkFlowBuildHistory) this.load("work_flow_build_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_build_history.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_build_history.ibatorgenerated_updateByExample", parms);
    }

    public WorkFlowBuildHistory loadFromWriteDB(Integer id) {
        WorkFlowBuildHistory key = new WorkFlowBuildHistory();
        key.setId(id);
        WorkFlowBuildHistory record = (WorkFlowBuildHistory) this.loadFromWriterDB("work_flow_build_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WorkFlowBuildHistoryCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WorkFlowBuildHistoryCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
