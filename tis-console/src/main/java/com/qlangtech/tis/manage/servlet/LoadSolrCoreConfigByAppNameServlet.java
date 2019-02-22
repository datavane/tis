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
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.util.LRUCache;
import com.qlangtech.tis.openapi.SnapshotNotFindException;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.openapi.impl.SnapshotDomainGetter;
import com.qlangtech.tis.pubhook.common.ConfigConstant;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * http://localhost/download/appconfig/appname/0/
 * runtimeEnvironment/schema.xml/solrconfig.xml
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LoadSolrCoreConfigByAppNameServlet extends BasicServlet {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(LoadSolrCoreConfigByAppNameServlet.class);

	private static final Pattern resourcePattern = Pattern
			.compile(DownloadServlet.replace(ConfigConstant.FILE_SCHEMA + "|" + ConfigConstant.FILE_SOLOR));

	public static final String pattern_runtime = "/(" + RunEnvironment.DAILY.getKeyName() + "|"
			+ RunEnvironment.ONLINE.getKeyName() + ")";

	private static final Pattern pattern = Pattern.compile(
			".+?/download/appconfig/(.+?)/(\\d{1,})" + pattern_runtime + "((/(" + resourcePattern.pattern() + "))+)");

	protected String getResources(HttpServletRequest request) throws ServletException {
		return this.getMatcher(request).group(4);
	}

	protected final Matcher getMatcher(HttpServletRequest request) throws ServletException {
		Matcher matcher = null;
		if (!(matcher = getUrlPattern().matcher(request.getRequestURL())).matches()) {
			throw new ServletException("has not match dowload url pattern:" + request.getRequestURL());
		}
		return matcher;
	}

	protected final Pattern getUrlPattern() {
		return pattern;
	}

	protected final boolean isFindAll() {
		return false;
	}

	private static final LRUCache<Integer, SnapshotDomain> resourceCache = new LRUCache<Integer, SnapshotDomain>();

	static {
		Map<String, String> args = new HashMap<String, String>();
		args.put("name", "resourceCache");
		args.put("size", "200");
		resourceCache.init(args, null);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		Matcher matcher = getMatcher(request);
		final AppKey appKey = new AppKey(
				matcher.group(1), /* appName ========== */
				Short.parseShort(matcher.group(2)), /* groupIndex */
				RunEnvironment.getEnum(matcher.group(3)),
				"true".equals(request.getParameter(HttpConfigFileReader.unmergeglobalparams)));
		setSnapshotId(request, appKey);
		log.warn("getres " + appKey.appName + ",unmerge:" + appKey.unmergeglobalparams + ",client ip:"
				+ request.getRemoteAddr() + "=>" + request.getRequestURI());
		List<PropteryGetter> needRes = new ArrayList<PropteryGetter>();
		matcher = resourcePattern.matcher(getResources(request));
		while ((matcher).find()) {
			needRes.add(ConfigFileReader.createPropertyGetter(matcher.group()));
		}
		SnapshotDomain colon = getSnapshotDomain(needRes, appKey, this.getContext());
		this.wirteXml2Client(resp, colon);
	}

	/**
	 * @param request
	 * @param appKey
	 */
	private void setSnapshotId(HttpServletRequest request, final AppKey appKey) {
		Long targetSnapshotId = null;
		try {
			targetSnapshotId = Long.parseLong(request.getParameter("snapshotid"));
			if (targetSnapshotId > 0) {
				appKey.setTargetSnapshotId(targetSnapshotId);
			}
		} catch (Throwable e) {
		}
	}

	/**
	 * @param request
	 * @param appKey
	 * @return
	 * @throws ServletException
	 */
	public static SnapshotDomain getSnapshotDomain(List<PropteryGetter> needRes, final AppKey appKey,
			RunContext runContext) throws ServletException {
		SnapshotDomain snapshot = null;
		snapshot = (SnapshotDomain) resourceCache.get(appKey.hashCode());
		try {
			if (!appKey.isFromCache() || snapshot == null) {
				synchronized (resourceCache) {
					snapshot = (SnapshotDomain) resourceCache.get(appKey.hashCode());
					if (!appKey.isFromCache() || snapshot == null) {
						SnapshotDomainGetter snapshotDomainGetter = new SnapshotDomainGetter(runContext);
						snapshot = snapshotDomainGetter.getSnapshot(appKey);
						snapshot = getSnapshot(true, needRes, snapshot);
						resourceCache.put(appKey.hashCode(), snapshot);
					}
				}
			}
		} catch (SnapshotNotFindException e) {
			throw new ServletException(e);
		}
		SnapshotDomain colon = getSnapshot(false, needRes, snapshot);
		return colon;
	}

	private static SnapshotDomain getSnapshot(boolean isFindAll, List<PropteryGetter> needRes, SnapshotDomain snapshot)
			throws ServletException {
		SnapshotDomain colon = new SnapshotDomain(snapshot.getSnapshot());
		// while (isFindAll || (matcher).find()) {
		for (PropteryGetter getter : needRes) {

			if (isFindAll || ConfigConstant.FILE_SCHEMA.equals(getter.getFileName())) {
				colon.setSolrSchema(snapshot.getSolrSchema());
				debugResContent(snapshot.getSolrSchema());
			}
			if (isFindAll || ConfigConstant.FILE_SOLOR.equals(getter.getFileName())) {
				colon.setSolrConfig(snapshot.getSolrConfig());
				debugResContent(snapshot.getSolrConfig());
			}
			if (isFindAll) {
				break;
			}
		}
		return colon;
	}

	private static void debugResContent(UploadResource res) {
	}

	public static void main(String[] arg) {
	}
}
