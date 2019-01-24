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

import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
