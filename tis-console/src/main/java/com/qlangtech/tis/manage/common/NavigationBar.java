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

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.module.screen.BuildNavData;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NavigationBar extends BuildNavData {

    /**
     */
    private static final long serialVersionUID = 1L;

    private final HttpServletRequest request;

    public NavigationBar(HttpServletRequest request) {
        super();
        this.request = request;
    }

    @Override
    protected String createBooksItemEnd(boolean isLast) {
        return "</ul></li>";
    }

    @Override
    protected String createBooksItemStart(String id, String text) {
        return "<li><h4>" + text + "</h4><ul class=\"\">";
    }

    @Override
    public String itemEnd() {
        return "</li>";
    }

    @Override
    protected String getNavtreehead() {
        return "<div id=\"CustomerCenterMenu\"><ul class=\"menuBody\">";
    }

    @Override
    protected String getTail() {
        return "</ul></div>";
    }

    @Override
    protected String createItemStart(String id, String text, String icon, Item item, boolean isLast) {
        final boolean selected = StringUtils.equals(this.request.getRequestURI(), item.getUrl());
        return "<li><a target='_top' href=\"" + item.getUrl() + "\" " + (selected ? "class=\"se\"" : StringUtils.EMPTY) + " >" + text + "</a></li>";
    }

    // @Override
    // public String getAppsFromDB() {
    // return StringUtils.EMPTY;
    // }
    public HttpServletRequest getRequest() {
        return request;
    }
    // public void setRequest(HttpServletRequest request) {
    // this.request = request;
    // }
    // @Override
    // public String rowStart() {
    // 
    // return super.rowStart();
    // }
}
