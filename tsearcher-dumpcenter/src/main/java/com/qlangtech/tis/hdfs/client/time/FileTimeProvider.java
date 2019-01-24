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
package com.qlangtech.tis.hdfs.client.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.qlangtech.tis.common.TerminatorCommonUtils;
import com.qlangtech.tis.exception.TimeManageException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.util.Constants;

/*
 * @description
 * @since 2011-8-11 04:30:18
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileTimeProvider implements TimeProvider {

    protected Log logger = LogFactory.getLog(FileTimeProvider.class);

    protected String path;

    protected Path hdfsPath;

    protected DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    protected Date startTime;

    protected Date endTime;

    protected String serviceName;

    protected TSearcherQueryContext context;

    protected Object object;

    FileSystem fileSystem;

    // public Date getStartTime() {
    // return startTime;
    // }
    // 
    // public void setStartTime(Date startTime) {
    // this.startTime = startTime;
    // }
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    private final String currentUser;

    String defaultHdfsUrl;

    /**
     * @param path
     *            本地时间保存
     * @throws TimeManageException
     */
    public FileTimeProvider(String path) throws TimeManageException {
        this.path = path;
        object = new Object();
        init();
        this.currentUser = null;
    }

    /**
     * @param coreName
     *            应用coreName
     * @param context
     *            应用上下文
     * @throws TimeManageException
     */
    public FileTimeProvider(TSearcherDumpContext context, String currentUser) throws TimeManageException {
        this.serviceName = context.getServiceName();
        this.context = context;
        this.fileSystem = context.getDistributeFileSystem();
        this.currentUser = currentUser;
        this.defaultHdfsUrl = context.getFSName();
        object = new Object();
        init();
    }

    public void init() throws TimeManageException {
        // if (fileSystem == null) {// 本地存储
        // String[] paths = path.split("\\" + File.separator);
        // File tmpFile = null;
        // try {
        // StringBuffer subPath = new StringBuffer();
        // for (int i = 0, n = paths.length; i < n; i++) {
        // if (i < n - 1)
        // subPath.append(paths[i] + File.separator);
        // else
        // subPath.append(paths[i]);
        // tmpFile = new File(subPath.toString());
        // if (i < n - 1) {
        // if (!tmpFile.exists()) {
        // tmpFile.mkdir();
        // }
        // } else {
        // if (!tmpFile.exists() && !tmpFile.isDirectory()) {
        // tmpFile.createNewFile();
        // }
        // }
        // }
        // } catch (IOException e) {
        // throw new TimeManageException("初始化时间文件失败", e);
        // }
        // } else {// HDFS存储
        hdfsPath = new Path(defaultHdfsUrl + Path.SEPARATOR + Constants.USER + Path.SEPARATOR + currentUser + Path.SEPARATOR + serviceName + Path.SEPARATOR + Constants.TIME + Path.SEPARATOR + Constants.TIMEFILENAME);
        try {
            if (!fileSystem.exists(hdfsPath)) {
                // 已经存在，不需要在创建了
                fileSystem.create(hdfsPath);
            }
        } catch (IOException e) {
            logger.warn("【错误】 在HDFS创建时间文件路径[" + hdfsPath + "]失败");
            throw new TimeManageException("初始化HDFS时间文件失败", e);
        }
    // }
    }

    @Override
    public void reSetEndTime() {
        logger.info(">>>>>>【注意】获取每次导入任务执行时间点<<<<<<<[" + endTime + "]");
        this.endTime = new Date();
    }

    @Override
    public StartAndEndTime justGetTimes() throws TimeManageException {
        return readTimeFormFile();
    }

    @Override
    public void reWriteTimeToFile() throws TimeManageException {
        // } else {// HDFS存储
        synchronized (object) {
            FSDataOutputStream output = null;
            try {
                fileSystem.delete(hdfsPath, true);
                output = fileSystem.create(hdfsPath);
                output.write(TerminatorCommonUtils.formatDate(this.endTime).getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
                throw new TimeManageException("写入导入任务结束时间到HDFS失败", e);
            } finally {
                if (output != null) {
                    try {
                        output.flush();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.warn("写入导入任务结束时间到HDFS后关闭写入流失败", e);
                    } finally {
                        output = null;
                    }
                }
            }
        }
    // }
    }

    public static void main(String[] args) {
    // try {
    // FileTimeProvider provider = new FileTimeProvider(
    // "D:\\time-format\\time.incr");
    // provider.reSetEndTime();
    // StartAndEndTime time = provider.justGetTimes();
    // System.out.println("time====>" + time.toString());
    // provider.reWriteTimeToFile();
    // } catch (TimeManageException e) {
    // 
    // e.printStackTrace();
    // }
    }

    @Override
    public StartAndEndTime readTimeFormFile() throws TimeManageException {
        // } else {
        synchronized (object) {
            FSDataInputStream input = null;
            try {
                if (!fileSystem.exists(hdfsPath)) {
                    // 已经存在，不需要在创建了
                    fileSystem.create(hdfsPath);
                }
                input = fileSystem.open(hdfsPath);
                String strStartTime = input.readLine();
                if (strStartTime != null) {
                    this.startTime = TerminatorCommonUtils.parseDate(strStartTime);
                    Date tmpTime = TerminatorCommonUtils.parseDate(df.format(this.endTime) + " 00:00:00");
                    if (startTime.before(tmpTime)) {
                        logger.warn("[注意]时间文件被写入过并时间在当天凌晨之前，那么将startTime设置为当天的凌晨00:00");
                        /**
                         * 出现当前正运行的应用方机器Down机后，由其他机器抢到
                         * 锁继续执行增量任务，那么判断当前时间文件是否已经写入过时间
                         * 如果写入过并时间在当天凌晨之前，那么将startTime设置为当天的凌晨00:00
                         */
                        this.startTime = tmpTime;
                    }
                } else {
                    String d = df.format(this.endTime);
                    logger.warn("时间文件生成，但是文件数据为空，所以初始化化时间为当天凌晨" + d);
                    try {
                        this.startTime = TerminatorCommonUtils.parseDate(d + " 00:00:00");
                    } catch (ParseException pe) {
                        logger.error("格式化时间文件失败", pe);
                        throw new TimeManageException("格式化时间文件失败", pe);
                    }
                }
            } catch (IOException e) {
                logger.error("读取HDFS时间文件发生异常：" + this.hdfsPath, e);
                throw new TimeManageException("读取HDFS时间文件发生异常：" + this.hdfsPath, e);
            } catch (ParseException e) {
                logger.error("解析文件中存储的时间失败！", e);
                throw new TimeManageException("解析文件中存储的时间失败！", e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        logger.warn("【注意】关闭 HDFS 读取流出现问题");
                    }
                }
            }
            return new StartAndEndTime(this.startTime, this.endTime);
        }
    }
    // }
}
