/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.FileAppender;

import java.io.File;
import java.util.*;

/**
 * 全量构建过程中通过的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-11 11:32
 */
public class RealtimeLoggerCollectorAppender extends FileAppender<LoggingEvent> {
    private static final String KEY_ASSEMBLE_TASK_DIR = "assemble.task.dir";

    private static final File getAssebleTaskDir() {
        return new File(System.getProperty(KEY_ASSEMBLE_TASK_DIR));
    }

    // extends CyclicBufferAppender<E>
    private static final Map<String, RealtimeLoggerCollectorAppender> bufferAppenderMap = new HashMap<>();

    public static final LoggerCollectorAppenderListenerWrapper appenderListener = new LoggerCollectorAppenderListenerWrapper();

    // /**
    // * 取得全量构建loggerBuffer
    // *
    // * @param taskId
    // * @return
    // */
    // public static RealtimeLoggerCollectorAppender getFullBuildLoggingBufferAppender(int taskId) {
    // return getBufferAppender(LogType.FULL.getValue() + "-" + taskId);
    // }
    public static void addListener(String targetAppenderName, LoggingEventMeta mtarget, LoggerCollectorAppenderListener listener) {
        appenderListener.addLoggerEventListener(targetAppenderName, mtarget, listener);
    }

    static RealtimeLoggerCollectorAppender getBufferAppender(String name) {
        RealtimeLoggerCollectorAppender appender = bufferAppenderMap.get(name);
        if (appender == null) {
            throw new IllegalStateException("appender:" + name + " relevant appender can not be null,exist keys:" + bufferAppenderMap.keySet());
        }
        return appender;
    }

    // CyclicBuffer<LoggingEvent> cb;
    // int maxSize = 256;
    // 
    // public void start() {
    // cb = new CyclicBuffer<LoggingEvent>(maxSize);
    // super.start();
    // this.getFile()
    // }
    // 
    // public void stop() {
    // cb = null;
    // super.stop();
    // }
    @Override
    protected void append(LoggingEvent eventObject) {
        super.append(eventObject);
        if (!isStarted()) {
            return;
        }
        // cb.add(eventObject);
        appenderListener.process(this.name, eventObject);
        // LoggerCollectorAppenderListener l = null;
        // Iterator<LoggerCollectorAppenderListener> it = appenderListener.iterator();
        // while (it.hasNext()) {
        // l = it.next();
        // if (l.isClosed()) {
        // it.remove();
        // } else {
        // l.process(eventObject);
        // }
        // }
    }

    public interface LoggerCollectorAppenderListener {

        public void process(LoggingEventMeta mtarget, LoggingEvent e);

        /**
         * 初次打开文件，读文件最后几条
         *
         * @param logFile
         */
        public void readLogTailer(LoggingEventMeta meta, File logFile);

        public boolean isClosed();
    }

    public static class LoggingEventMeta {

        public Integer taskid;

        public String collection;

        public int logTypeVal;
    }

    public static class LoggerCollectorAppenderListenerWrapper {

        private final Map<String, LogTypeListeners> targetAppenderLister = new HashMap<>();

        public LogTypeListeners getLogTypeListeners(String targetAppenderName) {
            return targetAppenderLister.get(targetAppenderName);
        }

        public void process(String targetAppenderName, LoggingEvent event) {
            LogTypeListeners targetListeners = targetAppenderLister.get(targetAppenderName);
            if (targetListeners == null) {
                return;
            }
            boolean hasListener = targetListeners.send(event);
            if (!hasListener) {
                synchronized (LoggerCollectorAppenderListenerWrapper.class) {
                    targetAppenderLister.remove(targetAppenderName);
                }
            }
        }

        public void addLoggerEventListener(String targetAppenderName, LoggingEventMeta mtarget, LoggerCollectorAppenderListener l) {
            synchronized (LoggerCollectorAppenderListenerWrapper.class) {
                LogTypeListeners listeners = targetAppenderLister.get(targetAppenderName);
                if (listeners == null) {
                    listeners = new LogTypeListeners(mtarget);
                    targetAppenderLister.put(targetAppenderName, listeners);
                }
                RealtimeLoggerCollectorAppender appender = bufferAppenderMap.get(targetAppenderName);
                File file = null;
                if (appender != null) {
                    file = new File(appender.getFile());
                } else {
                    file = new File(getAssebleTaskDir(), targetAppenderName + ".log");
                }
                if (file.exists()) {
                    l.readLogTailer(mtarget, file);
                }
                listeners.listeners.add(l);
            }
        }
    }

    public static class LogTypeListeners {

        private final LoggingEventMeta mtarget;

        private final List<LoggerCollectorAppenderListener> listeners = new ArrayList<>();

        public LogTypeListeners(LoggingEventMeta mtarget) {
            this.mtarget = mtarget;
        }

        // boolean notEmpty() {
        // return this.listeners.size() > 0;
        // }
        public int getListenerSize() {
            return this.listeners.size();
        }

        boolean send(LoggingEvent event) {
            Iterator<LoggerCollectorAppenderListener> it = listeners.iterator();
            LoggerCollectorAppenderListener listener = null;
            boolean hasListener = false;
            while (it.hasNext()) {
                listener = it.next();
                if (listener.isClosed()) {
                    it.remove();
                } else {
                    listener.process(this.mtarget, event);
                    hasListener = true;
                }
            }
            return hasListener;
        }
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        bufferAppenderMap.put(name, this);
    }
}
