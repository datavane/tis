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
package com.qlangtech.tis.realtime.yarn;

import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.realtime.transfer.*;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.realtime.yarn.rpc.ConsumeDataKeeper;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.tis.hadoop.rpc.AdapterAssembleSvcCompsiteCallback;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 增量转发节点执行内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月5日
 */
public class TransferIncrContainer extends BasicTransferTool {

    private static final Logger logger = LoggerFactory.getLogger(TransferIncrContainer.class);

    private final String collection;

    private final String hostName;

    // 反馈执行状态RPC
    private RpcServiceReference statusReporter;

    // private String collectionNames;
    private IncrStatusReportWorker statusReportWorker;


    /**
     * @param collection
     * @param classLoader
     */
    public TransferIncrContainer(String collection, long timestamp, URLClassLoader classLoader) {
        super(classLoader, timestamp);
        this.collection = collection;
        this.hostName = NetUtils.getHost();
    }

    public void start() throws Exception {
        this.info("exec collection:" + this.collection);
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
            this.hostName = NetUtils.getHost();
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
