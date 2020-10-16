package com.qlangtech.tis.sql.parser.stream.generate;

import com.google.common.collect.Lists;
import com.qlangtech.tis.sql.parser.BasicTestCase;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;

import java.util.List;

/**
 * @author: baisui 百岁
 * @create: 2020-10-14 16:34
 **/
public class TestS4EmployeeStreamComponentCodeGenerator extends BasicTestCase {

    public void testGeneratorCode() throws Exception {
        long timestamp = 20201111115959l;
        String collectionName = "search4test";
        String dfName = "current_dept_emp";
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(dfName);
        FacadeContext fc = new FacadeContext();
        fc.setFacadeInstanceName("employeesDAOFacade");
        fc.setFullFacadeClassName("com.qlangtech.tis.realtime.employees.dao.IEmployeesDAOFacade");
        fc.setFacadeInterfaceName("IEmployeesDAOFacade");
        List<FacadeContext> facadeList = Lists.newArrayList();
        facadeList.add(fc);
        StreamComponentCodeGenerator streamCodeGenerator
                = new StreamComponentCodeGenerator(collectionName, timestamp, facadeList, topology);
        streamCodeGenerator.build();

        TestStreamComponentCodeGenerator.assertGenerateContentEqual(
                timestamp, collectionName, "S4testListener.scala");
    }

}
