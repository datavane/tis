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
package com.qlangtech.tis.fullbuild.servlet;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.fullbuild.servlet.impl.HttpExecContext;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;

/*
 * 触发全量索引构建任务<br>
 * 例子： curl
 * 'http://localhost:8080/trigger?appname=search4totalpay&component.start=
 * tableJoin&ps=20160622110738'<br>
 * curl 'http://localhost:8080/trigger?component.start=indexBackflow&ps=
 * 20160623001000&appname=search4_fat_instance' <br>
 * curl
 * 'http://localhost:8080/trigger?component.start=indexBuild&ps=20160811001000&
 * appname=search4shop' <br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisServlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger(TisServlet.class);

	private static final long serialVersionUID = 1L;

	private IndexSwapTaskflowLauncher indexSwapTaskflowLauncher;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.indexSwapTaskflowLauncher = IndexSwapTaskflowLauncher
				.getIndexSwapTaskflowLauncher(config.getServletContext());
	}

	public static final String KEY_APP_NAME = "appname";

	private static final ExecutorService executeService = Executors.newCachedThreadPool(new ThreadFactory() {

		int index = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("triggerTask#" + (index++));
			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.error(e.getMessage(), e);
				}
			});
			return t;
		}
	});

	// private static final AtomicBoolean idle = new AtomicBoolean(true);
	private static final Map<String, ExecuteLock> idles = new HashMap<String, ExecuteLock>();

	// public TisServlet() {
	// super();
	// }
	protected ExecuteLock getExecLock(String indexName) {
		ExecuteLock lock = idles.get(indexName);
		if (lock == null) {
			synchronized (TisServlet.this) {
				lock = idles.get(indexName);
				if (lock == null) {
					lock = new ExecuteLock(indexName);
					idles.put(indexName, lock);
				}
			}
		}
		return lock;
	}

	/**
	 * 校验参数是否正确
	 *
	 * @param execContext
	 * @param req
	 * @param res
	 * @return
	 * @throws ServletException
	 */
	protected boolean isValidParams(HttpExecContext execContext, HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		return true;
	}

	protected boolean shallValidateCollectionExist() {
		return true;
	}

	protected final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		final HttpExecContext execContext = createHttpExecContext(req);
		final String indexName = execContext.getString(KEY_APP_NAME);
		MDC.put("app", indexName);
		try {
			if (!isValidParams(execContext, req, res)) {
				return;
			}
			if (StringUtils.isBlank(indexName)) {
				throw new IllegalStateException("param indexname can not be null");
			}
			if (shallValidateCollectionExist() && !this.indexSwapTaskflowLauncher.getIndexNames().contains(indexName)) {
				String msg = "indexName:" + indexName + " is not acceptable";
				getLog().warn(msg + ",exist collection:{}", this.indexSwapTaskflowLauncher.getIndexNames());
				writeResult(false, msg, res);
				return;
			}
			final ExecuteLock lock = getExecLock(indexName);
			getLog().info("start to execute index swap work flow");
			final CountDownLatch countDown = new CountDownLatch(1);
			// final Future<?> future =
			lock.futureQueue.add(executeService.submit(() -> {
				MDC.put("app", indexName);
				getLog().info("index swap start to work");
				try {
					while (true) {
						try {
							if (lock.lock()) {
								try {
									String msg = "execute index swap work flow successful";
									getLog().info(msg);
									writeResult(true, msg, res);
									countDown.countDown();
									startWork(execContext);
								} catch (Throwable e) {
									getLog().error(e.getMessage(), e);
									throw new RuntimeException(e);
								} finally {
									lock.unlock();
									lock.futureQueue.clear();
								}
							} else {
								if (lock.isExpire()) {
									getLog().warn("this lock has expire,this lock will cancel");
									// 执行已經超時
									lock.futureQueue.clear();
									lock.unlock();
									// while (lock.futureQueue.size() >= 1)
									// {
									// lock.futureQueue.poll().cancel(true);
									// }
									getLog().warn("this lock has expire,has unlocked");
									continue;
								} else {
									String msg = "pre task is executing ,so this commit will be ignor";
									getLog().warn(msg);
									writeResult(false, msg, res);
								}
								countDown.countDown();
							}
							// }
							break;
						} catch (Throwable e) {
							getLog().error(e.getMessage(), e);
							try {
								if (countDown.getCount() > 0) {
									writeResult(false, ExceptionUtils.getMessage(e), res);
								}
							} catch (Exception e1) {
							} finally {
								try {
									countDown.countDown();
								} catch (Throwable ee) {
								}
							}
							break;
						}
					}
				} finally {
					MDC.remove("app");
				}
				// end run
			}));
			try {
				countDown.await();
			} catch (InterruptedException e) {
			}
		} finally {
			MDC.remove("app");
		}
	}

	protected Logger getLog() {
		return log;
	}

	protected void startWork(final HttpExecContext execContext) throws Exception {
		indexSwapTaskflowLauncher.startWork(execContext);
	}

	protected HttpExecContext createHttpExecContext(ServletRequest req) {
		return new HttpExecContext(req);
	}

	protected class ExecuteLock {

		private final Queue<Future<?>> futureQueue = new ConcurrentLinkedQueue<Future<?>>();

		// private final ReentrantLock lock;
		private final AtomicBoolean lock = new AtomicBoolean(false);

		// 开始时间，需要用它判断是否超时
		private AtomicLong startTimestamp;

		// 超时时间为9个小时
		private static final long EXPIR_TIME = 1000 * 60 * 60 * 9;

		private final String indexName;

		/**
		 * @param lock
		 */
		public ExecuteLock(String indexName) {
			this.indexName = indexName;
			// 这个lock 的问题是必须要由拥有这个lock的owner thread 来释放锁，不然的话就会抛异常
			// this.lock = new ReentrantLock();
			this.startTimestamp = new AtomicLong(System.currentTimeMillis());
		}

		boolean isExpire() {
			long start = startTimestamp.get();
			long now = System.currentTimeMillis();
			// 没有完成
			// 查看是否超时
			boolean expire = ((start + EXPIR_TIME) < now);
			if (expire) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				log.info("time:" + format.format(new Date(start)) + "is expire");
			}
			return expire;
		}

		/**
		 * 尝试加锁
		 *
		 * @return
		 */
		public boolean lock() {
			if (this.lock.compareAndSet(false, true)) {
				long current = System.currentTimeMillis();
				this.startTimestamp.getAndSet(current);
				try {
					Path tokenPath = new Path(
							TSearcherConfigFetcher.get().getHDFSRootDir() + "/" + indexName + "/fullbuildtoken");
					FSDataOutputStream output = TISHdfsUtils.getFileSystem().create(tokenPath, true);
					IOUtils.write(String.valueOf(current), output);
				} catch (Throwable e) {
					log.warn(e.getMessage(), e);
				}
				return true;
			} else {
				return false;
			}
			//
			// if (lock.compareAndSet(false, true)) {
			// // 成功上锁
			// return (startTimestamp.compareAndSet(start, now));
			// } else {
			// //
			// return false;
			// }
		}

		/**
		 * 释放锁
		 */
		public void unlock() {
			// this.lock.unlock();
			try {
				Path tokenPath = new Path(
						TSearcherConfigFetcher.get().getHDFSRootDir() + "/" + indexName + "/fullbuildtoken");
				TISHdfsUtils.getFileSystem().delete(tokenPath, true);
			} catch (Throwable e) {
				log.warn(e.getMessage(), e);
			}
			this.lock.lazySet(false);
		}
	}

	protected void writeResult(boolean success, String msg, ServletResponse res) throws ServletException {
		res.setContentType("text/json");
		try {
			JSONObject json = new JSONObject();
			json.put("success", success);
			if (StringUtils.isNotBlank(msg)) {
				json.put("msg", msg);
			}
			res.getWriter().write(json.toString(1));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
