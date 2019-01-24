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
package com.qlangtech.tis.full.dump;

import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Assert;
import junit.framework.TestCase;
import com.qlangtech.tis.fullbuild.taskflow.ITask;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser;
import com.qlangtech.tis.fullbuild.taskflow.TaskConfigParser.ProcessTask;
import com.qlangtech.tis.fullbuild.taskflow.hive.HiveTaskFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTaskConfigParser extends TestCase {

    public void test() throws Exception {
        // HiveTaskFactory hiveTaskFactory = new HiveTaskFactory();
        // (hiveTaskFactory);
        TaskConfigParser parser = TaskConfigParser.getInstance();
        final AtomicInteger taskCount = new AtomicInteger();
        parser.traverseTask("search4totalpay", new ProcessTask() {

            public void process(ITask task) {
                System.out.println(task.getName());
                taskCount.incrementAndGet();
            }
        });
        Assert.assertEquals(20, taskCount.get());
    }
}
