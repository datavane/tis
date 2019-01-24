/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis;

import java.util.List;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.qlangtech.tis.trigger.biz.dal.dao.ITerminatorTriggerBizDalDAOFacade;
import com.qlangtech.tis.trigger.biz.dal.pojo.Task;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskTest extends TestCase {

    private static final ApplicationContext context;

    static {
        context = new ClassPathXmlApplicationContext("/conf/terminatorTriggerBizDal-dao-context.xml", "/jobtrigger-datasource-context.xml");
    }

    public void testTaskSelect() {
        ITerminatorTriggerBizDalDAOFacade daoFacade = context.getBean("terminatorTriggerBizDalDaoFacade", ITerminatorTriggerBizDalDAOFacade.class);
        TaskCriteria criteria = new TaskCriteria();
        criteria.createCriteria().andJobIdEqualTo(23l);
        List<Task> task = daoFacade.getTaskDAO().selectByExample(criteria, 1, 20);
        for (Task t : task) {
            System.out.println(t.getFromIp());
        }
    }
}
