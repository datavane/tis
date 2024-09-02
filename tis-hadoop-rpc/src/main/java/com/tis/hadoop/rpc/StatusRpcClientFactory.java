/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tis.hadoop.rpc;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.PingResult;
import com.qlangtech.tis.realtime.yarn.rpc.SynResTarget;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.qlangtech.tis.rpc.grpc.log.DefaultLoggerAppenderClient;
import com.qlangtech.tis.rpc.grpc.log.ILogReporter;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient;
import com.qlangtech.tis.rpc.grpc.log.ILoggerAppenderClient.LogLevel;
import com.qlangtech.tis.rpc.grpc.log.LogCollectorClient;
import com.qlangtech.tis.rpc.grpc.log.appender.LoggingEvent;
import com.qlangtech.tis.rpc.grpc.log.stream.PMonotorTarget;
import com.qlangtech.tis.rpc.grpc.log.stream.PPhaseStatusCollection;
import com.qlangtech.tis.rpc.server.IncrStatusClient;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.trigger.jst.ILogListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月22日
 */
public class StatusRpcClientFactory {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.+?):(\\d+)$");

    private static final Logger logger = LoggerFactory.getLogger(StatusRpcClientFactory.class);

    private static RpcServiceReference instance;// = new StatusRpcClient();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread("dataX ShutdownHook") {
            @Override
            public void run() {
                if (instance == null) {
                    return;
                }
                try {
                    ITISRpcService rpcService = instance.getRef().get();
                    if (rpcService != null) {
                        rpcService.close();
                    }
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        });
    }

    private StatusRpcClientFactory() {
    }

    public static RpcServiceReference getService(ITISCoordinator zookeeper, AdapterAssembleSvcCompsiteCallback... callbacks) throws Exception {
        if (instance == null) {
            synchronized (StatusRpcClientFactory.class) {
                if (instance == null) {
                    StatusRpcClientFactory clientFactory = new StatusRpcClientFactory();
                    instance = clientFactory.connect2RemoteIncrStatusServer(zookeeper, callbacks);
                    AssembleSvcCompsite.statusRpc = instance;
                }

            }
        }
        return instance;
    }

    /**
     * 连接日志收集节点地址
     *
     * @param zookeeper zookeeper client
     * @param reConnect 是否需要重连
     * @throws Exception 异常
     */
    private void connect2RemoteIncrStatusServer(final ITISCoordinator zookeeper, boolean reConnect, final AssembleSvcCompsiteCallback rpcCallback) {
        // 增量状态收集节点
        final String incrStateCollectAddress = ZkUtils.getFirstChildValue(zookeeper, ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH
                ,
                reConnect);
        connect2RemoteIncrStatusServer(incrStateCollectAddress, rpcCallback);
    }

    private ITISRpcService connect2RemoteIncrStatusServer(String incrStateCollectAddress, AssembleSvcCompsiteCallback rpcCallback) {
        InetSocketAddress address;
        Matcher matcher = ADDRESS_PATTERN.matcher(incrStateCollectAddress);
        if (matcher.matches()) {
            address = new InetSocketAddress(matcher.group(1), Integer.parseInt(matcher.group(2)));
        } else {
            // setDoReport(false);
            throw new IllegalStateException("incrStatusRpcServer:" + incrStateCollectAddress + " is not match the pattern:" + ADDRESS_PATTERN);
        }
        info("status server address:" + address);
        ITISRpcService oldRpc = rpcCallback.getOld();
        try {
            if (oldRpc != null) {
                // RPC.stopProxy(oldRpc);
                oldRpc.close();
            }
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(incrStateCollectAddress).usePlaintext().build();
            IncrStatusClient newRpc = new IncrStatusClient(channel);
            LogCollectorClient logCollectorClient = new LogCollectorClient(channel);
            DefaultLoggerAppenderClient loggerAppenderClient = new DefaultLoggerAppenderClient(channel);

            // IncrStatusUmbilicalProtocol newRpc = RPC.getProxy(IncrStatusUmbilicalProtocol.class, IncrStatusUmbilicalProtocol.versionID, address, new Configuration());
            info("successful connect to " + address + ",pingResult:" + newRpc.ping());
            return rpcCallback.process(oldRpc, new AssembleSvcCompsite(newRpc, logCollectorClient, loggerAppenderClient) {
                @Override
                public void close() {
                    try {
                        channel.shutdownNow().awaitTermination(2, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            error(e.getMessage(), e);
            // setDoReport(false);
            rpcCallback.errorOccur(oldRpc, e);
        }
        return null;
    }

    private static final ExecutorService reConnectSchedule = Executors.newSingleThreadExecutor();


    public static RpcServiceReference getMockStub() {
        final AtomicReference<ITISRpcService> ref = new AtomicReference<>();
        ref.set(AssembleSvcCompsite.MOCK_PRC);
        return new RpcServiceReference(ref, AssembleSvcCompsite.MOCK_PRC, () -> {
        });
    }

    /**
     * 连接到Assemble服务器
     *
     * @param
     * @throws Exception
     */
    private RpcServiceReference connect2RemoteIncrStatusServer(
            ITISCoordinator zookeeper, AdapterAssembleSvcCompsiteCallback... callbacks) throws Exception {
        RpcServiceReference svcRef = getMockStub();
        if (!zookeeper.shallConnect2RemoteIncrStatusServer()) {
            logger.warn("shallConnect2RemoteIncrStatusServer= false");
            return svcRef;
        }
        TryConnection connect = new TryConnection(zookeeper, svcRef.getRef(), callbacks);
        connect.run();
        startConnect2RPC(connect);
        return new RpcServiceReference(svcRef.getRef(), AssembleSvcCompsite.MOCK_PRC, connect);
    }

    public static class TryConnection implements Runnable {
        private ITISCoordinator zookeeper;
        private AtomicReference<ITISRpcService> ref;
        AtomicBoolean successConnected = new AtomicBoolean(false);
        AdapterAssembleSvcCompsiteCallback[] callbacks;

        private final ReentrantLock tryConnectLock = new ReentrantLock();

        public TryConnection(ITISCoordinator zookeeper, AtomicReference<ITISRpcService> ref, AdapterAssembleSvcCompsiteCallback[] callbacks) {
            this.zookeeper = zookeeper;
            this.ref = ref;
            this.callbacks = callbacks;
        }

        @Override
        public void run() {

            if (tryConnectLock.tryLock()) {
                try {
                    StatusRpcClientFactory statusRpcClient = new StatusRpcClientFactory();
                    statusRpcClient.connect2RemoteIncrStatusServer(zookeeper, true, /* reConnect */
                            new AssembleSvcCompsiteCallback() {
                                @Override
                                public ITISRpcService process(ITISRpcService oldrpc, final ITISRpcService newrpc) {
//                                    final ITISRpcService wrapperService = (ITISRpcService) Proxy.newProxyInstance(
//                                            this.getClass().getClassLoader(), new Class[]{ITISRpcService.class}
//                                            , new InvocationHandler() {
//                                                @Override
//                                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//
//                                                    try {
//                                                        Object result = method.invoke(newrpc, args);
//                                                        return result;
//                                                    } catch (StatusRuntimeException e) {
//                                                        synchronized (TryConnection.this) {
//                                                            TryConnection.this.notifyAll();
//                                                        }
//                                                    }
//
//                                                }
//                                            });

                                    ref.compareAndSet(oldrpc, newrpc);
                                    successConnected.set(true);
                                    for (AdapterAssembleSvcCompsiteCallback c : callbacks) {
                                        c.process(oldrpc, newrpc);
                                    }
                                    return newrpc;
                                }

                                @Override
                                public ITISRpcService getOld() {
                                    return ref.get().unwrap();
                                }

                                @Override
                                public void errorOccur(ITISRpcService oldrpc, Exception e) {
                                    ref.compareAndSet(oldrpc, AssembleSvcCompsite.MOCK_PRC);
                                    successConnected.set(false);
                                }
                            });
                } finally {
                    tryConnectLock.unlock();
                }
            }
        }
    }

    private static void startConnect2RPC(TryConnection connect) {

        AtomicInteger tryCount = new AtomicInteger();
        reConnectSchedule.submit(() -> {

            while (true) {
                logger.info("start reconnect rpc server,tryCount:" + tryCount.incrementAndGet());
                //  connect.tryConnectLock.tryLock();
                synchronized (connect) {
                    if (connect.successConnected.get()) {
                        //        connect.tryConnectLock.unlock();
                        connect.wait();
                        logger.info("reconnect process was notify,tryCount:" + tryCount.incrementAndGet());
                    }
                }

                connect.run();

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void main(String[] args) {
//        for (int i = 0; i < 3; i++) {
//            reConnectSchedule.schedule(() -> {
//                // logger.info("start reconnect rpc server");
//
//                System.out.println("start reconnect rpc server");
//            }, 5, TimeUnit.SECONDS);
//        }
//
//
//        System.out.println("============>");
    }

    public interface AssembleSvcCompsiteCallback {

        public ITISRpcService process(ITISRpcService oldrpc, ITISRpcService newrpc);

        public ITISRpcService getOld();

        // 当错误发生
        public void errorOccur(ITISRpcService oldrpc, Exception e);
    }

    /**
     * 将Assemble节点上的几个服务节点作一个组合，合并用一个端口
     */
    public abstract static class AssembleSvcCompsite implements ITISRpcService {

        public static RpcServiceReference statusRpc;// = new AtomicReference<>();


        private final IncrStatusUmbilicalProtocol statReceiveSvc;
        private final ILoggerAppenderClient loggerAppenderClient;

        // 汇总状态之后供，console节点来访问用
        private final ILogReporter statReportSvc;

        public static final AssembleSvcCompsite MOCK_PRC
                = new AssembleSvcCompsite(new MockIncrStatusUmbilicalProtocol(), new MockLogReporter(), new ILoggerAppenderClient() {
            @Override
            public void append(Map<String, String> headers, LogLevel level, String body) {
                // Map<String, String> headers = event.getHeadersMap();
                logger.warn("rpc msg write to mock instance,taskId:" + headers.get(JobParams.KEY_TASK_ID));
            }
        }) {
            @Override
            public void close() {
            }

            @Override
            public AssembleSvcCompsite unwrap() {
                return this;
            }
        };
        static {
            AtomicReference<ITISRpcService> ref = new AtomicReference<>();
            ref.set(StatusRpcClientFactory.AssembleSvcCompsite.MOCK_PRC);
            statusRpc = new RpcServiceReference(ref, StatusRpcClientFactory.AssembleSvcCompsite.MOCK_PRC, () -> {
            });
        }
        public AssembleSvcCompsite(IncrStatusUmbilicalProtocol statReceiveSvc, ILogReporter statReportSvc, ILoggerAppenderClient loggerAppenderClient) {
            Objects.requireNonNull(statReceiveSvc, "param statReceiveSvc can not be null");
            Objects.requireNonNull(statReportSvc, "param statReportSvc can not be null");
            Objects.requireNonNull(loggerAppenderClient, "param loggerAppenderClient can not be null");
            this.statReceiveSvc = statReceiveSvc;
            this.statReportSvc = statReportSvc;
            this.loggerAppenderClient = loggerAppenderClient;
        }

        @Override
        public AssembleSvcCompsite unwrap() {
            return this;
        }

        // 各个子节点汇报状态用


        public abstract void close();


        @Override
        public PhaseStatusCollection loadPhaseStatusFromLatest(Integer taskId) {
            return invokeGrpc(() -> {
                return statReceiveSvc.loadPhaseStatusFromLatest(taskId);
            });
        }

        /**
         * 重新抛出GrpcConnectionException，在RpcServiceReference中接收到之后，可以尝试重连
         *
         * @param supplier
         * @param <T>
         * @return
         */
        private <T> T invokeGrpc(Supplier<T> supplier) {
            try {
                return supplier.get();
            } catch (StatusRuntimeException e) {
                throw new GrpcConnectionException(e);
            }
        }

        /**
         * 分布式写日志
         */
        @Override
        public void append(Map<String, String> headers, LogLevel level, String body) {
            invokeGrpc(() -> {
                loggerAppenderClient.append(headers, level, body);
                return null;
            });
        }

        @Override
        public final void appendLog(LogLevel level, Integer taskId, Optional<String> appName, String message) {
            // LoggingEvent.Builder evtBuilder = LoggingEvent.newBuilder();
//            evtBuilder.setLevel(level);
//            evtBuilder.setBody(message);
            Map<String, String> headers = Maps.newHashMap();
            headers.put(JobParams.KEY_TASK_ID, String.valueOf(taskId));
            headers.put(JobParams.KEY_COLLECTION, appName.orElse("unknow"));
            headers.put("logtype", "fullbuild");
            // evtBuilder.putAllHeaders(headers);
            this.append(headers, level, message);
        }

        @Override
        public StreamObserver<PMonotorTarget> registerMonitorEvent(ILogListener logListener) {
            return invokeGrpc(() -> {
                return statReportSvc.registerMonitorEvent(logListener);
            });

        }

        @Override
        public java.util.Iterator<com.qlangtech.tis.rpc.grpc.log.stream.PPhaseStatusCollection>
        buildPhraseStatus(Integer taskid) {
            return invokeGrpc(() -> {
                return statReportSvc.buildPhraseStatus(taskid);
            });
        }

        public PingResult ping() {
            return invokeGrpc(() -> {
                return statReceiveSvc.ping();
            });
        }

        public MasterJob reportStatus(UpdateCounterMap upateCounter) {
            return invokeGrpc(() -> {
                return statReceiveSvc.reportStatus(upateCounter);
            });
        }

        public void reportJoinStatus(Integer taskId, JoinPhaseStatus.JoinTaskStatus joinStatus) {
            invokeGrpc(() -> {
                this.statReceiveSvc.reportJoinStatus(taskId, joinStatus);
                return null;
            });
        }

        public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
            invokeGrpc(() -> {
                statReceiveSvc.nodeLaunchReport(launchReportInfo);
                return null;
            });
        }

        public void reportDumpTableStatus(TableDumpStatus tableDumpStatus) {
            invokeGrpc(() -> {
                statReceiveSvc.reportDumpTableStatus(tableDumpStatus);
                return null;
            });
        }

        public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus) {
            invokeGrpc(() -> {
                statReceiveSvc.reportBuildIndexStatus(buildStatus);
                return null;
            });
        }

        public void initSynJob(PhaseStatusCollection buildStatus) {
            invokeGrpc(() -> {
                statReceiveSvc.initSynJob(buildStatus);
                return null;
            });
        }
    }

    private static void info(String msg) {
        System.out.println(msg);
    }

    private static void error(String msg, Throwable e) {
        info("err:" + msg);
        if (e != null) {
            info(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static class NoopStreamObserver<V> implements StreamObserver<V> {

        @Override
        public void onNext(V value) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onCompleted() {
        }
    }

    public static class MockLogReporter implements ILogReporter {

        @Override
        public StreamObserver<PMonotorTarget> registerMonitorEvent(ILogListener logListener) {
            return new NoopStreamObserver<>();
        }

        @Override
        public java.util.Iterator<PPhaseStatusCollection> buildPhraseStatus(Integer taskid) {
            return Iterators.forArray();
        }
    }

    private static class MockIncrStatusUmbilicalProtocol implements IncrStatusUmbilicalProtocol, Closeable {

        @Override
        public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
        }

        @Override
        public PhaseStatusCollection loadPhaseStatusFromLatest(Integer taskId) {
            return null;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void initSynJob(PhaseStatusCollection buildStatus) {

        }

        @Override
        public void reportJoinStatus(Integer taskId, JoinPhaseStatus.JoinTaskStatus joinTaskStatus) {

        }

        @Override
        public PingResult ping() {
            PingResult pingResult = new PingResult();
            pingResult.setValue(pingResult.getValue() + " from mock");
            return pingResult;
        }

        @Override
        public MasterJob reportStatus(UpdateCounterMap upateCounter) {
            logger.warn("stat report server has not connect on!!!! using Mock channel");
            return null;
        }

        @Override
        public void reportDumpTableStatus(TableDumpStatus tableDumpStatus) {
        }

        @Override
        public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus) {
        }
    }
}
