package com.qlangtech.tis.sql.parser.stream.generate;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author: baisui 百岁
 * @create: 2020-10-14 16:34
 **/
public class TestS4EmployeeStreamComponentCodeGenerator extends TestCase {

    static{
        CenterResource.setNotFetchFromCenterRepository();
    }

    public void testGeneratorCode() throws Exception {
        long timestamp = 20191111115959l;
        String collectionName = "search4test";
        String dfName = "current_dept_emp";
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(dfName);
        FacadeContext fc = new FacadeContext();
        fc.setFacadeInstanceName("order2DAOFacade");
        fc.setFullFacadeClassName("com.qlangtech.tis.realtime.order.dao.IOrder2DAOFacade");
        fc.setFacadeInterfaceName("IOrder2DAOFacade");
        List<FacadeContext> facadeList = Lists.newArrayList();
        facadeList.add(fc);
        StreamComponentCodeGenerator streamCodeGenerator = new StreamComponentCodeGenerator("collectionName", timestamp, facadeList, topology);
        streamCodeGenerator.build();

        //assertGenerateContentEqual(timestamp, collectionName, "S4totalpayListener.scala");
    }

}
