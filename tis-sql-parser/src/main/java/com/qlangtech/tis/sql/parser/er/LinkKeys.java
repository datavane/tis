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
package com.qlangtech.tis.sql.parser.er;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class LinkKeys {

    // 列名
    private final String from;

    // 列名
    private final String to;

    public LinkKeys(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getHeadLinkKey() {
        return this.from;
    }

    public String getTailerLinkKey() {
        return this.to;
    }
}
