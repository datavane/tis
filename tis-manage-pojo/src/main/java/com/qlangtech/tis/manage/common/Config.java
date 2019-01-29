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
package com.qlangtech.tis.manage.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* 
 * 全局配置信息加載
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Config {

	private static Config config;

	
	private final String tisRepository;

	private Map<String, String> userToken;

	private final String projectName;


	private Map<RunEnvironment, String> publishHook;

	private Map<RunEnvironment, String> responseTimeHost;

	// private Map<RunEnvironment, String> zkAddress;
	private Map<RunEnvironment, String> hdfshost;

	private final List<FuncGroup> funcGroup = new ArrayList<FuncGroup>();

	private final Map<RunEnvironment, String> hsfMonotorHost = new HashMap<RunEnvironment, String>();

	private Integer dptTerminatorId;

	private static String bundlePath;


	/**
	 * 设置加载的路径config文件加载路径
	 *
	 * @param path
	 */
	public static void setBundlePath(String path) {
		if (config != null) {
			throw new IllegalStateException("local config obj has been created,can not set path repeat");
		}
		bundlePath = path;
	}

	private Config() {
		ResourceBundle bundle = ResourceBundle.getBundle(StringUtils.defaultIfEmpty(bundlePath,
				System.getProperty("tis_config", "com/qlangtech/tis/manage/config")));
		// localRepository = bundle.getString("local.repository");
		P p = new P(bundle);

		this.tisRepository = p.getString("tis.repository");

		this.projectName = p.getString("project.name");
		if (StringUtils.isEmpty(this.projectName)) {
			throw new IllegalStateException("config param projectname can not be null");
		}
		userToken = new HashMap<String, String>();
		publishHook = new HashMap<RunEnvironment, String>();
		responseTimeHost = new HashMap<RunEnvironment, String>();
		// zkAddress = new HashMap<RunEnvironment, String>();
		hdfshost = new HashMap<RunEnvironment, String>();
		// this.tddlParseHost = p.getString("tddl.parse.host");
		Enumeration<String> keys = bundle.getKeys();
		String nameKey = null;
		final String hookHostPrefix = "publish.hook.host.";
		final String queryResponseTimeHost = "query.response.time.host.";
		final String zkaddress = "zkaddress.host.";
		while (keys.hasMoreElements()) {
			nameKey = keys.nextElement();
			if (StringUtils.startsWith(nameKey, "user.")) {
				userToken.put(StringUtils.substringAfter(nameKey, "user."), bundle.getString(nameKey));
			}
			setKey(publishHook, bundle, nameKey, hookHostPrefix);
			setKey(responseTimeHost, bundle, nameKey, queryResponseTimeHost);
			// setKey(zkAddress, bundle, nameKey, zkaddress);
			// hdfs 地址
			setKey(hdfshost, bundle, nameKey, "hdfs.host.");
			setKey(hsfMonotorHost, bundle, nameKey, "hsf.monitor.host.");
		}
		try {
			this.dptTerminatorId = Integer.parseInt(bundle.getString("dpt.terminator.id"));
		} catch (Throwable e) {
		}

		try {
			FuncGroup group = null;
			String[] groups = StringUtils.split(bundle.getString("func.groups"), ",");
			for (String g : groups) {
				group = new FuncGroup(Integer.parseInt(StringUtils.split(g, ":")[0]), StringUtils.split(g, ":")[1]);
				funcGroup.add(group);
			}
		} catch (Throwable e) {
		}

	}

	private class P {

		private final ResourceBundle bundle;

		public P(ResourceBundle bundle) {
			super();
			this.bundle = bundle;
		}

		public final String getString(String key) {
			try {
				return bundle.getString(key);
			} catch (Throwable e) {
			}
			return StringUtils.EMPTY;
		}
	}

	private void setKey(Map<RunEnvironment, String> store, ResourceBundle bundle, String nameKey, final String prefix) {
		if (StringUtils.startsWith(nameKey, prefix)) {
			RunEnvironment envir = RunEnvironment.getEnum(StringUtils.substringAfter(nameKey, prefix));
			store.put(envir, bundle.getString(nameKey));
		}
	}

	/**
	 * 终搜部门id
	 *
	 * @return
	 */
	public static int getDptTerminatorId() {
		return getInstance().dptTerminatorId;
	}

	public static List<FuncGroup> getFuncGroup() {
		return getInstance().funcGroup;
	}

	private static Config getInstance() {
		if (config == null) {
			synchronized (Config.class) {
				if (config == null) {
					config = new Config();
				}
			}
		}
		return config;
	}

	public static Map<String, String> getUserToken() {
		return getInstance().userToken;
	}

	public static String getProjectName() {
		return getInstance().projectName;
	}

	public static Map<RunEnvironment, String> getPublishHook() {
		return getInstance().publishHook;
	}

	public static String getResponseTimeHost(RunEnvironment runtime) {
		return getInstance().responseTimeHost.get(runtime);
	}

	public static String getHdfsNameNodeHost(RunEnvironment runtime) {
		return getInstance().hdfshost.get(runtime);
	}

	/**
	 * hsf 地址
	 *
	 * @param runtime
	 * @return
	 */
	public static String getHsfMonotorHost(RunEnvironment runtime) {
		return getInstance().hsfMonotorHost.get(runtime);
	}

	public static String getTisRepository() {
		return getInstance().tisRepository;
	}

	public static RunEnvironment getRunEnvironment() {
		return RunEnvironment.DAILY;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public static class FuncGroup {

		private final Integer key;

		private final String name;

		@Override
		public boolean equals(Object obj) {
			return this.hashCode() == obj.hashCode();
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		public FuncGroup(Integer key, String name) {
			super();
			this.key = key;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Integer getKey() {
			return key;
		}
	}
}
