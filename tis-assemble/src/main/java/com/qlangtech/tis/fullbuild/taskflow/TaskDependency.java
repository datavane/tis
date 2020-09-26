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
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * 保存了一个Task提來的前置任務
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年11月30日
 */
public class TaskDependency {

    private final BasicTask task;

    private final Map<String, AtomicBoolean> /* taskName */
    precondition = Maps.newHashMap();

    public TaskDependency(BasicTask task) {
        super();
        this.task = task;
    }

    public int getPreconditionCount() {
        return this.precondition.size();
    }

    public String getPrecondition() {
        return Joiner.on(",").join(precondition.keySet());
    }

    /**
     * 初始化狀態
     *
     * @param tasksMap
     */
    public void init(TaskWorkflow workflow) {
        for (BasicTask t : workflow.getAllTask()) {
            String[] tos = StringUtils.split(t.getSuccessTo(), ",");
            if (tos != null) {
                for (String to : tos) {
                    if (StringUtils.equals(task.getName(), to)) {
                        precondition.put(t.getName(), new AtomicBoolean(false));
                    }
                }
            }
        }
    }

    /**
     * 前置条件是否都满足
     *
     * @return
     */
    public synchronized boolean isAllSatisfaction() {
        for (AtomicBoolean satisfaction : precondition.values()) {
            if (!satisfaction.get()) {
                return false;
            }
        }
        return true;
    }

    public synchronized void satisfaction(String preConditionTaskName) {
        AtomicBoolean pre = this.precondition.get(preConditionTaskName);
        if (pre == null) {
            throw new IllegalStateException("task " + preConditionTaskName + " is not the pre condition of " + task.getName());
        }
        pre.set(true);
    }

    public boolean exexute(Map<String, Object> params) {
        if (!isAllSatisfaction()) {
            return false;
        }
        this.task.exexute(params);
        System.out.println("===========task:" + this.task.getName() + " successful");
        return true;
    }

    public synchronized void addPrecondition(String taskName) {
        precondition.put(taskName, new AtomicBoolean(false));
    }

    public BasicTask getTask() {
        return this.task;
    }
}
