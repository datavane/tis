/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.context;

import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.tis.hadoop.rpc.StatusRpcClient;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-11
 */
public interface TSearcherDumpContext extends TSearcherQueryContext {

    DataSourceFactory getDataSourceFactory();

    public StatusRpcClient.AssembleSvcCompsite getStatusReportRPC();

    TISTable getTisTable();

    public AtomicInteger getAllTableDumpRows();

    /**
     * 当前系统的用户
     * @return
     */
    // public String getCurrentUserName();
    /**
     * 每次执行dump都会生成一个新的taskid
     *
     * @return
     */
    public Integer getTaskId();

    @SuppressWarnings("all")
    public abstract BatchDataProcessor getDataProcessor();
    /**
     * ****************************************************
     * 以上属性都是和Dump相关的
     * *****************************************************
     */
}
