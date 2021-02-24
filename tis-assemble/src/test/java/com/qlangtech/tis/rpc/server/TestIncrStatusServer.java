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
package com.qlangtech.tis.rpc.server;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.BaseTestCase;
import com.qlangtech.tis.log.RealtimeLoggerCollectorAppender;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.realtime.yarn.rpc.*;
import com.qlangtech.tis.rpc.grpc.log.LogCollectorClient;
import com.qlangtech.tis.rpc.grpc.log.stream.PExecuteState;
import com.qlangtech.tis.rpc.grpc.log.stream.PMonotorTarget;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.trigger.socket.LogType;
import com.tis.hadoop.rpc.StatusRpcClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-17 21:18
 */
public class TestIncrStatusServer extends BaseTestCase {

    static final String collectionName = "search4totalpay";

    final int exportPort = 9999;

    final Exception[] ex = new Exception[1];

    public TestIncrStatusServer() {
        this.startRpcServer();
    }

    /**
     * 测试assemble节点发送暂停命令，服务端正常响应
     */
    public void testHasReceiveIncrPausedCommand() {
        StatusRpcClient.AssembleSvcCompsite svc = getRpcClient();
        String uuid = UUID.randomUUID().toString();
        final AtomicBoolean hasReceiveIncrPausedCommand = new AtomicBoolean(false);
        final AtomicBoolean hasReceiveIncrResumeCommand = new AtomicBoolean(false);
        Runnable runnable = (() -> {
            boolean incrProcessPaused = false;
            if (hasReceiveIncrResumeCommand.get()) {
                incrProcessPaused = false;
            } else if (hasReceiveIncrPausedCommand.get()) {
                incrProcessPaused = true;
            }
            UpdateCounterMap updateCt = createUpdateCounterMap(uuid, incrProcessPaused);
            MasterJob masterJob = svc.reportStatus(updateCt);
            if (masterJob != null && masterJob.isCollectionIncrProcessCommand(collectionName)) {
                if (masterJob.isStop()) {
                    hasReceiveIncrPausedCommand.set(true);
                } else {
                    hasReceiveIncrResumeCommand.set(true);
                }
            }
        });
        IncrStatusUmbilicalProtocolImpl instance = IncrStatusUmbilicalProtocolImpl.getInstance();
        IndexJobRunningStatus runningStatus = instance.getIndexJobRunningStatus(collectionName);
        assertFalse(runningStatus.isIncrGoingOn());
        assertFalse(runningStatus.isIncrProcessPaused());
        incrProcessReport(runnable);
        runningStatus = instance.getIndexJobRunningStatus(collectionName);
        assertTrue(runningStatus.isIncrGoingOn());
        assertFalse(runningStatus.isIncrProcessPaused());
        // 暂停
        assertTrue("pauseConsume shall success", instance.pauseConsume(collectionName));
        incrProcessReport(runnable);
        assertTrue("shall has receive", hasReceiveIncrPausedCommand.get());
        runningStatus = instance.getIndexJobRunningStatus(collectionName);
        assertTrue(runningStatus.isIncrGoingOn());
        assertTrue(runningStatus.isIncrProcessPaused());
        // 开启
        assertTrue("resumeConsume shall success", instance.resumeConsume(collectionName));
        incrProcessReport(runnable);
        assertTrue("shall has receive", hasReceiveIncrResumeCommand.get());
        runningStatus = instance.getIndexJobRunningStatus(collectionName);
        assertTrue(runningStatus.isIncrGoingOn());
        assertFalse(runningStatus.isIncrProcessPaused());
    }

