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
package com.qlangtech.tis.indexbuilder.merger;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.IPathInfo;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITISFileSystemFactory;
import com.qlangtech.tis.indexbuilder.IndexBuilderTask;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.index.IndexMerger;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.InterruptFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag.Flag;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.offline.FileSystemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexMergerImpl implements IndexMerger {

    public enum MergeMode {

        SERIAL, CONCURRENT
    }

    private final IndexSchema schema;

    private final SuccessFlag successFlag;

    private final ITISFileSystem fsFactory;

    public IndexMergerImpl(String name, IndexSchema schema, ITISFileSystem fsFactory) {
        this.successFlag = new SuccessFlag(name);
        this.schema = schema;
        this.fsFactory = fsFactory;
    }

    public static final Logger logger = LoggerFactory.getLogger(IndexMergerImpl.class);

    // private MergeScheduler mergeScheduler;
    private BlockingQueue<RAMDirectory> dirQueue;

    // private Directory[] diskIndexDirs;
    // private LargeBufferRAMDirectory []ramDirectorys;
    // private LargeBuffer
    // private RAMDirectory[] ramDirectorys;
    // private IndexWriter[] writers;
    // private IndexWriter[] ramWriters;
    private InterruptFlag interruptFlag;

    private AtomicInteger aliveIndexMakerCount;

    private IndexConf indexConf;

    // private String name;
    private Counters counters;

    private Messages messages;

    private long startTime;

    // private String[] cores;
    // private boolean mergings[];
    // private String taskAttemptId;
    private ThreadPoolExecutor es;

    // ArrayAllocator mergerAllocator;
    // private String taskAttemptId;
   // private ITISFileSystem fs;

    final AtomicInteger dirSeq = new AtomicInteger(0);

    public void init() throws Exception {
        successFlag.setMsg(Flag.SUCCESS, "start to index merge");
     //   fs = fsFactory;
        // 清理远程目标目录的旧索引
        cleanRemoteOutPath();
        this.startTime = System.currentTimeMillis();
        this.es = new BlockThreadPoolExecutor(indexConf.getMergeThreads(), indexConf.getMergeThreads(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(4));
    }

    @Override
    public void shutdown() {
        this.es.shutdownNow();
        try {
            this.es.awaitTermination(3, TimeUnit.SECONDS);
            logger.info("merge task has shutdow,successFlag:" + successFlag);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public SuccessFlag call() throws Exception {
        try {
            IndexBuilderTask.setMdcAppName(indexConf.getCollectionName());
            logger.warn(this.successFlag.getName() + " merge thread start!!!!!!!");
            // init();
            IndexWriter writer = IndexMaker.createRAMIndexWriter(this.indexConf, this.schema, true);
            AtomicInteger asynMergerThreadAliveCount = new AtomicInteger();
            RAMDirectory dir = null;
            while (true) {
                try {
                    dir = this.dirQueue.poll(20, TimeUnit.SECONDS);
                    if (Flag.FAILURE == this.successFlag.getFlag()) {
                        return successFlag;
                    }
                    if (dir == null) {
                        if (aliveIndexMakerCount.get() > 0) {
                            continue;
                        }
                        copy2Output(this.fsFactory, indexConf, writer, dirSeq);
                        while (asynMergerThreadAliveCount.get() > 0) {
                            // 全部异步执行的输出节点 还没有全部执行完成
                            logger.info("waitting for thread merge for index merge:" + asynMergerThreadAliveCount.get());
                            Thread.sleep(2000);
                        }
                        printSuccessMessage();
                        return successFlag;
                    }
                    writer.addIndexes(dir);
                    writer.commit();
                    long ramSize = ((RAMDirectory) writer.getDirectory()).ramBytesUsed();
                    boolean overSize = (ramSize >= this.indexConf.getOptimizeSizeThreshold());
                    logger.warn("ramSize=" + FileUtils.byteCountToDisplaySize(ramSize) + ",overSize:" + overSize);
                    // AtomicBoolean merging = new AtomicBoolean(true);
                    if (overSize) {
                        es.execute(new RamOptimizer(asynMergerThreadAliveCount, writer));
                        writer = IndexMaker.createRAMIndexWriter(this.indexConf, this.schema, true);
                    }
                } finally {
                    try {
                        if (dir != null) {
                            dir.close();
                        }
                    } catch (Throwable e) {
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("merge error:", e);
            // successFlag.setFlag(SuccessFlag.Flag.FAILURE);
            successFlag.setMsg(SuccessFlag.Flag.FAILURE, "merge error:" + e.toString());
        }
        return successFlag;
    }

    private void printSuccessMessage() {
        final String loggerMsg = successFlag.getName() + " end index!!!!!!!!!take:" + (System.currentTimeMillis() - startTime) / 1000 + " seconds";
        messages.addMessage(Messages.Message.INDEX_TIME, loggerMsg);
        logger.warn(loggerMsg);
        successFlag.setMsg(Flag.SUCCESS, loggerMsg);
    }

    class RamOptimizer implements Runnable {

        final IndexWriter writer;

        private final AtomicInteger asynMergerThreadAliveCount;

        public RamOptimizer(AtomicInteger asynMergerThreadAliveCount, IndexWriter writer) {
            this.writer = writer;
            asynMergerThreadAliveCount.incrementAndGet();
            this.asynMergerThreadAliveCount = asynMergerThreadAliveCount;
        }

        @Override
        public void run() {
            try {
                if (!writer.isOpen()) {
                    throw new IllegalStateException("index writer,  has been closed");
                }
                copy2Output(fsFactory, indexConf, this.writer, dirSeq);
            } catch (Throwable e) {
                logger.warn("-----", e);
                // successFlag.setFlag(Flag.FAILURE);
                successFlag.setMsg(Flag.FAILURE, stringifyException(e));
            } finally {
                asynMergerThreadAliveCount.decrementAndGet();
            }
        }
    }

    /**
     * 将dir中的内容写入到hdfs中
     *
     * @param fs
     * @param indexConf
     * @param writer
     * @param dirSeq
     * @throws Exception
     */
    private static void copy2Output(ITISFileSystem fs, IndexConf indexConf, IndexWriter writer, AtomicInteger dirSeq) throws Exception {
        long start = System.currentTimeMillis();
        writer.forceMerge(1, true);
        Directory dir = writer.getDirectory();
        writer.close();
        start = System.currentTimeMillis();
        String outPath = getRemoteOutSegPath(indexConf, fs, dirSeq);
        Directory remoteDir = AbstratFileSystemDirectory.createFileSystemDirectory(dir, fs, fs.getPath(outPath), true, indexConf);
        for (String file : dir.listAll()) {
            remoteDir.copyFrom(dir, file, file, IOContext.READONCE);
        }
        logger.warn("copy ramdir end,take " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        dir.close();
    }

    private void cleanRemoteOutPath() throws Exception {
        String destOutPath = indexConf.getOutputPath();
        IPath destPath = fsFactory.getPath(destOutPath);
        if (fsFactory.exists(destPath)) {
            List<IPathInfo> fileStatus = fsFactory.listChildren(destPath);
            if (fileStatus != null) {
                for (IPathInfo f : fileStatus) {
                    fsFactory.delete(f.getPath(), true);
                }
            }
            fsFactory.mkdirs(destPath);
        }
    }

    private static String getRemoteOutSegPath(IndexConf indexConf, ITISFileSystem fs, AtomicInteger dirSeq) throws Exception {
        String destOutPath = indexConf.getOutputPath();
        if (destOutPath == null) {
            throw new IllegalStateException(IndexBuildParam.INDEXING_OUTPUT_PATH + " param have not been config");
        }
        destOutPath = destOutPath + File.separator + "index";
        logger.warn("destOutPath1=" + destOutPath);
        destOutPath = destOutPath + File.separator + dirSeq.getAndIncrement();
        logger.warn("destOutPath2=" + destOutPath);
        return destOutPath;
    }

    public static String stringifyException(Throwable e) {
        StringWriter stm = new StringWriter();
        PrintWriter wrt = new PrintWriter(stm);
        e.printStackTrace(wrt);
        wrt.close();
        return stm.toString();
    }

    @Override
    public void setCounters(Counters counters) {
        this.counters = counters;
    }

    @Override
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void setAtomicInteger(AtomicInteger aliveIndexMakerCount) {
        this.aliveIndexMakerCount = aliveIndexMakerCount;
    }

    @Override
    public void setDirQueue(BlockingQueue<RAMDirectory> ramIndexQueue) {
        this.dirQueue = ramIndexQueue;
    }

    @Override
    public void setIndexConf(IndexConf indexConf) {
        this.indexConf = indexConf;
    }
}
