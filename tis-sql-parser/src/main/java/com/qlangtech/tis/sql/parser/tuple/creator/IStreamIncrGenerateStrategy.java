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
package com.qlangtech.tis.sql.parser.tuple.creator;

import com.qlangtech.tis.compiler.streamcode.IDBTableNamesGetter;
import com.qlangtech.tis.sql.parser.DBNode;
import com.qlangtech.tis.sql.parser.er.IERRules;

import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 09:49
 */
public interface IStreamIncrGenerateStrategy {


    boolean isExcludeFacadeDAOSupport();

    Map<IEntityNameGetter, List<IValChain>> getTabTriggerLinker();

    /**
     * map<dbname,list<tables>>
     *
     * @return
     */
    Map<DBNode, List<String>> getDependencyTables(IDBTableNamesGetter dbTableNamesGetter);

    IERRules getERRule();


}
