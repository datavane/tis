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
package com.qlangtech.tis.fullbuild.servlet;

import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.common.HttpUtils;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 15:39
 */
public class TestTisServlet {
    /**
     * 测试执行单表流程触发创建
     */
    @Test
    public void testCreateNewTask() {

        HttpUtils.addMockApply(0, "do_create_new_task", "create_new_task_single_table_index_build_response.json",
                TestTisServlet.class);

        String collectionName = "search4employee4local";
        Integer taskid = 800;
        String historyId = "666";
        File dagSpecPath = new File("/opt/data/tis/cfg_repo/tis_plugin_config/ap/mysql_mysql/dag-spec.json");
        TisServlet tisServlet = new TisServlet();
        IExecChainContext execChainContext = Mockito.mock(IExecChainContext.class, Mockito.withSettings().name("execChainContext"));

        Mockito.when(execChainContext.getWorkflowId()).thenReturn(null);
        Mockito.when(execChainContext.getExecutePhaseRange()).thenReturn(ExecutePhaseRange.fullRange());
        Mockito.when(execChainContext.hasIndexName()).thenReturn(true);
        Mockito.when(execChainContext.getIndexName()).thenReturn(collectionName);
        Mockito.when(execChainContext.getString(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID)).thenReturn(historyId);
        Integer newTask = IExecChainContext.createNewTask(execChainContext, dagSpecPath).getTaskid();
        assertEquals(taskid, newTask);

        Mockito.verify(execChainContext).setAttribute(JobCommon.KEY_TASK_ID, taskid);
        Mockito.verify(execChainContext).getExecutePhaseRange();
        Mockito.verify(execChainContext).hasIndexName();
        Mockito.verify(execChainContext).getIndexName();
        Mockito.verify(execChainContext).getString(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID);
    }
}
