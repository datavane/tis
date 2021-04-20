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
package com.qlangtech.tis.build.task;

import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;

/**
 * IRemoteJobTrigger 触发之后服务端对应的响应入口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface IServerTask {

    public void startTask(TaskMapper taskMapper, TaskContext taskContext) throws Exception;
}
