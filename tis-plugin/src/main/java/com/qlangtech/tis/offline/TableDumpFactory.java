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
package com.qlangtech.tis.offline;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.fs.IFs2Table;
import com.qlangtech.tis.fullbuild.indexbuild.*;
import com.qlangtech.tis.fullbuild.taskflow.ITableBuildTaskContext;
import com.qlangtech.tis.plugin.IdentityName;

/**
 * 导入表的方式，可以使用本地，k8s容器，YARN容器等方式
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class TableDumpFactory implements Describable<TableDumpFactory>, ITableDumpJobFactory, IFs2Table, ITableBuildTaskContext, IdentityName {

    public static final ITableDumpJobFactory NO_OP = new ITableDumpJobFactory() {

        @Override
        public void startTask(TaskMapper taskMapper, TaskContext taskContext) {
        // throw new UnsupportedOperationException();
        }

        @Override
        public IRemoteJobTrigger createSingleTableDumpJob(IDumpTable table, String startTime, TaskContext context) {
            return new IRemoteJobTrigger() {

                @Override
                public void submitJob() {
                }

                @Override
                public RunningStatus getRunningStatus() {
                    return new RunningStatus(1f, true, true);
                }
            };
        }
    };

    @Override
    public Descriptor<TableDumpFactory> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
}
