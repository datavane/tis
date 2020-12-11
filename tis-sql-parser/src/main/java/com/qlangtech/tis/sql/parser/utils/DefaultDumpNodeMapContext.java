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
package com.qlangtech.tis.sql.parser.utils;

import com.qlangtech.tis.sql.parser.IDumpNodeMapContext;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import org.apache.commons.lang.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DefaultDumpNodeMapContext implements IDumpNodeMapContext {

    private final Map<EntityName, List<TableTupleCreator>> dumpNodsContext;

    private List<SqlTaskNode> allJoinNodes;

    public DefaultDumpNodeMapContext(Map<EntityName, List<TableTupleCreator>> dumpNodsContext) {
        this.dumpNodsContext = dumpNodsContext;
    }

    public void setAllJoinNodes(List<SqlTaskNode> allJoinNodes) {
        this.allJoinNodes = allJoinNodes;
    }

    @Override
    public List<SqlTaskNode> getAllJoinNodes() {
        return this.allJoinNodes;
    }

    public SqlTaskNode geTaskNode(final EntityName entityName) throws Exception {
        if (entityName == null) {
            throw new IllegalArgumentException("param entityName can not be null");
        }
        List<SqlTaskNode> allNodes = this.getAllJoinNodes();
        if (allNodes == null) {
            throw new IllegalStateException("entityName:" + entityName + " relevant join node can not be null");
        }
        Optional<SqlTaskNode> node = allNodes.stream().filter((r) ->
                org.apache.commons.lang.StringUtils.equals(r.getExportName().getTabName(), entityName.getTabName())).findFirst();
        if (!node.isPresent()) {
            throw new IllegalStateException("nodename:" + entityName.getTabName() + " can not be find ,all:["
                    + allNodes.stream().map((e) -> e.getExportName().getTabName()).collect(Collectors.joining(",")) + "] ");
        }
        return node.get();
    }

    @Override
    public Map<EntityName, List<TableTupleCreator>> getDumpNodesMap() {
        return this.dumpNodsContext;
    }

    @Override
    public EntityName accurateMatch(String tabname) {
        // 
        List<EntityName> names = this.dumpNodsContext.keySet().stream().filter((e) -> StringUtils.equals(e.getTabName(), tabname)).collect(Collectors.toList());
        if (names.size() != 1) {
            throw new IllegalStateException("table:" + tabname + " relevant tab not equal with 1 ,size:" + names.size());
        }
        return names.get(0);
    }

    @Override
    public EntityName nullableMatch(String tabname) {
        // 
        List<EntityName> names = this.dumpNodsContext.keySet().stream().filter((e) -> StringUtils.equals(e.getTabName(), tabname)).collect(Collectors.toList());
        if (names.size() < 1) {
            return null;
        }
        if (names.size() != 1) {
            throw new IllegalStateException("table:" + tabname + " relevant tab not equal with 1 ,size:" + names.size());
        }
        return names.get(0);
    }
}
