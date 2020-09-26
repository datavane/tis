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
package com.qlangtech.tis.exec;

import com.qlangtech.tis.assemble.FullbuildPhase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年8月20日
 */
public class ExecutePhaseRange {

    private final FullbuildPhase start;

    private final FullbuildPhase end;

    public ExecutePhaseRange(FullbuildPhase start, FullbuildPhase end) {
        super();
        this.start = start;
        this.end = end;
    }

    /**
     * 阶段区间中是否包含phase？
     *
     * @param phase
     * @return
     */
    public boolean contains(FullbuildPhase phase) {
        return phase.getValue() >= this.start.getValue() && phase.getValue() <= this.end.getValue();
    }

    public FullbuildPhase getStart() {
        return this.start;
    }

    public FullbuildPhase getEnd() {
        return this.end;
    }
}
