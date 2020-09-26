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
package com.qlangtech.tis.hdfs.client.data;

import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class AbstractDBTableReaderTask implements Runnable {

    protected final CountDownLatch latch;

    protected final SourceDataProvider<String, String> dataProvider;

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
        return dataProvider.getDbHost();
    }

    // end 百岁添加
    public AbstractDBTableReaderTask(CountDownLatch latch, SourceDataProvider<String, String> dataProvider, Map<String, Object> threadResult, AtomicInteger dbHostBusyCount, final AtomicInteger processErrorCount, TSearcherDumpContext dumpContext) {
        this.latch = latch;
        this.dataProvider = dataProvider;
        this.threadResult = threadResult;
        this.dbHostBusyCount = dbHostBusyCount;
        if (processErrorCount == null) {
            throw new IllegalArgumentException("processErrorCount can not be null");
        }
        this.processErrorCount = processErrorCount;
        this.dumpContext = dumpContext;
    }
}
