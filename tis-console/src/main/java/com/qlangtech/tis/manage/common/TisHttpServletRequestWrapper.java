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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public TisHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    private AppDomainInfo domain;

    @Override
    public void setAttribute(String name, Object o) {
        try {
            if (ActionTool.REQUEST_DOMAIN_KEY.equals(name) && (domain == null)) {
                this.domain = (AppDomainInfo) o;
            } else {
                super.setAttribute(name, o);
            }
        } catch (Exception e) {
            throw new RuntimeException("name:" + name + ",value:" + o, e);
        }
    }

    private Map<String, Cookie> cookieMap;

    public Cookie getCookie(String name) {
        return getCookieMap().get(name);
    // return cookieMap.get(name);
    }

    private Map<String, Cookie> getCookieMap() {
        if (cookieMap == null) {
            synchronized (this) {
                if (cookieMap == null) {
                    cookieMap = new HashMap<String, Cookie>();
                    Cookie[] cs = this.getCookies();
                    if (cs != null) {
                        for (Cookie c : cs) {
                            cookieMap.put(c.getName(), c);
                        }
                    }
                }
            }
        }
        return cookieMap;
    }

    public void removeCookie(String cookiekey) {
        getCookieMap().remove(cookiekey);
    }

    // @Override
    // public ServletInputStream getInputStream() throws IOException {
    // 
    // return super.getInputStream();
    // }
    // 
    // @Override
    // public BufferedReader getReader() throws IOException {
    // 
    // return super.getReader();
    // }
    @Override
    public String[] getParameterValues(String name) {
        String[] params = super.getParameterValues(name);
        if (params != null) {
            return params;
        }
        List<String> result = new ArrayList<String>();
        int i = 0;
        String value = null;
        while (StringUtils.isNotBlank(value = this.getParameter(name + '[' + (i++) + ']'))) {
            result.add(value);
        }
        return result.toArray(new String[] {});
    }

    @Override
    public Object getAttribute(String name) {
        if (ActionTool.REQUEST_DOMAIN_KEY.equals(name)) {
            return this.domain;
        } else {
            return super.getAttribute(name);
        }
    }
}
