package com.qlangtech.tis.realtime.s4employee;

/**
 * @author: baisui 百岁
 * @create: 2020-11-02 11:43
 **/
public class TestS4EmployeeWithRealMQ extends BasicEmployeeTestCase {
    public TestS4EmployeeWithRealMQ() {
        super(true);
    }

    public void testReceiveMQ() throws Exception {
        synchronized (this) {
            this.wait();
        }
    }
}
