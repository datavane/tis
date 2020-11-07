package com.qlangtech.tis.realtime.s4employee;

import com.google.common.collect.Sets;
import com.qlangtech.tis.realtime.BasicIncrTestCase;
import com.qlangtech.tis.spring.LauncherResourceUtils;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 * @author: baisui 百岁
 * @create: 2020-11-02 11:45
 **/
public class BasicEmployeeTestCase extends BasicIncrTestCase {

    static final String collectionName = "search4test2";
    static final long wfTimestamp = 20201028174545l;
    static final String employees_test_dao_context = "/conf/employees-test-dao-context.xml";

    private static final Set<String> includeSpringContext;


    static {
        includeSpringContext = Sets.newHashSet();
        includeSpringContext.add("employees-dao-context.xml");
        LauncherResourceUtils.resourceFilter = (res) -> {
            return includeSpringContext.contains(res.getFilename());
        };
    }

    public BasicEmployeeTestCase(boolean shallRegisterMQ) {
        super(shallRegisterMQ, collectionName, wfTimestamp, employees_test_dao_context);
    }


    public static ApplicationContext createTestSpringContext() {
        //String collectionName, long wfTimestamp, String[] configLocations
        return BasicIncrTestCase.createTestSpringContext(collectionName, wfTimestamp, new String[]{employees_test_dao_context});
    }


}
