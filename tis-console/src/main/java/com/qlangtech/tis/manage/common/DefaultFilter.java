/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.ChangeDomainAction;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public final class DefaultFilter implements Filter {

  private static ThreadLocal<ServletResponse> responseLocal = new ThreadLocal<ServletResponse>();

  private static ThreadLocal<TISHttpServletRequestWrapper> requestLocal = new ThreadLocal<TISHttpServletRequestWrapper>();

  public static ServletResponse getRespone() {
    // ServletActionContext.getResponse();
    return responseLocal.get();
  }

  // private TerminatorEagleEyeFilter eagleEyeFilter;
  public static TISHttpServletRequestWrapper getReqeust() {
    TISHttpServletRequestWrapper request = requestLocal.get();
    Assert.assertNotNull("request has not been set in local thread", request);
    return request;
  }

  // private static final Pattern p2 = Pattern.compile("(.*?)_run(\\d+)");
  private static AppAndRuntime getRuntime(TISHttpServletRequestWrapper request) {
    final String key = "request" + ChangeDomainAction.COOKIE_SELECT_APP;
    if (request.getAttribute(key) == null) {
      AppAndRuntime appAndRuntime = new AppAndRuntime();

      String appName = StringUtils.defaultString(
        request.getHeader(TriggerBuildResult.KEY_APPNAME)
        , request.getParameter(TriggerBuildResult.KEY_APPNAME));
      // if (cookie == null) {
      if (StringUtils.isBlank(appName)) {
        // RunEnvironment.getSysEnvironment();//
        appAndRuntime.setRuntime(RunEnvironment.getSysRuntime());
        // ManageUtils.isDevelopMode()
        // ?
        // RunEnvironment.DAILY
        // :
        // RunEnvironment.ONLINE;
        request.setAttribute(key, appAndRuntime);
        // 只有预发和线上的可能了
        return appAndRuntime;
      }
      // Matcher match = p2.matcher(cookie.getValue());
      // if (match.matches()) {
      appAndRuntime.setAppName(appName);
      // RunEnvironment.getSysEnvironment();//
      appAndRuntime.setRuntime(RunEnvironment.getSysRuntime());
      // RunEnvironment.getEnum(Short.parseShort(match.group(2)));
      if (!ManageUtils.isDaily() && appAndRuntime.getRuntime() != RunEnvironment.DAILY) {
        request.setAttribute(key, appAndRuntime);
        // 只有预发和线上的可能了
        return appAndRuntime;
      }
      // }
      appAndRuntime.setRuntime(getRuntime());
      request.setAttribute(key, appAndRuntime);
      return appAndRuntime;
    }
    return (AppAndRuntime) request.getAttribute(key);
  }

  public static RunEnvironment getRuntime() {
    // RunEnvironment.getSysEnvironment();
    return RunEnvironment.getSysRuntime();
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
      request.setCharacterEncoding(TisUTF8.getName());
      response.setCharacterEncoding(TisUTF8.getName());
      final TISHttpServletRequestWrapper wrapperRequest = new TISHttpServletRequestWrapper((HttpServletRequest) request);
      final TISHttpServletResponseWrapper wrapperResponse = new TISHttpServletResponseWrapper((HttpServletResponse) response);
      responseLocal.set(response);
      requestLocal.set(wrapperRequest);
      AppAndRuntime.setAppAndRuntime(getRuntime(wrapperRequest));
      if (ManageUtils.isDaily()) {
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
      chain.doFilter(wrapperRequest, wrapperResponse);
    } finally {
      // responseLocal.set(null);
      // requestLocal.set(null);
      // System.out.println("consume:"
      // + (System.currentTimeMillis() - start));
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    //  AbstractTisCloudSolrClient.initHashcodeRouter();
  }
}
