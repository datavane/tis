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
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IOpenShiftWatchListener;
import com.openshift.restclient.IWatcher;
import com.openshift.restclient.NotFoundException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.IStoppable;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync.IPodLogListener;
import com.openshift.restclient.capability.resources.IPodLogRetrievalAsync.Options;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IReplicationController;
import com.openshift.restclient.model.IResource;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.IncrInitSpeAction;
import com.qlangtech.tis.runtime.module.action.IncrUtils;
import com.qlangtech.tis.runtime.module.action.IncrUtils.BuildStatus;
import com.qlangtech.tis.runtime.module.action.IncrUtils.DeploymentStatus;
import com.qlangtech.tis.runtime.module.action.IncrUtils.Status;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.MonotorTarget;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.PayloadMonitorTarget;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.RegisterMonotorTarget;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import com.qlangtech.tis.trigger.socket.LogType;

/*
 * 实时接收集群中其他服务节点反馈过来的日志信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LogFeedbackServlet extends WebSocketServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogFeedbackServlet.class);

    private static final long serialVersionUID = 1L;

    private static final LogCollectorClientManager logCollector = LogCollectorClientManager.getInstance();

    // @Override
    // public void init() throws ServletException {
    // super.init();
    // }
    private static IClient ocClient;

    @Override
    public void configure(WebSocketServletFactory factory) {
        // set a 10 second timeout
        factory.getPolicy().setIdleTimeout(240000);
        factory.getPolicy().setAsyncWriteTimeout(-1);
        factory.register(LogFeedbackSocket.class);
        ocClient = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()).getBean("ocClient", IClient.class);
        if (ocClient == null) {
            throw new IllegalStateException("occlient can not be null");
        }
    }

    public static class LogFeedbackSocket extends WebSocketAdapter implements LogCollectorClientManager.ILogListener {

        private String collectionName;

        // private Connection _connection;
        // private final MonotorTarget monitorTarget;
        private final Set<MonotorTarget> monitorSet = Collections.synchronizedSet(new HashSet<>());

        private final Set<LogType> logtypes = new HashSet<>();

        static final Joiner joiner = Joiner.on(",");

        final ExecutorService execService = Executors.newCachedThreadPool();

        private final List<IncrBuildMonitor> incrBuildMonitors = Lists.newArrayList();

        private final List<IncrDeployMonitor> incrDeployMonitors = Lists.newArrayList();

        // private Set<PayloadMonitorTarget> incrBuildMonitor = new HashSet<>();
        @Override
        public synchronized void read(ExecuteState<?> event) {
            // 向浏览器中发送消息
            try {
                if (this.isConnected() && StringUtils.equals(event.getCollectionName(), collectionName) && this.logtypes.contains(event.getLogType())) {
                    // _connection.sendMessage(event.serializeJSON());
                    this.getRemote().sendString(event.serializeJSON());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public Set<MonotorTarget> getMonitorTypes() {
            return this.monitorSet;
        }

        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            this.collectionName = getParameter("collection");
            List<RegisterMonotorTarget> typies = parseLogTypes(this.getParameter("logtype"));
            // LogSocket realtimeReportSocket = null;
            // 这个消息是不需要客户端来注册的，所以事先先加入
            this.logtypes.add(LogType.INCR_BUILD_STATUS_CHANGE);
            // LogCollectorClientManager.MonotorTarget monitorTarget = null;
            for (RegisterMonotorTarget monitor : typies) {
                // monitorTarget =
                // LogCollectorClientManager.MonotorTarget.createRegister(collection, type);
                this.addMonitor(monitor);
            // if (realtimeReportSocket == null) {
            // realtimeReportSocket = new LogSocket(monitorTarget);
            // } else {
            // realtimeReportSocket.addMonitor(monitorTarget);
            // }
            }
            logCollector.registerListener(this);
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            logger.info(this.collectionName + ": websocket " + joiner.join(monitorSet.stream().map((r) -> r.getLogType().getValue()).iterator()) + " is going to stop listen,statusCode:" + statusCode + ",reason:" + reason);
            logCollector.unregisterListener(this);
            // closeAllMonitor();
            incrBuildMonitors.forEach((r) -> r.stop());
            incrDeployMonitors.forEach((r) -> r.stop());
            execService.shutdownNow();
            try {
                execService.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }

        // private void closeAllMonitor() {
        // 
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // 
        // }
        // 
        // // watchers.clear();
        // // stoppableList.clear();
        // }
        // @Override
        // public void onClose(int closeCode, String message) {
        // logger.info(monitorTarget.getCollection() + ":" + monitorTarget.getLogType()
        // + " is going to stop listen");
        // logCollector.unregisterListener(this);
        // }
        private void addMonitor(MonotorTarget monitorTarget) {
            if (RunEnvironment.getSysRuntime() != RunEnvironment.DAILY && monitorTarget.getLogType() == LogType.INCR_SEND) {
                // 线上环境不提供详细日志发送
                return;
            }
           
            try {
                this.logtypes.add(monitorTarget.getLogType());
                if (monitorTarget.getLogType() == LogType.INCR_BUILD) {
                    PayloadMonitorTarget buildMonitor = (PayloadMonitorTarget) monitorTarget;
                    if (buildMonitor.isInitShow()) {
                        BuildStatus bstatus = IncrUtils.readLastBuildRecordStatus(buildMonitor.getCollection());
                        if (bstatus.getStatus() == Status.FAILD || bstatus.getStatus() == Status.CREATED) {
                            buildMonitor = MonotorTarget.createPayloadMonitor(monitorTarget.getCollection(), bstatus.getBuildName(), LogType.INCR_BUILD);
                        } else {
                            return;
                        }
                    }
                    PayloadMonitorTarget indexbuildMonitor = (PayloadMonitorTarget) monitorTarget;
                    synchronized (monitorSet) {
                        if (monitorSet.add(indexbuildMonitor)) {
                            incrBuildMonitors.forEach((r) -> r.stop());
                            IncrBuildMonitor monitor = new IncrBuildMonitor(indexbuildMonitor);
                            incrBuildMonitors.add(monitor);
                            execService.execute(monitor);
                        // monitorSet.add(indexbuildMonitor);
                        }
                    }
                } else if (monitorTarget.getLogType() == LogType.INCR_DEPLOY_STATUS_CHANGE) {
                    PayloadMonitorTarget buildMonitor = (PayloadMonitorTarget) monitorTarget;
                    synchronized (monitorSet) {
                        if (monitorSet.add(buildMonitor)) {
                            IncrDeployMonitor deployMonitor = new IncrDeployMonitor(buildMonitor);
                            incrDeployMonitors.forEach((r) -> r.stop());
                            execService.execute(deployMonitor);
                            incrDeployMonitors.add(deployMonitor);
                        // monitorSet.add(buildMonitor);
                        }
                    }
                } else {
                    synchronized (monitorSet) {
                        if (monitorSet.add(monitorTarget)) {
                         //   logCollector.registerMonitorEvent(monitorTarget);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String getParameter(String key) {
            for (String v : this.getSession().getUpgradeRequest().getParameterMap().get(key)) {
                return v;
            }
            return null;
        }

        private class IncrDeployMonitor implements Runnable {

            private final PayloadMonitorTarget incrDeployMonitor;

            private final int version;

            private List<IWatcher> watches = Lists.newArrayList();

            IncrDeployMonitor(PayloadMonitorTarget incrDeployMonitor) {
                this.incrDeployMonitor = incrDeployMonitor;
                this.version = Integer.parseInt(incrDeployMonitor.getPayLoad());
            }

            public void stop() {
                watches.forEach((e) -> e.stop());
            }

            @Override
            public void run() {
                DeploymentStatus deployment = new DeploymentStatus(StringUtils.lowerCase(incrDeployMonitor.getCollection()));
                deployment.setVersion(this.version);
                // IncrInitSpeAction.ocClient.get(ResourceKind.REPLICATION_CONTROLLER,
                // deployment.getReplicationControllerName(), IncrInitSpeAction.NAME_SPACE);
                final int[] prestat = new int[1];
                IWatcher watcher = ocClient.watch(IncrInitSpeAction.NAME_SPACE, new IOpenShiftWatchListener() {

                    @Override
                    public void connected(List<IResource> resources) {
                    }

                    @Override
                    public void disconnected() {
                    }

                    @Override
                    public void received(IResource resource, ChangeType change) {
                        final IReplicationController control = (IReplicationController) resource;
                        if (StringUtils.equals(resource.getName(), deployment.getReplicationControllerName())) {
                            Status stat = getDeployStatus(control);
                            int expect = control.getDesiredReplicaCount();
                            int now = control.getCurrentReplicaCount();
                            if (expect > 0 && prestat[0] != now) {
                                prestat[0] = now;
                                ExecuteState<DeploymentStatus> event = ExecuteState.create(LogType.INCR_DEPLOY_STATUS_CHANGE, IncrUtils.appendDeploymentRecord(deployment.getCollection(), stat, deployment.getVersion(), now, (expect == now), deployment.getVersion()));
                                event.setServiceName(incrDeployMonitor.getCollection());
                                read(event);
                            }
                        }
                    }

                    @Override
                    public void error(Throwable err) {
                    }
                }, ResourceKind.REPLICATION_CONTROLLER);
                watches.add(watcher);
            }
        }

        private class IncrBuildMonitor implements Runnable {

            private final PayloadMonitorTarget indexbuildMonitor;

            private boolean stoped = false;

            private final List<IWatcher> watchers = Lists.newArrayList();

            private final List<IStoppable> stoppableList = Lists.newArrayList();

            public IncrBuildMonitor(PayloadMonitorTarget indexbuildMonitor) {
                super();
                this.indexbuildMonitor = indexbuildMonitor;
            }

            public void stop() {
                // logger.info("i was killed");
                this.stoped = true;
                for (IWatcher w : watchers) {
                    try {
                        w.stop();
                    } catch (Throwable e) {
                    }
                }
                for (IStoppable s : stoppableList) {
                    try {
                        s.stop();
                    } catch (Throwable e) {
                    }
                }
            }

            @Override
            public void run() {
                // IndexBuildMonitorTarget indexbuildMonitor = (IndexBuildMonitorTarget)
                // monitorTarget;
                final String collection = indexbuildMonitor.getCollection();
                final String resourceName = StringUtils.lowerCase(indexbuildMonitor.getCollection());
                String buildName = indexbuildMonitor.getPayLoad();
                BuildStatus bstatus = IncrUtils.readLastBuildRecordStatus(resourceName);
                if (indexbuildMonitor.isInitShow()) {
                    if (bstatus.getStatus() != Status.CREATED) {
                        return;
                    }
                    buildName = bstatus.getBuildName();
                }
                final String bname = buildName;
                IBuild build = ocClient.get(ResourceKind.BUILD, buildName, IncrInitSpeAction.NAME_SPACE);
                final String[] prestat = new String[1];
                // closeAllMonitor();
                if (!IncrInitSpeAction.isFinished(build)) {
                    final CountDownLatch countDown = new CountDownLatch(1);
                    IWatcher w = ocClient.watch(IncrInitSpeAction.NAME_SPACE, new IOpenShiftWatchListener() {

                        @Override
                        public void connected(List<IResource> resources) {
                        }

                        @Override
                        public void disconnected() {
                        }

                        @Override
                        public void received(IResource resource, ChangeType change) {
                            IBuild build = (IBuild) resource;
                            if (StringUtils.equals(build.getName(), bname)) {
                                logger.info(build.getName() + "," + build.getStatus());
                                if (!isInitStatus(build)) {
                                    if (!StringUtils.equals(build.getStatus(), prestat[0])) {
                                        if (!IncrInitSpeAction.RUNNING.equals(build.getStatus())) {
                                            stop();
                                        }
                                        prestat[0] = build.getStatus();
                                        countDown.countDown();
                                        BuildStatus buildStatus = IncrInitSpeAction.createNewBuildStatus(build, resourceName);
                                        ExecuteState<BuildStatus> event = ExecuteState.create(LogType.INCR_BUILD_STATUS_CHANGE, buildStatus);
                                        event.setServiceName(collection);
                                        read(event);
                                    }
                                }
                            }
                        }

                        @Override
                        public void error(Throwable err) {
                        }
                    }, ResourceKind.BUILD);
                    watchers.add(w);
                    if (!isInitStatus(build)) {
                        countDown.countDown();
                    } else {
                        try {
                            countDown.await(60, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            // 现成被终止
                            return;
                        }
                    }
                }
                // build.accept(visitor, unsupportedCapabililityValue);
                final String podName = build.getName() + "-build";
                IPod pod = getPod(podName, 0);
                if (pod == null) {
                    return;
                }
                final String container = pod.getContainers().iterator().next().getName();
                logger.info("start fetch {} logs ", buildName);
                printAsyncLog(pod, container, 0, collection);
            // IWatcher w = IncrInitSpeAction.ocClient.watch(IncrInitSpeAction.NAME_SPACE,
            // new IOpenShiftWatchListener() {
            // 
            // @Override
            // public void connected(List<IResource> resources) {
            // latch.countDown();
            // }
            // 
            // @Override
            // public void disconnected() {
            // latch.countDown();
            // }
            // 
            // @Override
            // public void received(IResource resource, ChangeType change) {
            // 
            // IEvent event = (IEvent) resource;
            // if (StringUtils.startsWith(resource.getName(), build.getName())) {
            // // System.out.println(change.getValue() + "," + resource.getName() + "," +
            // // event.getReason() + ","
            // // + event.getMessage() + "," + event.getType() + ",first:" +
            // // event.getFirstSeenTimestamp()
            // // + ",last:" + event.getLastSeenTimestamp());
            // 
            // if ("BuildStarted".equalsIgnoreCase(event.getReason())) {
            // // System.out.println("start receive log============================");
            // printAsyncLog(pod, container, 0);
            // }
            // }
            // 
            // }
            // 
            // @Override
            // public void error(Throwable err) {
            // }
            // 
            // }, ResourceKind.EVENT);
            // try {
            // latch.await(10, TimeUnit.MINUTES);
            // } catch (InterruptedException e) {
            // logger.warn("pod monitor interrupted:{},container:{}", pod.getName(),
            // container);
            // }
            // w.stop();
            }

            private boolean isInitStatus(IBuild build) {
                return "Pending".equals(build.getStatus()) || "New".equals(build.getStatus());
            }

            private IPod getPod(String podName, int retryCount) {
                if (isInterrupted()) {
                    return null;
                }
                try {
                    return ocClient.get(ResourceKind.POD, podName, IncrInitSpeAction.NAME_SPACE);
                } catch (NotFoundException e) {
                    if (retryCount <= 200) {
                        try {
                            Thread.sleep(3000l);
                        } catch (InterruptedException e1) {
                            throw new RuntimeException("Interrupted", e1);
                        }
                        logger.info("get build pod,retry" + retryCount);
                        return getPod(podName, retryCount + 1);
                    } else {
                        throw e;
                    }
                }
            }

            protected void printAsyncLog(IPod pod, final String container, int retry, String indexName) {
                if (isInterrupted()) {
                    return;
                }
                final CountDownLatch latch = new CountDownLatch(2);
                AtomicBoolean connected = new AtomicBoolean(false);
                // https://docs.openshift.com/online/rest_api/api/v1.Pod.html#Get-api-v1-namespaces-namespace-pods-name-log
                IStoppable stoppable = pod.accept(new CapabilityVisitor<IPodLogRetrievalAsync, IStoppable>() {

                    @Override
                    public IStoppable visit(IPodLogRetrievalAsync capability) {
                        return capability.start(new IPodLogListener() {

                            @Override
                            public void onClose(int arg0, String arg1) {
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                latch.countDown();
                                latch.countDown();
                                connected.set(false);
                                logger.warn("retry{},podname:{},err:{}", retry, pod.getName(), t.getMessage());
                            }

                            @Override
                            public void onMessage(String message) {
                                connected.set(true);
                                ExecuteState<String> event = ExecuteState.create(LogType.INCR_BUILD, StringUtils.replaceOnce(message, "\n", StringUtils.EMPTY));
                                event.setServiceName(indexName);
                                read(event);
                            }

                            @Override
                            public void onOpen() {
                                latch.countDown();
                            }
                        }, new Options().follow().container(container).parameter("tailLines", "50"));
                    }
                }, null);
                stoppableList.add(stoppable);
                try {
                    latch.await(10, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    logger.warn("pod monitor interrupted:{}", pod.getName());
                    return;
                }
                stoppable.stop();
                if (!connected.get() && retry <= 200) {
                    try {
                        Thread.sleep(2000l);
                    } catch (InterruptedException e1) {
                        return;
                    }
                    logger.warn("retry{},podname:{}", retry, pod.getName());
                    printAsyncLog(pod, container, retry + 1, indexName);
                }
            }

            private boolean isInterrupted() {
                if (stoped || Thread.interrupted()) {
                    logger.info("this thread is stop");
                    return true;
                }
                return false;
            }
        }

        /**
         * 需要监听的实体的格式 “full”,“incrbuild:search4totalpay-1”
         *
         * @param logstype
         * @return
         */
        private List<RegisterMonotorTarget> parseLogTypes(String logstype) {
            List<RegisterMonotorTarget> types = new ArrayList<>();
            for (String t : StringUtils.split(logstype, ",")) {
                String[] arg = null;
                if (StringUtils.indexOf(t, ":") > 0) {
                    arg = StringUtils.split(t, ":");
                    if (arg.length != 2) {
                        throw new IllegalArgumentException("arg:" + t + " is not illegal");
                    }
                    types.add(LogCollectorClientManager.MonotorTarget.createPayloadMonitor(this.collectionName, arg[1], LogType.parse(arg[0])));
                } else {
                    types.add(LogCollectorClientManager.MonotorTarget.createRegister(this.collectionName, LogType.parse(t)));
                }
            }
            return types;
        }

        @Override
        public void onWebSocketText(String data) {
            if (isConnected()) {
                // 接收到 客户端发送过来的订阅消息
                // MonotorTarget monitorTarget = //
                // MonotorTarget.createRegister(this.collectionName, LogType.parse(data));
                parseLogTypes(data).forEach((t) -> {
                    this.addMonitor(t);
                });
            }
        }
    }

    public static Status getDeployStatus(IReplicationController control) {
        int expect = control.getDesiredReplicaCount();
        int now = control.getCurrentReplicaCount();
        Status stat = (expect > 0 && expect == now) ? Status.SUCCESS : Status.CREATED;
        return stat;
    }
}
