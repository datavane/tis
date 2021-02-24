/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.qlangtech.tis.BaseTestCase;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.rpc.server.FullBuildStatCollectorServer;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.trigger.jst.MonotorTarget;
import com.qlangtech.tis.trigger.socket.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-11 13:04
 */
public class TestRealtimeLoggerCollectorAppender extends BaseTestCase {

    private static final int taskid = 123;

    private static final String collection = "search4totalpay";

    private static final Logger logger = LoggerFactory.getLogger(TestRealtimeLoggerCollectorAppender.class);

    public void testIncrProcessLogger() {
        MonotorTarget mtarget = MonotorTarget.createRegister(collection, LogType.INCR);
        AtomicBoolean closed = new AtomicBoolean(false);
        final AtomicInteger receivedCount = new AtomicInteger();
        FullBuildStatCollectorServer.addListener(mtarget, new RealtimeLoggerCollectorAppender.LoggerCollectorAppenderListener() {

            @Override
            public void readLogTailer(RealtimeLoggerCollectorAppender.LoggingEventMeta meta, File logFile) {
            }

            @Override
            public void process(RealtimeLoggerCollectorAppender.LoggingEventMeta mtarget, LoggingEvent e) {
                receivedCount.incrementAndGet();
            }

            @Override
            public boolean isClosed() {
                return closed.get();
            }
        });
        IncrStatusUmbilicalProtocolImpl.setCollectionName(collection);
        IncrStatusUmbilicalProtocolImpl.statisLog.info("incr_1");
        IncrStatusUmbilicalProtocolImpl.statisLog.info("incr_2");
        String loggerName = "incr-" + collection;
        RealtimeLoggerCollectorAppender bufferAppender = RealtimeLoggerCollectorAppender.getBufferAppender(loggerName);
        assertNotNull(bufferAppender);
        assertEquals(2, receivedCount.get());
    }

    /**
     * 测试全量构建日志
     */
    public void testFullBuildLogger() {
        MDC.put(IParamContext.KEY_TASK_ID, String.valueOf(taskid));
        logger.info("start");
        String loggerName = "full-" + taskid;
        RealtimeLoggerCollectorAppender bufferAppender = RealtimeLoggerCollectorAppender.getBufferAppender(loggerName);
        assertNotNull(bufferAppender);
        AtomicBoolean closed = new AtomicBoolean(false);
        final AtomicInteger receivedCount = new AtomicInteger();
        MonotorTarget mtarget = MonotorTarget.createRegister("", LogType.FULL);
        mtarget.setTaskid(taskid);
        FullBuildStatCollectorServer.addListener(mtarget, new RealtimeLoggerCollectorAppender.LoggerCollectorAppenderListener() {

            @Override
            public void readLogTailer(RealtimeLoggerCollectorAppender.LoggingEventMeta meta, File logFile) {
            }

            @Override
            public void process(RealtimeLoggerCollectorAppender.LoggingEventMeta mtarget, LoggingEvent e) {
                receivedCount.incrementAndGet();
            }

            @Override
            public boolean isClosed() {
                return closed.get();
            }
        });
        logger.info("start to log");
        logger.info("start to log2");
        closed.set(true);
        logger.info("start to log3");
        RealtimeLoggerCollectorAppender.LogTypeListeners logTypeListeners = RealtimeLoggerCollectorAppender.appenderListener.getLogTypeListeners(loggerName);
        assertNull(logTypeListeners);
        // for (LoggingEvent o : bufferAppender.cb.asList()) {
        // 
        // System.out.println("=======" + o.getTimeStamp() + o + "," + o.getClass());
        // }
        System.out.println();
        // assertEquals(512, bufferAppender.getMaxSize());
        // assertEquals(3, bufferAppender.cb.length());
        assertEquals(2, receivedCount.get());
    }
}