    private void incrProcessReport(Runnable runnable) {
        // 一定要跑两次
        runnable.run();
        runnable.run();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试增量执行过程中向服务端发送各tag累计数
     */
    public void testReportStatus() {
        StatusRpcClient.AssembleSvcCompsite svc = getRpcClient();
        String uuid = UUID.randomUUID().toString();
        UpdateCounterMap updateCt = createUpdateCounterMap(uuid, false);
        MasterJob masterJob = svc.reportStatus(updateCt);
        assertNull(masterJob);
        IncrStatusUmbilicalProtocolImpl instance = IncrStatusUmbilicalProtocolImpl.getInstance();
        instance.pauseConsume(collectionName);
        masterJob = svc.reportStatus(updateCt);
        assertNotNull(masterJob);
        assertTrue("the processing of incr shall be stop", masterJob.isStop());
        instance.resumeConsume(collectionName);
        masterJob = svc.reportStatus(updateCt);
        assertNotNull(masterJob);
        assertFalse("the processing of incr shall be start", masterJob.isStop());
    }

    private UpdateCounterMap createUpdateCounterMap(String uuid, boolean incrProcessPaused) {
        UpdateCounterMap updateCt = new UpdateCounterMap();
        updateCt.setFrom(NetUtils.getHostname());
        updateCt.setGcCounter(12);
        updateCt.setUpdateTime(System.currentTimeMillis() / 1000);
        TableSingleDataIndexStatus tableUpdateCounter = new TableSingleDataIndexStatus();
        tableUpdateCounter.setUUID(uuid);
        tableUpdateCounter.setIncrProcessPaused(incrProcessPaused);
        tableUpdateCounter.setConsumeErrorCount(1);
        tableUpdateCounter.setBufferQueueRemainingCapacity(1000);
        tableUpdateCounter.put("test_table", 999l);
        updateCt.addTableCounter(collectionName, tableUpdateCounter);
        return updateCt;
    }

    /**
     * 增量节点启动向assemble节点汇报监听的内容
     */
    public void testNodeLaunchReport() {
        StatusRpcClient.AssembleSvcCompsite svc = getRpcClient();
        Map<String, TopicInfo> /**
         * collection
         */
        collectionFocusTopicInfo = Maps.newHashMap();
        String testTopic = "test-topic";
        TopicInfo topicInfo = new TopicInfo();
        topicInfo.addTag(testTopic, Sets.newHashSet("tag1", "tag2", "tag3"));
        collectionFocusTopicInfo.put(collectionName, topicInfo);
        LaunchReportInfo launchReportInfo = new LaunchReportInfo(collectionFocusTopicInfo);
        svc.nodeLaunchReport(launchReportInfo);
        IncrStatusUmbilicalProtocolImpl instance = IncrStatusUmbilicalProtocolImpl.getInstance();
        com.qlangtech.tis.grpc.TopicInfo receivedTopicInfo = instance.getFocusTopicInfo(collectionName);
        assertNotNull(receivedTopicInfo);
        assertEquals(1, receivedTopicInfo.getTopicWithTagsCount());
        assertEquals(testTopic, receivedTopicInfo.getTopicWithTags(0).getTopicName());
    }

    public void testServer() throws Exception {
        int taskid = 123;
        String appname = "baisuitest";
        AtomicBoolean clientClosed = new AtomicBoolean(false);
        RegisterMonitorEventHook eventHook = new RegisterMonitorEventHook() {

            boolean hasStartSession = false;

            boolean hasCloseSession = false;

            int send2ClientCount = 0;

            int readFromFileTailerCount = 0;

            @Override
            public void send2ClientFromFileTailer(File logFile, PExecuteState s) {
                readFromFileTailerCount++;
            }

            @Override
            void startSession() {
                hasStartSession = true;
            }

            @Override
            void send2Client(PExecuteState s, LoggingEvent e) {
                send2ClientCount++;
            }

            @Override
            void closeSession() {
                hasCloseSession = true;
            }

            public void validateExpect() {
                assertTrue(this.hasStartSession);
                assertTrue(this.hasCloseSession);
                assertTrue("send2ClientCount shall big than 0", send2ClientCount > 0);
                assertTrue("read from file buffer", readFromFileTailerCount > 0);
            }
        };
        FullBuildStatCollectorServer.registerMonitorEventHook = eventHook;
        // 启动rpc服务端
        Runnable writeLog = () -> {
            MDC.put(IParamContext.KEY_TASK_ID, String.valueOf(taskid));
            MDC.put("app", appname);
            String logMsg = "test_log_msg";
            Logger log = LoggerFactory.getLogger(TestIncrStatusServer.class);
            while (true) {
                // 需要会向RealtimeLoggerCollectorAppender 该appender中写入
                log.info(logMsg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        };
        Thread tt = new Thread(writeLog);
        tt.start();
        StatusRpcClient.AssembleSvcCompsite svc = getRpcClient();
        CountDownLatch countdown = new CountDownLatch(1);
        // BlockingQueue<MonotorTarget> focusTarget = new ArrayBlockingQueue<>(10);
        int[] receiveCount = new int[1];
        StreamObserver<PMonotorTarget> observer = svc.registerMonitorEvent(new ILogListener() {

            @Override
            public void sendMsg2Client(Object biz) throws IOException {
            }

            @Override
            public void read(Object event) {
                PExecuteState stat = (PExecuteState) event;
                System.out.println(stat.getTaskId() + " " + stat.getMsg());
                if (receiveCount[0]++ > 5) {
                    try {
                        // 收到服务端 5次消息之后，模拟webSocket session 关闭
                        clientClosed.set(true);
                    // throw new RuntimeException("dddddddddddddd");
                    } finally {
                        countdown.countDown();
                    }
                }
            }

            @Override
            public boolean isClosed() {
                return clientClosed.get();
            }
        });
        PMonotorTarget.Builder mt = PMonotorTarget.newBuilder();
        mt.setCollection("dummy");
        mt.setTaskid(taskid);
        mt.setLogtype(LogCollectorClient.convert(LogType.FULL.typeKind));
        PMonotorTarget mtarget = mt.build();
        observer.onNext(mtarget);
        String targetToken = FullBuildStatCollectorServer.addListener(LogCollectorClient.convert(mtarget), new RealtimeLoggerCollectorAppender.LoggerCollectorAppenderListener() {

            @Override
            public void process(RealtimeLoggerCollectorAppender.LoggingEventMeta mtarget, LoggingEvent e) {
                System.out.println("server side kk:" + e.getMessage());
            }

            @Override
            public void readLogTailer(RealtimeLoggerCollectorAppender.LoggingEventMeta meta, File logFile) {
            }

            @Override
            public boolean isClosed() {
                return clientClosed.get();
            }
        });
        assertTrue("countdown shall be execute", countdown.await(2, TimeUnit.MINUTES));
        // 需要再写几个日志
        Thread.sleep(2000l);
        RealtimeLoggerCollectorAppender.LogTypeListeners logListeners = RealtimeLoggerCollectorAppender.appenderListener.getLogTypeListeners(targetToken);
        assertEquals(1, logListeners.getListenerSize());
        eventHook.validateExpect();
        System.out.println("rpc test over");
    // synchronized (this) {
    // this.wait();
    // }
    }

    private StatusRpcClient.AssembleSvcCompsite getRpcClient() {
        return StatusRpcClient.connect2RemoteIncrStatusServer("localhost:" + exportPort);
    }

    @Override
    protected void tearDown() throws Exception {
        ex[0] = null;
        FullBuildStatCollectorServer.registerMonitorEventHook = new RegisterMonitorEventHook();
    }

    private void startRpcServer() {
        // grpc启动服务端
        Runnable runnable = () -> {
            try {
                IncrStatusServer incrStatusServer = new IncrStatusServer(exportPort);
                incrStatusServer.addService(IncrStatusUmbilicalProtocolImpl.getInstance());
                incrStatusServer.addService(FullBuildStatCollectorServer.getInstance());
                incrStatusServer.start();
                incrStatusServer.blockUntilShutdown();
            } catch (Exception e) {
                ex[0] = e;
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }
}
