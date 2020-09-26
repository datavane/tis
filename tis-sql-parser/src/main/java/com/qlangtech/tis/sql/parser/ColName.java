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
package com.qlangtech.tis.sql.parser;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ColName {

    private final String name;

    private final String alias;

    public boolean useAlias() {
        return !StringUtils.equals(this.name, this.alias);
    }

    public ColName(String name) {
        this(name, name);
    }

    public String getName() {
        return this.name;
    }

    public String getAliasName() {
        return this.alias;
    }

    public ColName(String name, String alias) {
        super();
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("param name can not be empty");
        }
        this.name = name;
        this.alias = alias;
    }

    @Override
    public int hashCode() {
        return alias.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        return this.hashCode() == obj.hashCode();
    }

    public String toString() {
        if (StringUtils.equals(name, this.alias)) {
            return "name:" + name;
        } else {
            return "name:" + name + ",alias:" + this.alias;
        }
    }
}
