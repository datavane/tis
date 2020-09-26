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
package com.qlangtech.tis.sql.parser.tuple.creator;

import com.qlangtech.tis.sql.parser.tuple.creator.impl.IScriptGenerateContext;
import com.qlangtech.tis.sql.parser.visitor.FunctionVisitor;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDataTupleCreator {

    public void accept(IDataTupleCreatorVisitor visitor);

    public EntityName getEntityName();

    public int refTableSourceCount();

    /**
     * 生成Groovy的執行腳本
     *
     * @return
     */
    public void generateGroovyScript(FunctionVisitor.FuncFormat rr, IScriptGenerateContext context, boolean processAggregationResult);
    // 退出當前棧之後執行回調處理
    // public void process(Object val);
    // public Object getVal(String name, IDataContext dataContext);
}
