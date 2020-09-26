/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.module.screen.BuildNavData;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-10
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
