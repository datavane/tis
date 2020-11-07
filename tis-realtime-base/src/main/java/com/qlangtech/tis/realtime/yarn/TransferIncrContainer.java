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
package com.qlangtech.tis.realtime.yarn;

import com.google.common.base.Joiner;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.realtime.transfer.*;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.realtime.yarn.rpc.ConsumeDataKeeper;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.tis.hadoop.rpc.AdapterAssembleSvcCompsiteCallback;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 增量转发节点执行内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月5日
 */
public class TransferIncrContainer extends BasicTransferTool {

    private static final Logger logger = LoggerFactory.getLogger(TransferIncrContainer.class);

    // private static final String logFlumeAddress;
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.+?):(\\d+)$");

    // private static final ScheduledExecutorService falconSendScheduler =
    // Executors.newScheduledThreadPool(1);
    // private TransferStatusMBean mbean;
    private static final Joiner joinerWith = Joiner.on("_").skipNulls();

    private final String collection;

    private final String hostName;

    // private static final Logger logger =
    // LoggerFactory.getLogger(TransferIncrContainer.class);
    // 反馈执行状态RPC
    private AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusReporter;

    // private String collectionNames;
    private IncrStatusReportWorker statusReportWorker;

  //  private boolean launchStatusReport;

//    public TransferIncrContainer(String collection, long timestamp, URLClassLoader classLoader) {
//        this(collection, timestamp, classLoader);
//    }

    /**
     * @param collection
     * @param classLoader
     */
    public TransferIncrContainer(String collection, long timestamp, URLClassLoader classLoader) {
        super(classLoader, timestamp);
        this.collection = collection;
        this.hostName = NetUtils.getHostname();
    }

    public void start() throws Exception {
        this.info("exec collection:" + this.collection);
        // this.collectionNames = joinerWith
        // .join(Iterators.transform(execIndexs.iterator(), input -> StringUtils.replace(input, "search4", "s4")));
        // final ITISCoordinator coordinator = BasicONSListener.cloudCoordinator;
        /**
         * *********************************************************************************
         * 启动spring
         * **********************************************************************************
         */
        this.startService(this.collection);
        this.info("launch_rm_success");
        final List<IOnsListenerStatus> allChannels = getAllTransferChannel();
        ITISCoordinator coordinator = BasicRMListener.getCloudClient().getCoordinator();
        if (coordinator.shallConnect2RemoteIncrStatusServer()) {
            connect2RemoteIncrStatusServer(coordinator, true);
            TransferStatusMBean mbean = new TransferStatusMBean(this.getIndexNames(), allChannels);
            mbean.afterPropertiesSet();
            statusReportWorker = new IncrStatusReportWorker(allChannels, mbean.getIndexUUID());
            statusReportWorker.run();
        }
        this.info("launch transfer node success");
    }

    // /**
    // * 发送节点状态到小米Falcon
    // */
    // void sendStatus2Falcon(List<IOnsListenerStatus> allChannels) throws Exception {
    // 
    // long timestamp = System.currentTimeMillis() / 1000;
    // // 发送数据格式：https://book.open-falcon.org/zh/usage/data-push.html
    // long fullgc = GCJmxUtils.getFullGC();
    // long yonggc = GCJmxUtils.getYongGC();
    // JSONArray result = new JSONArray();
    // MonitorSysTagMarker tagMarker = null;
    // for (IOnsListenerStatus status : allChannels) {
    // 
    // for (Map.Entry<String, IIncreaseCounter> entry : status.getUpdateStatic()) {
    // if ((tagMarker = entry.getValue().getMonitorTagMarker()) != null
    // && tagMarker.shallCollectByMonitorSystem()) {
    // result.put(addMetric(hostName, timestamp, status.getCollectionName(), entry.getKey(),
    // entry.getValue().getAccumulation(), CounterType.COUNTER, entry.getValue()));
    // }
    // }
    // 
    // result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_buf_remain",
    // status.getBufferQueueRemainingCapacity(), CounterType.GAUGE));
    // result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_consume_err",
    // status.getConsumeErrorCount(), CounterType.COUNTER));
    // result.put(addMetric(hostName, timestamp, status.getCollectionName(), "tis_ignore",
    // status.getIgnoreRowsCount(), CounterType.COUNTER));
    // // ===========================================================
    // }
    // 
    // result.put(addMetric(hostName, timestamp, collectionNames, "incr_fullgc", fullgc, CounterType.COUNTER));
    // result.put(addMetric(hostName, timestamp, collectionNames, "incr_yonggc", yonggc, CounterType.COUNTER));
    // 
    // String content = result.toString();
    // // System.out.println(content);
    // 
    // HttpUtils.post(new URL("http://127.0.0.1:1988/v1/push"), content.getBytes(Charset.forName("utf8")),
    // new PostFormStreamProcess<Object>() {
    // 
    // @Override
    // public ContentType getContentType() {
    // 
    // return ContentType.TEXT_HTML;
    // }
    // 
    // @Override
    // public Object p(int status, InputStream stream, String md5) {
    // // try {
    // // System.out.println(IOUtils.toString(stream, "utf8"));
    // // } catch (IOException e) {
    // //
    // // }
    // 
    // return null;
    // }
    // });
    // }
    private void info(String msg) {
        System.out.println(msg);
    }

    private void error(String msg, Throwable e) {
        info("err:" + msg);
        if (e != null) {
            info(ExceptionUtils.getFullStackTrace(e));
        }
    }

    // 是否需要继续把状态汇报给assemble节点
    private void setDoReport(boolean doReport) {
        if (statusReportWorker != null) {
            statusReportWorker.setDoReport(doReport);
        }
    }

    // static String[] keys = new String[] { "order", "info", "tab2", "tab3",
    // IIncreaseCounter.SOLR_CONSUME_COUNT };
    /**
     * 连接日志收集节点地址
     *
     * @param coordinator
     * @param reConnect   是否需要重连
     * @throws Exception 异常
     */
    private void connect2RemoteIncrStatusServer(final ITISCoordinator coordinator, boolean reConnect) throws Exception {
        this.statusReporter = StatusRpcClient.getService(coordinator, new AdapterAssembleSvcCompsiteCallback() {

            @Override
            public StatusRpcClient.AssembleSvcCompsite process(StatusRpcClient.AssembleSvcCompsite oldrpc, StatusRpcClient.AssembleSvcCompsite newrpc) {
                nodeLaunchReport(newrpc);
                return null;
            }
        });
        // coordinator.addOnReconnect(() -> {
        // nodeLaunchReport(this.statusReporter.get());
        // });
        setDoReport(true);
    }

    private void nodeLaunchReport(StatusRpcClient.AssembleSvcCompsite rpcClient) {
        logger.info("report collectionFocusTopicInfo to centerNode,size:" + this.collectionFocusTopicInfo.entrySet().stream().map((e) -> e.getKey() + ":[" + e.getValue().getTopicWithTags().entrySet().stream().map((tentry) -> tentry.getKey() + ":[" + tentry.getValue().stream().collect(Collectors.joining(",")) + "]").collect(Collectors.joining(",")) + "]").collect(Collectors.joining("\r\n")));
        rpcClient.ping();
        rpcClient.nodeLaunchReport(new LaunchReportInfo(this.collectionFocusTopicInfo));
    }

    private class IncrStatusReportWorker extends BasicIncrStatusReport {

        private final String hostName;

        private final Map<String, String> indexUUID;

        private boolean doReport = true;

        IncrStatusReportWorker(Collection<IOnsListenerStatus> incrChannels, Map<String, String> indexUUID) {
            super(incrChannels);
            this.hostName = NetUtils.getHostname();
            this.indexUUID = indexUUID;
        }

        private void setDoReport(boolean doReport) {
            this.doReport = doReport;
        }

        @Override
        protected void processSnapshot() throws Exception {
            if (!doReport) {
                return;
            }
            UpdateCounterMap updateCounterMap = new UpdateCounterMap();
            updateCounterMap.setGcCounter(BasicRMListener.getGarbageCollectionCount());
            updateCounterMap.setFrom(hostName);
            long currentTimeInSec = ConsumeDataKeeper.getCurrentTimeInSec();
            updateCounterMap.setUpdateTime(currentTimeInSec);
            // 汇总一个节点中所有索引的增量信息
            for (IOnsListenerStatus l : incrChannels) {
                TableSingleDataIndexStatus tableUpdateCounter = new TableSingleDataIndexStatus();
                tableUpdateCounter.setBufferQueueRemainingCapacity(l.getBufferQueueRemainingCapacity());
                tableUpdateCounter.setBufferQueueUsedSize(l.getBufferQueueUsedSize());
                tableUpdateCounter.setConsumeErrorCount((int) l.getConsumeErrorCount());
                tableUpdateCounter.setIgnoreRowsCount((int) l.getIgnoreRowsCount());
                tableUpdateCounter.setUUID(this.indexUUID.get(l.getCollectionName()));
                tableUpdateCounter.setIncrProcessPaused(l.isPaused());
                tableUpdateCounter.setTis30sAvgRT(((BasicRMListener) l).getTis30sAvgRT());
                // 汇总一个索引中所有focus table的增量信息
                for (Map.Entry<String, IIncreaseCounter> entry : l.getUpdateStatic()) {
                    // IncrCounter tableIncrCounter = new
                    // IncrCounter((int)entry.getValue().getIncreasePastLast());
                    // tableIncrCounter.setAccumulationCount(entry.getValue().getAccumulation());
                    // tableUpdateCounter.put(entry.getKey(), tableIncrCounter);
                    // 只记录一个消费总量和当前时间
                    tableUpdateCounter.put(entry.getKey(), entry.getValue().getAccumulation());
                }
                tableUpdateCounter.put(IIncreaseCounter.TABLE_CONSUME_COUNT, ((BasicRMListener) l).getTableConsumeCount());
                updateCounterMap.addTableCounter(l.getCollectionName(), tableUpdateCounter);
            }
            StatusRpcClient.AssembleSvcCompsite remote;
            if ((remote = statusReporter.get()) != null) {
                MasterJob masterJob = remote.reportStatus(updateCounterMap);
                // 接收主节点发送回来的消息
                if (masterJob != null) {
                    for (IOnsListenerStatus l : incrChannels) {
                        if ((masterJob.isCollectionIncrProcessCommand(l.getCollectionName()))) {
                            // if ((masterJob.getJobType() == JobType.IndexJobRunning) && StringUtils.equals(l.getCollectionName(), masterJob.getIndexName())) {
                            if (masterJob.isStop()) {
                                l.pauseConsume();
                            } else {
                                l.resumeConsume();
                            }
                            info("index receive a command:" + masterJob.getJobType() + ",index:" + masterJob.getIndexName() + ",stop:" + masterJob.isStop());
                            return;
                        }
                    }
                    error("index receive a command can not match any listener:" + masterJob.getJobType() + ",index:" + masterJob.getIndexName() + ",stop:" + masterJob.isStop(), null);
                }
            }
        }
    }
}
