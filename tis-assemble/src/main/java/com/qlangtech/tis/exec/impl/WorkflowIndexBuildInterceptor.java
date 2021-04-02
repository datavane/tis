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
package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.indexbuild.IndexBuildSourcePathCreator;
import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkflowIndexBuildInterceptor extends IndexBuildInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowIndexBuildInterceptor.class);

    public WorkflowIndexBuildInterceptor() {
    }

    @Override
    protected IndexBuildSourcePathCreator createIndexBuildSourceCreator(IExecChainContext execContext, ITabPartition ps) {

        return execContext.getIndexBuilderFactory().createIndexBuildSourcePathCreator(execContext, ps);

//        // 需要构建倒排索引的表名称
//        EntityName targetTableName = execContext.getAttribute(IExecChainContext.KEY_BUILD_TARGET_TABLE_NAME);
//        String fsPath = FSHistoryFileUtils.getJoinTableStorePath(execContext.getIndexBuildFileSystem().getRootDir(), targetTableName)  // execContext.getTableDumpFactory().getJoinTableStorePath(targetTableName)
//                + "/" + IDumpTable.PARTITION_PT + "=%s/" + IDumpTable.PARTITION_PMOD + "=%s";
//        logger.info("hdfs sourcepath:" + fsPath);
//        return (ctx, group, partition) -> String.format(fsPath, ps.getPt(), group);
    }

    @Override
    protected void setBuildTableTitleItems(String indexName, ImportDataProcessInfo processinfo, IExecChainContext execContext) {
        try {
            SqlTaskNodeMeta.SqlDataFlowTopology topology = execContext.getTopology();
            List<ColName> finalNode = topology.getFinalTaskNodeCols();
            processinfo.setBuildTableTitleItems(
                    finalNode.stream().map((k) -> k.getAliasName()).collect(Collectors.joining(",")));
        } catch (Exception e) {
            throw new RuntimeException("workflow:" + execContext.getWorkflowName(), e);
        }
    }
}
