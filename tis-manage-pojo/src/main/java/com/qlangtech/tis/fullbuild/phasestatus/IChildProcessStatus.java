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
 * 单个步骤执行任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月19日
 */
public interface IChildProcessStatus {

    /**
     * 因为要显示单位，所以要返回字符串
     * @return
     */
    public String getAll();

    /**
     * 因为要显示单位，所以要返回字符串
     * @return
     */
    public String getProcessed();

    public int getPercent();

    /**
     * 執行過程中發生錯誤或者失敗
     * @return
     */
    public boolean isFaild();

    /**
     * 是否完成了
     * @return
     */
    public boolean isComplete();

    public boolean isSuccess();

    /**
     * 是否正在等待
     * @return
     */
    public boolean isWaiting();

    /**
     * 子任务名称
     *
     * @return
     */
    public String getName();
}
