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

import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWorkFlowDAO {

    int countByExample(WorkFlowCriteria example);

    int countFromWriteDB(WorkFlowCriteria example);

    int deleteByExample(WorkFlowCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(WorkFlow record);

    Integer insertSelective(WorkFlow record);

    List<WorkFlow> selectByExample(WorkFlowCriteria criteria);

    List<WorkFlow> selectByExample(WorkFlowCriteria example, int page, int pageSize);

    List<WorkFlow> minSelectByExample(WorkFlowCriteria criteria);

    List<WorkFlow> minSelectByExample(WorkFlowCriteria example, int page, int pageSize);

    WorkFlow selectByPrimaryKey(Integer id);

    int updateByExampleSelective(WorkFlow record, WorkFlowCriteria example);

    int updateByExample(WorkFlow record, WorkFlowCriteria example);

    WorkFlow loadFromWriteDB(Integer id);
}
