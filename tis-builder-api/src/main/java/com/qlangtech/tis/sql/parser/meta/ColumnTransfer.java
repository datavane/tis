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

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ColumnTransfer {

    private String colKey;

    private String transfer;

    private String param;

    public ColumnTransfer() {
    }

    @Override
    public String toString() {
        return "ColumnTransfer{" + "colKey='" + colKey + '\'' + ", transfer='" + transfer + '\'' + ", param='" + param + '\'' + '}';
    }

    public ColumnTransfer(String colKey, String transfer, String param) {
        if (StringUtils.isEmpty(colKey)) {
            throw new IllegalArgumentException("param colKey can not be null");
        }
        if (StringUtils.isEmpty(transfer)) {
            throw new IllegalArgumentException("param transfer can not be null");
        }
        if (StringUtils.isEmpty(param)) {
            throw new IllegalArgumentException("param param can not be null");
        }
        this.colKey = colKey;
        this.transfer = transfer;
        this.param = param;
    }

    public String getColKey() {
        return colKey;
    }

    public void setColKey(String colKey) {
        this.colKey = colKey;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
