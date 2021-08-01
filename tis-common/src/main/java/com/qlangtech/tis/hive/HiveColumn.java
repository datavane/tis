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
package com.qlangtech.tis.hive;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月14日 下午1:51:04
 */
public class HiveColumn {

   // public static String HIVE_TYPE_STRING = "STRING";

    // 插入后的name
    private String name;

    // 原来的name rawName as name
    private String rawName;

    private String type;

    private int index;

    private String defalutValue;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (getRawName() == null) {
            setRawName(name);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
        if (getName() == null) {
            setName(rawName);
        }
    }

    public String getDefalutValue() {
        return defalutValue;
    }

    public void setDefalutValue(String defalutValue) {
        this.defalutValue = defalutValue;
    }

    public boolean hasAliasName() {
        return !StringUtils.equals(rawName, name);
    }

    public boolean hasDefaultValue() {
        return !StringUtils.isBlank(defalutValue);
    }

    @Override
    public String toString() {
        return getRawName() + " " + getName();
    }
}
