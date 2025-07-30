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
package com.qlangtech.tis.realtime.yarn.rpc;

import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月7日
 */
public class UpdateCounterMap {

    private HashMap<PipelineFlinkTaskId, TableSingleDataIndexStatus> data = new HashMap<>();

    // 增量转发节点执行增量的数量
    private long gcCounter;

    // 从哪个地址发送过来的
    private String from;

    private long updateTime;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public long getGcCounter() {
        return gcCounter;
    }

    public void setGcCounter(long gcCounter) {
        this.gcCounter = gcCounter;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    public boolean containIndex(PipelineFlinkTaskId indexName) {
        return data.containsKey(indexName);
    }

    public boolean containsIndex(PipelineFlinkTaskId indexName, String uuid) {
        TableSingleDataIndexStatus tableSingleDataIndexStatus = data.get(indexName);
        if (tableSingleDataIndexStatus == null) {
            return false;
        } else {
            return StringUtils.equals(tableSingleDataIndexStatus.getUUID(), uuid);
        }
    }


    public void setPipelineTableCounterMetric(
            PipelineFlinkTaskId flinkTaskId, TableSingleDataIndexStatus tableUpdateCounter) {
        this.data.put(flinkTaskId, tableUpdateCounter);
    }

    public HashMap<PipelineFlinkTaskId, TableSingleDataIndexStatus> getData() {
        return this.data;
    }

}
