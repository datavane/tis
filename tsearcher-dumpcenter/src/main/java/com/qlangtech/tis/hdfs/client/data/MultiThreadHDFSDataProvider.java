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
package com.qlangtech.tis.hdfs.client.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.exception.DataImportHDFSException;
import com.qlangtech.tis.exception.TerminatorInitException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.data.SingleTableSqlExcuteProvider.RowMetaData;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.hdfs.util.Constants;
import com.qlangtech.tis.hdfs.util.FormatTool;
import com.qlangtech.tis.hdfs.util.ServiceNameAware;
import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;
import com.qlangtech.tis.trigger.util.TriggerParam;

/*
 * @description 在写入方面为了充分利用HDFS集群的写入，针对一个表或者一个库启动一个线程写入<br>
 *              如果有分组情况，最终写入HDFS都是以分组为单位的文件<br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultiThreadHDFSDataProvider implements // Configurable,
		HDFSProvider<String, String>, ServiceNameAware {

	// 正在执行dump数据库的繁忙程度
	private static final ConcurrentHashMap<String, AtomicInteger> /* dbip */
	dbBusyStatistics = new ConcurrentHashMap<>();

	// 百岁 add20130513
	// 默认最大并发dump任务数量
	public static final int DEFAULT_MAX_POOL_SIZE = 4;

	// 默认任务等待队列长度
	public static final int DEFUALT_WAIT_QUEUE_SIZE = 10;

	private static final ExecutorService dbReaderExecutor;

	static {
		dbReaderExecutor = createDbReaderExecutor();
	}

	protected static final Log log = LogFactory.getLog(MultiThreadHDFSDataProvider.class);

	private AtomicInteger writerFinishedCount = new AtomicInteger(0);

	private AtomicInteger shardInitCount;

	private static final String TAB = "\t";

	private static final String NEWLINE = "\n";

	// private FileSystem fileSystem;
	// private String serviceName;
	// private BufferedWriter successResultsWriter = null;
	// protected FileTimeProvider timeManager;
	// private BufferedWriter errorResultsWriter = null;
	private AtomicBoolean isIncrOrNot = new AtomicBoolean(true);

	private AtomicBoolean isBusy = new AtomicBoolean(false);

	private AtomicBoolean hasWriteTitles = new AtomicBoolean(false);

	private List<RowMetaData> rowKeys;

	// private Set<String> groupNameSet;
	// private final ThreadPoolExecutor executor;
	// private int wirteHdfsThreadCount;
	private static AtomicInteger getDBServerBusyCount(String dbHost) {
		if (StringUtils.isBlank(dbHost)) {
			throw new IllegalArgumentException("dbHost can not be null");
		}
		AtomicInteger count = dbBusyStatistics.get(dbHost);
		if (count == null) {
			count = new AtomicInteger(0);
			AtomicInteger old = null;
			if ((old = dbBusyStatistics.putIfAbsent(dbHost, count)) != null) {
				count = old;
			}
		}
		return count;
	}

	@Override
	public void init() throws TerminatorInitException {
	}

	private TSearcherDumpContext dumpContext;

	private final int maxPoolSize;

	public MultiThreadHDFSDataProvider(int maxPoolSize, int waitQueueSize) {
		super();
		this.maxPoolSize = maxPoolSize;
		// BlockingQueue<Runnable> queue = new
		// ArrayBlockingQueue<Runnable>(waitQueueSize);
		// this.executor = new ThreadPoolExecutor(maxPoolSize, maxPoolSize, 30,
		// TimeUnit.SECONDS, queue,
		// new ThreadFactory() {
		// private AtomicInteger id = new AtomicInteger(0);
		//
		// public Thread newThread(Runnable r) {
		// return new Thread(r, "file read thread #" + id.getAndIncrement());
		// }
		// }, new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Override
	public String getServiceName() {
		return dumpContext.getServiceName();
	}

	public TSearcherDumpContext getDumpContext() {
		return dumpContext;
	}

	public void setDumpContext(TSearcherDumpContext dumpContext) {
		this.dumpContext = dumpContext;
	}

	// public void setGroupNameSet(Set<String> groupNameSet) {
	// this.groupNameSet = groupNameSet;
	// }
	// private ConcurrentMap<String, CopyOnWriteArrayList<HDFSImportTask>>
	// waitQueue;
	// private ConcurrentMap<String, List> finishTitleList;
	// private ConcurrentMap<String, String> cumap;
	// public Map getCumap() {
	// return this.cumap;
	// }
	// private ConcurrentMap<String, AtomicLong> shardRow;
	protected int configGroupNum(int groupNum) {
		return groupNum;
	}

	public Path createPath(int num, String utf8StrTime, boolean writeEndSuc) {
		StringBuffer sbPath = new StringBuffer();
		sbPath.append(this.dumpContext.getServiceName()).append(Path.SEPARATOR)
				.append(isIncrOrNot.get() ? "incr" : "all").append(Path.SEPARATOR).append(num).append(Path.SEPARATOR)
				.append(utf8StrTime).append(Path.SEPARATOR).append(dumpContext.getServiceName());
		if (writeEndSuc) {
			sbPath.append(".suc");
		}
		return new Path(sbPath.toString());
	}

	public Path createColumnMetaDataPath(String utf8StrTime) {
		StringBuffer sbPath = new StringBuffer();
		sbPath.append(this.dumpContext.getServiceName()).append(Path.SEPARATOR)
				.append(isIncrOrNot.get() ? "incr" : "all").append(Path.SEPARATOR).append(utf8StrTime)
				.append(Path.SEPARATOR).append("cols-metadata");
		return new Path(sbPath.toString());
	}

	public String getRowWrite2HdfsOutPutStream(Map<String, String> row) {
		// return "0";
		return dumpContext.getGroupRouter().getGroupName(row);
	}

	public void modifyNoticeList(Map context, List<String> noticeList) {
		context.put(Constants.SHARD_COUNT, noticeList);
	}

	public AtomicBoolean getIsIncrOrNot() {
		return isIncrOrNot;
	}

	private static final String KEY_DUMP_LAUNCH_TIME = "dumpLaunchTime";

	@SuppressWarnings("all")
	public static String getDumpLaunchTimestamp(Map context) {
		Date time = (Date) context.get(KEY_DUMP_LAUNCH_TIME);
		Assert.assertNotNull(time);
		return FormatTool.formatDate2Str8(time);
	}

	@SuppressWarnings("all")
	public static void setDumpLaunchTime(Map context, Date time) {
		Assert.assertNotNull(time);
		context.put(KEY_DUMP_LAUNCH_TIME, time);
	}

	@SuppressWarnings("all")
	@Override
	public synchronized void importServiceData(boolean isInrc, IWorkflowFeedback feedback, Map context, int groupNum)
			throws /* 组的总数 */
			DataImportHDFSException {
		Map<String, FSDataOutputStream> outMap = null;
		// List<Path> deleteList = null;
		final long currentTimeStamp = System.nanoTime();
		groupNum = configGroupNum(groupNum);
		try {
			this.isBusy().set(true);
			log.warn(currentTimeStamp + ":obj get execute lock");
			this.isIncrOrNot.set(isInrc);

			shardInitCount = new AtomicInteger(0);
			outMap = new HashMap<String, FSDataOutputStream>();
			// StartAndEndTime sae = this.dumpContext.getTimeProvider()
			// .justGetTimes();
			// 百岁修改 20121123 time_point 格式改变
			// context.put(Constants.TIME_POINT, FormatTool
			// .formatDate2Str8(sae.endTime));
			String utf8StrTime = getDumpLaunchTimestamp(context);
			TriggerParam triggerParam = new TriggerParam();
			triggerParam.setTime(utf8StrTime);
			context.put(Constants.TIME_POINT, triggerParam);
			// String utf8StrTime = FormatTool.formatDate2Str8(endTime);
			List<String> importCount = new ArrayList<String>();
			for (int i = 0; i < groupNum; i++) {
				Path path = createPath(i, utf8StrTime, false);
				getFileSystem().deleteOnExit(path);
				getFileSystem().createNewFile(path);
				FSDataOutputStream output = getFileSystem().create(path);
				// String group = String.valueOf(i);
				outMap.put(String.valueOf(i), output);
				importCount.add(String.valueOf(i));
			}
			context.put(Constants.IMPORT_COUNT, importCount);
			outMap = Collections.unmodifiableMap(outMap);
			// SqlFunctionCollectors sqlFuncs = ((MultiThreadSqlExcuteProvider)
			// dataProvider).sqlFuncs;
			// if (sqlFuncs != null)
			// sqlFuncs.register(sqlFuncs.new StartDateFunction());
			// this.sourceData.openResource();
			int split = 0;
			List<HashMap<String, Object>> resultCollect = new ArrayList<>();
			if (this.sourceDataFactory.getSourceDataProvider() != null
					&& (split = this.sourceDataFactory.getSourceDataProvider().size()) > 0) {
				CountDownLatch latch = new CountDownLatch(split);
				List<SourceDataProvider> dbs = new ArrayList<>(this.sourceDataFactory.getSourceDataProvider());
				Collections.shuffle(dbs);
				for (int ii = 0; ii < split; ii++) {
					// int j = 0;
					// if (splitSize >
					// this.dumpContext.getWirteHdfsThreadCount()) {
					// j = this.dumpContext.getWirteHdfsThreadCount();
					// } else {
					// j = splitSize;
					// }
					// final String loginfo = "能够并行导入的库数:"
					// + this.dumpContext.getWirteHdfsThreadCount()
					// + ",\n当前需要执行导入的库数:" + splitSize + ",\n本次并行导入的库数:"
					// + j;
					// log.warn(loginfo);
					// feedback.sendInfo(loginfo);
					//
					// for (int i = 0; i < j; i++) {
					// ▼▼▼▼百岁在HDFSImportTask 构造函数上添加一个feedback的参数
					SourceDataProvider singletabDataProvider = (SourceDataProvider) dbs.get(ii);
					HashMap<String, Object> result = new HashMap<String, Object>();
					// FutureTask<Map> task = new FutureTask<Map>(
					// , result);
					// ▲▲▲▲ 百岁修改 end
					// taskResList.add();
					// exec.submit(task);
					resultCollect.add(result);
					// 一张表为一个执行单元
					dbReaderExecutor.execute(new DBTableReaderTask(latch, singletabDataProvider, this, getFileSystem(),
							outMap, feedback, groupNum, utf8StrTime, result,
							getDBServerBusyCount(singletabDataProvider.getDbHost())));
				}
				latch.await(15, TimeUnit.HOURS);
				// waitDumpFinish(split, exec);
				String loginfo = (isInrc ? "incr" : "full") + "dump all task has over,readrows:"
						+ sourceDataFactory.getDbReaderCounter();
				log.warn(loginfo);
				feedback.sendInfo(loginfo);
				long count = 0;
				// List allError = new ArrayList<String>();
				for (Map<String, Object> map : resultCollect) {
					// count += (Long) map.get(Constants.IMPORT_HDFS_ROW_COUNT);
					Collection<String> errorList = (Collection) map.get(Constants.IMPORT_HDFS_ERROR);
					// allError.addAll(errorList);
					if (errorList != null) {
						for (String errorRow : errorList) {
							loginfo = (isInrc ? "【增量】" : "【全量】") + ">>>>>>>>>>>>>>>>导入行数据为[" + errorRow
									+ "]到HDFS集群中出错！！<<<<<<<<<<<<<<<<<";
							log.warn(loginfo);
							feedback.sendError(loginfo);
						}
					}
				}
				List noticeList = new ArrayList<String>();
				long importALLCount = 0L;
				// for (Entry<String, AtomicLong> entry : shardRow.entrySet()) {
				// String groupName = entry.getKey();
				// noticeList.add(groupName);
				// AtomicLong integer = entry.getValue();
				// if (integer != null) {
				// // Hive在绑定hdfs路径的时候不suc文件会导致记录条数有异常
				// // Path path = createPath(Integer.valueOf(groupName),
				// // utf8StrTime, true);
				// // getFileSystem().deleteOnExit(path);
				// // getFileSystem().createNewFile(path);
				// // FSDataOutputStream output = getFileSystem()
				// // .create(path);
				// // output.write(integer.toString().getBytes("UTF-8"));
				// // output.flush();
				// // output.close();
				// log.warn((isInrc ? "" : "【全量】") + "分组[" +
				// dumpContext.getServiceName() + "-" + groupName
				// + "]成功导入HDFS集群" + integer + "行数据<<<<<<<");
				// importALLCount = importALLCount + integer.intValue();
				// }
				// }
				// deleteList = new ArrayList<Path>();
				// for (int i = 0; i < groupNum; i++) {
				// String group = String.valueOf(i);
				// boolean flag = true;
				// // for (Entry<String, AtomicLong> entry :
				// // shardRow.entrySet()) {
				// // String groupName = entry.getKey();
				// // if (group.equals(groupName)) {//
				// // flag = false;
				// // }
				// // }
				// if (flag) {
				// Path path = createPath(Integer.valueOf(group), utf8StrTime,
				// false);
				// // Path path = new Path(dumpContext.getServiceName()
				// // + Path.SEPARATOR
				// // + (isIncrOrNot.get() ? "incr" : "all")
				// // + Path.SEPARATOR + group + Path.SEPARATOR
				// // + FormatTool.formatDate2Str8(sae.endTime)
				// // + Path.SEPARATOR);
				// deleteList.add(path);
				// // fileSystem.delete(path,true);
				// }
				//
				// }
				modifyNoticeList(context, noticeList);
				// context.put(Constants.SHARD_COUNT, noticeList);
				loginfo = "线程[" + Thread.currentThread().getName() + "]" + (isInrc ? "【增量】" : "【全量】") + ">>>>>>>数据库读出"
						+ count + "行数据,导入HDFS集群[" + importALLCount + "]行<<<<<<<\n" + (isInrc ? "【增量】" : "【全量】")
						+ ">>>>>>> 成功导入数据到HDFS集群后，重置文件时间管理器的此次批次导入时间<<<";
				log.warn(loginfo);
				// baisui add
				feedback.sendInfo(loginfo);
				// this.dumpContext.getTimeProvider().reWriteTimeToFile();
			} else {
				log.warn(">>>>>>【通知】没有任何源数据可以导入HDFS<<<<<<<<<<<,");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			feedback.sendError(e);
			throw new DataImportHDFSException("【警告】出现InterruptedException，错误信息:" + e.getMessage(), e);
			// } catch (ExecutionException e) {
			// e.printStackTrace();
			// feedback.sendError(e);
			// throw new
			// DataImportHDFSException("【警告】出现ExecutionException，错误信息:"
			// + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			feedback.sendError(e);
			throw new DataImportHDFSException("【警告】出现未知错误，错误信息:" + e.getMessage(), e);
		} finally {
			this.isBusy().set(false);
			log.warn(currentTimeStamp + ":has release the lock");
			// if (outMap != null) {
			for (Entry<String, FSDataOutputStream> entry : outMap.entrySet()) {
				FSDataOutputStream outputStream = null;
				String shard = null;
				try {
					shard = entry.getKey();
					outputStream = entry.getValue();
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					log.warn(">>>【警告】关闭HDFS写入流出现错误<<<", e);
					feedback.sendError(e);
				} finally {
					shard = null;
					outputStream = null;
					// outMap.put(shard, null);
				}
			}
			// }
			if (shardInitCount != null) {
				shardInitCount = null;
			}
			// if (waitQueue != null) {
			// waitQueue.clear();
			// waitQueue = null;
			// }
			// if (finishTitleList != null) {
			// finishTitleList.clear();
			// finishTitleList = null;
			// }
			// if (successResultsWriter != null) {
			// try {
			// successResultsWriter.flush();
			// successResultsWriter.close();
			// } catch (IOException e) {
			// log.warn(">>>【警告】关闭结果数据文件出现异常<<<", e);
			// feedback.sendError(e);
			// } finally {
			// successResultsWriter = null;
			// }
			// }
		}
	}

	/**
	 * @param split
	 * @param exec
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@SuppressWarnings("all")
	private static final int MAX_DUMP_THREAD_COUNT = 60;

	/**
	 * @return
	 */
	private static ExecutorService createDbReaderExecutor() {
		final PriorityBlockingQueue<Runnable> dbReaderQueue = new PriorityBlockingQueue<Runnable>(20,
				new Comparator<Runnable>() {

					@Override
					public int compare(Runnable o1, Runnable o2) {
						DBTableReaderTask task1 = (DBTableReaderTask) o1;
						DBTableReaderTask task2 = (DBTableReaderTask) o2;
						AtomicInteger task1DbBusy = getDBServerBusyCount(task1.getDbIP());
						AtomicInteger task2DbBusy = getDBServerBusyCount(task2.getDbIP());
						return task1DbBusy.get() - task2DbBusy.get();
					}
				});
		final MyUncaughtExceptionHandler exceptionHandler = new MyUncaughtExceptionHandler();
		// 最大执行线程数
		Integer maxDBDumpThreadCount = TSearcherConfigFetcher.get().getMaxDBDumpThreadCount();
		if (maxDBDumpThreadCount == null) {
			maxDBDumpThreadCount = MAX_DUMP_THREAD_COUNT;
		}
		final ExecutorService exec = new ThreadPoolExecutor(maxDBDumpThreadCount, maxDBDumpThreadCount, 30l,
				TimeUnit.SECONDS, dbReaderQueue, new ThreadFactory() {

					int index = 0;

					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, "read thread #" + index++);
						t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

							@Override
							public void uncaughtException(Thread t, Throwable e) {
								log.error(t.getName(), e);
								exceptionHandler.uncaughtException(t, e);
							}
						});
						return t;
					}
				}, new ThreadPoolExecutor.CallerRunsPolicy());
		exceptionHandler.setExecService(exec);
		return exec;
	}

	private static class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

		private ExecutorService execService;

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (execService == null) {
				throw new IllegalArgumentException("execService can not be null");
			}
			execService.shutdownNow();
		}

		public void setExecService(ExecutorService execService) {
			this.execService = execService;
		}
	}

	private static String toJSONString(Map<String, String> o) {
		JSONObject json = new JSONObject(o);
		return json.toString();
	}

	// 负责将将数数据库持久层数据导入到hdfs中去
	private class DBTableReaderTask implements // <Map<String, Object>>
			Runnable {

		private CountDownLatch latch = null;

		private final SourceDataProvider<String, String> dataProvider;

		// private MultiThreadHDFSDataProvider hdfsProvider;
		private FileSystem fileSystem;

		// private String filePath;
		private Map<String, FSDataOutputStream> outMap;

		private String threadName;

		private Boolean waitForFinishedLock;

		private int groupNum;

		private final String utf8StrTime;

		private AtomicInteger dbHostBusyCount;

		// 百岁添加
		private final IWorkflowFeedback feedback;

		private final Map<String, Object> threadResult;

		String getDbIP() {
			return dataProvider.getDbHost();
		}

		// end 百岁添加
		public DBTableReaderTask(CountDownLatch latch, SourceDataProvider<String, String> dataProvider,
				MultiThreadHDFSDataProvider hdfsProvider, FileSystem fileSystem, Map<String, FSDataOutputStream> outMap,
				IWorkflowFeedback feedback, int groupNum, String utf8StrTime, Map<String, Object> threadResult,
				AtomicInteger dbHostBusyCount) {
			this.latch = latch;
			this.dataProvider = dataProvider;
			// this.hdfsProvider = hdfsProvider;
			this.fileSystem = fileSystem;
			this.outMap = outMap;
			this.feedback = feedback;
			this.groupNum = groupNum;
			this.utf8StrTime = utf8StrTime;
			this.threadResult = threadResult;
			this.dbHostBusyCount = dbHostBusyCount;
		}

		// public void init() {
		//
		// }
		@Override
		public void run() {
			this.dbHostBusyCount.incrementAndGet();
			AtomicInteger filtercount = new AtomicInteger();
			// }
			//
			// @SuppressWarnings("all")
			// @Override
			// public Map call() throws Exception {
			this.threadName = Thread.currentThread().getName();
			try {
				dataProvider.openResource();
				writeTitle();
				long startTime = System.currentTimeMillis();
				long interTime = startTime;
				Collection<String> errorList = new ConcurrentLinkedQueue<String>();
				long threadBeginTime = System.currentTimeMillis();
				long allcount = 0;
				// DataContext dataContext = new DataContext();
				String info = null;
				AtomicInteger submitSize = new AtomicInteger(0);
				long readCount = 0;
				while (dataProvider.hasNext()) {
					readCount++;
					Map<String, String> row = dataProvider.next();
					// if (router == null) {
					// router = new ModGroupRouter();
					// router.setShardKey(shardKey);
					// }
					submit(row, errorList, submitSize, filtercount);
					if (++allcount % 10000 == 0) {
						info = "thread" + Thread.currentThread().getName() + "ip:" + dataProvider.getDbHost()
								+ " plush rows:10000 consume:" + (System.currentTimeMillis() - interTime) / 1000
								+ "s  all time consume: " + (System.currentTimeMillis() - startTime) / 1000
								+ "s accumulate: " + allcount;
						log.warn(info);
						// feedback.sendInfo(info);
						interTime = System.currentTimeMillis();
					}
				}
				log.info(dumpContext.getServiceName() + ":" + dataProvider.getDsName() + ",read:" + readCount);
				// waitForFinished(submitSize);
				if (writerFinishedCount.incrementAndGet() == sourceDataFactory.getSourceDataProvider().size()) {
					info = ">>>>>>>>>>>>>【注意】此次批量导入完成,消耗的时间 是: " + (System.currentTimeMillis() - threadBeginTime) / 1000
							+ " 秒<<<<<<<<<<<<<";
					log.warn(info);
					feedback.sendInfo(info);
				}
				threadResult.put(Constants.IMPORT_HDFS_ERROR, errorList);
				threadResult.put(Constants.IMPORT_HDFS_ROW_COUNT, allcount);
				// return threadResultMap;
				// } catch (SourceDataReadException e) {
				// log.error("【警告】读取数据库数据出现错误，具体错误信息为：", e);
				// // e.printStackTrace();
				// // dataProvider.closeResource();
				// feedback.sendError(e);
				// throw e;
			} catch (Exception e) {
				log.error(">>>>>【警告】[" + dumpContext.getServiceName() + "]执行导入HDFS集群出现未知错误<<<<<", e);
				feedback.sendError(e);
				// e.printStackTrace();
				throw new DataImportHDFSException("hdfs dump e", e);
			} finally {
				// 全部读取完毕
				// if (output != null) {
				// output.flush();
				// output.close();
				// output = null;s
				// }
				this.dbHostBusyCount.decrementAndGet();
				dataProvider.closeResource();
				latch.countDown();
			}
			// return threadResult;
		}

		private void writeTitle() {
			try {
				if (!hasWriteTitles.get() && dataProvider instanceof SingleTableSqlExcuteProvider
						&& hasWriteTitles.compareAndSet(false, true)) {
					List<RowMetaData> rowmetalist = ((SingleTableSqlExcuteProvider) dataProvider).getMetaData();
					MultiThreadHDFSDataProvider.this.rowKeys = Collections.unmodifiableList(rowmetalist);
					FSDataOutputStream output = fileSystem.create(createColumnMetaDataPath(utf8StrTime), true);
					IOUtils.write(JSON.toJSONString(rowmetalist, true), output, Charset.forName("utf8"));
					log.info("success write title" + dumpContext.getServiceName());
					IOUtils.closeQuietly(output);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// public void waitForFinished() {
		// // if (log.isInfoEnabled()) {
		// // log.info("waiting for import hdfs job finished. currentThread:"
		// // + Thread.currentThread().getName());
		// // }
		// try {
		// waitForFinishedLock = true;
		// while (true) {
		// if (executor.getActiveCount() > 0 || executor.getQueue().size() > 0)
		// {
		// try {
		// Thread.sleep(1000);
		//
		// } catch (InterruptedException e) {
		// throw new RuntimeException(e);
		// }
		// } else {
		// synchronized (waitForFinishedLock) {
		// waitForFinishedLock.notifyAll();
		// }
		// break;
		// }
		// }
		// } finally {
		// waitForFinishedLock = false;
		// }
		// }
		// public void waitForFinished(AtomicInteger submitSize) {
		//
		// final long start = System.currentTimeMillis();
		// if (log.isInfoEnabled()) {
		// log.info("waiting for import hdfs job finished. currentThread:" +
		// Thread.currentThread().getName());
		// }
		// try {
		// waitForFinishedLock = true;
		// while (true) {
		// if (executor.getActiveCount() > 0 || executor.getQueue().size() > 0)
		// {
		// try {
		// // log.info(
		// // "waiting for import hdfs job finished.
		// // activeCount="
		// // + executor.getActiveCount()
		// // + "queue="
		// // + executor.getQueue().size());
		//
		// Thread.sleep(1000);
		//
		// } catch (InterruptedException e) {
		// throw new RuntimeException(e);
		// }
		// } else if (submitSize.get() > 0) {
		// try {
		// // log.info(
		// // "waiting for import hdfs job finished.
		// // activeCount="
		// // + executor.getActiveCount()
		// // + "queue="
		// // + executor.getQueue().size());
		//
		// Thread.sleep(1000);
		//
		// } catch (InterruptedException e) {
		// throw new RuntimeException(e);
		// }
		// } else {
		// synchronized (waitForFinishedLock) {
		// waitForFinishedLock.notifyAll();
		// }
		// break;
		// }
		// }
		// log.info("All import hdfs job finished. activeCount=" +
		// executor.getActiveCount() + "queue="
		// + executor.getQueue().size());
		// } finally {
		// waitForFinishedLock = false;
		// log.warn("waitForFinished consume:" + (System.currentTimeMillis() -
		// start));
		// }
		// }
		@SuppressWarnings("all")
		private void submit(final Map<String, String> row, final Collection<String> errorList,
				final AtomicInteger batchSize, final AtomicInteger filtercount) throws Exception {
			// writeTitle();
			// if (MultiThreadHDFSDataProvider.this.rowKeys == null) {
			// throw new IllegalStateException("rowKeys can not be null");
			// }
			String info = null;
			BatchDataProcessor dataprocess = dumpContext.getDataProcessor();
			if (dataprocess != null && !dumpContext.getDataProcessor().process(row)) {
				filtercount.incrementAndGet();
				log.warn("record has been filtered:" + toJSONString(row));
				return;
			}
			FSDataOutputStream output = null;
			String shard = null;
			try {
				shard = getRowWrite2HdfsOutPutStream(row);
				// shard = dumpContext.getGroupRouter()
				// .getGroupName(row);
				output = outMap.get(shard);
			} catch (Exception e) {
				info = "【注意】数据经过自定义DataProcess加工处理出现错误，忽略这条记录：" + row.toString();
				log.warn(info);
				filtercount.incrementAndGet();
				feedback.sendError(info, e);
				return;
			}
			if (output == null) {
				throw new IllegalStateException("shard:" + shard + " output channel can not be null");
			}
			// if (shardInitCount.get() < groupNum)
			// if (!finishTitleList.containsKey(shard))
			// synchronized (finishTitleList) {
			// if (!finishTitleList.containsKey(shard)) {//
			// changed
			// // 将title不写到文件内容中
			// this.tryWirteTitle(output, row, shard);
			// }
			// }
			StringBuffer content = new StringBuffer();
			int count = 0;
			// titleKeys.size();
			int size = rowKeys.size();
			for (int i = 0; i < size; i++) {
				String value = row.get(rowKeys.get(i).getKey());
				content.append(value != null ? value : " ");
				if (count < size - 1)
					content.append(TAB);
				else if (count == size - 1) {
					content.append(NEWLINE);
				}
				count++;
			}
			// try {
			output.write(content.toString().getBytes("UTF-8"));
			content = null;
			// } catch (Exception e) {
			// // String errorKey = "";
			// if (content != null) {
			// errorList.add(content.toString());
			// }
			// // String consumeTime =
			// // String.valueOf(System.nanoTime()
			// // - beginTime);
			// //
			// errorResultsWriter.write(String.valueOf(System.nanoTime())
			// // + "," + consumeTime + "\r\n");
			// }
			// executor.execute(new Runnable() {
			//
			// public void run() {
			// try {
			// FSDataOutputStream output = null;
			// String info = null;
			// StringBuilder content;
			// long startTime = System.currentTimeMillis();
			// int filtercount = 0;
			//
			// BatchDataProcessor dataprocess = dumpContext.getDataProcessor();
			// for (Map<String, String> row : dataMap) {}
			//
			// // info = "【注意】线程" + Thread.currentThread().getName()
			// // + "写入[" + (1000 - filtercount)
			// // + "]记录到HDFS,同时DataProcessor过滤掉[" + filtercount
			// // + "]条记录,该过程总共花费的时间 :"
			// // + (System.currentTimeMillis() - startTime)
			// // / 1000 + "s";
			// // // feedback.sendInfo(info);
			// // log.warn(info);
			//
			// } catch (Exception e) {
			// throw new RuntimeException("thread" +
			// Thread.currentThread().getName() + "write hdfs", e);
			// } finally {
			// batchSize.decrementAndGet();
			// }
			// }
			//
			// // public void tryWirteTitle(FSDataOutputStream output,
			// // Map<String, String> row, String shard) throws Exception {
			// // int size = row.size();
			// // int count = 0;
			// // StringBuilder title = new StringBuilder();
			// // boolean flag = false;
			// // synchronized (shard) {
			// // List<String> list = new LinkedList<String>();
			// // for (Map.Entry<String, String> entry : row.entrySet()) {
			// // // isWriteTitle(output, shard, flag);
			// // // {
			// // // if (i == 0) {
			// // String column = entry.getKey();
			// // list.add(column);
			// // title.append(column);
			// // if (count < size - 1)
			// // title.append(TAB);
			// // else if (count == size - 1) {
			// // title.append(NEWLINE);
			// // }
			// // count++;
			// // // isWriteTitle(output, shard, flag);
			// //
			// // }
			// // //isWriteTitle(output, shard, flag);
			// //
			// // if (title != null && title.length() > 0) {
			// // log.warn("【注意】线程["
			// // + Thread.currentThread().getName()
			// // + "]在往HDFS写入["
			// // + dumpContext.getServiceName() + "-"
			// // + shard + "]的Title。。。");
			// // // output.write(title.toString().getBytes("UTF-8"));
			// // title = null;
			// // finishTitleList.put(shard, list);
			// // // finishTitleList.add(shard);
			// // }
			// //
			// // }
			// // }
			//
			// });
		}
		// /**
		// * @param output
		// * @param shard
		// * @return 0 为 写title,1 已经写过了，不用在写了，2 为不能写，等待其他线程写，-1 非正常情况，需要特殊处理
		// * @throws InterruptedException
		// */
		// public Integer isWriteTitle(FSDataOutputStream output, String shard,
		// boolean flag) throws InterruptedException {
		// if (flag) {
		// return 0;
		// }
		//
		// // synchronized (finishTitleList) {
		// if (!finishTitleList.containsKey(shard)) {// changed
		// if (shardInitCount.get() < groupNum) {
		// String threadName = Thread.currentThread().getName();
		// String threadName1 = cumap.putIfAbsent(shard, threadName);
		// if (threadName1 == null) {
		// log.warn("[" + threadName
		// + "]执行threadName1返回为Null 则代表获取到了写[" + shard
		// + "]分组Title的机会，则开始处理Title");
		// if (output.size() == 0) {//
		// shardInitCount.incrementAndGet();
		// return 0;
		// } else
		// return -1;
		// } else if (threadName1 != null
		// && !threadName.equals(threadName1)) {
		// return 2;
		// } else if (threadName1 != null
		// && threadName.equals(threadName1)) {
		//
		// return 1;
		// } else {
		// return 0;
		// }
		// } else {
		// return 1;
		// }
		// } else {
		// return 1;
		// }
		// // }
		// }
		// public String transformText(String value, String encoding) throws
		// UnsupportedEncodingException {
		// return new String(value.getBytes(encoding));
		// }
		// public void setDataProvider(SourceDataProvider<String, String>
		// dataProvider) {
		// this.dataProvider = dataProvider;
		// }
		// public SourceDataProvider<String, String> getDataProvider() {
		// return dataProvider;
		// }
	}

	public static void main(String[] args) {
	}

	@Override
	public AtomicBoolean isBusy() {
		return isBusy;
	}

	@SuppressWarnings("all")
	private SourceDataProviderFactory sourceDataFactory;

	@SuppressWarnings("all")
	public SourceDataProviderFactory getSourceData() {
		return sourceDataFactory;
	}

	@SuppressWarnings("all")
	public void setSourceData(SourceDataProviderFactory sourceData) {
		this.sourceDataFactory = sourceData;
	}

	/**
	 * @return the fileSystem
	 */
	private FileSystem getFileSystem() {
		return this.dumpContext.getDistributeFileSystem();
	}
}
