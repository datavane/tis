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
package com.qlangtech.tis.hdfs.client.context.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据DUMP上下文实现，（无状态）
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-12
 */
public class TSearcherDumpContextImpl implements TSearcherDumpContext, InitializingBean {

    // private final static String FS_DEFAULT_NAME = "fs.default.name";
    protected static final Log logger = LogFactory.getLog(TSearcherDumpContextImpl.class);

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

    private AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusReportRef;

    @Override
    public StatusRpcClient.AssembleSvcCompsite getStatusReportRPC() {
        return this.statusReportRef.get();
    }

    public void setStatusReportRef(AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusReportRef) {
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

    @Override
    public void afterPropertiesSet() throws Exception {
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
