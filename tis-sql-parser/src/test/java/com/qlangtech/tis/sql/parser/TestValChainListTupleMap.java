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
package com.qlangtech.tis.sql.parser;

import com.google.common.base.Joiner;
import com.qlangtech.tis.sql.parser.er.TestERRules;
import com.qlangtech.tis.sql.parser.tuple.creator.IEntityNameGetter;
import com.qlangtech.tis.sql.parser.tuple.creator.IValChain;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TaskNodeTraversesCreatorVisitor;

import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestValChainListTupleMap extends SqlTaskBaseTestCase {

    private static final String totalpay_summary = "totalpay_summary";

    public void generateCode() throws Exception {
        TableTupleCreator totalpaySummaryTuple = this.parseSqlTaskNode(totalpay_summary);
        TaskNodeTraversesCreatorVisitor visitor = new TaskNodeTraversesCreatorVisitor(TestERRules.getTotalpayErRules());
        totalpaySummaryTuple.accept(visitor);
        Map<IEntityNameGetter, List<IValChain>> tabTriggers = visitor.getTabTriggerLinker();
        for (Map.Entry<IEntityNameGetter, List<IValChain>> e : tabTriggers.entrySet()) {
            System.out.println(e.getKey().getEntityName());
            if ("payinfo".equals(e.getKey().getEntityName().getTabName()) || "card".equals(e.getKey().getEntityName().getTabName())) {
                System.out.println("====================================================");
                System.out.println(e.getKey().getEntityName().getTabName());
                for (IValChain chain : e.getValue()) {
                    System.out.println(Joiner.on("->").join(chain.mapChainValve((r) -> {
                        return r.getIdentityName();
                    }).iterator()));
                }
            }
        }
    }
}
