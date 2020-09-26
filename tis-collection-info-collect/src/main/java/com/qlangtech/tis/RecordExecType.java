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
package com.qlangtech.tis;

/**
 *  收集数据信息的种类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年2月5日 下午10:36:28
 */
public enum RecordExecType {

    UPDATE("update"), QUERY("query"), UPDATE_ERROR("uerror"), QUERY_ERROR("qerror");

    private final String value;

    private RecordExecType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RecordExecType parse(String value) {
        if (UPDATE.value.equals(value)) {
            return UPDATE;
        }
        if (QUERY.value.equals(value)) {
            return QUERY;
        }
        if (UPDATE_ERROR.value.equals(value)) {
            return UPDATE_ERROR;
        }
        if (QUERY_ERROR.value.equals(value)) {
            return QUERY_ERROR;
        }
        throw new IllegalArgumentException("value:" + value + " is not illegal");
    }
}
