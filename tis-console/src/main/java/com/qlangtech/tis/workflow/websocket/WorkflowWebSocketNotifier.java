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

package com.qlangtech.tis.workflow.websocket;

import com.alibaba.fastjson.JSONObject;

/**
 * Utility class for workflow WebSocket notifications
 * Provides convenient methods to push workflow and node status updates
 *
 * @author 百岁 (mozhenghua@gmail.com)
 * @date 2026-01-30
 */
public class WorkflowWebSocketNotifier {

    /**
     * Notify node status change
     *
     * @param instanceId workflow instance ID
     * @param nodeId     node ID
     * @param status     new status (WAITING/RUNNING/SUCCEED/FAILED/STOPPED)
     */
    public static void notifyNodeStatusChange(Long instanceId, String nodeId, String status) {
        notifyNodeStatusChange(instanceId, nodeId, status, null);
    }

    /**
     * Notify node status change with details
     *
     * @param instanceId workflow instance ID
     * @param nodeId     node ID
     * @param status     new status
     * @param details    additional details (optional)
     */
    public static void notifyNodeStatusChange(Long instanceId, String nodeId, String status, JSONObject details) {
        if (instanceId == null || nodeId == null || status == null) {
            return;
        }

        if (details == null) {
            details = new JSONObject();
        }

        WorkflowMonitorWebSocketHandler.broadcastNodeStatusChange(instanceId, nodeId, status, details);
    }

    /**
     * Notify workflow completion
     *
     * @param instanceId workflow instance ID
     * @param status     final status (SUCCEED/FAILED/STOPPED)
     */
    public static void notifyWorkflowCompletion(Long instanceId, String status) {
        notifyWorkflowCompletion(instanceId, status, null);
    }

    /**
     * Notify workflow completion with details
     *
     * @param instanceId workflow instance ID
     * @param status     final status
     * @param details    additional details (optional)
     */
    public static void notifyWorkflowCompletion(Long instanceId, String status, JSONObject details) {
        if (instanceId == null || status == null) {
            return;
        }

        if (details == null) {
            details = new JSONObject();
        }

        WorkflowMonitorWebSocketHandler.broadcastWorkflowCompletion(instanceId, status, details);
    }

    /**
     * Notify node execution started
     *
     * @param instanceId   workflow instance ID
     * @param nodeId       node ID
     * @param workerAddress worker address
     */
    public static void notifyNodeStarted(Long instanceId, String nodeId, String workerAddress) {
        JSONObject details = new JSONObject();
        details.put("workerAddress", workerAddress);
        details.put("startTime", System.currentTimeMillis());
        notifyNodeStatusChange(instanceId, nodeId, "RUNNING", details);
    }

    /**
     * Notify node execution succeeded
     *
     * @param instanceId workflow instance ID
     * @param nodeId     node ID
     * @param duration   execution duration in milliseconds
     */
    public static void notifyNodeSucceeded(Long instanceId, String nodeId, long duration) {
        JSONObject details = new JSONObject();
        details.put("duration", duration);
        details.put("endTime", System.currentTimeMillis());
        notifyNodeStatusChange(instanceId, nodeId, "SUCCEED", details);
    }

    /**
     * Notify node execution failed
     *
     * @param instanceId   workflow instance ID
     * @param nodeId       node ID
     * @param errorMessage error message
     */
    public static void notifyNodeFailed(Long instanceId, String nodeId, String errorMessage) {
        JSONObject details = new JSONObject();
        details.put("errorMessage", errorMessage);
        details.put("endTime", System.currentTimeMillis());
        notifyNodeStatusChange(instanceId, nodeId, "FAILED", details);
    }

    /**
     * Get active WebSocket session count
     *
     * @return number of active sessions
     */
    public static int getActiveSessionCount() {
        return WorkflowMonitorWebSocketHandler.getActiveSessionCount();
    }

    /**
     * Get subscription count for a workflow instance
     *
     * @param instanceId workflow instance ID
     * @return number of subscribers
     */
    public static int getSubscriptionCount(Long instanceId) {
        return WorkflowMonitorWebSocketHandler.getSubscriptionCount(instanceId);
    }
}
