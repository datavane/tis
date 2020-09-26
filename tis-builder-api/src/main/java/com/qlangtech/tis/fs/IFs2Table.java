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
package com.qlangtech.tis.fs;

import com.qlangtech.tis.dump.INameWithPathGetter;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import java.util.Set;

/**
 * FS持久文件映射到Table表相关执行内容 <br>
 * 等文件传输到FS之后,作操作
 * <ol>
 * <li>需要将FS中的文件和hive中的表进行映射</li>
 * <li>将历史的pt表删除</li>
 * <li>检查是否有添加字段，有则需要将历史表删除后新建</li>
 * </ol>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IFs2Table {

    public ITISFileSystemFactory getFileSystem();

    /**
     * 执行映射
     *
     * @param hiveTables
     * @param timestamp
     */
    public void bindTables(Set<EntityName> hiveTables, String timestamp, ITaskContext context);

    public void deleteHistoryFile(EntityName dumpTable, ITaskContext taskContext);

    /**
     * 删除特定timestamp下的文件，前一次用户已经导入了文件，后一次想立即重新导入一遍
     *
     * @param
     * @throws Exception
     */
    public void deleteHistoryFile(EntityName dumpTable, ITaskContext taskContext, String timestamp);

    /**
     * 删除hive中的历史表
     */
    public void dropHistoryTable(EntityName dumpTable, ITaskContext taskContext);

    public String getJoinTableStorePath(INameWithPathGetter pathGetter);
    // public void dropHistoryHiveTable(DumpTable dumpTable, Connection conn, PartitionFilter filter, int maxPartitionSave);
}
