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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.hdfs.client.bean.BasicTerminatorClient;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;
import com.qlangtech.tis.hdfs.client.bean.MockWorkflowFeedback;
import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestOrderinfoDump extends TestCase {

    public static final ClassPathXmlApplicationContext context;

    static {
        context = new ClassPathXmlApplicationContext("/search4dfireorderinfo.xml") {

            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
                // OrderCenterDataSourceRegister.setApplicationContext(factory);
                super.prepareBeanFactory(beanFactory);
            }
        };
        System.out.println("order30:" + context.getBean("order30"));
        System.out.println("static initialize ");
    }

    @SuppressWarnings("all")
    public void testImport() throws Exception {
        // 
        Map<String, HdfsRealTimeTerminatorBean> beans = context.getBeansOfType(HdfsRealTimeTerminatorBean.class);
        final Date startTime = new Date();
        final IWorkflowFeedback workflowFeedback = new MockWorkflowFeedback();
        ExecutorCompletionService<DumpResult> executeService = new ExecutorCompletionService(BasicTerminatorClient.threadPool);
        int dumpJobCount = 0;
        for (final HdfsRealTimeTerminatorBean dumpBean : beans.values()) {
            executeService.submit(new Callable<DumpResult>() {

                @Override
                public DumpResult call() throws Exception {
                    // return dumpResult;
                    return null;
                }
            });
            dumpJobCount++;
        }
        DumpResult dumpResult = null;
        for (int i = 0; i < dumpJobCount; i++) {
            dumpResult = executeService.take().get();
            System.out.println("dump job:" + dumpResult.serviceConfig.getServiceName() + " complete!!!");
        }
        System.out.println("all over ");
    }

    private class DumpResult {

        private IServiceConfig serviceConfig;
    }
}
