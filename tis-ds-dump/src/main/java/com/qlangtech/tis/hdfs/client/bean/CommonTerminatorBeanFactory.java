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
package com.qlangtech.tis.hdfs.client.bean;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl;
import com.qlangtech.tis.hdfs.client.data.MultiThreadDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProviderFactory;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-18
 */
public class CommonTerminatorBeanFactory implements FactoryBean<TISDumpClient> {

    private EntityName dumpTable;

    private Integer taskid;

    private TisZkClient tisZkClient;

    private AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusReportRef;

    private final TableDumpFactory flatTableBuilder;
    private final DataSourceFactory dataSourceFactory;

    public CommonTerminatorBeanFactory(TableDumpFactory flatTableBuilder, DataSourceFactory dataSourceFactory) {
        this.flatTableBuilder = flatTableBuilder;
        this.dataSourceFactory = dataSourceFactory;
    }

    @SuppressWarnings("all")
    private MultiThreadDataProvider fullDumpProvider;

    // @SuppressWarnings("all")
    // private HDFSProvider<String, String> incrDumpProvider;
    private TISDumpClient termiantorBean;

    private static final Logger logger = LoggerFactory.getLogger(CommonTerminatorBeanFactory.class);

    private BatchDataProcessor<String, String> dataprocess;

    public Integer getTaskid() {
        return taskid;
    }

    public void setTaskid(Integer taskid) {
        this.taskid = taskid;
    }

    @SuppressWarnings("all")
    public TSearcherDumpContextImpl afterPropertiesSet(StringBuffer dbNames) throws Exception {
        termiantorBean = createTerminatorBean();
        final TSearcherQueryContextImpl queryContext = new TSearcherQueryContextImpl();
        if (this.tisZkClient == null) {
            throw new IllegalStateException("solrZkClient can not be null");
        }
        queryContext.setZkClient(this.tisZkClient);
        queryContext.setDumpTable(this.dumpTable);
        queryContext.afterPropertiesSet();
        TSearcherDumpContextImpl dumpContext = new TSearcherDumpContextImpl();
        if (statusReportRef == null) {
            throw new IllegalStateException("statusReportRef can not be null");
        }
        dumpContext.setDataSourceFactory(this.dataSourceFactory);

        DataSourceFactoryPluginStore dbPluginStore = TIS.getDataBasePluginStore(null, new PostedDSProp(dumpTable.getDbName()));
        dumpContext.setTisTable(dbPluginStore.loadTableMeta(dumpTable.getTableName()));

        // dumpContext.setTisTable(GitUtils.$().getTableConfig(dumpTable.getDbName(), dumpTable.getTableName()));
        dumpContext.setStatusReportRef(statusReportRef);
        dumpContext.setQueryContext(queryContext);
        dumpContext.setDataprocessor(this.dataprocess);
        dumpContext.setTaskId(this.getTaskid());

        logger.info("exectaskid:" + dumpContext.getTaskId());
        dumpContext.afterPropertiesSet();
        SourceDataProvider datasourceProvider = null;
        initMultiThreadHdfsDataProvider(dumpContext);
        termiantorBean.setDumpContext(dumpContext);
        fullDumpProvider.setDumpContext(dumpContext);
        termiantorBean.setFullHdfsProvider(fullDumpProvider);
        // 让需要serviceconfig的模块全部设置上
        queryContext.fireServiceConfigChange();
        return dumpContext;
    }

    @Override
    public Class<? extends TISDumpClient> getObjectType() {
        return TISDumpClient.class;
    }

    protected TISDumpClient createTerminatorBean() {
        return new TISDumpClient(this.flatTableBuilder);
    }

    public void setTisZkClient(TisZkClient tisZkClient) {
        this.tisZkClient = tisZkClient;
    }

    @SuppressWarnings("all")
    private void initMultiThreadHdfsDataProvider(TSearcherDumpContextImpl dumpContext) throws Exception {

        DataSourceFactory dataSourceFactory = this.dataSourceFactory;

        SourceDataProviderFactory datasourceProvider = ((MultiThreadDataProvider) fullDumpProvider).getSourceData();
        if (datasourceProvider != null) {
            datasourceProvider.setDumpContext(dumpContext);
            datasourceProvider.init();
        }
    }

    @Override
    public TISDumpClient getObject() throws Exception {
        return this.termiantorBean;
    }

    public BatchDataProcessor<String, String> getDataprocess() {
        return dataprocess;
    }

    public void setDataprocess(BatchDataProcessor<String, String> dataprocess) {
        this.dataprocess = dataprocess;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public EntityName getDumpTable() {
        return this.dumpTable;
    }

    public void setDumpTable(EntityName dumpTable) {
        this.dumpTable = dumpTable;
    }

    public MultiThreadDataProvider getFullDumpProvider() {
        return fullDumpProvider;
    }

    @SuppressWarnings("all")
    public void setFullDumpProvider(MultiThreadDataProvider fullDumpProvider) {
        this.fullDumpProvider = fullDumpProvider;
    }


    public void setTermiantorBean(TISDumpClient termiantorBean) {
        this.termiantorBean = termiantorBean;
    }

    public AtomicReference<StatusRpcClient.AssembleSvcCompsite> getStatusReportRef() {
        return statusReportRef;
    }

    public void setStatusReportRef(AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusReportRef) {
        this.statusReportRef = statusReportRef;
    }
}
