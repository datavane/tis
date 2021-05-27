package com.qlangtech.tis.sql.parser.stream.generate;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.sql.parser.tuple.creator.IStreamIncrGenerateStrategy;

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

        IAppSource appSource = IAppSource.load(null,collectionName);
        assertNotNull(collectionName + " appSource can not be null", appSource);

        FacadeContext fc = new FacadeContext();
        fc.setFacadeInstanceName("employeesDAOFacade");
        fc.setFullFacadeClassName("com.qlangtech.tis.realtime.employees.dao.IEmployeesDAOFacade");
        fc.setFacadeInterfaceName("IEmployeesDAOFacade");
        List<FacadeContext> facadeList = Lists.newArrayList();
        facadeList.add(fc);
        StreamComponentCodeGenerator streamCodeGenerator
                = new StreamComponentCodeGenerator(collectionName, timestamp, facadeList, (IStreamIncrGenerateStrategy) appSource);
        // EasyMock.replay(streamIncrGenerateStrategy);
        streamCodeGenerator.build();

        TestStreamComponentCodeGenerator.assertGenerateContentEqual(
                timestamp, collectionName, "S4testListener.scala");
        // EasyMock.verify(streamIncrGenerateStrategy);
    }

}
