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
package com.qlangtech.tis.fullbuild.indexbuild;

import com.qlangtech.tis.build.task.IServerTask;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IIndexBuildJobFactory extends IServerTask {

    /**
     * 创建build索引任务
     *
     * @param timePoint
     * @param indexName
     * @param groupNum
     * @param buildParam
     * @return
     * @throws Exception
     */
    IRemoteJobTrigger createBuildJob(String timePoint, String indexName, String groupNum, IIndexBuildParam buildParam) throws Exception;
}
