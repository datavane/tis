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
package com.qlangtech.tis.common.zk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/*
 * ZK事件监听器，默认事件处理完毕之后再次将监听器绑定，达到长期，持久监听的目的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
@Deprecated
public abstract class TerminatorWatcher implements Watcher {

	private static Log log = LogFactory.getLog(TerminatorWatcher.class);

	/**
	 * @uml.property name="zkClient"
	 * @uml.associationEnd
	 */
	protected TerminatorZkClient zkClient = null;

	/**
	 * @uml.property name="watchType"
	 * @uml.associationEnd
	 */
	protected WatcherType watchType = null;

	public TerminatorWatcher() {
	}

	public TerminatorWatcher(TerminatorZkClient zkClient) {
		this.zkClient = zkClient;
	}

	/**
	 * @param zkClient
	 * @uml.property name="zkClient"
	 */
	public void setZkClient(TerminatorZkClient zkClient) {
		this.zkClient = zkClient;
	}

	/**
	 * @param watchType
	 * @uml.property name="watchType"
	 */
	public void setWatchType(WatcherType watchType) {
		this.watchType = watchType;
	}

	@Override
	public void process(WatchedEvent event) {
		try {
			this.handle(event, zkClient);
		} catch (Exception e) {
			log.error(e, e);
		}
		if (watchType != null) {
			try {
				this.rebindWatcher(event.getPath());
			} catch (TerminatorZKException e) {
				log.error("An Exception has thrown when rebind watcher.", e);
			}
		}
	}

	/**
	 * 重新绑定监听器
	 *
	 * @param path
	 */
	private void rebindWatcher(String path) throws TerminatorZKException {
		if (watchType.equals(WatcherType.GETDATA_TYPE)) {
			zkClient.getData(path, this);
		} else if (watchType.equals(WatcherType.GETCHILDREN_TYPE)) {
			zkClient.getChildren(path, this);
		} else if (watchType.equals(WatcherType.EXIST_TYPE)) {
			zkClient.exists(path, this);
		} else {
		}
	}

	/**
	 * 事件的处理
	 *
	 * @param event
	 * @param zkClient
	 */
	public abstract void handle(WatchedEvent event, TerminatorZkClient zkClient) throws Exception;

	public static class WatcherType {

		public static final WatcherType GETDATA_TYPE = new WatcherType(1, "getDate()'s Watcher");

		public static final WatcherType GETCHILDREN_TYPE = new WatcherType(2, "getChildren()'s Watcher");

		public static final WatcherType EXIST_TYPE = new WatcherType(3, "exsit()'s Watcher");

		private int code;

		private String desc;

		private WatcherType(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}

		public int getCode() {
			return code;
		}

		public String getDesc() {
			return desc;
		}

		public String toString() {
			return "code is {" + code + "},desc is {" + desc + "}";
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this || ((WatcherType) obj).getCode() == this.getCode());
		}
	}
}
