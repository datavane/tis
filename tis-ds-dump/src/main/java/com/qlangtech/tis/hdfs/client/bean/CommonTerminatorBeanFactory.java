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
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-18
 */
public class CommonTerminatorBeanFactory {

    private EntityName dumpTable;

    private Integer taskid;

    private TisZkClient tisZkClient;

    private RpcServiceReference statusReportRef;

    private final TableDumpFactory flatTableBuilder;
    private final DataSourceFactory dataSourceFactory;

    public CommonTerminatorBeanFactory(TableDumpFactory flatTableBuilder, DataSourceFactory dataSourceFactory) {
        Objects.requireNonNull(dataSourceFactory, "param dataSourceFactory can not be null");
        Objects.requireNonNull(flatTableBuilder, "param flatTableBuilder can not be null");
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
        // queryContext.afterPropertiesSet();
        TSearcherDumpContextImpl dumpContext = new TSearcherDumpContextImpl();
        if (statusReportRef == null) {
            throw new IllegalStateException("statusReportRef can not be null");
        }
        dumpContext.setDataSourceFactory(this.dataSourceFactory);

        TISTable tisTable = getTisTable(this.dumpTable);
        Objects.requireNonNull(tisTable, "tisTable can not be null");
        dumpContext.setTisTable(tisTable);

        dumpContext.setStatusReportRef(statusReportRef);
        dumpContext.setQueryContext(queryContext);
        dumpContext.setDataprocessor(this.dataprocess);
        dumpContext.setTaskId(this.getTaskid());

        logger.info("exectaskid:" + dumpContext.getTaskId());
        // dumpContext.afterPropertiesSet();
        SourceDataProvider datasourceProvider = null;
        initMultiThreadHdfsDataProvider(dumpContext);
        termiantorBean.setDumpContext(dumpContext);
        fullDumpProvider.setDumpContext(dumpContext);
        termiantorBean.setFullHdfsProvider(fullDumpProvider);
        // 让需要serviceconfig的模块全部设置上
        queryContext.fireServiceConfigChange();
        return dumpContext;
    }

    public static TISTable getTisTable(EntityName dumpTable) {
        try {
            DataSourceFactoryPluginStore dbPluginStore = TIS.getDataBasePluginStore(new PostedDSProp(dumpTable.getDbName()));
            return dbPluginStore.loadTableMeta(dumpTable.getTableName());
        } catch (Exception e) {
            throw new RuntimeException("dumpTable falid load table config:" + dumpTable.toString(), e);
        }
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

    //  @Override
    public TISDumpClient getObject() throws Exception {
        return this.termiantorBean;
    }

    public BatchDataProcessor<String, String> getDataprocess() {
        return dataprocess;
    }

    public void setDataprocess(BatchDataProcessor<String, String> dataprocess) {
        this.dataprocess = dataprocess;
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

    public RpcServiceReference getStatusReportRef() {
        return statusReportRef;
    }

    public void setStatusReportRef(RpcServiceReference statusReportRef) {
        this.statusReportRef = statusReportRef;
    }
}
