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
package com.qlangtech.tis.hdfs.client.context.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据DUMP上下文实现，（无状态）
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-12
 */
public class TSearcherDumpContextImpl implements TSearcherDumpContext {

    // private final static String FS_DEFAULT_NAME = "fs.default.name";
    protected static final Logger logger = LoggerFactory.getLogger(TSearcherDumpContextImpl.class);

    // protected String fsName;
    private TSearcherQueryContext queryContext;
    private DataSourceFactory dataSourceFactory;
    private Integer taskId;

    private TISTable tisTable;

    @Override
    public DataSourceFactory getDataSourceFactory() {
        if (this.dataSourceFactory == null) {
            throw new IllegalStateException("dataSourceFactory can not be null");
        }
        return this.dataSourceFactory;
    }

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    private final AtomicInteger allRows = new AtomicInteger();

    @Override
    public AtomicInteger getAllTableDumpRows() {
        return allRows;
    }

    @Override
    public TISTable getTisTable() {
        Objects.requireNonNull(this, "tisTable can not be null");
        return tisTable;
    }

    public void setTisTable(TISTable tisTable) {
        this.tisTable = tisTable;
    }

    private RpcServiceReference statusReportRef;

    @Override
    public StatusRpcClient.AssembleSvcCompsite getStatusReportRPC() {
        return this.statusReportRef.get();
    }

    public void setStatusReportRef(RpcServiceReference statusReportRef) {
        this.statusReportRef = statusReportRef;
    }

    @Override
    public Integer getTaskId() {
        if (this.taskId == null) {
            throw new IllegalStateException("taskId has not been set");
        }
        return this.taskId;
    }

    public void setTaskId(Integer taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId has not been set");
        }
        this.taskId = taskId;
    }

    @Override
    public void fireServiceConfigChange() {
        queryContext.fireServiceConfigChange();
    }

    @Override
    public TisZkClient getZkClient() {
        return queryContext.getZkClient();
    }


    public EntityName getDumpTable() {
        return this.queryContext.getDumpTable();
    }

    public TSearcherDumpContextImpl() {
        super();
    }

    @Override
    public Set<String> getGroupNameSet() {
        return Collections.emptySet();
    }

    public void setQueryContext(TSearcherQueryContext queryContext) {
        this.queryContext = queryContext;
    }

    @SuppressWarnings("all")
    private BatchDataProcessor dataprocessor;

    @SuppressWarnings("all")
    @Override
    public BatchDataProcessor getDataProcessor() {
        return this.dataprocessor;
    }

    @SuppressWarnings("all")
    public void setDataprocessor(BatchDataProcessor dataprocessor) {
        this.dataprocessor = dataprocessor;
    }
}
