/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.workflow.dao;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistoryCriteria;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWorkFlowBuildHistoryDAO {

    /**
     * 是否有正在运行的task实例
     *
     * @param name
     * @return
     */
    boolean hasRunningInstance(DataXName name);

    int countByExample(WorkFlowBuildHistoryCriteria example);

    int countFromWriteDB(WorkFlowBuildHistoryCriteria example);

    int deleteByExample(WorkFlowBuildHistoryCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(WorkFlowBuildHistory record);

    Integer insertSelective(WorkFlowBuildHistory record);

    List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria criteria);

    List<WorkFlowBuildHistory> selectByExample(WorkFlowBuildHistoryCriteria example, int page, int pageSize);

    WorkFlowBuildHistory selectByPrimaryKey(Integer id);

    int updateByExampleSelective(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example);

    int updateByExample(WorkFlowBuildHistory record, WorkFlowBuildHistoryCriteria example);

    WorkFlowBuildHistory loadFromWriteDB(Integer id);

    /**
     * 从写库加载工作流实例（加行锁）
     * 用于并发控制，防止多个线程同时修改同一个工作流实例
     *
     * @param id 工作流实例 ID
     * @return 工作流实例
     */
    WorkFlowBuildHistory loadFromWriteDBWithLock(Integer id);

    /**
     * 查询卡住的工作流实例
     * 用于故障恢复，查找长时间处于 RUNNING 状态的实例
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 卡住的工作流实例列表
     */
    List<WorkFlowBuildHistory> selectStuckInstances(int timeoutMinutes);

    /**
     * 更新工作流实例状态
     *
     * @param record 工作流实例
     * @return 更新行数
     */
    int updateByPrimaryKeySelective(WorkFlowBuildHistory record);
}
