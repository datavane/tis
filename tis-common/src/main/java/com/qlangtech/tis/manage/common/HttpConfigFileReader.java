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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.thoughtworks.xstream.XStream;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HttpConfigFileReader extends ConfigFileReader {

	private static final Logger logger = LoggerFactory.getLogger(HttpConfigFileReader.class);

	private final File localRepos;

	public static final String unmergeglobalparams = "unmergeglobalparams";

	protected static final XStream xstream = new XStream();

	private final Application application;

	/**
	 * @param snapshot
	 * @param appDomainDir
	 * @param localRepos
	 *            本地临时文件夹
	 */
	public HttpConfigFileReader(SnapshotDomain snapshot, URL appDomainDir, File localRepos, Application application) {
		super(snapshot, appDomainDir);
		this.localRepos = localRepos;
		this.application = application;
	}

	/**
	 * 取得本地存放索引的文件夹
	 *
	 * @return
	 */
	public File getLocalSolrHome() {
		return ConfigFileReader.getAppDomainDir(this.localRepos, this.application.getDptId(),
				this.application.getAppId());
	}

	@Override
	public String getPath(PropteryGetter pGetter) {
		return getSpecificUrl(pGetter).toString();
	}

	@Override
	public byte[] getContent(PropteryGetter getter) {
		URL apply = getSpecificUrl(getter);
		return ConfigFileContext.processContent(apply, new StreamProcess<byte[]>() {

			@Override
			public byte[] p(int status, InputStream stream, String md5) {
				try {
					return IOUtils.toByteArray(stream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		// return super.getContent(getter);
	}

	private URL getSpecificUrl(PropteryGetter getter) {
		try {
			URL apply = new URL(this.getAppDomainDir(), "/download/bypid/" + this.getSnapshot().getSnapshot().getSnId()
					+ "/snid/" + this.getSnapshot().getSnapshot().getSnId() + "/" + getter.getFileName());
			return apply;
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public File getNewFile(PropteryGetter pGetter, Long fileSufix) {
		if (fileSufix == null) {
			throw new IllegalArgumentException("fileSufix can not be null");
		}
		File localFile = getNewFile(ConfigFileReader
				.getAppDomainDir(this.localRepos, application.getDptId(), application.getAppId()).toURI(), pGetter,
				fileSufix);
		// 文件已经存在
		if (localFile.exists()) {
			return localFile;
		}
		// 将文件从远端服务器上下载下来
		byte[] content = getContent(pGetter);
		try {
			FileUtils.writeByteArrayToFile(localFile, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return localFile;
	}

	// // 百岁添加 20121200113 从后台repository中取得 配置为呢间信息，以及jar包
	// public PublishInfo getPublishInfo(String ipaddress,
	// RunEnvironment environment) throws MalformedURLException {
	//
	// // if (coreName == null) {
	// // throw new IllegalArgumentException("coreCofig can not be null");
	// // }
	// if (ipaddress == null) {
	// throw new IllegalArgumentException("ipaddress can not be null");
	// }
	//
	// final URL applyUrl = new URL(this.getAppDomainDir()
	// + "/download/getconfigbycorename/" + environment.getKeyName()
	// + "/" + ipaddress + "/" + this.application.getProjectName());
	//
	// return ConfigFileContext.processContent(applyUrl,
	// new StreamProcess<PublishInfo>() {
	// @Override
	// public PublishInfo p(int status, InputStream stream,
	// String md5) {
	// return checkFileHasNotDistort((PublishInfo) xstream
	// .fromXML(stream));
	// }
	// });
	//
	// }
	public static SnapshotDomain getResource(String terminatorUrl, String appName, RunEnvironment runtime,
			PropteryGetter... fileGetter) throws TerminatorRepositoryException {
		return getResource(terminatorUrl, appName, -1, /* targetSnapshotid */
				runtime, false, fileGetter);
	}

	/**
	 * @param terminatorUrl
	 * @param appName
	 * @param targetSnapshotid
	 *            需要下载的snapshot版本号
	 * @param runtime
	 * @param fileGetter
	 * @return
	 * @throws TerminatorRepositoryException
	 */
	public static SnapshotDomain getResource(String terminatorUrl, String appName, final long targetSnapshotid,
			RunEnvironment runtime, PropteryGetter... fileGetter) throws TerminatorRepositoryException {
		return getResource(terminatorUrl, appName, targetSnapshotid, runtime, false, fileGetter);
	}

	public static SnapshotDomain getResource(String terminatorUrl, String appName, final long targetSnapshotid,
			RunEnvironment runtime, boolean unmergeglobalparams, PropteryGetter... fileGetter)
			throws TerminatorRepositoryException {
		return getResource(terminatorUrl, appName, targetSnapshotid, runtime, unmergeglobalparams,
				true, /* reThrowNewException */
				fileGetter);
	}

	/**
	 * 从TIS仓库中取得资源
	 *
	 * @param terminatorUrl
	 * @param appName
	 * @param groupIndex
	 * @param runtime
	 * @param fileGetter
	 * @return
	 */
	public static SnapshotDomain getResource(String terminatorUrl, String appName, final long targetSnapshotid,
			RunEnvironment runtime, boolean unmergeglobalparams, boolean reThrowNewException,
			PropteryGetter... fileGetter) throws TerminatorRepositoryException {
		if (StringUtils.isEmpty(terminatorUrl)) {
			throw new IllegalArgumentException("parameter terminatorUrl can not be null");
		}
		if (StringUtils.isEmpty(appName)) {
			throw new IllegalArgumentException("parameter appName can not be null");
		}
		if (fileGetter == null || fileGetter.length < 1) {
			throw new IllegalArgumentException("parameter fileGetter can not be null or length < 1");
		}
		if (runtime == null) {
			throw new IllegalArgumentException("parameter runtime can not be null or length < 1");
		}
		try {
			StringBuffer url = new StringBuffer(terminatorUrl + "/download/appconfig/" + appName);
			url.append("/").append(0);
			url.append("/").append(runtime.getKeyName());
			for (int i = 0; i < fileGetter.length; i++) {
				url.append("/").append(fileGetter[i].getFileName());
			}
			url.append("?snapshotid=").append(targetSnapshotid);
			// 不需要合并全局参数
			if (unmergeglobalparams) {
				url.append("&").append(HttpConfigFileReader.unmergeglobalparams).append("=true");
			}
			URL requestUrl = new URL(url.toString());
			return ConfigFileContext.processContent(requestUrl, new StreamProcess<SnapshotDomain>() {

				@Override
				public SnapshotDomain p(int status, InputStream stream, String md5) {
					return (SnapshotDomain) xstream.fromXML(stream);
				}
			});
		} catch (Throwable e) {
			if (reThrowNewException) {
				throw new TerminatorRepositoryException(
						"config resource is not exist,appname:" + appName + " groupIndex:" + 0 + " runtime:" + runtime,
						e);
			} else {
				logger.warn("can not find resource:" + ExceptionUtils.getMessage(e));
			}
		}
		return null;
	}

	public static SnapshotDomain getResource(String terminatorUrl, Integer snid) throws TerminatorRepositoryException {
		try {
			StringBuffer url = new StringBuffer(terminatorUrl + "/download/appconfigbysnid/" + snid);
			URL requestUrl = new URL(url.toString());
			return ConfigFileContext.processContent(requestUrl, new StreamProcess<SnapshotDomain>() {

				@Override
				public SnapshotDomain p(int status, InputStream stream, String md5) {
					return (SnapshotDomain) xstream.fromXML(stream);
				}
			});
		} catch (Throwable e) {
			throw new TerminatorRepositoryException("config resource is not exist,snid:" + snid, e);
		}
	}

}
