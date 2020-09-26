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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatus;
import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatusVisitor;
import com.qlangtech.tis.fullbuild.phasestatus.IProcessDetailStatus;

/**
 * 一个阶段有几个子过程(通常子过程执行中会有几个子过程)需要有一个总出口，说明执行详细
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月26日
 */
public abstract class ProcessDetailStatusImpl<T extends IChildProcessStatus> implements IProcessDetailStatus<T> {

    private final Map<String, T> /* childname */
    childStatus;

    public ProcessDetailStatusImpl(Map<String, T> childStatus) {
        super();
        this.childStatus = childStatus;
    }

    public Collection<T> getDetails() {
        if (childStatus == null || this.childStatus.isEmpty()) {
            return Collections.singleton(createMockStatus());
        }
        return this.childStatus.values();
    }

    protected abstract T createMockStatus();

    @Override
    public int getProcessPercent() {
        // return (int) (result / 100);
        return 0;
    }

    @Override
    public void detailVisit(IChildProcessStatusVisitor visitor) {
        for (Map.Entry<String, T> entry : childStatus.entrySet()) {
            visitor.visit(entry.getValue());
        }
    }
}
