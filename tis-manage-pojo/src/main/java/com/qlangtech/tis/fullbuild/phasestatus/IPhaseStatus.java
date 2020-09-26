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
package com.qlangtech.tis.fullbuild.phasestatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public interface IPhaseStatus<T extends IChildProcessStatus> {

    public int getTaskId();

    /**
     * 是否完成 （成功或者失敗都是完成狀態）
     *
     * @return
     */
    public boolean isComplete();

    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccess();

    /**
     * 是否失败了
     * @return
     */
    public boolean isFaild();

    /**
     * 是否正在执行
     * @return
     */
    public boolean isProcessing();

    public IProcessDetailStatus<T> getProcessStatus();
}
