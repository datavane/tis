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
import com.qlangtech.tis.sql.parser.*;
import com.qlangtech.tis.sql.parser.er.TestERRules;
import com.qlangtech.tis.sql.parser.shop.TestShopTopologyParse;
import com.qlangtech.tis.sql.parser.stream.generate.TestFlatTableRelation;
import com.qlangtech.tis.sql.parser.stream.generate.TestStreamComponentCodeGenerator;
import com.qlangtech.tis.sql.parser.supplyGoods.TestSupplyGoodsParse;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestAll extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestShopTopologyParse.class);
        suite.addTestSuite(TestSupplyGoodsParse.class);
        suite.addTestSuite(TestFlatTableRelation.class);
        suite.addTestSuite(TestSqlDataFlowTopology.class);
        suite.addTestSuite(TestSqlRewriter.class);
        suite.addTestSuite(TestSqlTaskNodeMeta.class);
        suite.addTestSuite(TestSqlTaskNode.class);
        suite.addTestSuite(TestStreamComponentCodeGenerator.class);
        // suite.addTestSuite(TestMqConfigMeta.class);
        suite.addTestSuite(TestDBNode.class);
        suite.addTestSuite(TestERRules.class);
        return suite;
    }
}
