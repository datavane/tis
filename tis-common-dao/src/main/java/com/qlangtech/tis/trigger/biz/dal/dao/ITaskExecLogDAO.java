/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.trigger.biz.dal.dao;

import com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLog;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLogCriteria;

import java.util.List;

public interface ITaskExecLogDAO {
    int countByExample(TaskExecLogCriteria example);

    int countFromWriteDB(TaskExecLogCriteria example);

    int deleteByExample(TaskExecLogCriteria criteria);

    int deleteByPrimaryKey(Long execLogId);

    Long insert(TaskExecLog record);

    Long insertSelective(TaskExecLog record);

    List<TaskExecLog> selectByExampleWithBLOBs(TaskExecLogCriteria example);

    List<TaskExecLog> selectByExampleWithoutBLOBs(TaskExecLogCriteria criteria);

    List<TaskExecLog> selectByExampleWithoutBLOBs(TaskExecLogCriteria example, int page, int pageSize);

    TaskExecLog selectByPrimaryKey(Long execLogId);

    int updateByExampleSelective(TaskExecLog record, TaskExecLogCriteria example);

    int updateByExampleWithBLOBs(TaskExecLog record, TaskExecLogCriteria example);

    int updateByExampleWithoutBLOBs(TaskExecLog record, TaskExecLogCriteria example);

    TaskExecLog loadFromWriteDB(Long execLogId);
}