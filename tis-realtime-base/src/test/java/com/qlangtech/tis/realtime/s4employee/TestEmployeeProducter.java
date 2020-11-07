package com.qlangtech.tis.realtime.s4employee;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.realtime.MQProducerUtils;
import com.qlangtech.tis.realtime.test.employees.dao.IEmployeesDAOFacade;
import com.qlangtech.tis.realtime.test.employees.pojo.DeptEmp;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 模拟向mq中发送消息，因为是模拟需要同时向数据库中插入记录
 *
 * @author: baisui 百岁
 * @create: 2020-11-02 10:11
 **/
public class TestEmployeeProducter extends BasicEmployeeTestCase {

    private IEmployeesDAOFacade employeesDAOFacade;
    int empNo = 9536;

    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public TestEmployeeProducter() {
        //boolean shallRegisterMQ, String collectionName, long wfTimestamp, String... configLocations
        super(false);
        this.employeesDAOFacade = TestS4employee.getEmployeesDAOFacade(this.appContext);
    }


    public void testProducter() throws Exception {

        assertNotNull(employeesDAOFacade);

        DeptEmp deptEmp1 = new DeptEmp();
        deptEmp1.setDeptNo("d999");
        deptEmp1.setEmpNo(empNo);
        deptEmp1.setFromDate(dateFormat.get().parse("1991-04-28"));
        deptEmp1.setToDate(dateFormat.get().parse("1991-05-09"));

        DeptEmp deptEmp2 = new DeptEmp();
        deptEmp2.setDeptNo("d888");
        deptEmp2.setEmpNo(empNo);
        deptEmp2.setFromDate(dateFormat.get().parse("1991-04-29"));
        deptEmp2.setToDate(dateFormat.get().parse("1991-06-27"));

        TestS4employee.DeptEmpPojoCUD deptEmpCUD = new TestS4employee.DeptEmpPojoCUD(employeesDAOFacade);

        deptEmpCUD.initSyncWithDB(deptEmp1);
        deptEmpCUD.initSyncWithDB(deptEmp2);

        DefaultMQProducer producter = MQProducerUtils.createProducter();

        producter.send(MQProducerUtils.createMsg(createInsertMQMessage(deptEmp1).toJSONString(), deptEmpCUD.getTableName()));

        producter.send(MQProducerUtils.createMsg(createInsertMQMessage(deptEmp2).toJSONString(), deptEmpCUD.getTableName()));

        System.out.println("has send");
    }


    private JSONObject createInsertMQMessage(DeptEmp deptEmp) {

        JSONObject msg = new JSONObject();
        JSONObject after = new JSONObject();
        after.put("emp_no", deptEmp.getEmpNo());
        after.put("dept_no", deptEmp.getDeptNo());
        after.put("from_date", dateFormat.get().format(deptEmp.getFromDate()));
        after.put("to_date", dateFormat.get().format(deptEmp.getToDate()));
        msg.put("before", new JSONObject());
        msg.put("after", after);
        msg.put("dbName", "employee");
        msg.put("eventType", "INSERT");
        msg.put("orginTableName", "dept_emp");
        msg.put("targetTable", "test");
        return msg;
    }
}
