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
package com.qlangtech.tis.trigger.socket;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum InfoType {

    INFO(1), WARN(2), ERROR(3), FATAL(4);

    private final int type;

    private InfoType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static InfoType getType(int type) {
        if (INFO.type == type) {
            return INFO;
        }
        if (WARN.type == type) {
            return WARN;
        }
        if (ERROR.type == type) {
            return ERROR;
        }
        if (FATAL.type == type) {
            return FATAL;
        }
        throw new IllegalArgumentException("type:" + type + " is invalid");
    }
}
