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

import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-4
 */
public class BizANode extends ANode {

    // private final ANode bizNode;
    private final List<ANode> children;

    BizANode(String id, String text, String realname, boolean leaf, Integer bizid) {
        super(id, text, realname, leaf, bizid);
        this.children = new ArrayList<ANode>();
    }

    // public BizANode(int bizid, String bizName) {
    // super();
    // bizNode = ANode.createBizNode(bizid, bizName);
    // this.children = new ArrayList<ANode>();
    // }
    public void addAppNode(int appid, String appName) {
        this.children.add(ANode.createAppNode(appid, appName));
    }

    // public ANode getBizNode() {
    // return bizNode;
    // }
    public List<ANode> getChildren() {
        return children;
    }
}
