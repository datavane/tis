/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.jst;

import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.HttpUtils.PostParam;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.trigger.jst.TopicTagIncrStatus.TopicTagIncr;
import com.qlangtech.tis.trigger.socket.ExecuteState;
import com.qlangtech.tis.trigger.socket.LogType;
import com.qlangtech.tis.trigger.utils.RefCounted;

/*
 * 日志服务客户端管理器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LogCollectorClientManager {

    private static final String KEY_SOLR_CONSUME = "solrConsume";

    private static final String KEY_TABLE_CONSUME_COUNT = "tableConsumeCount";

    public static final Set<String> ALL_SUMMARY_KEYS = Sets.newHashSet(KEY_SOLR_CONSUME, KEY_TABLE_CONSUME_COUNT);

    private static final Logger log = LoggerFactory.getLogger(LogCollectorClientManager.class);

    private Map<MonotorTarget, RefCounted<ScheduledExecutorService>> tagsMonitor = new HashMap<>();

    // 监听的WebSocket列表
    private final Set<ILogListener> audienceList = new HashSet<>();

    private LogCollectorClientManager() {
    }

    private static LogCollectorClientManager instance;

    public static LogCollectorClientManager getInstance() {
        if (instance == null) {
            synchronized (LogCollectorClientManager.class) {
                if (instance == null) {
                    instance = new LogCollectorClientManager();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) throws Exception {
    }

    // private final AtomicReference<LogCollector> logCollector = new
    // AtomicReference<LogCollector>();
    // private final MonitorRegisterRepository logListener = new
    // MonitorRegisterRepository();
    // public List<ILogListener> getLogListener(MonotorTarget monitorTarget) {
    // List<ILogListener> listeners = logListener.get(monitorTarget);
    // return listeners;
    // }
    // 
    public Set<ILogListener> getAllListener() {
        return this.audienceList;
    }

    // private LogCollector getLogCollector() {
    // return LogCollector.getCollector(this);
    // }
    private static final String KEY_JSON_LAST = "last";

    public void registerListener(ILogListener listener) {
        synchronized (this.audienceList) {
            this.audienceList.add(listener);
        }
    }

    public void unregisterListener(ILogListener listener) {
        synchronized (this.audienceList) {
            if (this.audienceList.remove(listener)) {
                for (MonotorTarget monitorTarget : listener.getMonitorTypes()) {
                    if (monitorTarget.getLogType() == LogType.MQ_TAGS_STATUS) {
                        RefCounted<ScheduledExecutorService> scheduledRef = tagsMonitor.get(monitorTarget);
                        if (scheduledRef != null) {
                            scheduledRef.decref();
                        }
                    } else {
                        LogCollector collector = LogCollector.getCollector();
                        try {
                            // 需要向服务端发送一条监听命令
                            collector.sendInfo(RegisterMonotorTarget.createUnregister(monitorTarget));
                        } finally {
                            // 关闭长连接
                            collector.close();
                        }
                    }
                }
            }
        }
    }

    // private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT_SEC = new
    // ThreadLocal<SimpleDateFormat>() {
    // @Override
    // protected SimpleDateFormat initialValue() {
    // return new SimpleDateFormat("yyyyMMddHHmmss");
    // }
    // };
    /**
     * 注册监听
     *
     * @param monitorTarget
     * @param listener
     */
    public void registerMonitorEvent(MonotorTarget monitorTarget) throws Exception {
        try {
            synchronized (audienceList) {
                final Set<ILogListener> finallisteners = Collections.unmodifiableSet(audienceList);
                if (monitorTarget.getLogType() == LogType.MQ_TAGS_STATUS) {
                    RefCounted<ScheduledExecutorService> refScheduled = null;
                    synchronized (tagsMonitor) {
                        refScheduled = tagsMonitor.get(monitorTarget);
                        if (refScheduled == null || refScheduled.isClosed()) {
                            final List<FocusTags> focusTags = getFocusTags(monitorTarget);
                            if (focusTags.size() < 1) {
                                log.warn("monitorTarget:" + monitorTarget + ",focusTags is empty");
                                return;
                            }
                            StringBuffer tagsParams = new StringBuffer();
                            for (int i = 0; i < focusTags.size(); i++) {
                                tagsParams.append("&").append(focusTags.get(i));
                            }
                            refScheduled = new RefCounted<ScheduledExecutorService>(Executors.newScheduledThreadPool(1)) {

                                @Override
                                protected void init(ScheduledExecutorService resource) {
                                    final Map<String, TopicTagStatus> /* this.tag */
                                    binlogTopicTagStatus = new HashMap<>();
                                    final Map<String, TopicTagStatus> /* this.tag */
                                    transferTagStatus = new HashMap<>();
                                    TopicTagIncrStatus topicTagIncrStatus = new TopicTagIncrStatus(focusTags);
                                    // final Cache<String, TopicTagIncrStatus> c = CacheBuilder.newBuilder()
                                    // .expireAfterWrite(10, TimeUnit.SECONDS)
                                    // .build(new CacheLoader<String, TopicTagIncrStatus>() {
                                    // @Override
                                    // public TopicTagIncrStatus load(String key) throws Exception {
                                    // return new TopicTagIncrStatus();
                                    // }
                                    // });
                                    resource.scheduleAtFixedRate(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                getTopicTagStatus(monitorTarget, tagsParams, binlogTopicTagStatus);
                                                getIncrTransferTagUpdateMap(transferTagStatus, monitorTarget.getCollection());
                                                // KEY_SOLR_CONSUME
                                                long currSec = (System.currentTimeMillis() / 1000);
                                                topicTagIncrStatus.add(currSec, TopicTagIncr.create(KEY_SOLR_CONSUME, binlogTopicTagStatus, transferTagStatus));
                                                topicTagIncrStatus.add(currSec, TopicTagIncr.create(KEY_TABLE_CONSUME_COUNT, binlogTopicTagStatus, transferTagStatus));
                                                for (String tabTag : topicTagIncrStatus.getFocusTags()) {
                                                    topicTagIncrStatus.add(currSec, TopicTagIncr.create(tabTag, binlogTopicTagStatus, transferTagStatus));
                                                }
                                                ExecuteState<TisIncrStatus> event = ExecuteState.create(monitorTarget.logType, (topicTagIncrStatus.getAverageTopicTagIncr(false, /* average */
                                                false)));
                                                event.setServiceName(monitorTarget.getCollection());
                                                for (ILogListener l : finallisteners) {
                                                    l.read(event);
                                                }
                                                log.info(monitorTarget.toString());
                                            } catch (Throwable e) {
                                                log.error(monitorTarget.toString(), e);
                                            }
                                        }
                                    }, 10, 2000, TimeUnit.MILLISECONDS);
                                }

                                @Override
                                protected void close() {
                                    this.resource.shutdown();
                                }
                            };
                            tagsMonitor.put(monitorTarget, refScheduled);
                        }
                        refScheduled.incref();
                    }
                } else {
                    LogCollector logCollector = LogCollector.getCollector();
                    logCollector.start();
                    logCollector.sendInfo(monitorTarget);
                }
                log.info("send monitor register:" + monitorTarget.getCollection() + "," + monitorTarget.getLogType());
            }
        } catch (Exception e) {
            LogCollector.getCollector().processError(e);
            throw e;
        }
    }

    public static class TisIncrStatus {

        private final Map<String, Integer> summary;

        private final List<TopicTagIncr> tags;

        TisIncrStatus(List<TopicTagIncr> summary, List<TopicTagIncr> tags) {
            this.summary = Maps.newHashMap();
            summary.forEach((r) -> {
                this.summary.put(r.getTag(), r.getTrantransferIncr());
            });
            this.tags = tags;
        }

        public Map<String, Integer> getSummary() {
            return this.summary;
        }

        public List<TopicTagIncr> getTags() {
            return this.tags;
        }
    }

    /**
     * @param transfer
     * @param collection
     * @throws Exception
     */
    private void getIncrTransferTagUpdateMap(final Map<String, /* this.tag */
    TopicTagStatus> transferTagStatus, String collection) throws Exception {
        // curl -d"collection=search4totalpay&action=collection_topic_tags_status"
        // http://localhost:8080/incr-control?collection=search4totalpay
        URL apply = new URL(TSearcherConfigFetcher.get().getAssembleHost() + "/incr-control");
        List<PostParam> params = Lists.newArrayList();
        params.add(new PostParam("collection", collection));
        params.add(new PostParam("action", "collection_topic_tags_status"));
        HttpUtils.post(apply, params, new PostFormStreamProcess<String>() {

            @Override
            public String p(int status, InputStream stream, String md5) {
                JSONTokener tokener = new JSONTokener(stream);
                JSONObject result = new JSONObject(tokener);
                result.getJSONArray("tags").forEach(new Consumer<Object>() {

                    @Override
                    public void accept(Object t) {
                        JSONObject o = (JSONObject) t;
                        String tagName = o.getString("name");
                        int count = o.getInt("val");
                        TopicTagStatus tagStat = transferTagStatus.get(tagName);
                        if (tagStat == null) {
                            // String topic, String tag, int count, long lastUpdates
                            tagStat = new TopicTagStatus(StringUtils.EMPTY, tagName, count, -1);
                            transferTagStatus.put(tagName, tagStat);
                        }
                        tagStat.setCount(count);
                    }
                });
                return null;
            }
        });
    }

    void getTopicTagStatus(MonotorTarget monitorTarget, StringBuffer tagsParams, Map<String, TopicTagStatus> topicTagStatus) throws MalformedURLException {
        URL apply = null;
        TopicTagStatus tagStatus = null;
        List<String> mqStatisticsHost = TSearcherConfigFetcher.get().getMQStatisticsHost();
        // / binlog-msg/tag_count?topics=binlogmsg
        Map<String, AtomicLong> /* absolute count */
        tagsAbsoluteCount = new HashMap<>();
        for (String h : mqStatisticsHost) {
            apply = new URL(h + "/binlog-msg/tag_count?tformat=false" + tagsParams.toString());
            System.out.println(apply);
            HttpUtils.post(apply, Collections.emptyList(), new PostFormStreamProcess<Void>() {

                @Override
                public Void p(int status, InputStream stream, String md5) {
                    AtomicLong absoluteCount = null;
                    TopicTagStatus tagStatus = null;
                    TopicTagStatus exist = null;
                    JSONTokener tokener = new JSONTokener(stream);
                    JSONObject o = new JSONObject(tokener);
                    JSONObject tags = null;
                    JSONObject props = null;
                    int sendCount = 0;
                    for (String key : o.keySet()) {
                        tags = o.getJSONObject(key);
                        if (tags != null) {
                            for (String tagKey : tags.keySet()) {
                                // String topic, String tag, int count, String lastUpdate
                                // tagStatus = ;
                                props = tags.getJSONObject(tagKey);
                                sendCount = props.getInt("count");
                                long last = 0;
                                if (!props.isNull(KEY_JSON_LAST)) {
                                    last = props.getLong(KEY_JSON_LAST);
                                }
                                tagStatus = new TopicTagStatus(key, tagKey, 0, last);
                                absoluteCount = tagsAbsoluteCount.get(tagStatus.getTag());
                                if (absoluteCount == null) {
                                    absoluteCount = new AtomicLong();
                                    tagsAbsoluteCount.put(tagStatus.getTag(), absoluteCount);
                                }
                                absoluteCount.addAndGet(sendCount);
                                exist = topicTagStatus.get(tagStatus.getTag());
                                if (exist == null) {
                                    topicTagStatus.put(tagStatus.getTag(), tagStatus);
                                } else {
                                    exist.merge(tagStatus);
                                }
                            }
                        }
                    }
                    return null;
                }
            });
        }
        AtomicLong absoluteCount = null;
        for (Map.Entry<String, TopicTagStatus> e : topicTagStatus.entrySet()) {
            tagStatus = topicTagStatus.get(e.getKey());
            absoluteCount = tagsAbsoluteCount.get(tagStatus.getTag());
            if (absoluteCount != null) {
                tagStatus.setCount(absoluteCount.get());
            } else {
                tagStatus.clean();
            }
        }
    }

    public static class TopicTagStatus {

        @JSONField(serialize = true)
        public String getKey() {
            return this.topic + "." + this.tag;
        }

        /**
         * tab名称
         *
         * @return
         */
        public String getTag() {
            return this.tag;
        }

        private final String topic;

        private final String tag;

        private long count;

        private long incr;

        private long lastUpdateTime;

        public TopicTagStatus(String topic, String tag, int count, long lastUpdate) {
            super();
            this.topic = topic;
            this.tag = tag;
            this.count = count;
            this.lastUpdateTime = lastUpdate;
        }

        public void merge(TopicTagStatus n) {
            if (!StringUtils.equals(this.getKey(), n.getKey())) {
                throw new IllegalArgumentException("key1:" + this.getKey() + ",key2:" + n.getKey() + " is not equal");
            }
            // this.setCount(this.count + n.count);
            if (n.lastUpdateTime > this.lastUpdateTime) {
                this.lastUpdateTime = n.lastUpdateTime;
            }
        }

        public void setCount(long count) {
            if (this.count > 0 && count > this.count) {
                this.incr = count - this.count;
            } else {
                this.incr = 0;
            }
            this.count = count;
        }

        @JSONField(serialize = true)
        public long getIncr() {
            return this.incr;
        }

        @JSONField(serialize = true)
        public long getLastUpdateTime() {
            return this.lastUpdateTime;
        }

        void clean() {
            this.count = 0;
            this.incr = 0;
        }

        @Override
        public String toString() {
            return "topic:" + this.topic + ",tag:" + this.tag + ",count:" + this.count + ",incr:" + this.incr + ",lastUpdate:" + this.lastUpdateTime;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdateTime = lastUpdate;
        }
    }

    private List<FocusTags> getFocusTags(MonotorTarget monitorTarget) throws MalformedURLException {
        final URL apply = new URL(TSearcherConfigFetcher.get().getAssembleHost() + "/incr-control");
        List<PostParam> params = Lists.newArrayList(new PostParam("collection", monitorTarget.getCollection()), new PostParam("action", "getTopicTags"));
        // curl -d"collection=search4totalpay&action=getTopicTags"
        // http://localhost:8080/incr-control?collection=search4totalpay&action=incr_is_launching
        List<FocusTags> focusTags = HttpUtils.post(apply, params, new PostFormStreamProcess<List<FocusTags>>() {

            @Override
            public List<FocusTags> p(int status, InputStream stream, String md5) {
                List<FocusTags> focusTags = new ArrayList<>();
                JSONTokener tokener = new JSONTokener(stream);
                JSONObject result = new JSONObject(tokener);
                if (!result.getBoolean("success")) {
                    return focusTags;
                } else {
                    JSONObject topics = result.getJSONObject("topics");
                    JSONArray tags = null;
                    for (String topic : topics.keySet()) {
                        FocusTags t = new FocusTags(topic);
                        tags = topics.getJSONArray(topic);
                        tags.forEach((tt) -> {
                            t.tags.add((String) tt);
                        });
                        focusTags.add(t);
                    }
                }
                return focusTags;
            }
        });
        return focusTags;
    }

    public static class FocusTags {

        private static final Joiner joiner = Joiner.on(",").skipNulls();

        private final String topic;

        private List<String> tags = new ArrayList<String>();

        public FocusTags(String topic) {
            this.topic = topic;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public String getTopic() {
            return topic;
        }

        @Override
        public String toString() {
            return this.createParams();
        }

        private String createParams() {
            List<String> result = Lists.asList("topic=" + topic, tags.toArray(new String[] {}));
            return joiner.join(result);
        }
    }

    /**
     * 文件监听请求发送的命令
     */
    public static class MonotorTarget implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String collection;

        private final LogType logType;

        private static MonotorTarget create(String collection, LogType logtype) {
            if (logtype == null) {
                throw new IllegalArgumentException("log type can not be null");
            }
            return new MonotorTarget(collection, logtype);
        }

        public static RegisterMonotorTarget createRegister(MonotorTarget target) {
            return new RegisterMonotorTarget(true, target.collection, target.logType);
        }

        public static RegisterMonotorTarget createRegister(String collection, LogType logtype) {
            MonotorTarget target = create(collection, logtype);
            return new RegisterMonotorTarget(true, target.collection, target.logType);
        }

        /**
         * 创建索引构建监听
         *
         * @param collection
         * @param payload
         * @return
         */
        public static PayloadMonitorTarget createPayloadMonitor(String collection, String payload, LogType logtype) {
            // MonotorTarget target = create(collection, LogType.INCR_BUILD);
            return new PayloadMonitorTarget(true, collection, payload, logtype);
        }

        public static RegisterMonotorTarget createUnregister(String collection, LogType logtype) {
            MonotorTarget target = create(collection, logtype);
            return new RegisterMonotorTarget(false, target.collection, target.logType);
        }

        public static RegisterMonotorTarget createUnregister(MonotorTarget target) {
            return new RegisterMonotorTarget(false, target.collection, target.logType);
        }

        @Override
        public int hashCode() {
            return (collection + logType.getValue()).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        MonotorTarget(String collection, LogType logType) {
            super();
            this.collection = collection;
            this.logType = logType;
        }

        public String getCollection() {
            return collection;
        }

        @Override
        public String toString() {
            return "monitorTarget[collection:" + collection + ",type:" + logType + "]";
        }

        public LogType getLogType() {
            return logType;
        }
    }

    public static class RegisterMonotorTarget extends MonotorTarget {

        private static final long serialVersionUID = 1L;

        private final boolean register;

        /**
         * @param collection
         * @param logType
         */
        public RegisterMonotorTarget(boolean register, String collection, LogType logType) {
            super(collection, logType);
            this.register = register;
        }

        public boolean isRegister() {
            return register;
        }
    }

    public static class PayloadMonitorTarget extends RegisterMonotorTarget {

        private static final String INIT_SHOW = "build";

        private final String payload;

        private static final long serialVersionUID = 1L;

        public PayloadMonitorTarget(boolean register, String collection, String payload, LogType logtype) {
            super(register, collection, logtype);
            if (StringUtils.isEmpty(payload)) {
                throw new IllegalArgumentException("param buildName can not be null");
            }
            this.payload = payload;
        }

        public String getPayLoad() {
            return this.payload;
        }

        /**
         * 页面在第一次打开视图之后 ，查看之前的历史构建记录
         *
         * @return
         */
        public boolean isInitShow() {
            return INIT_SHOW.equals(this.getPayLoad());
        }

        @Override
        public int hashCode() {
            return (this.getCollection() + this.getLogType().getValue() + this.getPayLoad()).hashCode();
        }
    }

    public interface ILogListener {

        public void read(ExecuteState<?> event);

        public Set<MonotorTarget> getMonitorTypes();
    }

    public static class FilterLogListener implements ILogListener {

        private final ILogListener taget;

        private final MonotorTarget mt;

        @Override
        public Set<MonotorTarget> getMonitorTypes() {
            return taget.getMonitorTypes();
        }

        public FilterLogListener(MonotorTarget mt, ILogListener taget) {
            super();
            this.taget = taget;
            this.mt = mt;
        }

        @Override
        public void read(ExecuteState<?> event) {
            if (event.getLogType() == mt.getLogType() && StringUtils.equals(event.getCollectionName(), mt.getCollection())) {
                taget.read(event);
            }
        }
    }
    // private static class MonitorRegisterRepository {
    // private final ConcurrentHashMap<MonotorTarget, List<ILogListener>>
    // logListener;
    // 
    // public MonitorRegisterRepository() {
    // super();
    // this.logListener = new ConcurrentHashMap<>();
    // }
    // 
    // public List<ILogListener> putIfAbsent(MonotorTarget key, List<ILogListener>
    // value) {
    // synchronized (this) {
    // return logListener.putIfAbsent(key, value);
    // }
    // }
    // 
    // public List<ILogListener> get(MonotorTarget key) {
    // return logListener.get(key);
    // }
    // 
    // public Collection<List<ILogListener>> getAllListener() {
    // return logListener.values();
    // }
    // }
}
