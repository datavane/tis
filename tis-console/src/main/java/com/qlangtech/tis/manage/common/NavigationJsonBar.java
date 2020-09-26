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
 * @date 2013-4-22
 */
public class NavigationJsonBar extends BuildNavData {

    /**
     */
    private static final long serialVersionUID = 1L;

    private final HttpServletRequest request;

    public NavigationJsonBar(HttpServletRequest request) {
        super();
        this.request = request;
    }

    @Override
    protected String createBooksItemStart(String id, String text) {
        return "{groupname:\"" + text + "\",items:[";
    }

    @Override
    protected String createBooksItemEnd(boolean isLast) {
        return "]}" + (isLast ? StringUtils.EMPTY : ",");
    }

    @Override
    protected String createItemStart(String id, String text, String icon, Item item, boolean islast) {
        return "{href:\"" + item.getUrl() + "\",text:\"" + text + "\"}" + (islast ? StringUtils.EMPTY : ",");
    }

    @Override
    public String itemEnd() {
        // return "}";
        return StringUtils.EMPTY;
    }

    @Override
    protected String getNavtreehead() {
        return "{results:[";
    }

    @Override
    protected String getTail() {
        return "]}";
    }

    // @Override
    // public String getAppsFromDB() {
    // return StringUtils.EMPTY;
    // }
    public HttpServletRequest getRequest() {
        return request;
    }
}
