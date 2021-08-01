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
package com.qlangtech.tis.fullbuild.indexbuild;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRemoteJobTrigger {

    /**
     * 是否是异步任务
     *
     * @return
     */
    default boolean isAsyn() {
        return false;
    }

    /**
     * 异步任务名称
     *
     * @return
     */
    public default String getAsynJobName() {
        // 只有 isAsyn 返回true时候才能调用该方法
        throw new UnsupportedOperationException();
    }

    /**
     * 触发任务
     */
    public void submitJob();

    public RunningStatus getRunningStatus();
}
