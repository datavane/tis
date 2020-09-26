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

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-11
 */
public class ANode {

    // id1,
    // text 'A leaf Node'
    // leaf: true,
    private final String id;

    private final String text;

    private final boolean leaf;

    private final Integer bizid;

    private final String realname;

    public static ANode createBizNode(int bizid, String bizName) {
        return new ANode("b" + bizid, bizName, null, false, bizid);
    }

    public static BizANode createExtBizNode(int bizid, String bizName) {
        return new BizANode("b" + bizid, bizName, null, false, bizid);
    }

    public static ANode createAppNode(int appid, String appName) {
        return new ANode(String.valueOf(appid), trimName(appName), (appName), true, null);
    }

    private static String trimName(String value) {
        if (StringUtils.startsWith(value, "search4")) {
            return "s" + StringUtils.substring(value, 6);
        }
        return value;
    }

    protected ANode(String id, String text, String realname, boolean leaf, Integer bizid) {
        super();
        this.id = id;
        this.text = text;
        this.leaf = leaf;
        this.bizid = bizid;
        this.realname = realname;
    }

    public Integer getBizid() {
        return bizid;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isLeaf() {
        return leaf;
    }
}
