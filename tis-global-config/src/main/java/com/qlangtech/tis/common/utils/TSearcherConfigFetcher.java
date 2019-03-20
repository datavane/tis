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
package com.qlangtech.tis.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.pubhook.common.Nullable;
//import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * 从diamond上获取 terminatorbean 需要的配置信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherConfigFetcher {

	private static final Logger logger = LoggerFactory.getLogger(TSearcherConfigFetcher.class);

	public static final String CONFIG_LOG_FLUME_AGENT_ADDRESS = "log_flume_agent";

	public static final String CONFIG_ZKADDRESS = "zkaddress";

	public static final String CONFIG_HDFS_ADDRESS = "hdfsaddress";

	public static final String CONFIG_runenvironment = "runenvironment";

	public static final String CONFIG_terminator_host_address = "tis_host_address";

	public static final String CONFIG_TIS_HDFS_ROOT_DIR = "tis_hdfs_root_dir";

	public static final String LOG_SOURCE_ADDRESS = "log_source_address";

	public static final String TIS_ASSEMBLE_HOST = "tis_assemble_host";

	public static final String HIVE_HOST = "hivehost";

	// private List<String> mqStatisticsHost;

	// 所有回流到hdfs的根目录
	private final String tisHdfsRootDir;

	// private final String indexBuildCenterHost;

	// 组装节点
	private final String assembleHost;

	// TIS后台地址
	private final String tisConsoleHost;

	private final String zkAddress;

	// 线上zk地址 在用户需要打通线上和预发的时候使用
	private String onlineZkAddress;

	private final String hdfsAddress;

	// private final String runEnvironment;

	private final String logFlumeAgent;

	// private final String cnaddress;
	// private final Integer searcherServerPort;
	// private final String hbaseAddress;
	private String logSourceAddress;

	// private final String job_rpcserver;
	//
	// private final String job_transserver;

	// online:10.162.48.129:10001
	private final String hiveHost;

	// 最大数据库导入线程数目
	private final Integer maxDBDumpThreadCount;

	public TSearcherConfigFetcher() {

		// this.indexBuildCenterHost = null;
		this.assembleHost = null;
		this.tisConsoleHost = null;
		this.zkAddress = null;
		this.onlineZkAddress = null;
		this.hdfsAddress = null;
		this.logFlumeAgent = null;
		this.logSourceAddress = null;
		// this.job_rpcserver = null;
		// this.job_transserver = null;
		this.hiveHost = null;
		this.maxDBDumpThreadCount = null;
		this.tisHdfsRootDir = null;
	}

	private TSearcherConfigFetcher(String serviceName) {
		final RunEnvironment runtime = RunEnvironment.getSysRuntime();
		ServiceConfig servceConfig = null;
		try {
			URL url = new URL(runtime.getInnerRepositoryURL()
					+ "/config/config.ajax?action=global_parameters_config_action&event_submit_do_get_all=y&runtime="
					+ runtime.getKeyName() + "&resulthandler=advance_query_result");
			System.out.println("apply url:" + url);
			List<KeyPair> pairs = ConfigFileContext.processContent(url, new StreamProcess<List<KeyPair>>() {

				@Override
				public List<KeyPair> p(int status, InputStream stream, String md5) {
					try {
						return JSON.parseArray(IOUtils.toString(stream, Charset.forName("utf8")), KeyPair.class);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}, 20);
			servceConfig = new ServiceConfig(pairs);
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		// build中心host地址
		// this.indexBuildCenterHost =
		// servceConfig.getString(INDEX_BUILD_CENTER_HOST);
		// Assert.assertNotNull("indexBuildCenterHost can not be
		// null,key:index_build_center_host", indexBuildCenterHost);

		this.assembleHost = servceConfig.getString(TIS_ASSEMBLE_HOST);
		this.logFlumeAgent = servceConfig.getString(CONFIG_LOG_FLUME_AGENT_ADDRESS);
		this.tisHdfsRootDir = servceConfig.getString(CONFIG_TIS_HDFS_ROOT_DIR);

		this.maxDBDumpThreadCount = servceConfig.getInteger("max_db_dump_thread_count");
		this.hiveHost = servceConfig.getString(HIVE_HOST);
		zkAddress = servceConfig.getString(CONFIG_ZKADDRESS);
		hdfsAddress = servceConfig.getString(CONFIG_HDFS_ADDRESS);
		// ;
		// runEnvironment = servceConfig.getString(CONFIG_runenvironment);

		this.tisConsoleHost = servceConfig.getString(CONFIG_terminator_host_address);
		try {
			this.logSourceAddress = servceConfig.getString(LOG_SOURCE_ADDRESS);
		} catch (Throwable e) {
		}
		try {
			this.onlineZkAddress = servceConfig.getString("online_zkaddress");
		} catch (Throwable e) {
		}

		Assert.assertNotNull("zkaddress can not be null", zkAddress);
		Assert.assertNotNull("hdfsAddress can not be null", hdfsAddress);
		Assert.assertNotNull("hdfs_root_dir can not be null", this.tisHdfsRootDir);
	}

	private static class ServiceConfig {

		private Map<String, String> resourceBundle = new HashMap<String, String>();

		public ServiceConfig(List<KeyPair> pairs) {
			for (KeyPair p : pairs) {
				resourceBundle.put(p.getKey(), p.getValue());
			}
		}

		public String getString(String key) {
			try {
				return resourceBundle.get(key);
			} catch (Throwable e) {
			}
			return StringUtils.EMPTY;
		}

		public Long getLong(String key) {
			try {
				return Long.parseLong(resourceBundle.get(key));
			} catch (Throwable e) {
			}
			return null;
		}

		public Integer getInteger(String key) {
			try {
				return Integer.parseInt(resourceBundle.get(key));
			} catch (Throwable e) {
			}
			return null;
		}
	}

	public String getAssembleHost() {
		String assembleHost = getInstance().assembleHost;
		if (StringUtils.isBlank(assembleHost)) {
			throw new IllegalStateException("param assembleHost can not be null");
		}
		return assembleHost;
	}

	/**
	 * HDFS 的根路径
	 * 
	 * @return
	 */
	public String getHDFSRootDir() {
		return getInstance().tisHdfsRootDir;
	}

	public String getHiveHost() {
		return getInstance().hiveHost;
	}

	public RunEnvironment getRuntime() {
		return RunEnvironment.getSysRuntime();
	}

	public String getLogFlumeAddress() {
		return getInstance().logFlumeAgent;
	}

	public Integer getMaxDBDumpThreadCount() {
		return getInstance().maxDBDumpThreadCount;
	}

	/**
	 * 取得终搜后台地址
	 *
	 * @return
	 */
	public String getTisConsoleHostAddress() {
		return getInstance().tisConsoleHost;
	}

	public String getZkAddress() {
		return getInstance().zkAddress;
	}

	public String getOnlineZkAddress() {
		return getInstance().onlineZkAddress;
	}

	public String getHdfsAddress() {
		return getInstance().hdfsAddress;
	}

	public String getLogSourceAddress() {
		return logSourceAddress;
	}

	private TSearcherConfigFetcher getInstance() {
		return this;
	}

	private static final Map<String, TSearcherConfigFetcher> /* indexName */
	indexsConfig = new HashMap<String, TSearcherConfigFetcher>();

	public static TSearcherConfigFetcher get() {
		try {
			return getInstance("search4");
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
			e.printStackTrace();
			return new NullTSearcherConfigFetcher();
		}
	}

	private static class NullTSearcherConfigFetcher extends TSearcherConfigFetcher implements Nullable {
		public NullTSearcherConfigFetcher() {
			super();
		}

		@Override
		public String getHdfsAddress() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getHDFSRootDir() {
			throw new UnsupportedOperationException();
		}
	}

	private static TSearcherConfigFetcher getInstance(String serviceName) {
		Assert.assertTrue(StringUtils.startsWith(serviceName, "search4"));
		TSearcherConfigFetcher config = null;
		config = indexsConfig.get(serviceName);
		if (config == null) {
			synchronized (indexsConfig) {
				config = indexsConfig.get(serviceName);
				if (config == null) {
					config = new TSearcherConfigFetcher(serviceName);
					indexsConfig.put(serviceName, config);
				}
			}
		}
		logger.info("get global from tis repositroy:" + config.toString());
		return config;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TSearcherConfigFetcher config = TSearcherConfigFetcher.getInstance("search4sucainew");
		System.out.println(config.getZkAddress());
		// int i = 0;
		// while (i++ < 20) {
		// System.out.println((int) (Math.random() * 5));
		//
		// }
	}
}
