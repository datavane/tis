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
package com.qlangtech.tis.fullbuild.taskflow;

import java.util.Map;
import com.qlangtech.tis.fullbuild.taskflow.TaskWorkflow;
import com.qlangtech.tis.fullbuild.taskflow.impl.EndTask;
import com.qlangtech.tis.fullbuild.taskflow.impl.ForkTask;
import com.qlangtech.tis.fullbuild.taskflow.impl.StartTask;
import com.google.common.collect.Maps;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年12月1日
 */
public class TestWorkflow extends TestCase {

    public void testWorkflow() {
        TaskWorkflow workflow = new TaskWorkflow();
        StartTask start = new StartTask();
        start.setSuccessTo("fork");
        workflow.addTask(start);
        EndTask end = new EndTask();
        end.setName("final");
        workflow.addTask(end);
        ForkTask fork = new ForkTask();
        fork.setName("fork");
        fork.setSuccessTo("1,2,3");
        workflow.addTask(fork);
        TestTask t = new TestTask("1", "4");
        workflow.addTask(t);
        t = new TestTask("2", "4");
        workflow.addTask(t);
        t = new TestTask("3", "final");
        workflow.addTask(t);
        t = new TestTask("4", "fork2");
        workflow.addTask(t);
        fork = new ForkTask();
        fork.setName("fork2");
        fork.setSuccessTo("5,6,7");
        workflow.addTask(fork);
        t = new TestTask("5", "final");
        workflow.addTask(t);
        t = new TestTask("6", "final");
        workflow.addTask(t);
        t = new TestTask("7", "final");
        workflow.addTask(t);
        workflow.init();
        for (TaskDependency d : workflow.tasksMap.values()) {
            System.out.println(d.getTask().getName() + "->" + d.getPrecondition());
        }
        System.out.println("======================================");
        Map<String, Object> params = Maps.newHashMap();
        workflow.startExecute(params);
        System.out.println("execute successful");
    }
}
