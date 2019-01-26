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
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.ChangeDomainAction;
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
        // ServletActionContext.getResponse();
        return responseLocal.get();
    }

    // private TerminatorEagleEyeFilter eagleEyeFilter;
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

    // public static Cookie getCookie(HttpServletRequest request, String
    // cookieName) {
    // Cookie[] cookies = request.getCookies();
    // if (cookies == null) {
    // return null;
    // }
    // for (Cookie c : cookies) {
    // if (StringUtils.equals(c.getName(), cookieName)) {
    // return c;
    // }
    // }
    // return null;
    // }
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // long start = System.currentTimeMillis();
        try {
            request.setCharacterEncoding("utf8");
            response.setCharacterEncoding("utf8");
            response.setContentType("text/html; charset=utf-8");
            final TisHttpServletRequestWrapper wrapperRequest = new TisHttpServletRequestWrapper((HttpServletRequest) request);
            responseLocal.set(response);
            requestLocal.set(wrapperRequest);
            appAndRuntimeLocal.set(getRuntime(wrapperRequest));
            if (ManageUtils.isDevelopMode()) {
            // com.alibaba.hecla.acl.dataobject.SysUser user = new
            // com.alibaba.hecla.acl.dataobject.SysUser();
            // user.setId(18097);
            // user.setName("default");
            // user.setLoginTime(new Date((new Date()).getTime() - 10000));
            // securityContext.setUser(user);
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
    // public static class AdapterHttpRequest implements HttpServletRequest {
    // 
    // public long getContentLengthLong() {
    // 
    // return 0;
    // }
    // 
    // public String changeSessionId() {
    // 
    // return null;
    // }
    // 
    // public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws
    // IOException, ServletException {
    // 
    // return null;
    // }
    // 
    // public AsyncContext getAsyncContext() {
    // 
    // return null;
    // }
    // 
    // public Object getAttribute(String arg0) {
    // 
    // return null;
    // }
    // 
    // public Enumeration<String> getAttributeNames() {
    // 
    // return null;
    // }
    // 
    // public String getCharacterEncoding() {
    // 
    // return null;
    // }
    // 
    // public int getContentLength() {
    // 
    // return 0;
    // }
    // 
    // public String getContentType() {
    // 
    // return null;
    // }
    // 
    // public DispatcherType getDispatcherType() {
    // 
    // return null;
    // }
    // 
    // public ServletInputStream getInputStream() throws IOException {
    // 
    // return null;
    // }
    // 
    // public String getLocalAddr() {
    // 
    // return null;
    // }
    // 
    // public String getLocalName() {
    // 
    // return null;
    // }
    // 
    // public int getLocalPort() {
    // 
    // return 0;
    // }
    // 
    // public Locale getLocale() {
    // 
    // return null;
    // }
    // 
    // public Enumeration<Locale> getLocales() {
    // 
    // return null;
    // }
    // 
    // public String getParameter(String arg0) {
    // 
    // return null;
    // }
    // 
    // public Map<String, String[]> getParameterMap() {
    // 
    // return null;
    // }
    // 
    // public Enumeration<String> getParameterNames() {
    // 
    // return null;
    // }
    // 
    // public String[] getParameterValues(String arg0) {
    // 
    // return null;
    // }
    // 
    // public String getProtocol() {
    // 
    // return null;
    // }
    // 
    // public BufferedReader getReader() throws IOException {
    // 
    // return null;
    // }
    // 
    // public String getRealPath(String arg0) {
    // 
    // return null;
    // }
    // 
    // public String getRemoteAddr() {
    // 
    // return null;
    // }
    // 
    // public String getRemoteHost() {
    // 
    // return null;
    // }
    // 
    // public int getRemotePort() {
    // 
    // return 0;
    // }
    // 
    // public RequestDispatcher getRequestDispatcher(String arg0) {
    // 
    // return null;
    // }
    // 
    // public String getScheme() {
    // 
    // return null;
    // }
    // 
    // public String getServerName() {
    // 
    // return null;
    // }
    // 
    // public int getServerPort() {
    // 
    // return 0;
    // }
    // 
    // public ServletContext getServletContext() {
    // 
    // return null;
    // }
    // 
    // public boolean isAsyncStarted() {
    // 
    // return false;
    // }
    // 
    // public boolean isAsyncSupported() {
    // 
    // return false;
    // }
    // 
    // public boolean isSecure() {
    // 
    // return false;
    // }
    // 
    // public void removeAttribute(String arg0) {
    // }
    // 
    // public void setAttribute(String arg0, Object arg1) {
    // }
    // 
    // public void setCharacterEncoding(String arg0) throws
    // UnsupportedEncodingException {
    // }
    // 
    // public AsyncContext startAsync() {
    // 
    // return null;
    // }
    // 
    // public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
    // 
    // return null;
    // }
    // 
    // public boolean authenticate(HttpServletResponse arg0) throws IOException,
    // ServletException {
    // 
    // return false;
    // }
    // 
    // public String getAuthType() {
    // 
    // return null;
    // }
    // 
    // public String getContextPath() {
    // 
    // return null;
    // }
    // 
    // public Cookie[] getCookies() {
    // 
    // return null;
    // }
    // 
    // public long getDateHeader(String arg0) {
    // 
    // return 0;
    // }
    // 
    // public String getHeader(String arg0) {
    // 
    // return null;
    // }
    // 
    // public Enumeration<String> getHeaderNames() {
    // 
    // return null;
    // }
    // 
    // public Enumeration<String> getHeaders(String arg0) {
    // 
    // return null;
    // }
    // 
    // public int getIntHeader(String arg0) {
    // 
    // return 0;
    // }
    // 
    // public String getMethod() {
    // 
    // return null;
    // }
    // 
    // public Part getPart(String arg0) throws IOException, IllegalStateException,
    // ServletException {
    // 
    // return null;
    // }
    // 
    // public Collection<Part> getParts() throws IOException, IllegalStateException,
    // ServletException {
    // 
    // return null;
    // }
    // 
    // public String getPathInfo() {
    // 
    // return null;
    // }
    // 
    // public String getPathTranslated() {
    // 
    // return null;
    // }
    // 
    // public String getQueryString() {
    // 
    // return null;
    // }
    // 
    // public String getRemoteUser() {
    // 
    // return null;
    // }
    // 
    // public String getRequestURI() {
    // 
    // return null;
    // }
    // 
    // public StringBuffer getRequestURL() {
    // 
    // return null;
    // }
    // 
    // public String getRequestedSessionId() {
    // 
    // return null;
    // }
    // 
    // public String getServletPath() {
    // 
    // return null;
    // }
    // 
    // public HttpSession getSession() {
    // 
    // return null;
    // }
    // 
    // public HttpSession getSession(boolean arg0) {
    // 
    // return null;
    // }
    // 
    // public Principal getUserPrincipal() {
    // 
    // return null;
    // }
    // 
    // public boolean isRequestedSessionIdFromCookie() {
    // 
    // return false;
    // }
    // 
    // public boolean isRequestedSessionIdFromURL() {
    // 
    // return false;
    // }
    // 
    // public boolean isRequestedSessionIdFromUrl() {
    // 
    // return false;
    // }
    // 
    // public boolean isRequestedSessionIdValid() {
    // 
    // return false;
    // }
    // 
    // public boolean isUserInRole(String arg0) {
    // 
    // return false;
    // }
    // 
    // public void login(String arg0, String arg1) throws ServletException {
    // }
    // 
    // public void logout() throws ServletException {
    // }
    // 
    // }
}
