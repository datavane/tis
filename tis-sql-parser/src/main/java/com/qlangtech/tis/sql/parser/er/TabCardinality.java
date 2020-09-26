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

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum TabCardinality {

    ONE_ONE("1:1"), ONE_N("1:n");

    /*一对多*/
    private final String token;

    private TabCardinality(String val) {
        this.token = val;
    }

    public String getToken() {
        return this.token;
    }

    public static TabCardinality parse(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("token:" + token + " is illegal");
        }
        for (TabCardinality c : TabCardinality.values()) {
            if (c.token.equals(token)) {
                return c;
            }
        }
        throw new IllegalStateException("token:" + token + " is illegal");
    }
}
