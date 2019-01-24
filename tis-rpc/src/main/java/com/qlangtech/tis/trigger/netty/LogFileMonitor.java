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
package com.qlangtech.tis.trigger.netty;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.MonotorTarget;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LogFileMonitor {

    private static final ConcurrentHashMap<MonotorTarget, LogFileMonitor> logMonitorMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(LogFileMonitor.class);

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    // private final ConcurrentHashMap<String, AtomicInteger> audienceCountMap = new
    // ConcurrentHashMap<String, AtomicInteger>();
    // 记录被监听的
    private final AtomicInteger audienceCount = new AtomicInteger();

    private final MonotorTarget register;

    private Tailer tailer;

    private MyTailerListener listener;

    private static Field runField;

    private IProcessLine logFileTailerProcess;

    private final ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    static {
        try {
            runField = Tailer.class.getDeclaredField("run");
            runField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final TriggerLogServer triggerLogServer;

    public static LogFileMonitor getLogFileMonitor(MonotorTarget register, TriggerLogServer triggerLogServer) {
        LogFileMonitor monitor = logMonitorMap.get(register);
        LogFileMonitor tmp = null;
        if (monitor == null) {
            monitor = new LogFileMonitor(register, triggerLogServer);
            tmp = logMonitorMap.putIfAbsent(register, monitor);
            if (tmp != null) {
                monitor = tmp;
            }
        }
        return monitor;
    }

    public static void main(String[] args) {
        LogFileMonitor monitor = new LogFileMonitor(null, null);
        File f = new File("D:\\home\\admin\\logs\\2016-02-16-console.log");
        monitor.readLastNLine(f, 100, new IProcessLine() {

            @Override
            public void print(String line) {
                System.out.println(line);
            }
        });
    }

    public static Collection<LogFileMonitor> getAllMonitor() {
        return logMonitorMap.values();
    }

    /**
     * @param register
     */
    private LogFileMonitor(MonotorTarget register, TriggerLogServer triggerLogServer) {
        super();
        this.register = register;
        this.listener = new MyTailerListener(register);
        this.triggerLogServer = triggerLogServer;
        this.logFileTailerProcess = new IProcessLine() {

            @Override
            public void print(String line) {
                listener.handle(line);
            }
        };
    }

    /**
     * 开始监听文件
     */
    public void startMonitor(ChannelHandlerContext ctx) {
        try {
            synchronized (audienceCount) {
                int acount = audienceCount.incrementAndGet();
                if (acount < 1) {
                    audienceCount.compareAndSet(acount, 1);
                }
                this.recipients.add(ctx.channel());
                // 启动监听
                File monitorFile = register.getLogType().getMonitorFile(register.getCollection());
                // 显示最终200行
                readLastNLine(monitorFile, 200, this.logFileTailerProcess);
                if (isTailerStop()) {
                    this.tailer = new Tailer(monitorFile, listener, 500, true);
                    exec.execute(tailer);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("start new tailer,audience:" + audienceCount.get() + "," + register.getCollection() + "," + register.getLogType());
        }
    }

    private interface IProcessLine {

        void print(String line);
    }

    // public void stopMonitor(ChannelHandlerContext ctx) {
    // }
    /**
     * 将对应channel的activeCount歸零
     *
     * @param ctx
     */
    public void removeAudience(ChannelHandlerContext ctx) {
        // this.audienceCountMap.remove(ctx.name());
        logger.info("channel name:" + ctx.name() + " active count remove");
        // stopMonitor(ctx);
        synchronized (audienceCount) {
            int decrementAndGet = audienceCount.decrementAndGet();
            // int activeCount = 0;
            if ((decrementAndGet) < 1) {
                logger.info("stop tailer exec,audience:" + audienceCount.get() + "," + register.getCollection() + "," + register.getLogType() + ",channel:" + ctx.name());
                audienceCount.compareAndSet(decrementAndGet, 0);
                tailer.stop();
            } else {
                logger.info("decrease tailer still work:" + register.getCollection() + "," + register.getLogType() + ",activate count:" + decrementAndGet + ",channel:" + ctx.name());
            }
        }
    }

    // private int canRemoveMonitor() {
    // int activeCount = 0;
    // for (AtomicInteger audienceCount : audienceCountMap.values()) {
    // if ((activeCount = audienceCount.get()) > 0) {
    // return activeCount;
    // }
    // }
    // return activeCount;
    // }
    private boolean isTailerStop() {
        if (tailer == null) {
            return true;
        }
        try {
            return !(Boolean) runField.get(tailer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class MyTailerListener extends TailerListenerAdapter {

        private final MonotorTarget register;

        public MyTailerListener(MonotorTarget register) {
            super();
            this.register = register;
        }

        public void handle(String line) {
            // try {
            ExecuteState<String> logg = ExecuteState.create(register.getLogType(), line);
            // logg.setLogType(register.getLogType());
            logg.setServiceName(register.getCollection());
            // triggerLogServer.writeState(logg);
            recipients.write(logg);
        // } catch (UnknownHostException e) {
        // logger.error(e.getMessage(), e);
        // }
        }
    }

    /**
     * 打印文件的最后n行内容
     *
     * @param monitorFile
     * @param n
     * @param lineProcess
     */
    private void readLastNLine(File monitorFile, int n, IProcessLine lineProcess) {
        if (!monitorFile.exists()) {
            // 文件不存在就直接退出了
            return;
        }
        RandomAccessFile randomAccess = null;
        try {
            randomAccess = new RandomAccessFile(monitorFile, "r");
            // boolean eol = false;
            // int c = -1;
            long fileLength = randomAccess.length();
            long size = 1;
            boolean hasEncountReturn = false;
            ww: while (true) {
                long offset = fileLength - (size++);
                if (offset < 0) {
                    break ww;
                }
                randomAccess.seek(offset);
                switch(// c =
                randomAccess.read()) {
                    case '\n':
                    case '\r':
                        if (!hasEncountReturn && (n--) <= 0) {
                            randomAccess.seek(offset + 1);
                            break ww;
                        }
                        hasEncountReturn = true;
                        continue;
                    default:
                        hasEncountReturn = false;
                }
            }
            String line = null;
            int lineCount = 0;
            while ((line = randomAccess.readLine()) != null) {
                lineProcess.print(line);
                lineCount++;
            }
            logger.info("has read " + lineCount + " lines to client from file :" + monitorFile.getName());
            recipients.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(randomAccess);
        }
    }
}
