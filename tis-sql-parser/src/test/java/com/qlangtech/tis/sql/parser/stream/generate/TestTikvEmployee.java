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
package com.qlangtech.tis.sql.parser.stream.generate;

import com.google.common.collect.Lists;
import com.qlangtech.tis.sql.parser.BasicTestCase;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.tuple.creator.IStreamIncrGenerateStrategy;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestTikvEmployee extends BasicTestCase {

    public void testGeneratorCode() throws Exception {

        long timestamp = 20201111115959l;
        String collectionName = "search4employees";
        IStreamIncrGenerateStrategy streamIncrGenerateStrategy = EasyMock.createMock("streamIncrGenerateStrategy", IStreamIncrGenerateStrategy.class);
        String dfName = "tikv-employee";
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(dfName);
        FacadeContext fc = new FacadeContext();
        fc.setFacadeInstanceName("employeesDAOFacade");
        fc.setFullFacadeClassName("com.qlangtech.tis.realtime.employees.dao.IEmployeesDAOFacade");
        fc.setFacadeInterfaceName("IEmployeesDAOFacade");
        List<FacadeContext> facadeList = Lists.newArrayList();
        facadeList.add(fc);
        StreamComponentCodeGenerator streamCodeGenerator
                = new StreamComponentCodeGenerator(collectionName, timestamp, facadeList, streamIncrGenerateStrategy);
        EasyMock.replay(streamIncrGenerateStrategy);
        streamCodeGenerator.build();

        TestStreamComponentCodeGenerator.assertGenerateContentEqual(
                timestamp, collectionName, "S4employeesListener.scala");
        EasyMock.verify(streamIncrGenerateStrategy);
    }

}
