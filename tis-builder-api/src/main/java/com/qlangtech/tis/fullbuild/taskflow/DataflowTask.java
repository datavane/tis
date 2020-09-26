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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.assemble.FullbuildPhase;
import org.apache.commons.lang.StringUtils;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-03 15:32
 */
public abstract class DataflowTask {

    protected final String id;

    protected DataflowTask(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("parm id can not be blank");
        }
        this.id = id;
    }

    public abstract FullbuildPhase phase();

    public abstract String getIdentityName();

    public abstract void run() throws Exception;

    protected final void signTaskSuccess() {
        this.getTaskWorkStatus().put(this.id, true);
    }

    protected final void signTaskFaild() {
        this.getTaskWorkStatus().put(this.id, false);
    }

    // 每个节点的执行状态
    protected abstract Map<String, Boolean> getTaskWorkStatus();

    @Override
    public String toString() {
        return this.id;
    }
}
