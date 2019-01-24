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
package com.qlangtech.tis.trigger.biz.dal.dao;

import java.util.List;
import com.qlangtech.tis.trigger.biz.dal.pojo.ErrorJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.Task;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
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
