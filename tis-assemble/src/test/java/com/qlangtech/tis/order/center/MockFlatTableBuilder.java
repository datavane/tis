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

import com.google.common.collect.Maps;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fs.IFs2Table;
import com.qlangtech.tis.fs.ITableBuildTask;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.fullbuild.phasestatus.IJoinTaskStatus;
import com.qlangtech.tis.fullbuild.taskflow.DataflowTask;
import com.qlangtech.tis.fullbuild.taskflow.ITemplateContext;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.sql.parser.ISqlTask;
import java.sql.Connection;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-21 11:35
 */
public class MockFlatTableBuilder extends FlatTableBuilder {

    private static final String name = "mock-flat-table-builder";

    @Override
    public void startTask(ITableBuildTask dumpTask) {
        final Connection conn = null;
        ITaskContext context = new ITaskContext() {

            @Override
            public Connection getObj() {
                return conn;
            }
        };
        try {
            dumpTask.process(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataflowTask createTask(ISqlTask nodeMeta, boolean isFinalNode, ITemplateContext tplContext, ITaskContext taskContext, IFs2Table fs2Table, IJoinTaskStatus joinTaskStatus) {
        return new DataflowTask(nodeMeta.getId()) {

            private Map<String, Boolean> status = Maps.newHashMap();

            @Override
            public void run() throws Exception {
                this.signTaskSuccess();
            }

            @Override
            public FullbuildPhase phase() {
                return FullbuildPhase.JOIN;
            }

            @Override
            public String getIdentityName() {
                return nodeMeta.getExportName();
            }

            @Override
            protected Map<String, Boolean> getTaskWorkStatus() {
                return status;
            }
        };
    }

    @Override
    public String identityValue() {
        return name;
    }

    //    @Override
//    public String getName() {
//        return name;
//    }
}
