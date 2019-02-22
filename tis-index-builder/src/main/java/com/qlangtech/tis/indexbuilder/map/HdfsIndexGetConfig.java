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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.taobao.terminator.build.jobtask.TaskContext;
import com.taobao.terminator.build.task.TaskMapper;
import com.taobao.terminator.build.task.TaskReturn;
import com.qlangtech.tis.pubhook.common.ConfigConstant;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexGetConfig implements TaskMapper {

    public static final Logger logger = LoggerFactory.getLogger(HdfsIndexGetConfig.class);

    FileSystem fs;

    long startTime;

    // 由consel传入的taskid
    private String taskid = "";

    public HdfsIndexGetConfig() throws IOException {
        startTime = System.currentTimeMillis();
    // getAllFileName();
    // indexSchema = new IndexSchema(new
    // SolrResourceLoader("",IndexConf.class.getClassLoader() ,null),
    // indexConf.getSchemaName(), null);
    }

    @Override
    public TaskReturn map(TaskContext context) {
        IndexConf indexConf;
        indexConf = new IndexConf(false);
        indexConf.addResource("config.xml");
        try {
            // System.out.println("config.xml url:"
            // + this.getClass().getClassLoader()
            // .getResource("config.xml"));
            indexConf.loadFrom(context);
            // Configuration conf = new Configuration();
            // String fsName = indexConf.getFsName();
            String fsName = indexConf.getSourceFsName();
            logger.warn("remote hdfs host:" + fsName);
            // conf.set("fs.default.name", fsName);
            // FileSystem.get(conf);
            fs = TISHdfsUtils.getFileSystem();
            // createFileSystem(TSearcherConfigFetcher.get().getHdfsAddress());
            taskid = context.getUserParam("indexing.taskid");
            // 词典处理
            String serviceName = context.getUserParam("indexing.servicename");
            // DicManageClient dicManageClient=new DicManageClient(serviceName);
            // dicManageClient.checkRemoteDic();
            final String taskOutPath = context.getMapPath();
          //  String corepropPath = context.getUserParam("indexing." + ConfigConstant.FILE_CORE_PROPERTIES);
//            if (StringUtils.isNotBlank(corepropPath)) {
//                InputStream input = fs.open(new Path(corepropPath));
//                File coreprop = new File(new File(taskOutPath, "core"), ConfigConstant.FILE_CORE_PROPERTIES);
//                OutputStream output = FileUtils.openOutputStream(coreprop);
//                IOUtils.copy(input, output);
//                IOUtils.closeQuietly(input);
//                IOUtils.closeQuietly(output);
//                context.setUserParam("corepropfile", coreprop.getAbsolutePath());
//            }
            String schemaPath = context.getUserParam("indexing.schemapath");
            if (schemaPath == null) {
                logger.error("[taskid:" + taskid + "]" + "indexing.schemapath 参数没有配置");
                return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "indexing.schemapath 参数没有配置");
            }
            String configPath = context.getUserParam("indexing.configpath");
            try {
                Path srcPath = new Path(schemaPath);
                File dstP = new File(taskOutPath, "schema");
                if (dstP.exists()) {
                    dstP.delete();
                }
                dstP.mkdirs();
                Path dstPath = new Path(dstP.getAbsolutePath());
                fs.copyToLocalFile(srcPath, dstPath);
                logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " get schema done!");
                if (configPath != null) {
                    srcPath = new Path(configPath);
                    dstP = new File(taskOutPath, "config");
                    if (dstP.exists()) {
                        dstP.delete();
                    }
                    dstP.mkdirs();
                    dstPath = new Path(dstP.getAbsolutePath());
                    fs.copyToLocalFile(srcPath, dstPath);
                    logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " get config done!");
                    String normalizePath = configPath.replaceAll("\\\\", "/");
                    String configFile = dstP.getAbsolutePath() + File.separator + normalizePath.substring(normalizePath.lastIndexOf("/") + 1);
                    context.setUserParam("configFile", configFile);
                }
            } catch (IOException e) {
                // + e);
                return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "get schema error:" + ExceptionUtils.getStackTrace(e));
            }
            return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
        } catch (Throwable e) {
            // logger.error("[taskid:" + taskid + "]" + "get schema fail:", e);
            return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "get schema fail:" + ExceptionUtils.getStackTrace(e));
        }
    }

    // public static FileSystem createFileSystem(String hdfsHost) {
    // Configuration configuration = new Configuration();
    // FileSystem fileSys = null;
    // if (StringUtils.isEmpty(hdfsHost)) {
    // throw new IllegalStateException("hdfsHost can not be null");
    // }
    // try {
    // configuration.set("fs.default.name", hdfsHost);
    // // configuration.set("mapred.job.tracker",
    // // "10.232.36.131:9001");
    // // configuration.set("mapred.local.dir",
    // // "/home/yusen/hadoop/mapred/local");
    // // configuration.set("mapred.system.dir",
    // // "/home/yusen/hadoop/tmp/mapred/system");
    // // configuration.setInt("dump.split.size", 2);
    // //
    // configuration.addResource("core-site.xml");
    // configuration.addResource("mapred-site.xml");
    // 
    // fileSys = FileSystem.get(configuration);//
    // FileSystem.newInstance(configuration);
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // return fileSys;
    // }
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
