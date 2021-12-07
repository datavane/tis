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
