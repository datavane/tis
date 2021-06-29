/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.trigger;

/**
 * 每次触发的执行操作类型
 *
 * @author 百岁（baisui@taobao.com）
 * @date 2014年11月4日下午1:21:13
 */
public enum ExecType {

    UPDATE("update"), CREATE("create"), FULLBUILD("fullbuild");

    private final String type;

    public String getType() {
        return type;
    }

    public static ExecType parse(String value) {
        if (UPDATE.type.equals(value)) {
            return UPDATE;
        } else if (CREATE.type.equals(value)) {
            return CREATE;
        } else if (FULLBUILD.type.equals(value)) {
            return FULLBUILD;
        } else {
            throw new IllegalStateException("value is illeal:" + value);
        }
    }

    private ExecType(String type) {
        this.type = type;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

}
