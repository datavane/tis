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
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.fullbuild.indexbuild.IIndexBuildParam;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-25 14:52
 */
public class DefaultIndexBuilderTriggerFactory extends IndexBuilderTriggerFactory {

    private static final String NAME = "testIndexBuilderTriggerFactory";

    private final FileSystemFactory buildFileSystem;

    public DefaultIndexBuilderTriggerFactory(FileSystemFactory buildFileSystem) {
        this.buildFileSystem = buildFileSystem;
    }

    @Override
    public FileSystemFactory getFsFactory() {
        return this.buildFileSystem;
    }

    @Override
    public IRemoteJobTrigger createBuildJob(String timePoint, String indexName, String groupNum, IIndexBuildParam buildParam) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startTask(TaskMapper taskMapper, TaskContext taskContext) throws Exception {
        taskMapper.map(taskContext);
    // if (execResult.getReturnCode() == TaskReturn.ReturnCode.FAILURE) {
    // throw new RuntimeException("execResult.getReturnCode() invalid:" + execResult.getReturnCode());
    // }
    }

    @Override
    public String identityValue() {
        return NAME;
    }
//    @Override
//    public String getName() {
//        return NAME;
//    }
}
