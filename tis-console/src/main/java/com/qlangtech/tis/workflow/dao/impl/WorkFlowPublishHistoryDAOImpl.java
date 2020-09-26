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

import com.qlangtech.tis.workflow.dao.IWorkFlowPublishHistoryDAO;
import com.qlangtech.tis.workflow.pojo.WorkFlowPublishHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowPublishHistoryCriteria;
import com.qlangtech.tis.manage.common.BasicDAO;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowPublishHistoryDAOImpl extends BasicDAO<WorkFlowPublishHistory, WorkFlowPublishHistoryCriteria> implements IWorkFlowPublishHistoryDAO {

    public WorkFlowPublishHistoryDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "work_flow_publish_history";
    }

    public int countByExample(WorkFlowPublishHistoryCriteria example) {
        Integer count = (Integer) this.count("work_flow_publish_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(WorkFlowPublishHistoryCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("work_flow_publish_history.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(WorkFlowPublishHistoryCriteria criteria) {
        return this.deleteRecords("work_flow_publish_history.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(Integer id) {
        WorkFlowPublishHistory key = new WorkFlowPublishHistory();
        key.setId(id);
        return this.deleteRecords("work_flow_publish_history.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public Integer insert(WorkFlowPublishHistory record) {
        Object newKey = this.insert("work_flow_publish_history.ibatorgenerated_insert", record);
        return (Integer) newKey;
    }

    public Integer insertSelective(WorkFlowPublishHistory record) {
        Object newKey = this.insert("work_flow_publish_history.ibatorgenerated_insertSelective", record);
        return (Integer) newKey;
    }

    public List<WorkFlowPublishHistory> selectByExampleWithBLOBs(WorkFlowPublishHistoryCriteria example) {
        return this.selectByExampleWithBLOBs((example), 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlowPublishHistory> selectByExampleWithBLOBs(WorkFlowPublishHistoryCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlowPublishHistory> list = this.list("work_flow_publish_history.ibatorgenerated_selectByExampleWithBLOBs", example);
        return list;
    }

    public List<WorkFlowPublishHistory> selectByExampleWithoutBLOBs(WorkFlowPublishHistoryCriteria criteria) {
        return this.selectByExampleWithoutBLOBs(criteria, 1, 100);
    }

    @SuppressWarnings("unchecked")
    public List<WorkFlowPublishHistory> selectByExampleWithoutBLOBs(WorkFlowPublishHistoryCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<WorkFlowPublishHistory> list = this.list("work_flow_publish_history.ibatorgenerated_selectByExample", example);
        return list;
    }

    public WorkFlowPublishHistory selectByPrimaryKey(Integer id) {
        WorkFlowPublishHistory key = new WorkFlowPublishHistory();
        key.setId(id);
        WorkFlowPublishHistory record = (WorkFlowPublishHistory) this.load("work_flow_publish_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_publish_history.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExampleWithBLOBs(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        int rows = this.updateRecords("work_flow_publish_history.ibatorgenerated_updateByExampleWithBLOBs", parms);
        return rows;
    }

    public int updateByExampleWithoutBLOBs(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("work_flow_publish_history.ibatorgenerated_updateByExample", parms);
    }

    public WorkFlowPublishHistory loadFromWriteDB(Integer id) {
        WorkFlowPublishHistory key = new WorkFlowPublishHistory();
        key.setId(id);
        WorkFlowPublishHistory record = (WorkFlowPublishHistory) this.loadFromWriterDB("work_flow_publish_history.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends WorkFlowPublishHistoryCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, WorkFlowPublishHistoryCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
