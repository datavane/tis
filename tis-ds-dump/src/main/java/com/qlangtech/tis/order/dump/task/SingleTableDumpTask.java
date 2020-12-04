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
import com.tis.hadoop.rpc.StatusRpcClient;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单表导入，重新生成dump任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 3/31/2017.
 */
public class SingleTableDumpTask extends AbstractTableDumpTask implements ITableDumpConstant {

    private EntityName dumpTable;

    private final TisZkClient zkClient;

    private static final String TABLE_DUMP_ZK_PREFIX = "/tis/table_dump/";

    private SourceDataProviderFactory.ISourceDataProviderFactoryInspect sourceDataProviderFactoryInspect = (meta, f) -> {
    };

    private final AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusRpc;

    public SingleTableDumpTask(TableDumpFactory taskFactory, DataSourceFactory dataSourceFactory, TisZkClient zkClient, AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusRpc) {
        super(taskFactory, dataSourceFactory);
        if (zkClient == null) {
            throw new IllegalArgumentException("param zkClient can not be null");
        }
        this.zkClient = zkClient;
        this.statusRpc = statusRpc;
    }

    public void setSourceDataProviderFactoryInspect(SourceDataProviderFactory.ISourceDataProviderFactoryInspect sourceDataProviderFactoryInspect) {
        this.sourceDataProviderFactoryInspect = sourceDataProviderFactoryInspect;
    }

//    @Override
//    protected DBConfig getDataSourceConfig() {
//        return GitUtils.$().getDbLinkMetaData(this.dumpTable.getDbName(), DbScope.DETAILED);
//    }

    @Override
    protected void registerExtraBeanDefinition(DefaultListableBeanFactory factory) {
    }

    @Override
    public void map(TaskContext context) {
        init(context);
        super.map(context);
    }

    private void init(TaskContext context) {
        // DumpTable.create(context.get(DUMP_DBNAME), context.get(DUMP_TABLE_NAME));
        this.dumpTable = context.parseDumpTable();
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

        //this.dataSourceFactory
        SourceDataProviderFactory dataProviderFactory = new SourceDataProviderFactory();
        //final DBConfig dbLinkMetaData = null;
        //final Map<String, DataSource> dsMap = new HashMap<>();
        final StringBuffer dbNames = new StringBuffer();
        AtomicInteger dbCount = new AtomicInteger();
//        final DataSourceRegister.DBRegister dbRegister = new DataSourceRegister.DBRegister(dbLinkMetaData.getName(), dbLinkMetaData) {
//
//            @Override
//            protected void createDefinition(String dbDefinitionId, String driverClassName, String jdbcUrl, String userName, String password) {
//                BasicDataSource ds = new BasicDataSource();
//                ds.setDriverClassName(driverClassName);
//                ds.setUrl(jdbcUrl);
//                ds.setUsername(userName);
//                ds.setPassword(password);
//                ds.setValidationQuery("select 1");
//                synchronized (dbNames) {
//                    dsMap.put(dbDefinitionId, ds);
//                    dbCount.incrementAndGet();
//                    dbNames.append(dbDefinitionId).append(";");
//                }
//            }
//        };
//        dbRegister.visitAll();
//        if (dsMap.size() != dbCount.get()) {
//            throw new IllegalStateException("dsMap.size():" + dsMap.size() + ",dbCount.get():" + dbCount.get() + " shall be equal");
//        }
        // dataProviderFactory.setDataSourceGetter(dsMap::get);
        dataProvider.setSourceData(dataProviderFactory);
        beanFactory.setFullDumpProvider(dataProvider);
        //beanFactory.setGrouprouter(null);
        beanFactory.afterPropertiesSet(dbNames);
        // 单元测试过程中可以测试是否正常
        sourceDataProviderFactoryInspect.look(null, dataProviderFactory);
        TISDumpClient dumpBean = beanFactory.getObject();
        return dumpBean;
    }

}
