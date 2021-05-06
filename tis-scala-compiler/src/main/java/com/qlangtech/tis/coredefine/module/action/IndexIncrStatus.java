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
public class IndexIncrStatus extends K8SControllerStatus {
    public IndexIncrStatus() {
    }

    private long  incrScriptTimestamp;
    // k8s的插件是否配置完成
    private boolean k8sPluginInitialized = false;

    // 增量执行任务是否正在执行
    private IndexJobRunningStatus incrProcessStatus;


    public IndexJobRunningStatus getIncrProcess() {
        return this.incrProcessStatus;
    }

    public void setIncrProcess(IndexJobRunningStatus incrProcessStatus) {
        this.incrProcessStatus = incrProcessStatus;
    }



    // 增量脚本是否已经生成？
    private boolean incrScriptCreated;

    private String incrScriptMainFileContent;


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

    public long getIncrScriptTimestamp() {
        return incrScriptTimestamp;
    }

    public void setIncrScriptTimestamp(long incrScriptTimestamp) {
        this.incrScriptTimestamp = incrScriptTimestamp;
    }
}
