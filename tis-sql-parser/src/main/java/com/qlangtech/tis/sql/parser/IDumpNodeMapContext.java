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
package com.qlangtech.tis.sql.parser;

import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDumpNodeMapContext {

    public Map<EntityName, List<TableTupleCreator>> getDumpNodesMap();

    /**
     * @param tabname
     * @return
     */
    public EntityName accurateMatch(String tabname);

    /**
     * 没有匹配成功 可返回空
     *
     * @param tabname
     * @return
     */
    public EntityName nullableMatch(String tabname);

    public List<SqlTaskNode> getAllJoinNodes();

    public SqlTaskNode geTaskNode(final EntityName entityName) throws Exception;
}
