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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-2-4
 */
public class DefaultOperationDomainLogger implements OperationDomainLogger {

    private String appName;

    private String memo;

    private Short runtime;

    private String opDesc;

    @Override
    public String getOpDesc() {
        return this.opDesc;
    }

    public void setOpDesc(String opDesc) {
        this.opDesc = opDesc;
    }

    @Override
    public boolean isLogHasBeenSet() {
        return true;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Short getRuntime() {
        return runtime;
    }

    public void setRuntime(Short runtime) {
        this.runtime = runtime;
    }

    @Override
    public String getOperationLogAppName() {
        return this.appName;
    }

    @Override
    public String getOperationLogMemo() {
        return this.memo;
    }

    @Override
    public Short getOperationLogRuntime() {
        return this.runtime;
    }
}
