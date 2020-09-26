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

import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreatorVisitor;
import com.qlangtech.tis.sql.parser.visitor.FunctionVisitor;

/**
 * 表数据发生器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年5月31日
 */
public class TableTupleCreator implements IDataTupleCreator {

    private final String mediaTabRef;

    private NodeType nodetype;

    private ColRef colsRefs = null;

    private EntityName realEntityName;

    public TableTupleCreator(String mediaTabRef, NodeType nodetype) {
        super();
        this.mediaTabRef = mediaTabRef;
        this.nodetype = nodetype;
    }

    @Override
    public int refTableSourceCount() {
        return 1;
    }

    @Override
    public void generateGroovyScript(FunctionVisitor.FuncFormat rr, IScriptGenerateContext context, boolean processAggregationResult) {
        rr.append("tab:" + this.realEntityName);
    }

    @Override
    public void accept(IDataTupleCreatorVisitor visitor) {
        visitor.visit(this);
    }

    public void setNodetype(NodeType nodetype) {
        this.nodetype = nodetype;
    }

    @Override
    public EntityName getEntityName() {
        return this.realEntityName;
    }

    public String getMediaTabRef() {
        return this.mediaTabRef;
    }

    public void setRealEntityName(EntityName realEntityName) {
        this.realEntityName = realEntityName;
    }

    public ColRef getColsRefs() {
        return this.colsRefs;
    }

    public void setColsRefs(ColRef colsRefs) {
        this.colsRefs = colsRefs;
    }

    public NodeType getNodetype() {
        return this.nodetype;
    }

    @Override
    public String toString() {
        if (this.realEntityName == null) {
            return "ref:" + mediaTabRef;
        } else {
            return "ref:" + mediaTabRef + ",entity:" + this.realEntityName;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableTupleCreator)) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        if (this.realEntityName == null) {
            throw new IllegalStateException("'realEntityName' can not be null");
        }
        return this.realEntityName.hashCode();
    }
}
