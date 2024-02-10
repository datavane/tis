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

package com.qlangtech.tis.datax.job;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.coredefine.module.action.LoopQueue;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.datax.job.DataXJobWorker.K8SWorkerCptType;
import com.qlangtech.tis.datax.job.DataXJobWorker.LaunchToken;
import com.qlangtech.tis.datax.job.DefaultSSERunnable.LaunchWALLineVisitor;
import com.qlangtech.tis.datax.job.DefaultSSERunnable.SubJobLog;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;
import com.qlangtech.tis.datax.job.SSERunnable.SSEEventType;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * 标记K8S K8SWorker 启动执行状态
 * observer: k8SLaunching
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-25 09:49
 **/
public class ServerLaunchToken extends Observable implements Closeable {
    // 已经启动标志文件
    private final File launchedToken;
    // 正在启动标志文件
    private final File launchingToken;
    public final K8SWorkerCptType workerCptType;

    /**
     * 不为空，说明该token正在被执行启动流程，正在写入
     */
    private Object writeOwner;

    private static final Map<LaunchTokenKey, ServerLaunchToken> launchTokens = Maps.newHashMap();

    private static class LaunchTokenKey {
        private final TargetResName workerType;
        private final boolean launchTokenUseCptType;
        private final K8SWorkerCptType workerCptType;

        public LaunchTokenKey(TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType workerCptType) {
            this.workerType = workerType;
            this.launchTokenUseCptType = launchTokenUseCptType;
            this.workerCptType = workerCptType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LaunchTokenKey that = (LaunchTokenKey) o;
            return launchTokenUseCptType == that.launchTokenUseCptType &&
                    com.google.common.base.Objects.equal(workerType.getName(), that.workerType.getName()) &&
                    workerCptType == that.workerCptType;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(workerType.getName(), launchTokenUseCptType, workerCptType);
        }
    }

//    public static ServerLaunchToken create(File launchTokenParentDir, BasicDescriptor basicDesc) {
//        return create(launchTokenParentDir, basicDesc.getWorkerType(), false, basicDesc.getWorkerCptType());
//    }

