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

package com.qlangtech.tis.datax.job;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.k8s.HorizontalpodAutoscaler;
import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.RcHpaStatus;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.coredefine.module.action.impl.RcDeployment;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.plugin.k8s.K8sImage;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.jst.ILogListener;

import java.util.List;
import java.util.Objects;

/**
 * DataX 任务执行容器
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-23 17:49
 **/
public abstract class DataXJobWorker implements Describable<DataXJobWorker> {

    public static final String KEY_FIELD_NAME = "k8sImage";

    public static final TargetResName K8S_INSTANCE_NAME = new TargetResName("datax-worker");
    public static final TargetResName K8S_FLINK_CLUSTER_NAME = new TargetResName("flink-cluster");


    @FormField(ordinal = 0, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String k8sImage;

    public static DataXJobWorker getDataxJobWorker() {
        PluginStore<DataXJobWorker> dataxJobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
        DataXJobWorker jobWorker = dataxJobWorkerStore.getPlugin();
        Objects.requireNonNull(jobWorker, "jobWorker can not be null");
        return jobWorker;
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
    public abstract RcDeployment getRCDeployment();

    public abstract RcHpaStatus getHpaStatus();

    /**
     * 开始增量监听
     *
     * @param listener
     */
    public abstract WatchPodLog listPodAndWatchLog(String podName, ILogListener listener);

    /**
     * dataXWorker service 是否是启动状态
     *
     * @return
     */
    public static boolean isDataXWorkerServiceOnDuty() {
        PluginStore<DataXJobWorker> jobWorkerStore = TIS.getPluginStore(DataXJobWorker.class);
        List<DataXJobWorker> services = jobWorkerStore.getPlugins();
        return services.size() > 0 && jobWorkerStore.getPlugin().inService();
    }

    /**
     * 通过Curator来实现分布式任务overseer-worker模式
     *
     * @return
     */
    public abstract String getZookeeperAddress();

    public abstract String getZkQueuePath();

    protected final K8sImage getK8SImage() {
        K8sImage k8sImage = TIS.getPluginStore(K8sImage.class).find(this.k8sImage);
        Objects.requireNonNull(k8sImage, "k8sImage:" + this.k8sImage + " can not be null");
        return k8sImage;
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
     * 服务是否已经启动
     *
     * @return
     */
    public abstract boolean inService();

    /**
     * 将控制器删除掉
     */
    public abstract void remove();

    /**
     * 启动服务
     */
    public abstract void launchService();


    @Override
    public Descriptor<DataXJobWorker> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }


    protected static class BasicDescriptor extends Descriptor<DataXJobWorker> {

        public BasicDescriptor() {
            super();
            this.registerSelectOptions(KEY_FIELD_NAME, () -> {
                PluginStore<K8sImage> images = TIS.getPluginStore(K8sImage.class);
                return images.getPlugins();
            });
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            return true;
        }
    }


}
