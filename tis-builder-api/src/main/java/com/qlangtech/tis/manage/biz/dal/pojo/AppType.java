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
package com.qlangtech.tis.manage.biz.dal.pojo;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-19 22:34
 */
public enum AppType {
    SolrIndex(1), DataXPipe(2);

    private final int type;

    AppType(int type) {
        this.type = type;
    }

    public static AppType parse(int type) {
        for (AppType t : AppType.values()) {
            if (t.type == type) {
                return t;
            }
        }

        throw new IllegalStateException("invalid type:" + type);
    }
}
