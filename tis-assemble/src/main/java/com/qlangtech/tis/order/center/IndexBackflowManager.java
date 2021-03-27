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
package com.qlangtech.tis.order.center;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.cloud.ICoreAdminAction;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus.NodeBackflowStatus;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.trigger.jst.AbstractIndexBuildJob.BuildResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.apache.solr.common.cloud.ZkStateReader.BASE_URL_PROP;
import static org.apache.solr.common.cloud.ZkStateReader.CORE_NAME_PROP;

/**
 * 索引回流控制器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月4日 下午5:46:59
 */
public class IndexBackflowManager {

    int phrase = 0;

    private static final Logger log = LoggerFactory.getLogger(IndexBackflowManager.class);

    private static final ConcurrentHashMap<String, ReentrantLock> /*
     * nodename,example:10.1.5.19
     */
            nodeLockMap = new ConcurrentHashMap<>();

    private final ExecutorService backFlowExecutor;

    private final DocCollection collection;

    private final ITaskPhaseInfo taskPhaseinfo;

    // 执行参数
    private final IExecChainContext execContext;

    public IExecChainContext getExecContext() {
        return this.execContext;
    }

    private Map<String, ConcurrentLinkedQueue<BuildResult>> /* nodename,example:10.1.5.19 */ nodeBackflowLock;

    private final Map<String, List<Replica>> /* shardName,example:shard1 */ shardMap;

    private final int nodeSize;
    private final CountDownLatch replicaCountDown;

    private boolean executeSuccess = true;

    public boolean isExecuteSuccess() {
        return this.executeSuccess;
    }

    public static final int MAX_WAIT_TIMEOUT = 10;

