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
package com.qlangtech.tis.pubhook.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public enum RunEnvironment {

	// ///////////
	DAILY("daily", (short) 0, "日常环境", GlobalConfigRepositoryHost.DAILY_GlobalConfigRepositoryHost, ""),
	// //////////////////////
	ONLINE("online", (short) 2, "生产环境", GlobalConfigRepositoryHost.ONLINE_GlobalConfigRepositoryHost, // /
			"http://tis2.2dfire-inc.com");

	private static final Logger logger = LoggerFactory.getLogger(RunEnvironment.class);

	public static final String KEY_RUNTIME = "runtime";

	public static boolean isDevelopMode() {
		return !isOnlineMode();
		// return ("true".equals(System.getProperty("daily")));
	}

	public static void setSysRuntime(RunEnvironment runtime) {
		System.setProperty(RunEnvironment.KEY_RUNTIME, runtime.getKeyName());
	}

	public static boolean isOnlineMode() {
		// !(DAILY.keyName.equals(System.getProperty("runtime")));
		return !(getSysRuntime() == DAILY);
	}

	private static RunEnvironment runtime;

	public static RunEnvironment getSysRuntime() {
		if (runtime == null) {
			synchronized (RunEnvironment.class) {
				if (runtime == null) {
					String run = null;
					try {
						ResourceBundle solrwebConfig = ResourceBundle.getBundle("solr-web-config/config");
						run = solrwebConfig.getString(RunEnvironment.KEY_RUNTIME);
						logger.info("runtime get from \"solr-web-config/config\":" + run);
					} catch (Throwable e) {
					}
					if (StringUtils.isBlank(run)) {
						run = System.getProperty(RunEnvironment.KEY_RUNTIME, "daily");
					}
					runtime = RunEnvironment.getEnum(run);
				}
			}
		}
		return runtime;
	}

	public static RunEnvironment getSysEnvironment() {
		return getSysRuntime();
	}

	// public static RunEnvironment current() {
	// return isOnlineMode() ? RunEnvironment.ONLINE : RunEnvironment.DAILY;
	// }
	private final Short id;

	private final String keyName;

	private final String describe;

	private final String innerRepositoryURL;

	private final String publicRepositoryURL;

	private RunEnvironment(String keyName, Short id, String describe, String innerRepositoryURL,
			String publicRepositoryURL) {
		this.id = id;
		this.keyName = keyName;
		this.describe = describe;
		this.innerRepositoryURL = innerRepositoryURL;
		this.publicRepositoryURL = publicRepositoryURL;
	}

	public String getInnerRepositoryURL() {
		return innerRepositoryURL;
	}

	public String getPublicRepositoryURL() {
		return this.publicRepositoryURL;
	}

	public Short getId() {
		return id;
	}

	public String getDescribe() {
		return describe;
	}

	public String getKeyName() {
		return keyName;
	}

	public static RunEnvironment getEnum(String key) {
		EnumSet<RunEnvironment> all = EnumSet.allOf(RunEnvironment.class);
		for (RunEnvironment r : all) {
			if (r.getKeyName().equals(key)) {
				return r;
			}
		}
		throw new IllegalArgumentException("key:" + key + " is invalid");
	}

	public static RunEnvironment getEnum(short key) {
		EnumSet<RunEnvironment> all = EnumSet.allOf(RunEnvironment.class);
		for (RunEnvironment r : all) {
			if (r.getId() == key) {
				return r;
			}
		}
		throw new IllegalArgumentException("key:" + key + " is invalid");
	}

	private static List<RunEnvironment> environmentList = new ArrayList<RunEnvironment>();

	static {
		try {
			RunEnvironment[] fields = RunEnvironment.values();
			// Object o = null;
			for (RunEnvironment f : fields) {
				// o = f.get(null);
				// if (o instanceof RunEnvironment) {
				// environmentList.add(((RunEnvironment) o));
				// }
				environmentList.add(f);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static List<RunEnvironment> getRunEnvironmentList() {
		return environmentList;
	}

	public static void main(String[] arg) throws Exception {
		List<RunEnvironment> environmentList = RunEnvironment.getRunEnvironmentList();
		for (RunEnvironment envir : environmentList) {
			System.out.println(envir);
		}
	}
}
