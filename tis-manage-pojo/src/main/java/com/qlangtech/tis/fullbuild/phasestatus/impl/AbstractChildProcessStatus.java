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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年7月6日
 */
public abstract class AbstractChildProcessStatus implements IChildProcessStatus {

    private boolean faild;

    private boolean complete;

    // 等待导入中
    private boolean waiting = true;

    public final boolean isWaiting() {
        return this.waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isComplete() {
        return complete;
    }

    public final void setComplete(boolean complete) {
        if (complete) {
            this.waiting = false;
        }
        this.complete = complete;
    }

    /**
     * 是否成功，完成了且没有错误
     */
    public final boolean isSuccess() {
        return this.isComplete() && !this.isFaild();
    }

    @Override
    public boolean isFaild() {
        return this.faild;
    }

    public void setFaild(boolean faild) {
        this.faild = faild;
    }
}
