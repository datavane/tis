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
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.RunningStatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-21 12:35
 */
public class MockRemoteJobTrigger implements IRemoteJobTrigger {

    private final boolean success;

    public MockRemoteJobTrigger(boolean success) {
        this.success = success;
    }

    @Override
    public void submitJob() {
    }

    @Override
    public RunningStatus getRunningStatus() {
        if (success) {
            return RunningStatus.SUCCESS;
        } else {
            return RunningStatus.FAILD;
        // throw new IllegalStateException("run faild");
        }
    // return success ? RunningStatus.SUCCESS : RunningStatus.FAILD;
    }
}
