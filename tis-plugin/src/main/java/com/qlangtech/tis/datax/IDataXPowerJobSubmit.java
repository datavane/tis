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

package com.qlangtech.tis.datax;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.powerjob.IDataFlowTopology;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * 与Powerjob 相关的任务提交
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/29
 */
public interface IDataXPowerJobSubmit {
    /**
     * 创建任务, 例如此处可以初始化powerjob的workflow实例
     *
     * @param module
     * @param context
     * @param dataxProcessor
     */
    public void createJob(IControlMsgHandler module, final Context context, DataxProcessor dataxProcessor);


    /**
     * 创建workflow任务
     *
     * @param module
     * @param context
     * @param topology
     */
    public void createWorkflowJob(IControlMsgHandler module, final Context context, IDataFlowTopology topology);


    /**
     * 取得所有当前管理的实例,Powerjob 中实现返回所有的workflow
     * pager.getCurPage(), pager.getRowsPerPage()
     *
     * @param <T>
     * @return
     */
    public <T> Pair<Integer, List<T>> fetchAllInstance(Map<String, Object> criteria, int page, int pageSize);

    /**
     * 更新任务
     * tech.powerjob.common.response.WorkflowInfoDTO
     *
     * @param module
     * @param context
     * @param dataxProcessor
     */
    public abstract <WorkflowInfoDTO> WorkflowInfoDTO saveJob(IControlMsgHandler module, Context context, DataxProcessor dataxProcessor);
}
