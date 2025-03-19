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

package com.qlangtech.tis.powerjob;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.CuratorDataXTaskMessage;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPostTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.trigger.util.JsonUtil;

import java.util.List;
import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/11
 */
public class SelectedTabTriggers {
    private static final String KEY_TABLE = "table";
    private static final String KEY_PRE = "pre";
    private static final String KEY_POST = "post";
    private static final String KEY_EXEC = "exec";
    private static final String KEY_RES_TYPE = "resType";
    private static final String KEY_JOB_INFO = "jobInfo";
    private IRemoteTaskPreviousTrigger preTrigger;
    private IRemoteTaskPostTrigger postTrigger;
    private List<IRemoteTaskTrigger> splitTabTriggers;
    private final ISelectedTab entry;
    private final IDataxProcessor appSource;
    private JSONObject mrParams = null;

    public SelectedTabTriggers(ISelectedTab entry, IDataxProcessor appSource) {
        this.entry = Objects.requireNonNull(entry, "selected tab can not be null");
        this.appSource = Objects.requireNonNull(appSource, "appSource can not be null");
    }

    public String getTabName() {
        return this.entry.getName();
    }

    public IRemoteTaskPreviousTrigger getPreTrigger() {
        return preTrigger;
    }

    public void setPreTrigger(IRemoteTaskPreviousTrigger preTrigger) {
        this.preTrigger = preTrigger;
    }

    public IRemoteTaskPostTrigger getPostTrigger() {
        return postTrigger;
    }

    public void setPostTrigger(IRemoteTaskPostTrigger postTrigger) {
        this.postTrigger = postTrigger;
    }

    public List<IRemoteTaskTrigger> getSplitTabTriggers() {
        return splitTabTriggers;
    }

    public void setSplitTabTriggers(List<IRemoteTaskTrigger> splitTabTriggers) {
        this.splitTabTriggers = splitTabTriggers;
    }


    public static SelectedTabTriggersConfig deserialize(JSONObject jobParams) {
        if (jobParams == null) {
            throw new IllegalArgumentException("param jobParams can not be null");
        }
        if (!jobParams.containsKey(KEY_EXEC)) {
            throw new IllegalStateException("shall contain property with key:"
                    + KEY_EXEC + "\n" + JsonUtil.toString(jobParams));
        }
        JSONObject exec = jobParams.getJSONObject(KEY_EXEC);


        SelectedTabTriggersConfig triggersConfig
                = new SelectedTabTriggersConfig(StoreResourceType.valueOf(exec.getString(KEY_RES_TYPE))
                , jobParams.getString(DataxUtils.DATAX_NAME), jobParams.getString(KEY_TABLE));
        if (jobParams.containsKey(KEY_PRE)) {
            triggersConfig.preTrigger = jobParams.getString(KEY_PRE);
        }

        if (jobParams.containsKey(KEY_POST)) {
            triggersConfig.postTrigger = jobParams.getString(KEY_POST);
        }


        CuratorDataXTaskMessage taskMessage = null;


        JSONArray splitTabCfgs = exec.getJSONArray(KEY_JOB_INFO);
        List<SplitTabInfo> splits = splitTabCfgs.toJavaList(SplitTabInfo.class);
        for (SplitTabInfo s : splits) {
            taskMessage = jobParams.getObject(KEY_EXEC, CuratorDataXTaskMessage.class);
            taskMessage.setJobName(s.getDataXInfo());
            taskMessage.setTaskSerializeNum(s.getTaskSerializeNum());
            triggersConfig.addSplitCfg(taskMessage);
        }
        return triggersConfig;
    }

    public JSONObject createMRParams() {

        if (this.mrParams == null) {
            JSONObject execCfg = null;
            JSONArray dataxJobInfo = null;
            PowerJobRemoteTaskTrigger splitTabTrigger = null;
            mrParams = new JSONObject();
            mrParams.put(KEY_TABLE, this.entry.getName());
            mrParams.put(DataxUtils.DATAX_NAME, this.appSource.identityValue());
            if (this.getPreTrigger() != null) {
                mrParams.put(KEY_PRE, this.getPreTrigger().getTaskName());
            }

            if (this.getPostTrigger() != null) {
                mrParams.put(KEY_POST, this.getPostTrigger().getTaskName());
            }

            execCfg = null;

            for (IRemoteTaskTrigger splitTrigger : this.getSplitTabTriggers()) {
                splitTabTrigger = Objects.requireNonNull((PowerJobRemoteTaskTrigger) splitTrigger);
                if (execCfg == null) {
                    mrParams.put(KEY_EXEC, execCfg = JSONObject.parseObject(splitTabTrigger.getTskMsgSerialize()));
                    dataxJobInfo = new JSONArray();
                    execCfg.put(KEY_JOB_INFO, dataxJobInfo);
                }

                dataxJobInfo.add(new SplitTabInfo(splitTabTrigger.getTaskSerializeNum(), splitTabTrigger.getDataXJobInfo().serialize()));
            }
        }


        return mrParams;
    }

    public static class SplitTabInfo {
        private int taskSerializeNum;
        private String dataXInfo;

        public SplitTabInfo(int taskSerializeNum, String dataXInfo) {
            this.taskSerializeNum = taskSerializeNum;
            this.dataXInfo = dataXInfo;
        }

        public int getTaskSerializeNum() {
            return taskSerializeNum;
        }

        public String getDataXInfo() {
            return dataXInfo;
        }

        public void setTaskSerializeNum(int taskSerializeNum) {
            this.taskSerializeNum = taskSerializeNum;
        }

        public void setDataXInfo(String dataXInfo) {
            this.dataXInfo = dataXInfo;
        }
    }

    public static class PowerJobRemoteTaskTrigger implements IRemoteTaskTrigger {
        private final DataXJobInfo dataXJobInfo;
        private final CuratorDataXTaskMessage tskMsg;

        public PowerJobRemoteTaskTrigger(DataXJobInfo dataXJobInfo, CuratorDataXTaskMessage tskMsg) {
            this.dataXJobInfo = dataXJobInfo;
            this.tskMsg = tskMsg;
        }

        @Override
        public String getTaskName() {
            return dataXJobInfo.jobFileName;
        }

        public String getTskMsgSerialize() {
            this.tskMsg.setJobName(null);
            return CuratorDataXTaskMessage.serialize(this.tskMsg);
        }

        public int getTaskSerializeNum() {
            return tskMsg.getTaskSerializeNum();
        }

        public DataXJobInfo getDataXJobInfo() {
            return this.dataXJobInfo;
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException();
        }
    }
}
