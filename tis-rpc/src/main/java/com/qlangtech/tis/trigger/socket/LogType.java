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
package com.qlangtech.tis.trigger.socket;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 网络传输的日志类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public enum LogType {

    // 部署状态变化
    INCR_DEPLOY_STATUS_CHANGE("incrdeploy-change"),
    // 增量索引构建
    INCR_BUILD("incrbuild"),
    // 增量构建事件变化
    INCR_BUILD_STATUS_CHANGE("incrbuildstatus_change"),
    // 
    MQ_TAGS_STATUS("mq_tags_status"),
    FULL("full", new MonitorFileCreator() {

        public File create(String collection) {
            String logdir = System.getProperty("log.dir");
            if (logdir == null) {
                throw new IllegalStateException("log dir can not be null");
            }
            return new File(System.getProperty("log.dir") + File.separator + "assemble" + File.separator + "full-" + collection + ".log");
        }
    }),
    // 
    INCR("incr", new MonitorFileCreator() {

        public File create(String collection) {
            return new File(System.getProperty("log.dir") + File.separator + "incr" + File.separator + "incr-" + collection + ".log");
        }
    }),
    // 
    INCR_SEND("incrsend", new MonitorFileCreator() {

        public File create(String collection) {
            return new File(System.getProperty("log.dir") + File.separator + "incr" + File.separator + "send-" + collection + ".log");
        }
    });

    private static final Logger logger = LoggerFactory.getLogger(LogType.class);

    @Override
    public String toString() {
        return this.value;
    }

    public static LogType parse(String value) {
        if (FULL.value.equals(value)) {
            return FULL;
        }
        if (INCR.value.equals(value)) {
            return INCR;
        }
        if (INCR_SEND.value.equals(value)) {
            return INCR_SEND;
        }
        if (MQ_TAGS_STATUS.value.equals(value)) {
            return MQ_TAGS_STATUS;
        }
        if (INCR_BUILD.value.equals(value)) {
            return INCR_BUILD;
        }
        if (INCR_DEPLOY_STATUS_CHANGE.value.equals(value)) {
            return INCR_DEPLOY_STATUS_CHANGE;
        }
        throw new IllegalStateException("value:" + value + " is illegal");
    }

    private final String value;

    private final MonitorFileCreator monitorFileCreator;

    public String getValue() {
        return this.value;
    }

    public File getMonitorFile(String collection) {
        File monitorFile = this.monitorFileCreator.create(collection);
        logger.info("monitor file:" + monitorFile.getAbsolutePath());
        return monitorFile;
    }

    private LogType(String value, MonitorFileCreator monitorFileCreator) {
        this.value = value;
        this.monitorFileCreator = monitorFileCreator;
    }

    private LogType(String value) {
        this(value, new MonitorFileCreator());
    }

    private static class MonitorFileCreator {

        File create(String collection) {
            return null;
        }
    }
}
