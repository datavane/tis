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
package com.qlangtech.tis.indexbuilder.map;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.taobao.terminator.build.jobtask.TaskContext;
import com.taobao.terminator.build.task.TaskMapper;
import com.taobao.terminator.build.task.TaskReturn;
import com.qlangtech.tis.indexbuilder.source.SourceType;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexDeliver implements TaskMapper {

    public static final Logger logger = LoggerFactory.getLogger(HdfsIndexDeliver.class);

    IndexConf indexConf;

    FileSystem fs;

    long startTime;

    public HdfsIndexDeliver() throws IOException {
        startTime = System.currentTimeMillis();
        indexConf = new IndexConf(false);
        indexConf.addResource("config.xml");
    // getAllFileName();
    // indexSchema = new IndexSchema(new
    // SolrResourceLoader("",IndexConf.class.getClassLoader() ,null),
    // indexConf.getSchemaName(), null);
    }

    @Override
    public TaskReturn map(TaskContext context) {
        try {
            long start = System.currentTimeMillis();
            indexConf.loadFrom(context);
            // Configuration conf = new Configuration();
            // String fsName = indexConf.getFsName();
            // conf.set("fs.default.name", fsName);
            // FileSystem.get(conf);
            fs = TISHdfsUtils.getFileSystem();
            String taskOutPath = context.getMapPath();
            String destOutPath = context.getUserParam("indexing.outputpath");
            if (destOutPath == null) {
                return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "indexing.outputpath 参数没有配置");
            }
            Path destPath = new Path(destOutPath);
            // copy ok file
            if (indexConf.getSourceType().equals(SourceType.YUNTI)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                if (indexConf.get("indexing.okfileTimeZone") != null) {
                    format.setTimeZone(TimeZone.getTimeZone(indexConf.get("indexing.okfileTimeZone")));
                }
                Path localIncrPath = new Path(context.getMapPath() + File.separator + "okfile" + File.separator + indexConf.getOkFileName());
                Path remoteIncrPath = new Path(indexConf.getTargetOkFilePath() + File.separator + indexConf.getGroupNum() + File.separator + format.format(new Date()));
                if (fs.exists(remoteIncrPath)) {
                    fs.delete(remoteIncrPath, true);
                }
                fs.mkdirs(remoteIncrPath);
                fs.copyFromLocalFile(localIncrPath, remoteIncrPath);
            } else if (indexConf.getSourceType().equals(SourceType.YUNTI2)) {
                File file = new File(context.getMapPath() + File.separator + "okfile" + File.separator + indexConf.getOkFileName());
                if (!file.exists()) {
                    File parent = new File(context.getMapPath() + File.separator + "okfile");
                    parent.mkdirs();
                    file.createNewFile();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String date = sdf.format(new Date(System.currentTimeMillis()));
                PrintWriter pw = new PrintWriter(file);
                pw.write(date);
                pw.close();
                Path localIncrPath = new Path(context.getMapPath() + File.separator + "okfile" + File.separator + indexConf.getOkFileName());
                Path remoteIncrPath = new Path(indexConf.getTargetOkFilePath() + File.separator + indexConf.getGroupNum() + File.separator + new SimpleDateFormat("yyyyMMdd").format(new Date()));
                if (fs.exists(remoteIncrPath)) {
                    fs.delete(remoteIncrPath, true);
                }
                fs.mkdirs(remoteIncrPath);
                fs.copyFromLocalFile(localIncrPath, remoteIncrPath);
            } else if (SourceType.ODPS.equals(indexConf.getSourceType())) {
                File file = new File(context.getMapPath() + File.separator + "okfile" + File.separator + indexConf.getOkFileName());
                if (!file.exists()) {
                    File parent = new File(context.getMapPath() + File.separator + "okfile");
                    parent.mkdirs();
                    file.createNewFile();
                }
                FileUtils.writeStringToFile(file, indexConf.getIncrTime());
                Path localIncrPath = new Path(context.getMapPath() + File.separator + "okfile" + File.separator + indexConf.getOkFileName());
                Path remoteIncrPath = new Path(indexConf.getTargetOkFilePath() + File.separator + indexConf.getGroupNum() + File.separator + new SimpleDateFormat("yyyyMMdd").format(new Date()));
                if (fs.exists(remoteIncrPath)) {
                    fs.delete(remoteIncrPath, true);
                }
                fs.mkdirs(remoteIncrPath);
                fs.copyFromLocalFile(localIncrPath, remoteIncrPath);
            }
            logger.warn(indexConf.getCoreName() + " deliver done!take " + (System.currentTimeMillis() - start) / 1000 + " seconds");
            return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
        } catch (Throwable e) {
            logger.error("deliver error:" + e);
            e.printStackTrace();
            return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "deliver fail:" + e);
        }
    }

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        System.out.println(sdf.format(new Date(System.currentTimeMillis())));
    }
    /*
	 * @Override public void killTask() { // TODO Auto-generated method stub
	 * interruptFlag.notifyAll(); interruptFlag.flag = InterruptFlag.Flag.KILL;
	 * }
	 */
}
