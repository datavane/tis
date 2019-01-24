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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DfireTool extends Configured implements Tool {

    private static final Log log = LogFactory.getLog(DfireTool.class);

    @Override
    public int run(String[] args) throws Exception {
        JobConf conf = new JobConf(getConf());
        conf.setMaxMapAttempts(1);
        conf.setJobPriority(JobPriority.VERY_HIGH);
        log.info("tis-repository:" + args[0]);
        conf.setIfUnset("tis-repository", args[0]);
        conf.set("jobtracker.rpcserver", "hdfs://10.1.7.25:9000");
        conf.set("jobtracker.transserver", "hdfs://10.1.7.25:9000");
        System.out.println("start execute");
        FileOutputFormat.setOutputPath(conf, new Path("/user/admin/xxx" + (int) (Math.random() * 1000)));
        Job job = new Job(conf);
        String source = "/user/admin/yarn-proto.jar";
        FileInputFormat.setInputPaths(job, new Path(source));
        job.setMapperClass(DumpMapper.class);
        // job.setReducerClass(cls);
        job.waitForCompletion(true);
        // JobClient.runJob(conf);
        System.out.println("complete");
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new DfireTool(), args);
        System.exit(res);
    }
}
