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
package com.qlangtech.tis.fullbuild.taskflow.impl;

import java.util.Map;
import com.qlangtech.tis.fullbuild.taskflow.BasicTask;

/**
 * 分叉执行
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年8月9日上午10:24:44
 */
public class ForkTask extends BasicTask {

    // private List<BasicTask> tasks = new ArrayList<BasicTask>();
    // private void addTask(ODPSTask task) {
    // this.tasks.add(task);
    // }
    // 
    // public void addTask(List<ITask> subtask) {
    // this.tasks.addAll(subtask);
    // }
    // @Override
    // public void init(TaskWorkflow workflow) {
    // String[] tos = StringUtil.split(this.getSuccessTo(), ",");
    // BasicTask toTask = null;
    // for (String to : tos) {
    // toTask = tasksMap.get(to);
    // if (toTask == null) {
    // throw new IllegalStateException("to task:'" + to + "' can not be found in
    // taskMap");
    // }
    // tasks.add(toTask);
    // }
    // }
    @Override
    public void exexute(Map<String, Object> params) {
    // if (tasks.isEmpty()) {
    // throw new IllegalStateException("child task is empty");
    // }
    // 
    // for (final ITask task : tasks) {
    // ForTask.executor.execute(new Runnable() {
    // @Override
    // public void run() {
    // try {
    // task.exexute(params);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // }
    }
}
