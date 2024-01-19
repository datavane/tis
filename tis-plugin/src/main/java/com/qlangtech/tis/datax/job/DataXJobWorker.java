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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.config.k8s.HorizontalpodAutoscaler;
import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.RcHpaStatus;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.coredefine.module.action.impl.RcDeployment;
import com.qlangtech.tis.datax.job.ServerLaunchToken.FlinkClusterType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.plugin.k8s.K8sImage;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.util.HeteroEnum;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DataX 任务执行容器
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-23 17:49
 **/
@Public
public abstract class DataXJobWorker implements Describable<DataXJobWorker> {
    protected static final String CLUSTER_ENTRYPOINT_HOST = "server_port_host";
    public static final String KEY_FIELD_NAME = "k8sImage";
    public static final String KEY_WORKER_TYPE = "workerType";
    public static final String GROUP_KEY_JOBWORKER = "jobworker";
    public static final String KEY_CPT_TYPE = "cptType";

    public static final TargetResName K8S_DATAX_INSTANCE_NAME = new TargetResName("datax-worker");
    public static final FlinkSessionResName K8S_FLINK_CLUSTER_NAME = new FlinkSessionResName();

    /**
     * 取得执行编排
     *
     * @return
     */
    public static ILaunchingOrchestrate getOrchestrate(DataXJobWorker dataXWorker) {
        if (!(isOrchestrate(dataXWorker))) {
            throw new IllegalArgumentException("dataXWorker must be type of " + ILaunchingOrchestrate.class.getName());
        }
        return (ILaunchingOrchestrate) dataXWorker;
    }

    public static boolean isOrchestrate(DataXJobWorker dataXWorker) {
        return Objects.requireNonNull(dataXWorker, "dataXWorker can not be null") instanceof ILaunchingOrchestrate;
    }

    /**
     * 更新到最新PodNumber
     *
     * @param podNum
     */
    public void updatePodNumber(SSERunnable sseRunnable, TargetResName cptType, Integer podNum) {
        throw new UnsupportedOperationException();
    }

    public enum K8SWorkerCptType {
        Server("powerjob-server", null), Worker("powerjob-worker"), JobTpl("powerjob-job-tpl"),
        // 具体app中可以覆写默认JobTpl中指定的参数值，并且还能定时任务表达式
        JobTplAppOverwrite("powerjob-job-tpl-app-overwrite") //
        , UsingExistCluster("powerjob-use-exist-cluster", null),
        FlinkCluster("flink-cluster", null),
        FlinkKubernetesApplicationCfg("flink-kubernetes-application-cfg"),
        K8SPods("k8s-pods");
        public final String token;
        public final String storeSuffix;

        private K8SWorkerCptType(String token) {
            this(token, token);
        }

        private K8SWorkerCptType(String token, String storeSuffix) {
            this.token = token;
            this.storeSuffix = storeSuffix;
        }

        public static K8SWorkerCptType parse(String cptType) {
            for (K8SWorkerCptType type : K8SWorkerCptType.values()) {
                if (type.token.equalsIgnoreCase(cptType)) {
                    return type;
                }
            }

            throw new IllegalStateException("param cptType:" + cptType + " is not illegal");
        }
    }


    public static void validateTargetName(String targetName) {
        if (K8S_DATAX_INSTANCE_NAME.getName().equals(targetName)
                || K8S_FLINK_CLUSTER_NAME.match(targetName)) {
            return;
        }
        throw new IllegalArgumentException("targetName:" + targetName + " is illegal");
    }

