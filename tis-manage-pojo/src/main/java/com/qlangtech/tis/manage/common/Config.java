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
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

/* 
 * 全局配置信息加載
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Config {

	private static Config config;

	private final String tisHostIp;
	// 线上同步地址
	private final String tisOnlineRepository;

	private final String assembleHostAddress;// =10.1.29.64
	private final String yarnResourceManagerHost;

	private final String projectName;

	private final List<FuncGroup> funcGroup = new ArrayList<FuncGroup>();

	private static String bundlePath;

	public static int getDptTisId() {
		return 1;
	}

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
		ResourceBundle bundle = ResourceBundle.getBundle(
				StringUtils.defaultIfEmpty(bundlePath, System.getProperty("tis_config", "tis-web-config/config")));
		// localRepository = bundle.getString("local.repository");
		P p = new P(bundle);

		this.tisHostIp = p.getString("tis.host");
		this.tisOnlineRepository = p.getString("tis.online.repository");

		this.assembleHostAddress = p.getString("assemble.host");
		
		this.yarnResourceManagerHost = p.getString("yarn.resource.manager.host");

		this.projectName = p.getString("project.name");

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

	public static String getProjectName() {
		return getInstance().projectName;
	}

	public static String getOnlineTisRepository() {
		return getInstance().tisOnlineRepository;
	}

	public static String getYarnResourceManagerHost() {
		return getInstance().yarnResourceManagerHost;
	}

	public static String getAssembleHostAddress() {
		return getInstance().assembleHostAddress;
	}

	public static String getTisRepository() {
		return "http://" + getTISHostIp() + ":8080";
	}

	public static String getTISHostIp() {
		String hostIp = getInstance().tisHostIp;
		if (StringUtils.isEmpty(hostIp)) {
			throw new IllegalStateException("hostIP can not be null");
		}
		return hostIp;
	}

	// public static RunEnvironment getRunEnvironment() {
	// return RunEnvironment.DAILY;
	// }

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
