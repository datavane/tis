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
package com.qlangtech.tis.indexbuilder.source.impl;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-01 23:05
 */
class Text {

    private String content;

    public void set(byte[] data, int offset, int len) {
        this.content = new String(data, offset, len);
    }

    @Override
    public String toString() {
        return this.content;
    }
}
