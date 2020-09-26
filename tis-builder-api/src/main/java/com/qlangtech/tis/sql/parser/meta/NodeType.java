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
package com.qlangtech.tis.sql.parser.meta;

/**
 * 节点类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年4月30日
 */
public enum NodeType {

    JOINER_SQL("join"), DUMP("table"), UNION_SQL("union");

    private final String type;

    private NodeType(String type) {
        this.type = type;
    }

    public static NodeType parse(String type) {
        if (JOINER_SQL.type.equals(type)) {
            return NodeType.JOINER_SQL;
        }
        if (UNION_SQL.type.equals(type)) {
            return UNION_SQL;
        }
        if (DUMP.type.equals(type)) {
            return DUMP;
        }
        throw new IllegalStateException("illegal type:'" + type + "'");
    }

    public String getType() {
        return type;
    }
}
