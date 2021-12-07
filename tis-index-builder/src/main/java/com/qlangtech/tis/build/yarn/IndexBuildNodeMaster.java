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

import com.qlangtech.tis.build.NodeMaster;
import com.qlangtech.tis.fs.FSHistoryFileUtils;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.IndexBuilderTask;
import com.qlangtech.tis.indexbuilder.map.IndexGetConfig;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.tis.hadoop.rpc.RpcServiceReference;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-15 16:08
 */
public class IndexBuildNodeMaster extends NodeMaster {

    private final IndexBuilderTriggerFactory propsGetter;

    public IndexBuildNodeMaster(IndexBuilderTriggerFactory propsGetter) {
        super(propsGetter);
        this.propsGetter = propsGetter;
    }

    @Override
    protected void startExecute(TaskContext context, RpcServiceReference statusRpc) {
        executeIndexBuild(context, this.propsGetter, statusRpc);
    }

    public static void executeIndexBuild(TaskContext context, IndexBuilderTriggerFactory buildTrigger, RpcServiceReference statusRpc) {
        executeIndexBuild(context, buildTrigger, statusRpc, true);
    }

    /**
     * 倒排索引构建入口
     *
     * @param context
     * @param buildTrigger
     * @param statusRpc
     */
    public static void executeIndexBuild(TaskContext context, IndexBuilderTriggerFactory buildTrigger
            , RpcServiceReference statusRpc, boolean shllCopyCfgResFromRemote) {
        if (shllCopyCfgResFromRemote) {
            /* 执行索引build start */
            IndexGetConfig configJob = new IndexGetConfig(buildTrigger.getFileSystem());
            configJob.map(context);
            configJob.removeTmpConfigDirOnExit(context.getCollectionName());
        }
        IndexBuilderTask indexBuilder = new IndexBuilderTask(buildTrigger, statusRpc);
        if (!shllCopyCfgResFromRemote) {
            // 当使用单表拷贝的时候不需要事先拷贝到FS文件系统上，只要从默认的文件系统中读取就行
            indexBuilder.setSchemaCfgFileGetter((cfg) -> {
                String coreName = context.get(IndexBuildParam.INDEXING_CORE_NAME);
                File schemaFile = new File(ConfigFileReader.FILE_SCHEMA.getFsPath(buildTrigger.getFileSystem(), coreName));
                if (!schemaFile.exists()) {
                    throw new IllegalStateException("schema file in not exist:" + schemaFile.getAbsolutePath());
                }
                return schemaFile;
            });
        }

        // 删除历史build索引文件
        FSHistoryFileUtils.removeHistoryBuildFile(buildTrigger.getFileSystem(), context.getCollectionName());
        indexBuilder.map(context);
    }


}
