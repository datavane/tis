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


import com.qlangtech.tis.trigger.biz.dal.pojo.ErrorJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.Task;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

import java.util.List;

public interface ITaskDAO {
    int countByExample(TaskCriteria example);

    int countFromWriteDB(TaskCriteria example);

    int deleteByExample(TaskCriteria criteria);

    int deleteByPrimaryKey(Long taskId);

    Long insert(Task record);

    Long insertSelective(Task record);

    List<Task> selectByExample(TaskCriteria criteria);

    List<Task> selectByExample(TaskCriteria example, int page, int pageSize);

    Task selectByPrimaryKey(Long taskId);

    int updateByExampleSelective(Task record, TaskCriteria example);

    int updateByExample(Task record, TaskCriteria example);

    Task loadFromWriteDB(Long taskId);

    List<ErrorJob> getRecentExecuteJobs(String environment);
}