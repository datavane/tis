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
package com.qlangtech.tis.fullbuild.servlet;

import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.order.center.IParamContext;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 15:39
 */
public class TestTisServlet extends TestCase {
    /**
     * 测试执行单表流程触发创建
     */
    public void testCreateNewTask() {

        HttpUtils.addMockApply(0, "do_create_new_task"
                , "create_new_task_single_table_index_build_response.json", TestTisServlet.class);

        String collectionName = "search4employee4local";
        Integer taskid = 800;
        String historyId = "666";
        TisServlet tisServlet = new TisServlet();
        IExecChainContext execChainContext = EasyMock.createMock("execChainContext", IExecChainContext.class);

        EasyMock.expect(execChainContext.getWorkflowId()).andReturn(null).anyTimes();
        execChainContext.setAttribute(IParamContext.KEY_TASK_ID, taskid);
        EasyMock.expect(execChainContext.getExecutePhaseRange()).andReturn(ExecutePhaseRange.fullRange());
        EasyMock.expect(execChainContext.hasIndexName()).andReturn(true);
        EasyMock.expect(execChainContext.getIndexName()).andReturn(collectionName);
        EasyMock.expect(execChainContext.getString(IFullBuildContext.KEY_BUILD_HISTORY_TASK_ID)).andReturn(historyId);
        EasyMock.replay(execChainContext);
        Integer newTask = tisServlet.createNewTask(execChainContext);
        assertEquals(taskid, newTask);

        EasyMock.verify(execChainContext);
    }
}