    public ServerLaunchLog buildWALLog(List<ExecuteStep> executeSteps) {
        ServerLaunchLog k8SLaunching = new ServerLaunchLog(this.isLaunchingTokenExist());
        k8SLaunching.setExecuteSteps(executeSteps);

        if (!this.isLaunchingTokenExist()) {
            k8SLaunching.setMilestones(Collections.emptyList());
            return k8SLaunching;
        }

        try {
            LineIterator lines = FileUtils.lineIterator(this.getLaunchingToken(), TisUTF8.getName());
//      String[] line = null;
//      SSEEventType event;
//      String data;

            Map<String, SubJobMilestone> milestones = Maps.newHashMap();

            LoopQueue<SubJobLog> loggerQueue = new LoopQueue<>(new SubJobLog[100]);
            //   final JSONArray[] subJobExecStepsJSONArray = new JSONArray[1];
            while (lines.hasNext()) {
//        line = StringUtils.split(lines.nextLine(), splitChar);
//        event = SSEEventType.parse(line[0]);
//        data = line[1];

                processLaunchWALLine(lines.nextLine(), new LaunchWALLineVisitor() {

                    @Override
                    public void process(SubJobLog jobLog) {
                        loggerQueue.write(jobLog);
                    }

                    @Override
                    public void process(SubJobMilestone stone) {
                        milestones.put(stone.getName(), stone);
                    }
                });
            }

            k8SLaunching.setMilestones(Lists.newArrayList(milestones.values()));
            k8SLaunching.setExecuteSteps(SubJobMilestone.readSubJobJSONArray(executeSteps, (subJobName) -> milestones.get(subJobName)));
            k8SLaunching.setLogs(loggerQueue.readBuffer());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return k8SLaunching;
    }

    public static void processLaunchWALLine(String line, LaunchWALLineVisitor lineStructVisitor) {
        String[] lineSplit = StringUtils.split(line, SSERunnable.splitChar);
        SSEEventType event = SSEEventType.parse(lineSplit[0]);
        String data = lineSplit[1];

        switch (event) {
            case TASK_LOG:
                lineStructVisitor.process(SubJobLog.readSubJobLog(data));
                break;
            case TASK_MILESTONE:
                SubJobMilestone stone = SubJobMilestone.readSubJobMilestoneJson(data);
                lineStructVisitor.process(stone);
                break;
            case TASK_EXECUTE_STEPS:
                //JSONArray subJobExecStepsJSONArray = JSONArray.parseArray(data);
                // lineStructVisitor.process(subJobExecStepsJSONArray);
                break;
            default:
                throw new IllegalStateException("illegal token:" + event);
        }

    }

    private static final FlinkClusterTokenManager flinkClusterTokenManager = new FlinkClusterTokenManager();

    public static FlinkClusterTokenManager createFlinkClusterToken() {
//        File flinkClusterParentDir = new File(Config.getMetaCfgDir(), K8SWorkerCptType.FlinkCluster.token);
//        return create(flinkClusterParentDir, new TargetResName(clusterType.token + workerType.getName()), false, K8SWorkerCptType.FlinkCluster);
        return flinkClusterTokenManager;
    }

    public static class FlinkClusterTokenManager {
        public final static String JSON_KEY_WEB_INTERFACE_URL = "webInterfaceURL";
        public final static String JSON_KEY_CLUSTER_ID = "clusterId";
        public final static String JSON_KEY_APP_NAME = DataxUtils.DATAX_NAME;
        public final static String JSON_KEY_CLUSTER_TYPE = "clusterType";
        public final static String JSON_KEY_K8S_NAMESPACE = "k8s_namespace";
        public final static String JSON_KEY_K8S_BASE_PATH = "k8s_base_path";
        public final static String JSON_KEY_K8S_ID = "k8s_id";

        public final static String JSON_KEY_LAUNCH_TIME = "launchTime";

        private List<FlinkClusterPojo> _clusters;

        final File flinkClusterParentDir = new File(Config.getMetaCfgDir(), K8SWorkerCptType.FlinkCluster.token);

        public ServerLaunchToken token(FlinkClusterType clusterType, TargetResName workerType) {
            return create(flinkClusterParentDir, new TargetResName(clusterType.token + workerType.getName()), false, K8SWorkerCptType.FlinkCluster);
        }

        public void cleanCache() {
            this._clusters = null;
        }

        public List<FlinkClusterPojo> getAllClusters() throws Exception {

            if (_clusters == null) {

                _clusters = Lists.newLinkedList();
                FlinkClusterPojo c = null;
                String[] clusters = flinkClusterParentDir.list();
                JSONObject meta = null;
                File clusterFile = null;
                for (String cluster : clusters) {
                    clusterFile = new File(flinkClusterParentDir, cluster);
                    try {
                        c = new FlinkClusterPojo();
                        try {
                            meta = JSONObject.parseObject(FileUtils.readFileToString(clusterFile, TisUTF8.get()));
                            if (meta == null) {
                                continue;
                            }
                        } catch (Throwable e) {
                            continue;
                        }
                        c.setClusterId(meta.getString(JSON_KEY_CLUSTER_ID));
                        c.setDataXName(meta.getString(JSON_KEY_APP_NAME));
                        c.setWebInterfaceURL(meta.getString(JSON_KEY_WEB_INTERFACE_URL));
                        c.setClusterType(FlinkClusterType.parse(meta.getString(JSON_KEY_CLUSTER_TYPE)));
                        c.setK8sNamespace(meta.getString(JSON_KEY_K8S_NAMESPACE));
                        c.setK8sBasePath(meta.getString(JSON_KEY_K8S_BASE_PATH));
                        c.setK8sId(meta.getString(JSON_KEY_K8S_ID));
                        c.setCreateTime(meta.getLongValue(JSON_KEY_LAUNCH_TIME));
                        _clusters.add(c);
                    } catch (Exception e) {
                        throw new RuntimeException(clusterFile.getAbsolutePath(), e);
                    }
                }

            }
            return _clusters;
        }

        public FlinkClusterPojo find(FlinkClusterType clusterType, String clusterId) {
            if (StringUtils.isEmpty(clusterId)) {
                throw new IllegalArgumentException("param clusterId can not be empty");
            }
            try {
                for (FlinkClusterPojo cluster : getAllClusters()) {
                    if (cluster.clusterType == Objects.requireNonNull(clusterType)
                            && StringUtils.equals(cluster.getClusterId(), clusterId)) {
                        return cluster;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        }
    }

    public enum FlinkClusterType {
        K8SApplication("k8s_application_"),
        K8SSession("k8s_session_"),
        Standalone("standalone_");

        public static FlinkClusterType parse(String token) {
            for (FlinkClusterType type : FlinkClusterType.values()) {
                if (type.token.equals(token)) {
                    return type;
                }
            }
            throw new IllegalStateException("invalid token:" + token);
        }

        private final String token;

        public String getToken() {
            return token;
        }

        FlinkClusterType(String token) {
            this.token = token;
        }
    }

    public static ServerLaunchToken create(
            File launchTokenParentDir, TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType workerCptType) {
        synchronized (ServerLaunchToken.class) {
            ServerLaunchToken launchToken = null;
            LaunchTokenKey tokenKey = new LaunchTokenKey(workerType, launchTokenUseCptType, workerCptType);
            if ((launchToken = launchTokens.get(tokenKey)) == null) {
                launchToken = new ServerLaunchToken(launchTokenParentDir, workerType, launchTokenUseCptType, workerCptType);
                launchTokens.put(tokenKey, launchToken);
            }
            return launchToken;
        }
    }

    public boolean hasWriteOwner() {
        return this.writeOwner != null;
    }

//    private ServerLaunchToken(File launchTokenParentDir, BasicDescriptor basicDesc) {
//        this(launchTokenParentDir, Objects.requireNonNull(basicDesc.getWorkerType(), "workType can not be null")
//                , basicDesc.getWorkerCptType());
//    }

    private ServerLaunchToken(File launchTokenParentDir, TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType workerCptType) {
        this.launchedToken = LaunchToken.SUCCESS_COMPLETE.getTokenFile(launchTokenParentDir, workerType, launchTokenUseCptType, workerCptType);// new File(launchTokenParentDir, getTokenFileName(workerType));// Objects.requireNonNull(launchToken, "launchToken can not be null");
        this.launchingToken = LaunchToken.DOING.getTokenFile(launchTokenParentDir, workerType, launchTokenUseCptType, workerCptType);// new File(launchTokenParentDir, getTokenFileName(workerType));
        this.workerCptType = Objects.requireNonNull(workerCptType, "workerCptType can not be null");
    }

    public void setWriteOwner(Object writeOwner) {
        this.writeOwner = writeOwner;
    }

    /**
     * @param arg
     */
    @Override
    public void notifyObservers(Object arg) {
        super.setChanged();
        super.notifyObservers(arg);
    }

    public File getLaunchingToken() {
        return this.launchingToken;
    }

    public boolean isLaunchingTokenExist() {
        return this.getLaunchingToken().exists();
    }

    public void deleteLaunchToken() {
        FileUtils.deleteQuietly(this.launchingToken);
        FileUtils.deleteQuietly(this.launchedToken);
    }

    public boolean isLaunchTokenExist() {
        return this.launchedToken.exists();
    }

    public K8SWorkerCptType getWorkerCptType() {
        return this.workerCptType;
    }


    /**
     * 启动成功之后写入相应的配置信息
     */
    public void writeLaunchToken(Callable<Optional<JSONObject>> bizLogic) {
        if (this.isLaunchTokenExist()) {
            throw TisException.create("launch token :" + this.launchedToken.getPath() + " shall not be exist");
        }
        try {
            Optional<JSONObject> t = Objects.requireNonNull(bizLogic.call(), "bizLogic can not be null");
            JSONObject token = t.orElseGet(() -> new JSONObject());
            //  TimeFormat.yyyyMMddHHmmss.format()
            token.put(FlinkClusterTokenManager.JSON_KEY_LAUNCH_TIME, TimeFormat.getCurrentTimeStamp());
            token.put(DataXJobWorker.KEY_CPT_TYPE, workerCptType.token);
            FileUtils.write(launchedToken, JsonUtil.toString(token, true), TisUTF8.get(), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private PrintWriter launchingWriter;

    public void touchLaunchingToken() {
        try {
            FileUtils.touch(this.launchingToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendLaunchingLine(String line) {
        try {
            synchronized (this) {
                if (launchingWriter == null) {
                    launchingWriter = new PrintWriter(FileUtils.openOutputStream(this.launchingToken));
                }

                launchingWriter.println(line);
                launchingWriter.flush();
                this.notifyObservers(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (ServerLaunchToken.class) {
            IOUtils.closeQuietly(launchingWriter, null);
            launchTokens.remove(this.workerCptType);
        }
    }


}
