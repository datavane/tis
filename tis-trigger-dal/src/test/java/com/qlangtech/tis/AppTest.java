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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.qlangtech.tis.trigger.biz.dal.dao.IJobMetaDataDAO;
import com.qlangtech.tis.trigger.biz.dal.dao.ITriggerBizDalDAOFacade;
import com.qlangtech.tis.trigger.biz.dal.pojo.Task;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

/*
 * Unit test for simple App.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppTest extends TestCase {

    private static final ApplicationContext context;

    static {
        context = new ClassPathXmlApplicationContext("/conf/terminatorTriggerBizDal-dao-context.xml", "/conf/terminator-job-trigger-relation-context.xml", "/test-context.xml", "/jobtrigger-datasource-context.xml");
    }

    private static final SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");

    public void testTaskDAO() throws Exception {
        // System.out.println(IOUtils.toString(this.getClass()
        // .getResourceAsStream("/log4j.properties")));
        ITriggerBizDalDAOFacade facade = context.getBean("terminatorTriggerBizDalDaoFacade", ITriggerBizDalDAOFacade.class);
        Assert.assertNotNull(facade);
        TaskCriteria criteria = new TaskCriteria();
        criteria.createCriteria().andJobIdEqualTo((long) 15).andDomainEqualTo("terminator").andGmtCreateInSameDay(new Date());
        List<Task> tskList = facade.getTaskDAO().selectByExample(criteria);
        Assert.assertTrue(tskList.size() > 0);
        for (Task task : tskList) {
            System.out.println(task.getTaskId());
        }
    }

    public void testJobMetaDataDAO() {
        IJobMetaDataDAO jobMetaDataDAO = context.getBean("jobMetaDataDAO", IJobMetaDataDAO.class);
        Assert.assertNotNull(jobMetaDataDAO.queryJob("search4realwidget", 1));
    }
}
