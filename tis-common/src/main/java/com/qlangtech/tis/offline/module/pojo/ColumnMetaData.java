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
package com.qlangtech.tis.offline.module.pojo;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ColumnMetaData {

    private final String key;

    private final int type;

    private final int index;

    // private final String dbType;
    // private final String hiveType;
    // 是否是主键
    private final boolean pk;

    /**
     * @param key  column名字
     * @param type column类型
     */
    public ColumnMetaData(int index, String key, int type, boolean pk) {
        super();
        this.pk = pk;
        this.key = key;
        this.type = type;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    public int getType() {
        return type;
    }

    public boolean isPk() {
        return this.pk;
    }
}
