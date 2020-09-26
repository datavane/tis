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
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.build.NodeMaster;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.fullbuild.indexbuild.impl.JobConfParams;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import junit.framework.TestCase;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-23 14:00
 */
public class TestTableDumpNodeMaster extends TestCase {

    static {
    // PluginStore<TableDumpFactory> tableDumpFactoryStore = TIS.getPluginStore(TableDumpFactory.class);
    // Thread.currentThread().setContextClassLoader(tableDumpFactoryStore.getPlugin().getClass().getClassLoader());
    }

    public void testDump() throws Exception {
        EntityName dumpTable = EntityName.parse("order.totalpayinfo");
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        TaskContext taskContext = TaskContext.create();
        JobConfParams tabDumpParams = JobConfParams.createTabDumpParams(taskContext, dumpTable, f.format(new Date()), "yarn_tab_dump");
        NodeMaster.main(tabDumpParams.paramsArray());
    }
}
