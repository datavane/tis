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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket handler for workflow monitoring
 * Handles client connections, subscriptions, and real-time updates
 *
 * @author 百岁 (mozhenghua@gmail.com)
 * @date 2026-01-30
 */
public class WorkflowMonitorWebSocketHandler extends WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowMonitorWebSocketHandler.class);

    // All active sessions
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    // Subscription map: instanceId -> Set<Session>
    private static final Map<Long, Set<Session>> subscriptions = new ConcurrentHashMap<>();

    // Session to subscribed instances map
    private static final Map<Session, Set<Long>> sessionSubscriptions = new ConcurrentHashMap<>();

    // Heartbeat scheduler
    private static final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // Start heartbeat task (every 30 seconds)
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                logger.error("Failed to send heartbeat", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        sessions.add(session);
        sessionSubscriptions.put(session, new CopyOnWriteArraySet<>());
        logger.info("WebSocket connected: {}, total sessions: {}", session.getRemoteAddress(), sessions.size());

        // Send welcome message
        sendMessage(session, createMessage("connected", "Welcome to workflow monitor"));
    }

    @Override
    public void onWebSocketText(String message) {
        Session session = getSession();
        logger.debug("Received message from {}: {}", session.getRemoteAddress(), message);

        try {
            JSONObject json = JSON.parseObject(message);
            String type = json.getString("type");

            switch (type) {
                case "subscribe":
                    handleSubscribe(session, json);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, json);
                    break;
                case "ping":
                    handlePing(session);
                    break;
                default:
                    logger.warn("Unknown message type: {}", type);
                    sendMessage(session, createErrorMessage("Unknown message type: " + type));
            }
        } catch (Exception e) {
            logger.error("Failed to process message: " + message, e);
            sendMessage(session, createErrorMessage("Failed to process message: " + e.getMessage()));
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Session session = getSession();
        logger.info("WebSocket closed: {}, status: {}, reason: {}", session.getRemoteAddress(), statusCode, reason);

        // Clean up subscriptions
        Set<Long> subscribedInstances = sessionSubscriptions.remove(session);
        if (subscribedInstances != null) {
            for (Long instanceId : subscribedInstances) {
                Set<Session> subscribers = subscriptions.get(instanceId);
                if (subscribers != null) {
                    subscribers.remove(session);
                    if (subscribers.isEmpty()) {
                        subscriptions.remove(instanceId);
                    }
                }
            }
        }

        sessions.remove(session);
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        logger.error("WebSocket error: " + getSession().getRemoteAddress(), cause);
        super.onWebSocketError(cause);
    }

    /**
     * Handle subscribe request
     */
    private void handleSubscribe(Session session, JSONObject json) {
        Long instanceId = json.getLong("instanceId");
        if (instanceId == null) {
            sendMessage(session, createErrorMessage("instanceId is required"));
            return;
        }

        // Add subscription
        subscriptions.computeIfAbsent(instanceId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionSubscriptions.get(session).add(instanceId);

        logger.info("Session {} subscribed to workflow instance {}", session.getRemoteAddress(), instanceId);
        sendMessage(session, createMessage("subscribed", "Subscribed to instance " + instanceId));
    }

    /**
     * Handle unsubscribe request
     */
    private void handleUnsubscribe(Session session, JSONObject json) {
        Long instanceId = json.getLong("instanceId");
        if (instanceId == null) {
            sendMessage(session, createErrorMessage("instanceId is required"));
            return;
        }

        // Remove subscription
        Set<Session> subscribers = subscriptions.get(instanceId);
        if (subscribers != null) {
            subscribers.remove(session);
            if (subscribers.isEmpty()) {
                subscriptions.remove(instanceId);
            }
        }

        Set<Long> subscribedInstances = sessionSubscriptions.get(session);
        if (subscribedInstances != null) {
            subscribedInstances.remove(instanceId);
        }

        logger.info("Session {} unsubscribed from workflow instance {}", session.getRemoteAddress(), instanceId);
        sendMessage(session, createMessage("unsubscribed", "Unsubscribed from instance " + instanceId));
    }

    /**
     * Handle ping request
     */
    private void handlePing(Session session) {
        sendMessage(session, createMessage("pong", "pong"));
    }

    /**
     * Send heartbeat to all sessions
     */
    private static void sendHeartbeat() {
        JSONObject heartbeat = createMessage("heartbeat", System.currentTimeMillis());
        broadcast(heartbeat.toJSONString());
    }

    /**
     * Broadcast node status change to subscribers
     */
    public static void broadcastNodeStatusChange(Long instanceId, String nodeId, String status, JSONObject details) {
        Set<Session> subscribers = subscriptions.get(instanceId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        JSONObject message = new JSONObject();
        message.put("type", "nodeStatusChange");
        message.put("instanceId", instanceId);
        message.put("nodeId", nodeId);
        message.put("status", status);
        message.put("details", details);
        message.put("timestamp", System.currentTimeMillis());

        String messageStr = message.toJSONString();
        for (Session session : subscribers) {
            sendMessage(session, messageStr);
        }

        logger.debug("Broadcasted node status change to {} subscribers: instance={}, node={}, status={}",
                subscribers.size(), instanceId, nodeId, status);
    }

    /**
     * Broadcast workflow completion to subscribers
     */
    public static void broadcastWorkflowCompletion(Long instanceId, String status, JSONObject details) {
        Set<Session> subscribers = subscriptions.get(instanceId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        JSONObject message = new JSONObject();
        message.put("type", "workflowCompletion");
        message.put("instanceId", instanceId);
        message.put("status", status);
        message.put("details", details);
        message.put("timestamp", System.currentTimeMillis());

        String messageStr = message.toJSONString();
        for (Session session : subscribers) {
            sendMessage(session, messageStr);
        }

        logger.info("Broadcasted workflow completion to {} subscribers: instance={}, status={}",
                subscribers.size(), instanceId, status);
    }

    /**
     * Broadcast message to all sessions
     */
    private static void broadcast(String message) {
        for (Session session : sessions) {
            sendMessage(session, message);
        }
    }

    /**
     * Send message to a session
     */
    private static void sendMessage(Session session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                logger.error("Failed to send message to session: " + session.getRemoteAddress(), e);
            }
        }
    }

    /**
     * Send JSON message to a session
     */
    private static void sendMessage(Session session, JSONObject message) {
        sendMessage(session, message.toJSONString());
    }

    /**
     * Create a message JSON object
     */
    private static JSONObject createMessage(String type, Object data) {
        JSONObject message = new JSONObject();
        message.put("type", type);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * Create an error message JSON object
     */
    private static JSONObject createErrorMessage(String error) {
        JSONObject message = new JSONObject();
        message.put("type", "error");
        message.put("error", error);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * Get active session count
     */
    public static int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Get subscription count for an instance
     */
    public static int getSubscriptionCount(Long instanceId) {
        Set<Session> subscribers = subscriptions.get(instanceId);
        return subscribers != null ? subscribers.size() : 0;
    }
}
