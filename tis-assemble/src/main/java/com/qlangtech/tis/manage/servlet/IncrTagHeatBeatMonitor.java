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
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.realtime.transfer.ListenerStatusKeeper.LimitRateTypeAndRatePerSecNums;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import com.qlangtech.tis.trigger.socket.LogType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-31 10:04
 */
public class IncrTagHeatBeatMonitor {

    private static final Logger logger = LoggerFactory.getLogger(IncrTagHeatBeatMonitor.class);

    private final ILogListener messagePush;

    private final Map<String, TopicTagStatus> transferTagStatus;

    private final Map<String, TopicTagStatus> binlogTopicTagStatus;

    private final TopicTagIncrStatus topicTagIncrStatus;


    private final String collectionName;

    private final Long collectionInterval;

    private final IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtoco;

    public IncrTagHeatBeatMonitor(IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtoco, String collectionName
            , ILogListener messagePush, Map<String, TopicTagStatus> transferTagStatus
            , Map<String, TopicTagStatus> binlogTopicTagStatus, TopicTagIncrStatus topicTagIncrStatus //
            , Long collectionInterval) {
        this.collectionName = collectionName;
        this.messagePush = messagePush;
        this.transferTagStatus = transferTagStatus;
        this.binlogTopicTagStatus = binlogTopicTagStatus;
        this.topicTagIncrStatus = topicTagIncrStatus;
        this.collectionInterval = Objects.requireNonNull(collectionInterval, "collectionInterval can not be null");
        this.incrStatusUmbilicalProtoco = incrStatusUmbilicalProtoco;
    }

    public void build() {
        TopicTagStatus tagStat = null;
        TopicTagIncrStatus.TisIncrStatus averageTopicTagIncr;
        LimitRateTypeAndRatePerSecNums rateLimitState = null;
        try {
            while (!messagePush.isClosed()) {
                // long start = System.currentTimeMillis();
                long currSec = (System.currentTimeMillis() / 1000);
                rateLimitState = getIncrTransferTagUpdateMap(transferTagStatus, collectionName);
//        for (String tabTag : topicTagIncrStatus.getFocusTags()) {
//          topicTagIncrStatus.add(currSec, TopicTagIncrStatus.TopicTagIncr.create(tabTag, Collections.emptyMap(), transferTagStatus));
//        }
                for (String summaryKey : TopicTagIncrStatus.ALL_SUMMARY_KEYS) {
                    topicTagIncrStatus.add(currSec, TopicTagIncrStatus.TopicTagIncr.create(summaryKey, transferTagStatus));
                }
                // logger.info("p4{}", System.currentTimeMillis() - start);
                // start = System.currentTimeMillis();
                averageTopicTagIncr = topicTagIncrStatus.getAverageTopicTagIncr(false, /** average */false);

                averageTopicTagIncr.setRateLimitConfig(rateLimitState);
                // logger.info("p5{}", System.currentTimeMillis() - start);
                // start = System.currentTimeMillis();
                ExecuteState<TopicTagIncrStatus.TisIncrStatus> event = ExecuteState.create(LogType.MQ_TAGS_STATUS, averageTopicTagIncr);
                messagePush.sendMsg2Client(event);
                // start = System.currentTimeMillis();
                try {
                    Thread.sleep(collectionInterval);
                } catch (InterruptedException e) {
                }
            }
        } catch (Exception e) {
            logger.error(this.collectionName, e);
            throw new RuntimeException(e);
        } finally {
            // consumerStatus.close();
        }
    }

    /**
     * @param collection
     * @throws Exception
     */
    private LimitRateTypeAndRatePerSecNums getIncrTransferTagUpdateMap(
            final Map<String, /* this.tag */    TopicTagStatus> transferTagStatus, String collection) throws Exception {

        final Pair<Map<String, /* tag */ Long>, LimitRateTypeAndRatePerSecNums> /* absolute count */
                tagCountMap = this.incrStatusUmbilicalProtoco.getUpdateAbsoluteCountMap(collection);

        for (Map.Entry<String, Long> entry : tagCountMap.getKey().entrySet()) {
            setMetricCount(transferTagStatus, entry.getKey(), entry.getValue());
        }

        LimitRateTypeAndRatePerSecNums limitRateConfig = tagCountMap.getValue();
        return limitRateConfig;
        // curl -d"collection=search4totalpay&action=collection_topic_tags_status" http://localhost:8080/incr-control?collection=search4totalpay
        // http://localhost:8083/tis-assemble/incr-control?collection=mysql_mysql&action=collection_topic_tags_status
//        JobType.RemoteCallResult<Void> tagCountMap
//                = JobType.Collection_TopicTags_status.assembIncrControl(
//                CoreAction.getAssembleNodeAddress(coordinator),
//                collection, Collections.emptyList(), new JobType.IAssembIncrControlResult() {
//                    @Override
//                    public LogFeedbackServlet.TagCountMap deserialize(JSONObject json) {
//                        // LogFeedbackServlet.TagCountMap result = new LogFeedbackServlet.TagCountMap();
//                        for (String key : json.keySet()) {
//                            setMetricCount(transferTagStatus, key, json.getIntValue(key));
//                        }
//                        return null;
//                    }
//                });
    }

    private void setMetricCount(Map<String, TopicTagStatus> tagStatus, String tagName, Long count) {
        TopicTagStatus tagStat;
        tagStat = tagStatus.get(tagName);
        logger.info("tagName:{},count:{}", tagName, count);
        if (tagStat == null) {
            // String topic, String tag, int count, long lastUpdates
            tagStat = new TopicTagStatus(StringUtils.EMPTY, tagName, count, System.currentTimeMillis());
            tagStatus.put(tagName, tagStat);
        } else {
            tagStat.setCount(count);
            tagStat.setLastUpdate(System.currentTimeMillis());
            // tagStat.merge(tagStat);
        }
    }
}
