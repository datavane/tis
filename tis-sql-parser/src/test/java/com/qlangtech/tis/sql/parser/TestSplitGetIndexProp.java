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

import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestSplitGetIndexProp extends TestCase {

    public void testGetIndexProp() throws Exception {
    // TableTupleCreator tuple = null;
    // try (InputStream read = this.getClass()
    // .getResourceAsStream(TestSplitGetIndexProp.class.getSimpleName() + ".txt")) {
    // 
    // SqlTaskNode taskNode = new SqlTaskNode(EntityName.parse("testExportName"), NodeType.JOINER_SQL);
    // taskNode.setContent(IOUtils.toString(read, "utf8"));
    // tuple = taskNode.parse();
    // }
    // 
    // ColName col = new ColName("customer_ids");
    // IDataTupleCreator colTuple = tuple.getColsRefs().colRefMap.get(col);
    // Assert.assertTrue(colTuple instanceof FunctionDataTupleCreator);
    // FunctionDataTupleCreator funcTuple = (FunctionDataTupleCreator) colTuple;
    // FunctionVisitor.FuncFormat funcFormat = new FunctionVisitor.FuncFormat();
    // 
    // IScriptGenerateContext context = null;
    // 
    // funcTuple.generateGroovyScript(funcFormat, context);
    // Assert.assertTrue(funcFormat.toString().length() > 1);
    // 
    // System.out.println(funcFormat.toString());
    // 
    // Assert.assertEquals(FunctionVisitor.SubscriptFunctionName
    // + "(split(row.getColumn(\"batch_msg\"),\"[\\\\w\\\\W]*\\\\|\"),1)", funcFormat.toString());
    }
}
