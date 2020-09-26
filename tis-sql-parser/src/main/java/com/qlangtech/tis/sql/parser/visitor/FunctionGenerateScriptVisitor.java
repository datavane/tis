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
package com.qlangtech.tis.sql.parser.visitor;

import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.IScriptGenerateContext;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.ColRef;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月21日
 */
public class FunctionGenerateScriptVisitor extends FunctionVisitor {

    public FunctionGenerateScriptVisitor(ColRef colRef, Map<ColName, IDataTupleCreator> params, FuncFormat funcFormat, IScriptGenerateContext generateContext, boolean processAggregationResult) {
        super(colRef, new UnWriteableMap(params), funcFormat, generateContext, processAggregationResult);
    }

    @Override
    protected void processJoinPointPram(ColName param) {
        IDataTupleCreator tuple = this.functionParams.get(param);
        if (this.generateContext.isNotDeriveFrom(tuple.getEntityName())) {
        // System.out.println("notderivefrom:" + param);
        }
    }

    private static class UnWriteableMap extends HashMap<ColName, IDataTupleCreator> {

        private static final long serialVersionUID = 1L;

        public UnWriteableMap(Map<? extends ColName, ? extends IDataTupleCreator> m) {
            super(m);
        }

        @Override
        public IDataTupleCreator put(ColName key, IDataTupleCreator value) {
            return null;
        }

        @Override
        public void putAll(Map<? extends ColName, ? extends IDataTupleCreator> m) {
        }

        @Override
        public IDataTupleCreator putIfAbsent(ColName key, IDataTupleCreator value) {
            return null;
        }
    }
}
