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

package com.qlangtech.tis.coredefine.module.action;

import java.util.List;

/**
 * Flink Job Checkpoint 简化信息（供前端监控面板使用）
 */
public class JobCheckpointInfo {

    private boolean available = false;
    private String errMsg;

    // 最近一次 checkpoint
    private Long latestDuration;   // ms
    private Long latestSize;       // bytes
    private Long latestTriggerTime; // timestamp
    private String latestStatus;   // COMPLETED / FAILED / IN_PROGRESS

    // 历史统计
    private int totalCount;
    private int completedCount;
    private int failedCount;

    // 最近 N 次历史（用于图表）
    private List<CheckpointHistoryItem> history;

    public static class CheckpointHistoryItem {
        private long id;
        private String status;
        private long triggerTime;
        private long duration;
        private long size;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getTriggerTime() { return triggerTime; }
        public void setTriggerTime(long triggerTime) { this.triggerTime = triggerTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getErrMsg() { return errMsg; }
    public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
    public Long getLatestDuration() { return latestDuration; }
    public void setLatestDuration(Long latestDuration) { this.latestDuration = latestDuration; }
    public Long getLatestSize() { return latestSize; }
    public void setLatestSize(Long latestSize) { this.latestSize = latestSize; }
    public Long getLatestTriggerTime() { return latestTriggerTime; }
    public void setLatestTriggerTime(Long latestTriggerTime) { this.latestTriggerTime = latestTriggerTime; }
    public String getLatestStatus() { return latestStatus; }
    public void setLatestStatus(String latestStatus) { this.latestStatus = latestStatus; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<CheckpointHistoryItem> getHistory() { return history; }
    public void setHistory(List<CheckpointHistoryItem> history) { this.history = history; }
}