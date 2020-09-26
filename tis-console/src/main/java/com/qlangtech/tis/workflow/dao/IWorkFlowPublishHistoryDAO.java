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
package com.qlangtech.tis.workflow.dao;

import com.qlangtech.tis.workflow.pojo.WorkFlowPublishHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowPublishHistoryCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWorkFlowPublishHistoryDAO {

    int countByExample(WorkFlowPublishHistoryCriteria example);

    int countFromWriteDB(WorkFlowPublishHistoryCriteria example);

    int deleteByExample(WorkFlowPublishHistoryCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(WorkFlowPublishHistory record);

    Integer insertSelective(WorkFlowPublishHistory record);

    List<WorkFlowPublishHistory> selectByExampleWithBLOBs(WorkFlowPublishHistoryCriteria example);

    List<WorkFlowPublishHistory> selectByExampleWithBLOBs(WorkFlowPublishHistoryCriteria example, int page, int pageSize);

    List<WorkFlowPublishHistory> selectByExampleWithoutBLOBs(WorkFlowPublishHistoryCriteria criteria);

    List<WorkFlowPublishHistory> selectByExampleWithoutBLOBs(WorkFlowPublishHistoryCriteria example, int page, int pageSize);

    WorkFlowPublishHistory selectByPrimaryKey(Integer id);

    int updateByExampleSelective(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example);

    int updateByExampleWithBLOBs(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example);

    int updateByExampleWithoutBLOBs(WorkFlowPublishHistory record, WorkFlowPublishHistoryCriteria example);

    WorkFlowPublishHistory loadFromWriteDB(Integer id);
}
