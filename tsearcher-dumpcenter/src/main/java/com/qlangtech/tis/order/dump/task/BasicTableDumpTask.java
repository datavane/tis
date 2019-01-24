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
package com.qlangtech.tis.order.dump.task;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taobao.terminator.build.jobtask.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTableDumpTask extends AbstractTableDumpTask {

    private static final Logger logger = LoggerFactory.getLogger(BasicTableDumpTask.class);

    public BasicTableDumpTask() {
        super();
    }

    protected Reader getDataSourceConfigStream() {
        try {
            return new InputStreamReader(this.getClass().getResourceAsStream("config.properties"), "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Collection<HdfsRealTimeTerminatorBean> getDumpBeans(TaskContext context) {
        // 启动dump任务
        Map<String, HdfsRealTimeTerminatorBean> /* 索引名称 */
        dumpBeans = springContext.getBeansOfType(HdfsRealTimeTerminatorBean.class);
        Collection<HdfsRealTimeTerminatorBean> dumpbeans = dumpBeans.values();
        return dumpbeans;
    }
}
