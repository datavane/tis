/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.meta;

import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.tdfs.TDFSLinker;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-04-05 15:30
 **/
public abstract class MetaDataWriter implements Describable<MetaDataWriter> {


    /**
     * 创建元数据写入异步任务
     *
     * @param dfsLinker
     * @param tab
     * @return
     */
    public abstract IRemoteTaskPreviousTrigger createMetaDataWriteTask(TDFSLinker dfsLinker,
                                                                       IExecChainContext execContext,
                                                                       EntityName entity, ISelectedTab tab);

    /**
     * 得到存放元数据文件的路径
     *
     * @param dfsLinker
     * @param tableName
     * @return
     */
    public String getDfsTargetDir(TDFSLinker dfsLinker, String tableName) {
        return dfsLinker.getRootPath();
    }
}
