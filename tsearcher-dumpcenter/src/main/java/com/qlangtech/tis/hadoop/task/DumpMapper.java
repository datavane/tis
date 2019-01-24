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
package com.qlangtech.tis.hadoop.task;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import com.qlangtech.tis.order.dump.task.CommonTableDumpTask;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DumpMapper extends Mapper<Object, Text, String, IntWritable> {

    private static final Log log = LogFactory.getLog(DumpMapper.class);

    @Override
    public void run(Context context) throws IOException, InterruptedException {
        final String tisRepository = context.getConfiguration().get("tis-repository");
        if (tisRepository == null) {
            throw new IllegalArgumentException("tisRepository can not be null");
        }
        log.info("tis-repository:" + tisRepository);
        System.setProperty("globalConfigRepositoryHost", tisRepository);
        Thread.sleep(30000);
        final long start = System.currentTimeMillis();
        log.warn("CommonTableDumpTask start work");
        CommonTableDumpTask.main(null);
        log.warn("CommonTableDumpTask end," + (System.currentTimeMillis() - start));
    }

    protected void map(Object splitConditions, org.apache.hadoop.io.Text val, // NullWritable
    Context context) throws IOException, InterruptedException {
    // System.out
    // .println("splitConditions:" + splitConditions + ",val:" + val);
    // int i = 0;
    // while (true) {
    // System.out.print("hello," + (i++) + ","
    // + Inet4Address.getLocalHost().getHostAddress());
    // Thread.sleep(3000);
    // }
    }
}
