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
package com.qlangtech.tis.order.dump.task;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.dump.INameWithPathGetter;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITISFileSystemFactory;
import com.qlangtech.tis.fs.ITableBuildTask;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-02 18:48
 */
public class MockTableDumpFactory extends TableDumpFactory {
    private final ITISFileSystemFactory fileSystemFactory;

    public MockTableDumpFactory(ITISFileSystemFactory fileSystemFactory) {
        this.fileSystemFactory = fileSystemFactory;
    }

    @Override
    public ITISFileSystem getFileSystem() {
        return this.fileSystemFactory.getFileSystem();
    }

    @Override
    public void bindTables(Set<EntityName> hiveTables, String timestamp, ITaskContext context) {

    }

    @Override
    public void deleteHistoryFile(EntityName dumpTable, ITaskContext taskContext) {

    }

    @Override
    public void deleteHistoryFile(EntityName dumpTable, ITaskContext taskContext, String timestamp) {

    }

    @Override
    public void dropHistoryTable(EntityName dumpTable, ITaskContext taskContext) {

    }

    @Override
    public String getJoinTableStorePath(INameWithPathGetter pathGetter) {
        return null;
    }

    @Override
    public IRemoteJobTrigger createSingleTableDumpJob(IDumpTable table, String startTime, TaskContext context) {
        return null;
    }

    @Override
    public void startTask(TaskMapper taskMapper, TaskContext taskContext) throws Exception {
        taskMapper.map(taskContext);
    }

    @Override
    public void startTask(ITableBuildTask dumpTask) {
        try {
            ITaskContext context = new ITaskContext() {
                @Override
                public <T> T getObj() {
                    return null;
                }
            };
            dumpTask.process(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