    @FormField(ordinal = 1, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String k8sImage;

//    @Override
//    public final String identityValue() {
//        return ((BasicDescriptor) this.getDescriptor()).getWorkerType().getName();
//    }


//    public static DataXJobWorker getDataxJobWorker() {
//        return getJobWorker(K8S_DATAX_INSTANCE_NAME);
//    }

//    public static DataXJobWorker getFlinkClusterWorker() {
//        return getJobWorker(K8S_FLINK_CLUSTER_NAME, Optional.empty());
//    }

    public static DataXJobWorker getJobWorker(TargetResName resName) {

        //ServerLaunchToken.createFlinkClusterToken()
        Optional<ServerLaunchToken> token = getLaunchToken(resName);
        Optional<K8SWorkerCptType> powerjobCptType = token.map((t) -> t.workerCptType);
        if (!powerjobCptType.isPresent()) {
            throw new IllegalStateException("resName:" + resName.getName() + " relevant powerjobCptType can not be empty");
        }
        // if (resName.equalWithName(K8S_DATAX_INSTANCE_NAME.getName())) {
        return getJobWorker(resName, powerjobCptType);
//        } else if (resName.equalWithName(K8S_FLINK_CLUSTER_NAME.getName())) {
//            return getJobWorker(K8S_FLINK_CLUSTER_NAME, powerjobCptType);
//        }

        // throw new IllegalStateException("illegal resName:" + resName);
    }

    public static DataXJobWorker getJobWorker(TargetResName resName, Optional<K8SWorkerCptType> powerjobCptType) {

        if (!(resName.equalWithName(K8S_DATAX_INSTANCE_NAME.getName()) || K8S_FLINK_CLUSTER_NAME.match(resName))) {
            throw new IllegalStateException("illegal resName:" + resName);
        }

        IPluginStore<DataXJobWorker> dataxJobWorkerStore = getJobWorkerStore(resName, powerjobCptType);
//        Optional<DataXJobWorker> firstWorker
//                = dataxJobWorkerStore.getPlugins().stream().filter((p) -> isJobWorkerMatch(resName, p.getDescriptor())).findFirst();
//        if (firstWorker.isPresent()) {
//            return firstWorker.get();
//        }
//        return null;
        return dataxJobWorkerStore.getPlugin();
    }


    public static IPluginStore<DataXJobWorker> getFlinkKubernetesApplicationCfgStore() {
        return DataXJobWorker.getJobWorkerStore(DataXJobWorker.K8S_FLINK_CLUSTER_NAME.group(), Optional.of(K8SWorkerCptType.FlinkKubernetesApplicationCfg));
    }

    public static IPluginStore<DataXJobWorker> getJobWorkerStore(TargetResName resName, Optional<K8SWorkerCptType> powerjobCptType) {
        if (!(resName.equalWithName(K8S_DATAX_INSTANCE_NAME.getName())
                || K8S_FLINK_CLUSTER_NAME.match(resName))) {
            throw new IllegalStateException("illegal resName:" + resName);
        }
        return TIS.getPluginStore(
                new KeyedPluginStore.Key(GROUP_KEY_JOBWORKER
                        , new KeyedPluginStore.KeyVal(resName.getName()
                        , powerjobCptType.map((type) -> type.storeSuffix).map((storeSuffix) -> ("-" + storeSuffix)).orElse(StringUtils.EMPTY)) {
                    public String getKeyVal() {
                        return (getVal() + this.suffix);
                    }
                }, DataXJobWorker.class));
    }

    public static void setJobWorker(TargetResName resName, Optional<K8SWorkerCptType> powerjobCptType, DataXJobWorker worker) {
        IPluginStore<DataXJobWorker> store = getJobWorkerStore(resName, powerjobCptType);
        store.setPlugins(null, Optional.empty(), Collections.singletonList(PluginStore.getDescribablesWithMeta(store, worker)));
    }

    public static List<Descriptor<DataXJobWorker>> getDesc(TargetResName resName) {
        return HeteroEnum.DATAX_WORKER.descriptors().stream()
                .map((d) -> (Descriptor<DataXJobWorker>) d)
                .filter((desc) -> {
                    return isJobWorkerMatch(resName, desc);
                })
                .collect(Collectors.toList());
    }

    private static boolean isJobWorkerMatch(TargetResName targetName, Descriptor<DataXJobWorker> desc) {
        return targetName.getName().equals(desc.getExtractProps().get(DataXJobWorker.KEY_WORKER_TYPE));
    }

    /**
     * 服务是否已经启动
     *
     * @return
     */
    public boolean inService() {
        return this.getProcessTokenFile().isLaunchTokenExist();
    }

//    private void writeLaunchToken() {
//        ServerLaunchToken launchToken = this.getProcessTokenFile();
//
//        launchToken.writeLaunchToken();
    //  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        JSONObject token = new JSONObject();
//        //  TimeFormat.yyyyMMddHHmmss.format()
//        token.put("launchTime", TimeFormat.getCurrentTimeStamp());
//        token.put("");
//
//        FileUtils.write(launchToken, timeFormat.format(new Date()), TisUTF8.get());
    //  }

    protected void deleteLaunchToken() {
        this.getProcessTokenFile().deleteLaunchToken();
    }

    public ServerLaunchToken getProcessTokenFile() {

        BasicDescriptor basicDesc = Objects.requireNonNull((BasicDescriptor) this.getDescriptor(), "basicDesc can not be null");
        return this.getProcessTokenFile(basicDesc.getWorkerType(), false, basicDesc.getWorkerCptType());
    }

    public ServerLaunchToken getProcessTokenFile(TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType cptType) {

        BasicDescriptor basicDesc = Objects.requireNonNull((BasicDescriptor) this.getDescriptor(), "basicDesc can not be null");
        IPluginStore<DataXJobWorker> workerStore = basicDesc.getJobWorkerStore();
        // TargetResName workerType = basicDesc.getWorkerType();
//        IPluginStore<DataXJobWorker> workerStore = getJobWorkerStore(workerType);
        File target = workerStore.getTargetFile().getFile();
        return ServerLaunchToken.create(target.getParentFile().getParentFile(), workerType, launchTokenUseCptType, cptType);
    }


    enum LaunchToken {
        /**
         * 正在执行
         */
        DOING(".launching_token")
        /**
         * 成功启动
         */
        , SUCCESS_COMPLETE(".launched_token");

        private final String token;

        private LaunchToken(String token) {
            this.token = token;
        }

        public File getTokenFile(File parent, TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType workerCptType) {
            return new File(parent,
                    getTokenFileName(workerType, launchTokenUseCptType, workerCptType));
        }

        public String getTokenFileName(TargetResName workerType, boolean launchTokenUseCptType, K8SWorkerCptType workerCptType) {
            return (workerType.getName()
                    + (launchTokenUseCptType
                    ? ("-" + Objects.requireNonNull(workerCptType.token))
                    : StringUtils.EMPTY) + this.token);
        }
    }


//    private static String getTokenFileName(TargetResName workerType) {
//        return (workerType.getName() + ".launched_token");
//    }

    public static Optional<ServerLaunchToken> getLaunchToken(TargetResName workerType) {
        return getLaunchToken(workerType, Optional.empty());
    }

    public static Optional<ServerLaunchToken> getLaunchToken(TargetResName workerType, K8SWorkerCptType cptType) {
        return getLaunchToken(workerType, Optional.of(cptType));
    }

    private static Optional<ServerLaunchToken> getLaunchToken(TargetResName workerType, Optional<K8SWorkerCptType> cptType) {


        if (DataXJobWorker.K8S_FLINK_CLUSTER_NAME.match(workerType)) {
            ServerLaunchToken flinkClusterToken = ServerLaunchToken.createFlinkClusterToken().token(
                    FlinkClusterType.K8SSession, DataXJobWorker.K8S_FLINK_CLUSTER_NAME.resName(workerType));
            return Optional.ofNullable(flinkClusterToken.isLaunchTokenExist() ? flinkClusterToken : null);
        }


        File parent = new File(TIS.pluginCfgRoot, GROUP_KEY_JOBWORKER);
        boolean launchTokenUseCptType = cptType.isPresent();
        File tokenFile = new File(parent,
                LaunchToken.SUCCESS_COMPLETE.getTokenFileName(workerType, launchTokenUseCptType, cptType.orElse(null)));
        try {
            if (!tokenFile.exists()) {
                return Optional.empty();
            }
            JSONObject content = JSONObject.parseObject(FileUtils.readFileToString(tokenFile, TisUTF8.get()));

            // K8SWorkerCptType cptType =   K8SWorkerCptType.parse(content.getString(KEY_CPT_TYPE));
            return Optional.of(ServerLaunchToken.create(parent, workerType, launchTokenUseCptType, cptType.orElseGet(() -> {
                return K8SWorkerCptType.parse(content.getString(KEY_CPT_TYPE));
            })));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 重启全部Pod节点
     *
     * @param
     */
    public abstract void relaunch();

    public abstract void relaunch(String podName);


    /**
     * 获取已经启动的RC运行参数
     *
     * @param
     * @return
     */
    public abstract List<RcDeployment> getRCDeployments();

    /**
     * 需要额外带上的配置信息
     *
     * @return
     */
    public Map<String, Object> getPayloadInfo() {
        return Collections.emptyMap();
    }

    public abstract RcHpaStatus getHpaStatus();

    /**
     * 开始增量监听
     *
     * @param listener
     */
    public abstract WatchPodLog listPodAndWatchLog(String podName, ILogListener listener);

//    /**
//     * dataXWorker service 是否是启动状态
//     *
//     * @return
//     */
//    public static boolean isDataXWorkerServiceOnDuty() {
//        PluginStore<DataXJobWorker> jobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
//        List<DataXJobWorker> services = jobWorkerStore.getPlugins();
//        return services.size() > 0 && jobWorkerStore.getPlugin().inService();
//    }

//    /**
//     * 通过Curator来实现分布式任务overseer-worker模式
//     *
//     * @return
//     */
//    public abstract String getZookeeperAddress();
//
//    public abstract String getZkQueuePath();

    protected <T extends K8sImage> T getK8SImage() {
        T k8sImage = (T) this.getK8SImageCategory().getPluginStore() //(T) K8sImage.getPluginStore(this.getK8SImageCategory())
                .find(Objects.requireNonNull(this.k8sImage, "k8sImage can not be null"));
        Objects.requireNonNull(k8sImage, "k8sImage:" + this.k8sImage + " can not be null");
        return k8sImage;
    }

    protected K8sImage.ImageCategory getK8SImageCategory() {
        return K8sImage.ImageCategory.DEFAULT_DESC_NAME;
    }

    private ReplicasSpec replicasSpec;

    // 是否支持弹性伸缩容量
    private HorizontalpodAutoscaler hpa;

    public HorizontalpodAutoscaler getHpa() {
        return hpa;
    }

    public void setHpa(HorizontalpodAutoscaler hpa) {
        this.hpa = hpa;
    }

    protected boolean supportHPA() {
        return hpa != null;
    }

    public ReplicasSpec getReplicasSpec() {
        return replicasSpec;
    }

    public void setReplicasSpec(ReplicasSpec replicasSpec) {
        this.replicasSpec = replicasSpec;
    }


    /**
     * 将控制器删除掉
     */
    public abstract void remove();

    public final void executeLaunchService(SSERunnable launchProcess) {
        ServerLaunchToken launchToken = this.getProcessTokenFile();

        launchToken.writeLaunchToken(() -> {
            return this.launchService(launchProcess);
            // return Optional.empty();
        });
        //  this.writeLaunchToken();
        launchProcess.afterLaunched();
    }

    /**
     * 启动服务
     */
    protected abstract Optional<JSONObject> launchService(SSERunnable launchProcess);


    @Override
    public Descriptor<DataXJobWorker> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }


    protected static abstract class BasicDescriptor extends Descriptor<DataXJobWorker> {

        public BasicDescriptor() {
            super();
            this.registerSelectOptions(KEY_FIELD_NAME, () -> {

                IPluginStore pluginStore = this.getK8SImageCategory().getPluginStore();
                // IPluginStore<K8sImage> images = TIS.getPluginStore(getK8SImageCfgClazz());
                return pluginStore.getPlugins();
            });
        }

        protected K8sImage.ImageCategory getK8SImageCategory() {
            return K8sImage.ImageCategory.DEFAULT_DESC_NAME;
        }

        @Override
        public final String getDisplayName() {
            return this.getWorkerCptType().token;
        }

        public abstract K8SWorkerCptType getWorkerCptType();

        @Override
        public Map<String, Object> getExtractProps() {
            Map<String, Object> extractProps = super.getExtractProps();
            extractProps.put(KEY_WORKER_TYPE, getWorkerType().getName());
            return extractProps;
        }

        protected abstract TargetResName getWorkerType();

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            return true;
        }

        public IPluginStore<DataXJobWorker> getJobWorkerStore() {
            return DataXJobWorker.getJobWorkerStore(getWorkerType()
                    , Optional.of(K8SWorkerCptType.parse(this.getDisplayName())));
        }
    }


}
