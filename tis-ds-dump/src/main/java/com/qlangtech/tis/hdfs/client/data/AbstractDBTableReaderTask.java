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
package com.qlangtech.tis.hdfs.client.data;

import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.plugin.ds.IDataSourceDumper;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class AbstractDBTableReaderTask implements Runnable {

    protected final CountDownLatch latch;

    protected final IDataSourceDumper dumper;

    // private MultiThreadHDFSDataProvider hdfsProvider;
    // protected FileSystem fileSystem;
    // private String filePath;
    // private String threadName;
    // private Boolean waitForFinishedLock;
    // private int groupNum;
    // protected final String utf8StrTime;
    protected final AtomicInteger dbHostBusyCount;

    protected final AtomicInteger processErrorCount;

    protected final TSearcherDumpContext dumpContext;

    protected final Map<String, Object> threadResult;

    String getDbIP() {
        return dumper.getDbHost();
    }

    // end 百岁添加
    public AbstractDBTableReaderTask(CountDownLatch latch, IDataSourceDumper dumper, Map<String, Object> threadResult, AtomicInteger dbHostBusyCount, final AtomicInteger processErrorCount, TSearcherDumpContext dumpContext) {
        this.latch = latch;
        this.dumper = dumper;
        this.threadResult = threadResult;
        this.dbHostBusyCount = dbHostBusyCount;
        if (processErrorCount == null) {
            throw new IllegalArgumentException("processErrorCount can not be null");
        }
        this.processErrorCount = processErrorCount;
        this.dumpContext = dumpContext;
    }
}
