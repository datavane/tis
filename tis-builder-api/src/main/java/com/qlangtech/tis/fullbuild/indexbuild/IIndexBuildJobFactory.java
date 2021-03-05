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
package com.qlangtech.tis.fullbuild.indexbuild;

import com.qlangtech.tis.build.task.IServerTask;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.order.center.IJoinTaskContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IIndexBuildJobFactory extends IServerTask {

    IndexBuildSourcePathCreator createIndexBuildSourcePathCreator(IJoinTaskContext execContext, ITabPartition ps);

    /**
     * 全量构建使用的文件系统
     *
     * @return
     */
    ITISFileSystem getFileSystem();

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
