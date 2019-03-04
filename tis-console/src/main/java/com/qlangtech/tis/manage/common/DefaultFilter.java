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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.ChangeDomainAction;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;

import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class DefaultFilter implements Filter {

	private static ThreadLocal<ServletResponse> responseLocal = new ThreadLocal<ServletResponse>();

	private static ThreadLocal<AppAndRuntime> appAndRuntimeLocal = new ThreadLocal<AppAndRuntime>();

	private static ThreadLocal<TisHttpServletRequestWrapper> requestLocal = new ThreadLocal<TisHttpServletRequestWrapper>();

	public static AppAndRuntime getAppAndRuntime() {
		return appAndRuntimeLocal.get();
	}

	public static void setAppAndRuntime(AppAndRuntime appAndRuntime) {
		appAndRuntimeLocal.set(appAndRuntime);
	}

	public static ServletResponse getRespone() {
		
		return responseLocal.get();
	}

	public static TisHttpServletRequestWrapper getReqeust() {
		TisHttpServletRequestWrapper request = requestLocal.get();
		Assert.assertNotNull("request has not been set in local thread", request);
		return request;
	}

	private static final Pattern p2 = Pattern.compile("(.*?)_run(\\d+)");

	private static AppAndRuntime getRuntime(TisHttpServletRequestWrapper request) {
		final String key = "request" + ChangeDomainAction.COOKIE_SELECT_APP;
		if (request.getAttribute(key) == null) {
			AppAndRuntime appAndRuntime = new AppAndRuntime();
			Cookie cookie = request.getCookie(ChangeDomainAction.COOKIE_SELECT_APP);
			if (cookie == null) {
				appAndRuntime.runtime = RunEnvironment.getSysRuntime();
				request.setAttribute(key, appAndRuntime);
				// 只有预发和线上的可能了
				return appAndRuntime;
			}
			Matcher match = p2.matcher(cookie.getValue());
			if (match.matches()) {
				appAndRuntime.appName = match.group(1);
				appAndRuntime.runtime = RunEnvironment.getEnum(Short.parseShort(match.group(2)));
				if (!ManageUtils.isDevelopMode() && appAndRuntime.runtime != RunEnvironment.DAILY) {
					request.setAttribute(key, appAndRuntime);
					// 只有预发和线上的可能了
					return appAndRuntime;
				}
			}
			// getRuntime();
			appAndRuntime.runtime = RunEnvironment.getSysRuntime();
			request.setAttribute(key, appAndRuntime);
			return appAndRuntime;
		}
		return (AppAndRuntime) request.getAttribute(key);
	}

	public static class AppAndRuntime {

		private String appName;

		private RunEnvironment runtime;

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public void setRuntime(RunEnvironment runtime) {
			this.runtime = runtime;
		}

		public String getAppName() {
			return appName;
		}

		public RunEnvironment getRuntime() {
			return runtime;
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// long start = System.currentTimeMillis();
		try {

			final TisHttpServletRequestWrapper wrapperRequest = new TisHttpServletRequestWrapper(
					(HttpServletRequest) request);

			request.setCharacterEncoding("utf8");
			response.setCharacterEncoding("utf8");
			response.setContentType("text/html; charset=utf-8");

			responseLocal.set(response);
			requestLocal.set(wrapperRequest);
			appAndRuntimeLocal.set(getRuntime(wrapperRequest));
			if (ManageUtils.isDevelopMode()) {

			} else {
				// securityContext.setUser(HeclaLoginValve
				// .getSysUser(wrapperRequest));
			}
			// SecurityContextHolder.setContext(securityContext);
			chain.doFilter(wrapperRequest, response);
		} finally {
			// responseLocal.set(null);
			// requestLocal.set(null);
			// System.out.println("consume:"
			// + (System.currentTimeMillis() - start));
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		AbstractTisCloudSolrClient.initHashcodeRouter();
	}

}
