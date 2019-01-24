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
package com.qlangtech.tis.realtime.yarn.rpc.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RPC.Server;
import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import com.qlangtech.tis.realtime.transfer.TableMultiDataIndexStatus;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.yarn.rpc.ConsumeDataKeeper;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.JobType;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.PingResult;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import com.google.common.collect.Maps;

/*
 * 服务端接收客户端发送过来的日志消息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrStatusUmbilicalProtocolImpl implements IncrStatusUmbilicalProtocol {

    // private static final Logger logger = LoggerFactory
    // .getLogger(IncrStatusUmbilicalProtocolImpl.class);
    private final HashMap<String, ConcurrentHashMap<String, TableMultiDataIndexStatus>> // 
    updateCounterStatus = new HashMap<>();

    // 存储各个索引執行以来的topic及tags
    private final ConcurrentHashMap<String, TopicInfo> /* indexName */
    indexTopicInfo = new ConcurrentHashMap<>();

    private final BlockingQueue<MasterJob> jobQueue = new ArrayBlockingQueue<>(100);

    // 单位s
    private static final int JOB_EXPIRE_TIME = 30;

    private static final int TABLE_COUNT_GAP = 5;

    private static final String TABLE_CONSUME_COUNT = "tableConsumeCount";

    // 定时任务，打印日志
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static final Logger log = LoggerFactory.getLogger(IncrStatusUmbilicalProtocolImpl.class);

    private static final Logger statisLog = LoggerFactory.getLogger("statis");

    private static final ThreadLocal<SimpleDateFormat> formatYyyyMMddHHmmss = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Override
    public PingResult ping() {
        return new PingResult();
    }

    /**
     * 增量引擎是否开启中
     *
     * @param collection
     * @return
     */
    public boolean isIncrGoingOn(String collection) {
        if (!this.updateCounterStatus.containsKey(collection)) {
            return false;
        }
        ConcurrentHashMap<String, TableMultiDataIndexStatus> /* uuid发送过来的节点id */
        indexStatus = updateCounterStatus.get(collection);
        return (indexStatus.size() > 0);
    }

    public void resumeConsume(String indexName) {
        addJob(indexName, false);
    }

    public void pauseConsume(String indexName) {
        addJob(indexName, true);
    }

    private void addJob(String indexName, boolean isPaused) {
        if (!updateCounterStatus.containsKey(indexName)) {
            log.error(indexName + " doesn't not exist in assemble node");
            return;
        }
        for (String uuid : updateCounterStatus.get(indexName).keySet()) {
            MasterJob job = new MasterJob(JobType.IndexJobRunning, indexName, uuid);
            job.setStop(isPaused);
            this.sendJob2Worker(job);
        }
    }

    private void sendJob2Worker(MasterJob job) {
        try {
            jobQueue.put(job);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
        synchronized (indexTopicInfo) {
            for (Map.Entry<String, TopicInfo> /* collection */
            entry : launchReportInfo.getCollectionFocusTopicInfo().entrySet()) {
                this.indexTopicInfo.put(entry.getKey(), entry.getValue());
                log.info("collection:" + entry.getKey() + " topicfocuse:" + JSON.toJSONString(entry.getValue(), true));
            }
        }
    }

    private static final TopicInfo NULL_TOPIC_INFO = new TopicInfo();

    public TopicInfo getFocusTopicInfo(String collection) {
        return indexTopicInfo.getOrDefault(collection, NULL_TOPIC_INFO);
    // return indexTopicInfo.get(collection);
    }

    public Map<String, TableMultiDataIndexStatus> getIndexUpdateCounterStatus(String collection) {
        return updateCounterStatus.get(collection);
    }

    // TableSingleDataIndexStatus getIndexUpdateCounterStatus(String collection) {
    // return updateCounterStatus.get(collection);
    // // return updateCounterStatus.get(collection);
    // }
    void removeIndexUpdateCounterStatus(String collection) {
        this.updateCounterStatus.remove(collection);
    }

    /**
     */
    @Override
    public MasterJob reportStatus(UpdateCounterMap updateCounter) {
        String from = updateCounter.getFrom();
        long updateTime = updateCounter.getUpdateTime();
        for (Map.Entry<String, TableSingleDataIndexStatus> entry : updateCounter.getData().entrySet()) {
            String indexName = entry.getKey();
            TableSingleDataIndexStatus updateCounterFromClient = entry.getValue();
            String uuid = updateCounterFromClient.getUUID();
            ConcurrentHashMap<String, TableMultiDataIndexStatus> indexStatus = updateCounterStatus.get(indexName);
            if (indexStatus == null) {
                synchronized (updateCounterStatus) {
                    indexStatus = updateCounterStatus.computeIfAbsent(indexName, k -> new ConcurrentHashMap<>());
                }
            }
            TableMultiDataIndexStatus tableMultiDataIndexStatus = indexStatus.get(uuid);
            if (tableMultiDataIndexStatus == null) {
                tableMultiDataIndexStatus = indexStatus.computeIfAbsent(uuid, k -> new TableMultiDataIndexStatus());
            }
            tableMultiDataIndexStatus.setBufferQueueRemainingCapacity(updateCounterFromClient.getBufferQueueRemainingCapacity());
            tableMultiDataIndexStatus.setConsumeErrorCount(updateCounterFromClient.getConsumeErrorCount());
            tableMultiDataIndexStatus.setIgnoreRowsCount(updateCounterFromClient.getIgnoreRowsCount());
            tableMultiDataIndexStatus.setUUID(updateCounterFromClient.getUUID());
            tableMultiDataIndexStatus.setFromAddress(from);
            tableMultiDataIndexStatus.setUpdateTime(updateTime);
            tableMultiDataIndexStatus.setTis30sAvgRT(updateCounterFromClient.getTis30sAvgRT());
            for (Map.Entry<String, Long> tabUpdate : entry.getValue().getTableConsumeData().entrySet()) {
                tableMultiDataIndexStatus.put(tabUpdate.getKey(), new ConsumeDataKeeper(tabUpdate.getValue(), updateTime));
            }
        }
        return pollJob(updateCounter);
    }

    public void startLogging() {
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    long currentTimeInSec = ConsumeDataKeeper.getCurrentTimeInSec();
                    for (String indexName : updateCounterStatus.keySet()) {
                        ConcurrentHashMap<String, TableMultiDataIndexStatus> /* uuid发送过来的节点id */
                        indexStatus = updateCounterStatus.get(indexName);
                        indexStatus.entrySet().removeIf(entry -> {
                            synchronized (indexTopicInfo) {
                                boolean expire = entry.getValue().isExpire(currentTimeInSec);
                                // }
                                return expire;
                            }
                        });
                        if (indexStatus.size() <= 0) {
                            continue;
                        }
                        setCollectionName(indexName);
                        printLog(indexStatus, currentTimeInSec);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, 15, TABLE_COUNT_GAP, TimeUnit.SECONDS);
    }

    /**
     * 取得索引对应的各个tag下的绝对更新累计值
     * @param collection
     * @return
     */
    public Map<String, /* tag */
    Long> getUpdateAbsoluteCountMap(String collection) {
        return getTableUpdateCountMap(updateCounterStatus.get(collection));
    }

    private void printLog(ConcurrentHashMap<String, TableMultiDataIndexStatus> indexStatus, long currentTimeInSec) {
        String dateString = formatYyyyMMddHHmmss.get().format(new Date());
        Map<String, YarnStateStatistics> yarnStateMap = getYarnStateMap(indexStatus, currentTimeInSec);
        YarnStateStatistics yarnState = getMapCount(yarnStateMap);
        statisLog.info(dateString + ", tbTPS:" + yarnState.getTbTPS() + ", tisTPS:" + yarnState.getSorlTPS() + "\n" + "detail:" + getYarnStateString(yarnStateMap) + "tableCount:" + getTableUpdateCount(indexStatus) + "\n");
    }

    private Map<String, YarnStateStatistics> getYarnStateMap(ConcurrentHashMap<String, /* uuid */
    TableMultiDataIndexStatus> indexStatus, long currentTimeInSec) {
        Map<String, YarnStateStatistics> yarnStateMap = new HashMap<>();
        for (String uuid : indexStatus.keySet()) {
            TableMultiDataIndexStatus status = indexStatus.get(uuid);
            YarnStateStatistics yarnStateStatistics = new YarnStateStatistics();
            yarnStateMap.put(uuid, yarnStateStatistics);
            // add queueRC
            yarnStateStatistics.setQueueRC(status.getBufferQueueRemainingCapacity());
            // add from
            yarnStateStatistics.setFrom(status.getFromAddress());
            // add Tis30sAvgRT
            yarnStateStatistics.setTis30sAvgRT(status.getTis30sAvgRT());
            // add tbTPS
            LinkedList<ConsumeDataKeeper> consumeDataKeepers = status.getConsumeDataKeepList(TABLE_CONSUME_COUNT);
            if (consumeDataKeepers == null || consumeDataKeepers.size() <= 0) {
                yarnStateStatistics.setTbTPS(0L);
            } else {
                ConsumeDataKeeper last = consumeDataKeepers.getLast();
                ConsumeDataKeeper start = null;
                for (int i = consumeDataKeepers.size() - 2; i >= 0; i--) {
                    ConsumeDataKeeper tmpKeeper = consumeDataKeepers.get(i);
                    if (tmpKeeper.getCreateTime() > currentTimeInSec - TABLE_COUNT_GAP) {
                        start = tmpKeeper;
                    } else {
                        break;
                    }
                }
                if (start == null) {
                    yarnStateStatistics.setTbTPS(last.getAccumulation() / TABLE_COUNT_GAP);
                } else {
                    yarnStateStatistics.setTbTPS((last.getAccumulation() - start.getAccumulation()) / TABLE_COUNT_GAP);
                }
            }
            // add solrTPS
            consumeDataKeepers = status.getConsumeDataKeepList(IIncreaseCounter.SOLR_CONSUME_COUNT);
            if (consumeDataKeepers == null || consumeDataKeepers.size() <= 0) {
                yarnStateStatistics.setTbTPS(0L);
            } else {
                ConsumeDataKeeper last = consumeDataKeepers.getLast();
                ConsumeDataKeeper start = null;
                for (int i = consumeDataKeepers.size() - 2; i >= 0; i--) {
                    ConsumeDataKeeper tmpKeeper = consumeDataKeepers.get(i);
                    if (tmpKeeper.getCreateTime() > currentTimeInSec - TABLE_COUNT_GAP) {
                        start = tmpKeeper;
                    } else {
                        break;
                    }
                }
                if (start == null) {
                    yarnStateStatistics.setSorlTPS(last.getAccumulation() / TABLE_COUNT_GAP);
                } else {
                    yarnStateStatistics.setSorlTPS((last.getAccumulation() - start.getAccumulation()) / TABLE_COUNT_GAP);
                }
            }
        }
        return yarnStateMap;
    }

    private YarnStateStatistics getMapCount(Map<String, YarnStateStatistics> yarnStateMap) {
        YarnStateStatistics yarnState = new YarnStateStatistics();
        for (YarnStateStatistics yarnStateStatistics : yarnStateMap.values()) {
            yarnState.setTbTPS(yarnState.getTbTPS() + yarnStateStatistics.getTbTPS());
            yarnState.setSorlTPS(yarnState.getSorlTPS() + yarnStateStatistics.getSorlTPS());
        // yarnState.setQueueRC(yarnState.getQueueRC() +
        // yarnStateStatistics.getQueueRC());
        // yarnState.setTis30sAvgRT(yarnState.getTis30sAvgRT() +
        // yarnStateStatistics.getTis30sAvgRT());
        }
        // yarnState.setTis30sAvgRT(yarnState.getTis30sAvgRT() / yarnStateMap.size());
        return yarnState;
    }

    private static String getYarnStateString(Map<String, YarnStateStatistics> yarnStateMap) {
        StringBuilder sb = new StringBuilder("\n");
        for (YarnStateStatistics yarnStateStatistics : yarnStateMap.values()) {
            String state = String.format("{'host':'%s', 'tbTPS':%d, 'solrTPS':%d, 'solr_30s_avg_rt':%dms, 'queueRC':%d}\n", yarnStateStatistics.getFrom(), yarnStateStatistics.getTbTPS(), yarnStateStatistics.getSorlTPS(), yarnStateStatistics.getTis30sAvgRT(), yarnStateStatistics.getQueueRC());
            // JSONObject json = new JSONObject();
            // json.put("host", yarnStateStatistics.getFrom());
            // json.put("tbTPS", yarnStateStatistics.getTbTPS());
            // json.put("solrTPS", yarnStateStatistics.getSorlTPS());
            // json.put("tis30sAvgRT", yarnStateStatistics.getTis30sAvgRT() + "ms");
            // json.put("queueRC", yarnStateStatistics.getQueueRC());
            sb.append(state);
        }
        return sb.toString();
    }

    private MasterJob pollJob(UpdateCounterMap upateCounter) {
        if (jobQueue.size() <= 0) {
            return null;
        }
        MasterJob job;
        // 先剔除过期的job
        long nowTime = System.currentTimeMillis() / 1000;
        while (true) {
            job = jobQueue.peek();
            if (job == null) {
                return null;
            }
            if (Math.abs(nowTime - job.getCreateTime()) > JOB_EXPIRE_TIME) {
                jobQueue.poll();
            } else {
                break;
            }
        }
        if (upateCounter.containsIndex(job.getIndexName(), job.getUUID()) && jobQueue.remove(job)) {
            return job;
        } else {
            return null;
        }
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException {
        return IncrStatusUmbilicalProtocol.versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash) throws IOException {
        return ProtocolSignature.getProtocolSignature(this, protocol, clientVersion, clientMethodsHash);
    }

    public static void main(String[] args) {
        IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocolServer = new IncrStatusUmbilicalProtocolImpl();
        // getConfig();
        Configuration conf = getConfig();
        try {
            Server server = new RPC.Builder(conf).setProtocol(IncrStatusUmbilicalProtocol.class).setInstance(incrStatusUmbilicalProtocolServer).setBindAddress("0.0.0.0").setPort(1234).setNumHandlers(2).setVerbose(false).build();
            server.start();
            InetSocketAddress address = server.getListenerAddress();
            // NetUtils.createSocketAddrForHost(null,
            // server.getListenerAddress().getPort());
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            System.out.println(address.getHostName());
            System.out.println(address.getAddress().getHostName());
        } catch (IOException e) {
            throw new YarnRuntimeException(e);
        }
    }

    public static Configuration getConfig() {
        return new Configuration();
    }

    private void setCollectionName(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalStateException("app name can not be blank");
        }
        MDC.put("app", collectionName);
    }

    /**
     * 取得索引最终更新的时间戳
     *
     * @param index
     * @return
     */
    public Map<String, /* FromAddress */
    Long> getLastUpdateTimeSec(String index) {
        ConcurrentHashMap<String, TableMultiDataIndexStatus> /* uuid发送过来的节点id */
        status = updateCounterStatus.get(index);
        Map<String, Long> /* node last update timesec */
        result = Maps.newHashMap();
        for (Map.Entry<String, TableMultiDataIndexStatus> /* uuid发送过来的节点id */
        entry : status.entrySet()) {
            result.put(entry.getValue().getFromAddress(), entry.getValue().getLastUpdateSec());
        }
        return result;
    }

    private String getTableUpdateCount(ConcurrentHashMap<String, /* uuid */
    TableMultiDataIndexStatus> indexStatus) {
        if (indexStatus == null || indexStatus.size() <= 0) {
            return "[]";
        }
        Map<String, Long> updateCountMap = getTableUpdateCountMap(indexStatus);
        JSONArray array = new JSONArray();
        for (Map.Entry<String, Long> entry : updateCountMap.entrySet()) {
            JSONObject json = new JSONObject();
            json.put(entry.getKey(), entry.getValue());
            array.put(json);
        }
        return array.toString(1);
    }

    private Map<String, /* tag */
    Long> getTableUpdateCountMap(ConcurrentHashMap<String, TableMultiDataIndexStatus> indexStatus) {
        if (indexStatus == null) {
            return Collections.emptyMap();
        }
        Map<String, Long> updateCountMap = new HashMap<>();
        for (TableMultiDataIndexStatus aIndexStatus : indexStatus.values()) {
            for (String tableName : aIndexStatus.getTableNames()) {
                LinkedList<ConsumeDataKeeper> consumeDataKeepers = aIndexStatus.getConsumeDataKeepList(tableName);
                if (consumeDataKeepers.size() <= 0) {
                    continue;
                }
                long accumulation = consumeDataKeepers.getLast().getAccumulation();
                if (updateCountMap.containsKey(tableName)) {
                    updateCountMap.put(tableName, updateCountMap.get(tableName) + accumulation);
                } else {
                    updateCountMap.put(tableName, accumulation);
                }
            }
        }
        return updateCountMap;
    }
}
