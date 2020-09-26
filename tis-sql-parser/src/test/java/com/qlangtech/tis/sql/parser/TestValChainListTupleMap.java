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

import java.util.List;
import java.util.Map;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.er.TestERRules;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TaskNodeTraversesCreatorVisitor;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.ValChain;
import com.google.common.base.Joiner;

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
        Map<TableTupleCreator, List<ValChain>> tabTriggers = visitor.getTabTriggerLinker();
        for (Map.Entry<TableTupleCreator, List<ValChain>> e : tabTriggers.entrySet()) {
            System.out.println(e.getKey().getEntityName());
            if ("payinfo".equals(e.getKey().getEntityName().getTabName()) || "card".equals(e.getKey().getEntityName().getTabName())) {
                System.out.println("====================================================");
                System.out.println(e.getKey().getEntityName().getTabName());
                for (ValChain chain : e.getValue()) {
                    System.out.println(Joiner.on("->").join(chain.mapChainValve((r) -> {
                        return r.getIdentityName();
                    }).iterator()));
                }
            }
        }
    }
}
