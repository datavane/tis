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

import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月18日
 */
public interface IScriptGenerateContext {

    boolean isLastFunctInChain();

    boolean isGroupByFunction();

    /**
     * 离开源表更近的tuple是聚合的计算类型？
     * @return
     */
    boolean isNextGroupByFunction();

    /**
     * 是否汇聚两个tule的计算单元
     *
     * @return
     */
    boolean isJoinPoint();

    public EntityName getEntityName();

    public ColName getOutputColName();

    public FunctionDataTupleCreator getFunctionDataTuple();
    public IDataTupleCreator getTupleCreator();

    public boolean isNotDeriveFrom(EntityName entityName);
}
