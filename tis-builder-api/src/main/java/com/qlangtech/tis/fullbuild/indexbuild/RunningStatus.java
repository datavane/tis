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
package com.qlangtech.tis.fullbuild.indexbuild;

import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RunningStatus {

    public static final RunningStatus SUCCESS = new RunningStatus(1f, true, true);

    public static final RunningStatus FAILD = new RunningStatus(0f, true, false);

    private final float progress;

    private final boolean complete;

    private final boolean success;

    /**
     * @param progress
     * @param complete
     */
    public RunningStatus(float progress, boolean complete, boolean success) {
        super();
        this.progress = progress;
        this.complete = complete;
        this.success = success;
    }

    public float progress() throws IOException {
        return this.progress;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isSuccess() {
        return success;
    }
}
