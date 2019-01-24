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
package com.qlangtech.tis.hdfs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.exception.TimeManageException;

/*
 * @description
 * @since 2011-9-7 02:40:55
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SystemFileUtil {

    private static Log logger = LogFactory.getLog(SystemFileUtil.class);

    public static void writeTimePointToFile(String rootDirStr, String timePoint) {
        File rootDir = new File(rootDirStr);
        writeTimePointToFile(rootDirStr, timePoint);
    }

    public static String readTimePointFromFile(String rootDirStr) throws TimeManageException {
        File rootDir = new File(rootDirStr);
        return readTimePointFormFile(rootDir);
    }

    public static boolean checkFileExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 读取时间点
     *
     * @param path
     * @param timePoint
     * @return
     * @throws TimeManageException
     */
    public static String readTimePointFormFile(File rootDirStr) throws TimeManageException {
        File timeFile = new File(rootDirStr, SysFileEnum.TIME_POINT_NAME.getValue());
        String timePoint = null;
        if (timeFile.exists()) {
            // 文件存在
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(timeFile));
                String strStartTime = reader.readLine();
            } catch (FileNotFoundException fnfe) {
                // 理论上不会抛出这个异常的……
                logger.error("指定的记录时间文件不存在:" + timeFile, fnfe);
                throw new TimeManageException("指定的记录时间文件不存在:" + timeFile, fnfe);
            } catch (IOException ioe) {
                logger.error("读取时间文件发生异常：" + timeFile, ioe);
                throw new TimeManageException("读取时间文件发生异常：" + timeFile, ioe);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        logger.error("读取文件后关闭文件发生异常", ioe);
                    }
                }
            }
        }
        // }
        return timePoint;
    }

    /**
     * @param rootDir
     * @param timePoint
     */
    public static void writeTimePointToFile(File rootDir, String timePoint) {
        File incrStartTimeFile = new File(rootDir, SysFileEnum.TIME_POINT_NAME.getValue());
        if (!incrStartTimeFile.exists()) {
            try {
                incrStartTimeFile.createNewFile();
            } catch (IOException e) {
                logger.error("创建时间点记录文件失败。。。", e);
                return;
            }
        }
        FileWriter writer = null;
        int tryNum = 0;
        // 写入时间记录
        while (tryNum < 3) {
            try {
                writer = new FileWriter(incrStartTimeFile);
                writer.write(timePoint);
            } catch (IOException e) {
                tryNum++;
                logger.error("写增量时间开始时间文件失败。。。。第<" + tryNum + ">次", e);
                continue;
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    logger.error("关闭写增量时间开始时间文件的FileWriter失败。。。", e);
                }
            }
            break;
        }
        if (tryNum > 0) {
            logger.error("重试写增量时间开始时间文件3次之后仍然失败，不写了。。。。");
        }
    }

    /**
     * @author   yingyuan.lyq
     */
    public enum SysFileEnum {

        /**
         * @uml.property  name="iNDEX_FILE_NAME"
         * @uml.associationEnd
         */
        INDEX_FILE_NAME("terminator_idx"),
        /**
         * @uml.property  name="tMP_INDEX_FILE_SUFFIX"
         * @uml.associationEnd
         */
        TMP_INDEX_FILE_SUFFIX(".tmp"),
        /**
         * @uml.property  name="iNDEX_FILE_SUFFIX"
         * @uml.associationEnd
         */
        INDEX_FILE_SUFFIX(".tidx"),
        /**
         * @uml.property  name="iNCR_START_TIME_FILE"
         * @uml.associationEnd
         */
        INCR_START_TIME_FILE("incr_start_time"),
        /**
         * @uml.property  name="sYNC_XML_FROM_MASTER_TIME"
         * @uml.associationEnd
         */
        SYNC_XML_FROM_MASTER_TIME("sync_xml_start_time"),
        /**
         * @uml.property  name="dATE_TIME_PATTERN"
         * @uml.associationEnd
         */
        DATE_TIME_PATTERN("yyyy-MM-dd-HH-mm-ss-SSSS"),
        /**
         * @uml.property  name="tIME_POINT_NAME"
         * @uml.associationEnd
         */
        TIME_POINT_NAME("local_time_point"),
        /**
         * @uml.property  name="dILATATION_MARK"
         * @uml.associationEnd
         */
        DILATATION_MARK("dilatation_mark");

        /**
         * @uml.property  name="value"
         */
        private String value;

        SysFileEnum(String value) {
            this.value = value;
        }

        /**
         * @return
         * @uml.property  name="value"
         */
        public String getValue() {
            return this.value;
        }
    }
}
