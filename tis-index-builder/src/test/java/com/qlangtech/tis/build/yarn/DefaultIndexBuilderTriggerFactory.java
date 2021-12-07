/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.indexbuild.*;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.order.center.IJoinTaskContext;

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
    public ITISFileSystem getFileSystem() {
        return this.buildFileSystem.getFileSystem();
    }

    @Override
    public IRemoteJobTrigger createBuildJob(IJoinTaskContext execContext, String timePoint, String indexName, String groupNum, IIndexBuildParam buildParam) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexBuildSourcePathCreator createIndexBuildSourcePathCreator(IJoinTaskContext execContext, ITabPartition ps) {
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
}
