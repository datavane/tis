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
package com.qlangtech.tis.trigger.jst;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.trigger.feedback.AbstractClient;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.ILogListener;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import com.qlangtech.tis.trigger.socket.LogType;
import io.netty.channel.ChannelHandlerContext;

/*
 * 接收Assemble节点日志的客户端节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LogCollector extends AbstractClient<ExecuteState<String>> {

    private static final Logger log = LoggerFactory.getLogger(LogCollector.class);

    private final AtomicInteger activeAudienceCount = new AtomicInteger();

    private final LogCollectorClientManager collectorManager;

    private static LogCollector logCollector;

    public static final int LOG_COLLECTOR_SOCKET_PORT_56789 = 56789;

    // private static final ScheduledThreadPoolExecutor delayExecutor = new
    // ScheduledThreadPoolExecutor(
    // 1);
    public static LogCollector getCollector() {
        if (logCollector == null) {
            synchronized (LogCollector.class) {
                if (logCollector == null) {
                    logCollector = new LogCollector(LogCollectorClientManager.getInstance());
                }
            }
        }
        return logCollector;
    }

    private LogCollector(LogCollectorClientManager collectorManager) {
        super(LOG_COLLECTOR_SOCKET_PORT_56789, /* port */
        3, TimeUnit.MINUTES, 100);
        this.collectorManager = collectorManager;
    }

    public void start() {
        int activeCount = activeAudienceCount.getAndIncrement();
        if (activeCount < 1) {
            try {
                connect2Remote();
                log.info("activeCount:" + activeCount + " connect to remote log server");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("activeCount:" + activeCount + " there is an exist connection channel");
        }
    }

    @Override
    protected void reConnect(List<String> linkHost) throws Exception {
        StringBuffer hosts = new StringBuffer("connect to");
        for (String ip : linkHost) {
            hosts.append("[").append(ip).append("]");
        }
        ExecuteState<String> state = ExecuteState.create(LogType.INCR, hosts.toString());
        for (ILogListener targetListener : collectorManager.getAllListener()) {
            targetListener.read(state);
        }
        super.reConnect(linkHost);
    }

    public void processError(Exception e) {
        activeAudienceCount.decrementAndGet();
        log.error(e.getMessage(), e);
    // if (activeCount < 1) {
    // try {
    // connect2Remote();
    // log.info("activeCount:" + activeCount + " connect to remote log
    // server");
    // } catch (Exception e) {
    // throw new RuntimeException();
    // }
    // } else {
    // log.info("activeCount:" + activeCount + " there is an exist
    // connection channel");
    // }
    }

    @Override
    protected void disconnectChannel(ChannelHandlerContext ctx) {
        activeAudienceCount.lazySet(0);
    }

    public void close() {
        int activeCount = activeAudienceCount.decrementAndGet();
        if (activeCount < 1) {
            try {
                getChannelGroup().close().sync();
                activeAudienceCount.compareAndSet(activeCount, 0);
                log.info("connection to server have been closed");
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void processError(Throwable e) {
        activeAudienceCount.lazySet(0);
    }

    /**
     * @param state
     */
    @Override
    protected void processMessage(ExecuteState state) {
        String collection = state.getCollectionName();
        if (!StringUtils.startsWith(collection, "search4")) {
            throw new IllegalStateException("collection:" + collection + " is illegal");
        }
        for (ILogListener l : collectorManager.getAllListener()) {
            l.read(state);
        }
    }
}
