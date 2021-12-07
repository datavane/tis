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
package com.qlangtech.tis.order.dump.task;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.CommonTerminatorBeanFactory;
import com.qlangtech.tis.hdfs.client.bean.TISDumpClient;
import com.qlangtech.tis.hdfs.client.data.MultiThreadDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProviderFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.tis.hadoop.rpc.RpcServiceReference;

import java.util.Objects;

/**
 * 单表导入，重新生成dump任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 3/31/2017.
 */
public class SingleTableDumpTask extends AbstractTableDumpTask implements ITableDumpConstant {

    private final EntityName dumpTable;

    private final TisZkClient zkClient;

    private static final String TABLE_DUMP_ZK_PREFIX = "/tis/table_dump/";

    private final RpcServiceReference statusRpc;

    public SingleTableDumpTask(EntityName dumpTable, TableDumpFactory taskFactory, DataSourceFactory dataSourceFactory
            , TisZkClient zkClient, RpcServiceReference statusRpc) {
        super(taskFactory, dataSourceFactory);
        if (zkClient == null) {
            throw new IllegalArgumentException("param zkClient can not be null");
        }
        Objects.requireNonNull(statusRpc, "statusRpc can not be null");
        this.zkClient = zkClient;
        this.statusRpc = statusRpc;
        this.dumpTable = dumpTable;
    }


    @Override
    public void map(TaskContext context) {
        init(context);
        super.map(context);
    }

    private void init(TaskContext context) {
        //this.dumpTable = context.parseDumpTable();
        this.registerZKDumpNodeIn(context);
    }

    protected void registerZKDumpNodeIn(TaskContext context) {
        String path = TABLE_DUMP_ZK_PREFIX + this.dumpTable.getDbName() + "_" + this.dumpTable.getTableName();
        try {
            ZkUtils.registerTemporaryContent(this.zkClient, path, context.get(DUMP_START_TIME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TISDumpClient getDumpBeans(TaskContext context) throws Exception {
        CommonTerminatorBeanFactory beanFactory = new CommonTerminatorBeanFactory(this.tableDumpFactory, this.dataSourceFactory);
        beanFactory.setStatusReportRef(this.statusRpc);
        try {
            int taskid = Integer.parseInt(context.get(IParamContext.KEY_TASK_ID));
            beanFactory.setTaskid(taskid);
        } catch (Throwable e) {
            throw new IllegalArgumentException("param taskid is illegal", e);
        }
        beanFactory.setTisZkClient(this.zkClient);
        if (this.dumpTable == null) {
            throw new IllegalStateException("dumptable can not be null");
        }
        beanFactory.setDumpTable(this.dumpTable);
        MultiThreadDataProvider dataProvider = new MultiThreadDataProvider(tableDumpFactory, this.dataSourceFactory
                , MultiThreadDataProvider.DEFUALT_WAIT_QUEUE_SIZE, MultiThreadDataProvider.DEFUALT_WAIT_QUEUE_SIZE);

        SourceDataProviderFactory dataProviderFactory = new SourceDataProviderFactory();
        final StringBuffer dbNames = new StringBuffer();

        dataProvider.setSourceData(dataProviderFactory);
        beanFactory.setFullDumpProvider(dataProvider);
        beanFactory.afterPropertiesSet(dbNames);

        TISDumpClient dumpBean = beanFactory.getObject();
        return dumpBean;
    }

}
