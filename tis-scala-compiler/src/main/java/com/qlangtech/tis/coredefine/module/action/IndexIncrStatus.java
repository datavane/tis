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
package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.realtime.yarn.rpc.IndexJobRunningStatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class IndexIncrStatus {

    // k8s的插件是否配置完成
    private boolean k8sPluginInitialized = false;

    // k8s的RC是否已经创建
    private boolean k8sReplicationControllerCreated;

    // 增量执行任务是否正在执行
    private IndexJobRunningStatus incrProcessStatus;

    private IncrDeployment incrDeployment;

    public IndexJobRunningStatus getIncrProcess() {
        return this.incrProcessStatus;
    }

    public void setIncrProcess(IndexJobRunningStatus incrProcessStatus) {
        this.incrProcessStatus = incrProcessStatus;
    }

    public IncrDeployment getIncrDeployment() {
        return incrDeployment;
    }

    public void setIncrDeployment(IncrDeployment incrDeployment) {
        this.incrDeployment = incrDeployment;
    }

    // 增量脚本是否已经生成？
    private boolean incrScriptCreated;

    private String incrScriptMainFileContent;

    // private MqConfigMeta mqConfig;
    public boolean isK8sReplicationControllerCreated() {
        return k8sReplicationControllerCreated;
    }

    public void setK8sReplicationControllerCreated(boolean k8sReplicationControllerCreated) {
        this.k8sReplicationControllerCreated = k8sReplicationControllerCreated;
    }

    public boolean isK8sPluginInitialized() {
        return k8sPluginInitialized;
    }

    public void setK8sPluginInitialized(boolean k8sPluginInitialized) {
        this.k8sPluginInitialized = k8sPluginInitialized;
    }

    public boolean isIncrScriptCreated() {
        return incrScriptCreated;
    }

    public String getIncrScriptMainFileContent() {
        return incrScriptMainFileContent;
    }

    public void setIncrScriptMainFileContent(String incrScriptMainFileContent) {
        this.incrScriptMainFileContent = incrScriptMainFileContent;
    }

    public void setIncrScriptCreated(boolean incrScriptCreated) {
        this.incrScriptCreated = incrScriptCreated;
    }
    // public static class MqConfig {
    // private String topic;
    // private String consumeId;
    // private String namesrvAddr;
    // 
    // public String getTopic() {
    // return topic;
    // }
    // 
    // public void setTopic(String topic) {
    // this.topic = topic;
    // }
    // 
    // public String getConsumeId() {
    // return consumeId;
    // }
    // 
    // public void setConsumeId(String consumeId) {
    // this.consumeId = consumeId;
    // }
    // 
    // public String getNamesrvAddr() {
    // return namesrvAddr;
    // }
    // 
    // public void setNamesrvAddr(String namesrvAddr) {
    // this.namesrvAddr = namesrvAddr;
    // }
    // }
}
