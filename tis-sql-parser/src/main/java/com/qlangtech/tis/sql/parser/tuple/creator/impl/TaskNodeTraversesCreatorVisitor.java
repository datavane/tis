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
package com.qlangtech.tis.sql.parser.tuple.creator.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreatorVisitor;
import org.apache.commons.lang.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年5月15日
 */
public class TaskNodeTraversesCreatorVisitor implements IDataTupleCreatorVisitor {

    public final Stack<PropGetter> propStack = new Stack<>();

    private final Map<TableTupleCreator, List<ValChain>> tabTriggerLinker = Maps.newHashMap();

    private final ERRules erRules;

    public TaskNodeTraversesCreatorVisitor(ERRules erRules) {
        this.erRules = erRules;
    }

    @Override
    public void visit(FunctionDataTupleCreator function) {
        PropGetter peek = getPeek();
        if (peek == null) {
            throw new IllegalStateException("peek can not be null");
        }
        function.getParams().entrySet().stream().forEach((r) -> /**
         * Map<ColName, IDataTupleCreator>
         */
        {
            // function
            this.pushPropGetter(// function
            new ColName(r.getKey().getName(), peek.getOutputColName().getAliasName()), // function
            peek.getTupleCreator().getEntityName(), r.getValue());
            try {
                r.getValue().accept(TaskNodeTraversesCreatorVisitor.this);
            } finally {
                propStack.pop();
            }
        });
    }

    @Override
    public void visit(TableTupleCreator tableTuple) {
        if (tableTuple.getNodetype() == NodeType.DUMP) {
            ValChain propGetters = new ValChain();
            PropGetter prop = null;
            for (int i = 0; i < propStack.size(); i++) {
                prop = propStack.get(i);
                propGetters.add(prop);
            }
            if (prop != null) {
                propGetters.add(new PropGetter(new ColName(prop.getOutputColName().getName()), tableTuple.getEntityName(), null));
            }
            List<ValChain> propsGetters = tabTriggerLinker.get(tableTuple);
            if (propsGetters == null) {
                propsGetters = Lists.newArrayList();
                tabTriggerLinker.put(tableTuple, propsGetters);
            }
            propsGetters.add(propGetters);
            return;
        } else if (tableTuple.getNodetype() == NodeType.JOINER_SQL) {
            PropGetter peek = getPeek();
            for (Map.Entry<ColName, IDataTupleCreator> /* colName */
            centry : tableTuple.getColsRefs().getColRefMap().entrySet()) {
                if (peek == null || StringUtils.equals(peek.getOutputColName().getName(), centry.getKey().getAliasName())) {
                    this.pushPropGetter(centry.getKey(), tableTuple.getEntityName(), centry.getValue());
                    if (centry.getValue() == null) {
                        throw new IllegalStateException("centry.getKey():" + centry.getKey() + " relevant value IDataTupleCreator can not be null");
                    }
                    try {
                        centry.getValue().accept(this);
                    } finally {
                        this.propStack.pop();
                    }
                    if (peek != null) {
                        break;
                    }
                }
            }
        } else {
            throw new IllegalStateException("tableTuple.getNodetype():" + tableTuple.getNodetype() + " is illegal");
        }
    }

    public PropGetter getPeek() {
        PropGetter peek = null;
        if (!propStack.isEmpty()) {
            peek = propStack.peek();
        }
        // }
        return peek;
    }

    public int getPropGetterStackSize() {
        return this.propStack.size();
    }

    public void clearPropStack() {
        this.propStack.clear();
    }

    public void pushPropGetter(ColName output, EntityName entityName, IDataTupleCreator tupleCreator) {
        this.propStack.push(new PropGetter(output, entityName, tupleCreator));
    }

    public Map<TableTupleCreator, List<ValChain>> getTabTriggerLinker() {
        return this.tabTriggerLinker;
    }
}
