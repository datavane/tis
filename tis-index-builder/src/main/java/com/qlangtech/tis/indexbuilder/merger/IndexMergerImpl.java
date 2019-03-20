/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.indexbuilder.merger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.indexbuilder.index.IndexMaker;
import com.qlangtech.tis.indexbuilder.index.IndexMerger;
import com.qlangtech.tis.indexbuilder.map.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.InterruptFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag;
import com.qlangtech.tis.indexbuilder.map.SuccessFlag.Flag;
import com.qlangtech.tis.manage.common.IndexBuildParam;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexMergerImpl implements IndexMerger {

	public enum MergeMode {

		SERIAL, CONCURRENT
	}

	private final IndexSchema schema;
	private final SuccessFlag successFlag;

	public IndexMergerImpl(String name, IndexSchema schema) {
		this.successFlag = new SuccessFlag(name);
		this.schema = schema;
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
	private ExecutorService es;

	// ArrayAllocator mergerAllocator;
	// private String taskAttemptId;
	private FileSystem fs;

	final AtomicInteger dirSeq = new AtomicInteger(0);

	public void init() throws Throwable {
		// logger.warn("11-------------------------1------------");
		// Configuration conf = new Configuration();
		// logger.warn("loader2:"+conf.getClass().getClassLoader());
		String fsName = indexConf.getFsName();
		logger.warn("fsName=" + fsName);
		// conf.set("fs.default.name", fsName);
		// logger.warn("conf:"+conf.toString());
		successFlag.setFlag(Flag.SUCCESS);
		/*
		 * for(Iterator<Map.Entry<String, String>> it =
		 * conf.iterator();it.hasNext();) { Map.Entry<String, String> entry =
		 * it.next();
		 * logger.warn("name="+entry.getKey()+",value="+entry.getValue()); }
		 */
		// FileSystem.get(FileSystem.getDefaultUri(conf),
		fs = TISHdfsUtils.getFileSystem();
		// conf);
		// 清理远程目标目录的旧索引
		cleanRemoteOutPath();
		/*
		 * ClassLoadUtil mapclu = new ClassLoadUtil();
		 * mapclu.loadJar(indexConf.getHdfsjarpath(),null);
		 * Thread.currentThread().setContextClassLoader(mapclu.getLoader());
		 * FileSystemUtils fsu = new FileSystemUtils(); fs =
		 * fsu.getFileSystem(indexConf.getFsName(),null);
		 */
		this.startTime = System.currentTimeMillis();
		// cores = indexConf.getCores();
		// dirSeq = 0;
		// this.diskIndexDirs = new Directory[cores.length];
		// this.ramDirectorys = new RAMDirectory[cores.length];
		// this.setRamWriters(new IndexWriter[cores.length]);
		// this.writers = new IndexWriter[cores.length];call
		this.es = Executors.newFixedThreadPool(indexConf.getMergeThreads());
		
	}

	
	@Override
	public SuccessFlag call() throws Exception {
		try {
			HdfsIndexBuilder.setMdcAppName(indexConf.getCollectionName());
			logger.warn(this.successFlag.getName() + " merge thread start!!!!!!!");
			init();
			IndexWriter writer = IndexMaker.createRAMIndexWriter(this.indexConf, this.schema, true/* merge */);
			AtomicInteger asynMergerThreadAliveCount = new AtomicInteger();
			RAMDirectory dir = null;
			while (true) {
				dir = this.dirQueue.poll(20, TimeUnit.SECONDS);
				if (Flag.FAILURE == this.successFlag.getFlag()) {
					return successFlag;
				}
				if (dir == null) {
					if (aliveIndexMakerCount.get() > 0) {
						continue;
					}
					copy2Output(fs, indexConf, writer, dirSeq);
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
					writer = IndexMaker.createRAMIndexWriter(this.indexConf, this.schema, true/* merge */);
				}
			}
		} catch (Throwable e) {
			logger.error("merge error:", e);
			successFlag.setFlag(SuccessFlag.Flag.FAILURE);
			successFlag.setMsg("merge error:" + e.toString());
		}
		return successFlag;
	}

	private void printSuccessMessage() {
		messages.addMessage(Messages.Message.INDEX_TIME, (System.currentTimeMillis() - startTime) / 1000 + " seconds");
		logger.warn(successFlag.getName() + " end index!!!!!!!!!take:" + (System.currentTimeMillis() - startTime) / 1000
				+ " seconds");
		successFlag.setFlag(Flag.SUCCESS);
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
				copy2Output(fs, indexConf, this.writer, dirSeq);
			} catch (Throwable e) {
				logger.warn("-----", e);
				successFlag.setFlag(Flag.FAILURE);
				successFlag.setMsg(stringifyException(e));
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
	private static void copy2Output(FileSystem fs, IndexConf indexConf, IndexWriter writer, AtomicInteger dirSeq)
			throws Exception {
		long start = System.currentTimeMillis();
		writer.forceMerge(1, true);
		Directory dir = writer.getDirectory();
		writer.close();
		start = System.currentTimeMillis();
		String outPath = getRemoteOutSegPath(indexConf, fs, dirSeq);
		Directory remoteDir = AbstratFileSystemDirectory.createFileSystemDirectory(dir, fs, new Path(outPath), true,
				indexConf);
		for (String file : dir.listAll()) {
			remoteDir.copyFrom(dir, file, file, IOContext.READONCE);
		}
		logger.warn("copy ramdir end,take " + (System.currentTimeMillis() - start) / 1000 + " seconds");
		dir.close();
	}

	private void cleanRemoteOutPath() throws Exception {
		String destOutPath = indexConf.getOutputPath();

		Path destPath = new Path(destOutPath);
		if (fs.exists(destPath)) {
			FileStatus[] fileStatus = fs.listStatus(destPath);
			if (fileStatus != null) {
				for (FileStatus f : fileStatus) {
					fs.delete(f.getPath(), true);
				}
			}
			fs.mkdirs(destPath);
		}
	}

	private static String getRemoteOutSegPath(IndexConf indexConf, FileSystem fs, AtomicInteger dirSeq)
			throws Exception {
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
