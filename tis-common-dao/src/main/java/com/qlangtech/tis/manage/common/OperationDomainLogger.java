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
public interface OperationDomainLogger {

    public String getOperationLogAppName();

    public Short getOperationLogRuntime();

    /**
     * 当前执行日志的 备注信息
     *
     * @return
     */
    public abstract String getOperationLogMemo();

    /**
     * 操作描述，默认是将插入的参数序列化成json格式的形式保存，如果该方法返回为空，则说明不需要覆写默认值
     *
     * @return
     */
    public abstract String getOpDesc();

    public boolean isLogHasBeenSet();
}
