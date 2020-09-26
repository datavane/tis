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
package com.qlangtech.tis.sql.parser.stream.generate;

import com.qlangtech.tis.sql.parser.BasicTestCase;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.er.TestERRules;
import com.google.common.collect.Lists;
import junit.framework.TestCase;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestStreamComponentCodeGenerator extends BasicTestCase {

    public void testGeneratorCode() throws Exception {
        long timestamp = 20191111115959l;
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology("totalpay");
        FacadeContext fc = new FacadeContext();
        fc.setFacadeInstanceName("order2DAOFacade");
        fc.setFullFacadeClassName("com.qlangtech.tis.realtime.order.dao.IOrder2DAOFacade");
        fc.setFacadeInterfaceName("IOrder2DAOFacade");
        List<FacadeContext> facadeList = Lists.newArrayList();
        facadeList.add(fc);
        StreamComponentCodeGenerator streamCodeGenerator = new StreamComponentCodeGenerator("search4totalpay", timestamp, facadeList, topology);
        streamCodeGenerator.build();
    }
}
