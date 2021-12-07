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

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.build.NodeMaster;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.dump.task.SingleTableDumpTask;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 到数据库表Yarn服务端入口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-15 15:25
 */
public class TableDumpNodeMaster extends NodeMaster {

    private static final Logger logger = LoggerFactory.getLogger(TableDumpNodeMaster.class);

    private final TableDumpFactory tableDumpFactory;
    private final DataSourceFactory dataSourceFactory;

    public TableDumpNodeMaster(TableDumpFactory factory, DataSourceFactory dataSourceFactory) {
        super(factory);
        Objects.requireNonNull(factory, "TableDumpFactory can not be null");
        Objects.requireNonNull(dataSourceFactory, "dataSourceFactory can not be null");
        this.tableDumpFactory = factory;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    protected void startExecute(TaskContext context, RpcServiceReference statusRpc) {

        TisZkClient zkClient = context.getCoordinator().unwrap();

        Objects.requireNonNull(zkClient, "zkClient can not be null");
        Objects.requireNonNull(statusRpc, "statusRpc can not be null");
        SingleTableDumpTask tableDumpTask = new SingleTableDumpTask(context.parseDumpTable(), tableDumpFactory, dataSourceFactory, zkClient, statusRpc);
        tableDumpTask.map(context);
    }
}
