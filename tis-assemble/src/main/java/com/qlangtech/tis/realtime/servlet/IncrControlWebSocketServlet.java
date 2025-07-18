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

package com.qlangtech.tis.realtime.servlet;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.manage.servlet.IncrTagHeatBeatMonitor;
import com.qlangtech.tis.manage.servlet.TopicTagIncrStatus;
import com.qlangtech.tis.manage.servlet.TopicTagIncrStatus.FocusTags;
import com.qlangtech.tis.manage.servlet.TopicTagStatus;
import com.qlangtech.tis.plugin.rate.IndexCollectionConfig;
import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.trigger.jst.MonotorTarget;
import com.qlangtech.tis.trigger.jst.RegisterMonotorTarget;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import com.qlangtech.tis.trigger.socket.LogType;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-17 15:23
 **/
public class IncrControlWebSocketServlet extends WebSocketServlet {
    private static final Logger logger = LoggerFactory.getLogger(IncrControlWebSocketServlet.class);

    private IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocol;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void configure(WebSocketServletFactory factory) {
        this.incrStatusUmbilicalProtocol = IncrStatusUmbilicalProtocolImpl.getInstance();
        factory.getPolicy().setIdleTimeout(240000);
        factory.getPolicy().setAsyncWriteTimeout(-1);
        factory.setCreator((req, rep) -> {
            return new IncrSocket(incrStatusUmbilicalProtocol);
        });

    }


    public class IncrSocket extends WebSocketAdapter implements ILogListener {
        private DataXName collectionName;
        private final IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtoco;

        public IncrSocket(IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtoco) {
            this.incrStatusUmbilicalProtoco = incrStatusUmbilicalProtoco;
        }

        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            this.collectionName = DataXName.createDataXPipeline(getParameter("collection", Collections.singletonList(MonotorTarget.DUMP_COLLECTION)));
            List<RegisterMonotorTarget> typies = RegisterMonotorTarget.parseLogTypes(this.collectionName, -1, this.getParameter("logtype"));
            addMonitor(typies);
        }

        private void addMonitor(List<RegisterMonotorTarget> typies) {
            typies.forEach((t) -> {
                try {
                    addMonitor(t);
                } catch (Exception e) {
                    logger.error(t.toString(), e);
                    throw new RuntimeException(e);
                }
            });
        }

        private String getParameter(String key) {
            return this.getParameter(key, Collections.emptyList());
        }

        private String getParameter(String key, List<String> dft) {
            Map<String, List<String>> params = this.getSession().getUpgradeRequest().getParameterMap();
            for (String v : params.getOrDefault(key, dft)) {
                return v;
            }
            throw new IllegalArgumentException("key:" + key + " relevant val is not exist in request");
        }

        /**
         * @param monitorTarget
         */
        private void addMonitor(MonotorTarget monitorTarget) throws Exception {

            if (monitorTarget.testLogType(LogType.ALL_RUNNING_PIPELINE_CONSUME_TAGS_STATUS)) {
                // 实时取得正在运行的增量实例
                executorService.execute(() -> {
                    while (!this.isClosed()) {
                        try {

                            ExecuteState<Map<String, Long>> event
                                    = ExecuteState.create(
                                    LogType.ALL_RUNNING_PIPELINE_CONSUME_TAGS_STATUS, incrStatusUmbilicalProtoco.getRunPipelineIncrAccumulationCount());
                            sendMsg2Client(event);
                            Thread.sleep(8000);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else if (monitorTarget.testLogType(LogType.MQ_TAGS_STATUS)) {
                // PluginStore<MQListenerFactory> mqListenerFactory = (PluginStore<MQListenerFactory>) TIS.getPluginStore(this.collectionName, MQListenerFactory.class);
                // MQListenerFactory plugin = mqListenerFactory.getPlugin();
                // 增量节点处理
                final Map<String, TopicTagStatus> /* this.tag */
                        transferTagStatus = new HashMap<>();
                final Map<String, TopicTagStatus> /* this.tag */
                        binlogTopicTagStatus = new HashMap<>();
                List<TopicTagIncrStatus.FocusTags> focusTags = getFocusTags(collectionName.getPipelineName());
                // 如果size为0，则说明远程工作节点没有正常执行
                if (focusTags.size() > 0) {
                    TopicTagIncrStatus topicTagIncrStatus = new TopicTagIncrStatus(focusTags);
                    executorService.execute(() -> {
                        IndexCollectionConfig collectionConfig = IndexCollectionConfig.getIndexCollectionConfig(collectionName);

                        Long collectionInterval = Optional.ofNullable(collectionConfig)
                                .map((cfg) -> cfg.duration.toMillis()).orElse(IndexCollectionConfig.defaultDuration() * 1000l);

                        IncrTagHeatBeatMonitor incrTagHeatBeatMonitor = new IncrTagHeatBeatMonitor(this.incrStatusUmbilicalProtoco, this.collectionName.getPipelineName(), this
                                , transferTagStatus, binlogTopicTagStatus, topicTagIncrStatus, collectionInterval);
                        incrTagHeatBeatMonitor.build();
                    });
                }
            } else {
                throw new IllegalStateException("monitor type:" + monitorTarget + " is illegal");
            }
        }

        public List<TopicTagIncrStatus.FocusTags> getFocusTags(String collectionName) throws MalformedURLException {
            TopicTagIncrStatus.FocusTags focusTags = new FocusTags(collectionName, Collections.singletonList(IIncreaseCounter.TABLE_CONSUME_COUNT));
            return Collections.singletonList(focusTags);
            //
//    JobType.RemoteCallResult<TopicInfo> topicInfo = JobType.ACTION_getTopicTags.assembIncrControlWithResult(
//      CoreAction.getAssembleNodeAddress(zookeeper),
//      collectionName, Collections.emptyList(), TopicInfo.class);
//    if (topicInfo.biz.getTopicWithTags().size() < 1) {
//      // 返回为空的话可以证明没有正常启动
//      return Collections.emptyList();
//    }
//    TopicInfo topicTags = topicInfo.biz;
//    return topicTags.getTopicWithTags()
//      .entrySet().stream().map((entry) -> new TopicTagIncrStatus.FocusTags(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        }

        @Override
        public void sendMsg2Client(Object biz) throws IOException {
            sendMsg2Client(JSON.toJSONString(biz, false));
        }

        private void sendMsg2Client(String jsonContent) throws IOException {
            synchronized (IncrSocket.this) {
                if (this.isClosed()) {
                    throw new IllegalStateException("ws conn has closed,jsonContent:" + jsonContent);
                }
                // webSocket 不能多线程发送消息，所以要在这里加一个锁
                // https://stackoverflow.com/questions/36305830/blocking-message-pending-10000-for-blocking-using-spring-websockets
                this.getRemote().sendString(jsonContent);
            }
        }

        @Override
        public void read(Object event) {

        }

        @Override
        public boolean isClosed() {
            return this.isNotConnected();
        }
    }
}
