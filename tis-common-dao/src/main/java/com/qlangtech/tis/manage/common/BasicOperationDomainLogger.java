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
package com.qlangtech.tis.manage.common;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-2-5
 */
public abstract class BasicOperationDomainLogger implements OperationDomainLogger {

    @JSONField(serialize = false)
    private OperationDomainLogger logger;

    @JSONField(serialize = false)
    public String getOperationLogAppName() {
        return logger.getOperationLogAppName();
    }

    @JSONField(serialize = false)
    public String getOperationLogMemo() {
        return logger.getOperationLogMemo();
    }

    @Override
    @JSONField(serialize = false)
    public String getOpDesc() {
        return logger.getOpDesc();
    }

    @JSONField(serialize = false)
    public Short getOperationLogRuntime() {
        return logger.getOperationLogRuntime();
    }

    public void setLogger(OperationDomainLogger logger) {
        this.logger = logger;
    }

    @Override
    @JSONField(serialize = false)
    public boolean isLogHasBeenSet() {
        return this.logger != null;
    }
}