    public void await() {
        boolean timeout;
        try {
            timeout = !replicaCountDown.await(MAX_WAIT_TIMEOUT, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (timeout) {
            throw new IllegalStateException("wait indexbackflow timeout " + MAX_WAIT_TIMEOUT + " hour");
        }
    }

    public List<Replica> getReplicByShard(int shard) {
        final String shardName = "shard" + (shard + 1);
        List<Replica> replics = shardMap.get(shardName);
        if (replics == null || replics.size() < 1) {
            throw new IllegalStateException(shardName + " relevant replics is null");
        }
        return replics;
    }

    public IndexBackflowManager(final DocCollection collection, IExecChainContext execContext, ITaskPhaseInfo taskinfo) {
        this.collection = collection;
        this.taskPhaseinfo = taskinfo;
        this.execContext = execContext;
        final Map<String, List<Replica>> /* shardName,example:shard1 */
                shardMap = new HashMap<>();
        final Map<String, ConcurrentLinkedQueue<BuildResult>> /* nodename,example:10.1.5.19 */
                nodeBackflowLock = new HashMap<>();
        List<Replica> nodeReplics = null;
        ConcurrentLinkedQueue<BuildResult> lock = null;
        int replicaCount = 0;
        for (Slice slice : collection.getSlices()) {
            nodeReplics = shardMap.get(slice.getName());
            if (nodeReplics == null) {
                nodeReplics = new ArrayList<>();
                shardMap.put(slice.getName(), nodeReplics);
            }
            for (Replica replica : slice.getReplicas()) {
                // 初始化每個core節點的狀態
                this.getReplicaNodeStatus(replica).setWaiting(true);
                replicaCount++;
                lock = nodeBackflowLock.get(replica.getNodeName());
                if (lock == null) {
                    lock = new ConcurrentLinkedQueue<BuildResult>();
                    nodeBackflowLock.put(replica.getNodeName(), lock);
                }
                nodeReplics.add(replica);
            }
        }
        this.replicaCountDown = new CountDownLatch(replicaCount);
        this.nodeSize = nodeBackflowLock.keySet().size();
        if (this.nodeSize < 1) {
            throw new IllegalStateException("nodeSize can not small than 1");
        }
        this.backFlowExecutor = Executors.newFixedThreadPool(nodeSize);
        this.nodeBackflowLock = Collections.unmodifiableMap(nodeBackflowLock);
        this.shardMap = Collections.unmodifiableMap(shardMap);
    }

    /**
     * 遍历所有的副本
     *
     * @param action
     */
    public void vistAllReplica(Consumer<Replica> action) {
        shardMap.values().forEach((a) -> {
            a.forEach(action);
        });
    }

    /**
     * group size
     *
     * @return
     */
    public int getGroupSize() {
        return this.shardMap.keySet().size();
    }

    public void addBackFlowTask(BuildResult buildResult) {
        ConcurrentLinkedQueue<BuildResult> backflowQueue = this.nodeBackflowLock.get(buildResult.getNodeName());
        if (backflowQueue == null) {
            throw new IllegalStateException("node:" + buildResult.getNodeName() + " relevant backflow queue can not be null");
        }
        backflowQueue.offer(buildResult);
    }

    public void startSwapClusterIndex(final int taskid) throws Exception {
        // this.execContext.get
        for (final Map.Entry<String, ConcurrentLinkedQueue<BuildResult>> /* nodename,example:10.1.5.19 */
                entry : nodeBackflowLock.entrySet()) {
            this.backFlowExecutor.execute(() -> {
                // MDC.put("app", collection.getName());
                this.execContext.rebindLoggingMDCParams();
                while (true) {
                    BuildResult buildResult = entry.getValue().poll();
                    if (buildResult == null) {
                        if (replicaCountDown.getCount() < 1) {
                            // 说明任务已经全部结束需要退出了
                            return;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (Throwable e) {
                        }
                        continue;
                    }
                    ReentrantLock lock = nodeLockMap.get(entry.getKey());
                    if (lock == null) {
                        lock = new ReentrantLock();
                        ReentrantLock tmp = nodeLockMap.putIfAbsent(entry.getKey(), lock);
                        if (tmp != null) {
                            lock = tmp;
                        }
                    }
                    try {
                        log.info("node:" + entry.getKey() + " gain the lock");
                        lock.lockInterruptibly();
                        BackflowResult backflowResult = triggerIndexBackflow(buildResult.getReplica(), Long.parseLong(buildResult.getTimepoint()), taskid);
                        if (backflowResult.isSuccess()) {
                            this.replicaCountDown.countDown();
                        } else {
                            shortCircuit();
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        shortCircuit();
                        throw new RuntimeException(e);
                    } finally {
                        getBackflowPhaseStatus().isComplete();
                        lock.unlock();
                    }
                }
            });
        }
    }

    public void shortCircuit() {
        this.executeSuccess = false;
        // 说明有一组回流失败了，需要短路任务执行
        while (this.replicaCountDown.getCount() > 0) {
            this.replicaCountDown.countDown();
        }
    }

    public static String RUNNING = "running";

    public static String COMPLETED = "completed";

    public static String FAILED = "failed";

    public static String NOT_FOUND = "notfound";

    /**
     * 向Core节点触发执行，索引回流事件，并且监控回流进度
     *
     * @param replica
     * @param timestamp
     * @param
     * @param taskid
     * @return
     * @throws Exception
     */
    private BackflowResult triggerIndexBackflow(final Replica replica, long timestamp, final int taskid) throws Exception {
        final String requestId = taskid + "_p_" + (phrase++);
        if (replica == null) {
            throw new IllegalArgumentException("replica can not be null");
        }

        log.info("start " + replica.getCoreUrl() + " index back");
        URL url = new URL(replica.getStr(BASE_URL_PROP) + "/admin/cores?action=CREATEALIAS&execaction="
                + ICoreAdminAction.ACTION_SWAP_INDEX_FILE + "&core=" + replica.getStr(CORE_NAME_PROP)
                + "&property.hdfs_timestamp=" + timestamp + "&property.hdfs_user=admin&" + CommonAdminParams.ASYNC + "=" + requestId);
        log.info("apply swap index url:" + url);
        BackflowResult result = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

            @Override
            public BackflowResult p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                BackflowResult result = new BackflowResult();
                try {
                    log.info(IOUtils.toString(stream, TisUTF8.get()));
                    URL url = new URL(replica.getStr(BASE_URL_PROP) + "/admin/cores?action=requeststatus&wt=json&" + CoreAdminParams.REQUESTID + "=" + requestId);
                    log.info("check is successful :" + url);
                    return getCallbackResult(replica, url);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return result;
            }
        }, 5);
        return result;
    }

    // 将近两个小时
    private static final int MAX_RETRY = 1600;

    /**
     * 轮询是否回流完成
     *
     * @param replica
     * @param url
     * @return
     */
    private BackflowResult getCallbackResult(Replica replica, URL url) {
        int applyCount = 0;
        BackflowResult callbackResult = null;
        while (applyCount++ < MAX_RETRY) {
            callbackResult = HttpUtils.processContent(url, new StreamProcess<BackflowResult>() {

                @Override
                public BackflowResult p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    BackflowResult callbackResult = null;
                    try {
                        String body = IOUtils.toString(stream, TisUTF8.get());
                        callbackResult = JSON.parseObject(body, BackflowResult.class);
                        callbackResult.setResponseBody(body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return callbackResult;
                }
            }, 10);
            NodeBackflowStatus nodeStatus = getReplicaNodeStatus(replica);
            nodeStatus.setWaiting(false);
            if (callbackResult.isFaild()) {
                log.error(replica.getCoreUrl() + ",index back faild:" + callbackResult.getMsg() + "\n body:" + callbackResult.getResponseBody());
                nodeStatus.setFaild(true);
                return callbackResult;
            }
            String coreName = null;
            if (!callbackResult.isSuccess()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                coreName = replica.getStr(CORE_NAME_PROP);
                log.info("waitting index flowback " + coreName + "," + callbackResult.getCopyStatus()
                        + "retry count:" + applyCount + ",remain:" + this.replicaCountDown.getCount() + "nodes");
                // ▼▼▼ 取得当前阶段的执行状态
                nodeStatus.setAllSize((int) callbackResult.indexflowback_status.getAll());
                nodeStatus.setReaded((int) callbackResult.indexflowback_status.getReaded());
                // ▲▲▲
                continue;
            } else {
                callbackResult.getCopyStatus();
                // 执行完成
                final int allSize = (int) callbackResult.indexflowback_status.getAll();
                nodeStatus.setAllSize(allSize);
                nodeStatus.setReaded(allSize);
                nodeStatus.setComplete(true);
            }
            return callbackResult;
        }
        if (applyCount >= MAX_RETRY) {
            log.error(replica.getStr(CORE_NAME_PROP) + " index back faild:exceed the max retry count " + MAX_RETRY);
        }
        return callbackResult;
    }

    protected NodeBackflowStatus getReplicaNodeStatus(Replica replica) {
        IndexBackFlowPhaseStatus backflowStatus = getBackflowPhaseStatus();
        return backflowStatus.getNode(replica.getStr(CORE_NAME_PROP));
    }

    protected IndexBackFlowPhaseStatus getBackflowPhaseStatus() {
        IndexBackFlowPhaseStatus backflowStatus = this.taskPhaseinfo.getPhaseStatus(this.execContext, FullbuildPhase.IndexBackFlow);
        return backflowStatus;
    }

    public static class BackflowResult {

        private boolean result = false;

        private String responseBody;

        private String msg;

        private String STATUS;

        private String Response;

        private IndexflowbackStatus indexflowback_status;//= new IndexflowbackStatus();

        public String getCopyStatus() {
            if (indexflowback_status == null) {
                indexflowback_status = new IndexflowbackStatus();
                if (this.Response == null) {
                    return StringUtils.EMPTY;
                }
                String[] pairs = StringUtils.split(this.Response, " ");
                for (String p : pairs) {
                    if (StringUtils.indexOf(p, IndexBackFlowPhaseStatus.KEY_INDEX_BACK_FLOW_STATUS) > -1) {
                        JSONObject s = JSON.parseObject(p.split("=")[1]);
                        indexflowback_status.setAll(s.getLongValue(TISCollectionUtils.INDEX_BACKFLOW_ALL));
                        indexflowback_status.setReaded(s.getLongValue(TISCollectionUtils.INDEX_BACKFLOW_READED));
                        break;
                    }
                }
            }
            Objects.requireNonNull(indexflowback_status, "indexflowback_status can not be null");
            return FileUtils.byteCountToDisplaySize(indexflowback_status.readed)
                    + "/" + FileUtils.byteCountToDisplaySize(indexflowback_status.all)
                    + "(" + (int) ((((double) indexflowback_status.readed) / indexflowback_status.all) * 100) + "%),";
        }

        public boolean isFaild() {
            if (FAILED.equalsIgnoreCase(STATUS) || NOT_FOUND.equalsIgnoreCase(STATUS)) {
                return true;
            }
            return false;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public String getResponse() {
            return this.Response;
        }

        public void setResponse(String response) {
            this.Response = response;
        }

        public boolean isSuccess() {
            return COMPLETED.equalsIgnoreCase(STATUS);
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getSTATUS() {
            return STATUS;
        }

        public void setSTATUS(String sTATUS) {
            STATUS = sTATUS;
        }

        @Override
        public String toString() {
            return "status:" + getSTATUS() + ",msg:" + getMsg();
        }
    }

    public static class IndexflowbackStatus {

        private long all;

        private long readed;

        public long getAll() {
            return all;
        }

        public void setAll(long all) {
            this.all = all;
        }

        public long getReaded() {
            return readed;
        }

        public void setReaded(long readed) {
            this.readed = readed;
        }
    }
}
