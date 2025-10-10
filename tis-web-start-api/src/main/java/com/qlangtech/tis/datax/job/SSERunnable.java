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

import com.qlangtech.tis.coredefine.module.action.ResName;
import com.qlangtech.tis.trigger.feedback.IJobFeedback;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 由于PowerJob集群启动需要比较长时间，需要通过SSE（Server Send Event技术将服务端执行的状态反馈给客户端）
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-23 13:34
 **/
public interface SSERunnable extends Runnable, IJobFeedback {
    char splitChar = '\005';
    static ThreadLocal<SSERunnable> local = new ThreadLocal<>();

    Map<Class, Object> contextAttrs = new HashMap<>();

    default <T> void setContextAttr(Class<T> key, T val) {
        contextAttrs.put(Objects.requireNonNull(key, "key can not be null")
                , Objects.requireNonNull(val, "val can not be null"));
    }

    default <T> void cleanContextAttr(Class<T> key) {
        contextAttrs.remove(Objects.requireNonNull(key));
    }

    default <T> T getContextAttr(Class<T> key) {
        return Objects.requireNonNull((T) contextAttrs.get(key), "key:" + key + " relevant instance can not be null");
    }

    public static void setLocalThread(SSERunnable sseRunnable) {
        local.set(sseRunnable);
    }

    public static SSERunnable createMock() {
        return new SSERunnable() {
            @Override
            public void writeComplete(ResName subJob, boolean success) {

            }

            @Override
            public void info(String serviceName, long timestamp, String msg) {
                System.out.println("sseInfo:" + msg);
            }

            @Override
            public void error(String serviceName, long timestamp, String msg) {
                System.out.println("sseError:" + msg);
            }

            @Override
            public void fatal(String serviceName, long timestamp, String msg) {
            }

            @Override
            public void run() {
            }
        };
    }

    /**
     * 执行流程中取得写入实例
     *
     * @return
     */
    public static SSERunnable getLocal() {
        return Objects.requireNonNull(local.get(), "instance shall not null in threadlocal");
    }

    public static boolean sseAware() {
        return local.get() != null;
    }

    /**
     * 成功执行之后回调执行
     */
    default void afterLaunched() {

    }

    /**
     * 子任务执行状态
     *
     * @param subJob
     * @param success
     */
    public void writeComplete(ResName subJob, boolean success);

    enum SSEEventType {
        TASK_MILESTONE("taskMilestone"),
        TASK_EXECUTE_STEPS("executeSteps"),
        TASK_LOG("message"),

        /**
         * AI AGENT relevant
         */
        AI_AGNET_SESSION("session"),
        AI_AGNET_MESSAGE("ai_agent_message"),
        AI_AGNET_TOKEN("ai_agent_token"),
        AI_AGNET_PROGRESS("ai_agent_progress"),
        AI_AGNET_PLUGIN("ai_agent_plugin"),
        AI_AGNET_ERROR("ai_agent_error"),
        AI_AGENT_INPUT_REQUEST("ai_agent_input_request"),
        /**
         * 让用户选择不同的Descriptor（例如：MySQL-v5、MySQL-v8之一）
         */
        AI_AGNET_SELECTION_REQUEST("ai_agent_selection_request"),
        AI_AGNET_DONE("ai_agent_done");

        private final String eventType;

        private SSEEventType(String type) {
            this.eventType = type;
        }

        public static SSEEventType parse(String token) {
            for (SSEEventType type : SSEEventType.values()) {
                if (type.eventType.equalsIgnoreCase(token)) {
                    return type;
                }
            }
            throw new IllegalStateException("illegal token:" + token);
        }

        public String getEventType() {
            return this.eventType;
        }
    }


}
