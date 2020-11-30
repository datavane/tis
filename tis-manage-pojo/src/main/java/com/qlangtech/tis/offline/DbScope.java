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
package com.qlangtech.tis.offline;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum DbScope {

    // 用于Dump数据库表的
    DETAILED(StringUtils.EMPTY, "detailed"),
    // 用于增量任务执行
    FACADE("_facade", "facade");

    private final String type;
    private final String token;

    /**
     *
     * @param val
     * @return
     */
    public static DbScope parse(String val) {
        if (StringUtils.isEmpty(val)) {
            throw new IllegalArgumentException("param 'val' can not be null");
        }
        for (DbScope type : DbScope.values()) {
            if (type.token.equals(val)) {
                return type;
            }
        }

        throw new IllegalStateException("illegal val:" + val);
    }

    private DbScope(String type, String token) {
        this.type = type;
        this.token = token;
    }

    public String getDBType() {
        return this.type;
    }
}
