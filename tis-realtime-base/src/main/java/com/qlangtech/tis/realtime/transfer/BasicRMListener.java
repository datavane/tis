/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.qlangtech.tis.async.message.client.consumer.AsyncMsg;
import com.qlangtech.tis.async.message.client.consumer.impl.AbstractConsumerHandle;
import com.qlangtech.tis.cloud.ITisCloudClient;
import com.qlangtech.tis.cloud.ITisCloudClientFactory;
import com.qlangtech.tis.manage.common.SendSMSUtils;
import com.qlangtech.tis.realtime.transfer.impl.DefaultFocusTags;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPk;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPojo;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;
import com.qlangtech.tis.wangjubao.jingwei.ITableProcessGetter;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import com.qlangtech.tis.wangjubao.jingwei.TableCluster;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月25日 下午4:32:17
 */
@SuppressWarnings("all")
public abstract class BasicRMListener extends AbstractConsumerHandle implements InitializingBean, IOnsListenerStatus, ITableProcessGetter, ITableFocuseGetter {

    public static final String KEY_COLLECTION = "app";

    private static final Pattern PATTERN_COLLECTION = Pattern.compile("\\.(search4.+?)\\.");

    private final String collectionName;

    private List<IPojoVisit> pojoInterceptors = null;

    private IPojoVisit pojoVisit = (pojo, doc, shareId) -> {
//        Object sharedId = doc.getFieldValue(getShardIdName());
//        if (sharedId == null) {
//            doc.setField(getShardIdName(), shareId);
//        }
    };

    public ITisCloudClient cloudClient;

    private static ITisCloudClient tisCloudClient;

    @Override
    public String getName() {
        return "default";
    }

    protected final Map<String, AliasList> /**
     *  tablename
     */
            tabColumnMetaMap;

    protected final Map<String, AliasList> /**
     *  tablename
     */
            primaryTables;

    private static final Set<String> collectionNames = new HashSet<String>();

    private final MonitorSysTagMarker[] monitorSysTagMarker;

    private final RateLimiter rateLimiter;

    private static final boolean drain;

    static {
        drain = "true".equals(System.getProperty("drain"));
        if (drain) {
            System.out.println("######I will drain the data remain in MQ######");
        }
    }

    /**
     * 防止增量刚启动的时候被RM中堆積的消息沖垮需要限流
     */
    public BasicRMListener() {
        super();
        Matcher m = PATTERN_COLLECTION.matcher(this.getClass().getName());
        if (!m.find()) {
            throw new IllegalStateException("class name:" + this.getClass().getName() + " is not illegal");
        }
        this.collectionName = m.group(1);
        collectionNames.add(this.collectionName);
        this.monitorSysTagMarker = MonitorSysTagMarker.createMonitorSysTagMarker(this);
        this.rateLimiter = this.createProcessRate();
        AliasList.BuilderList builder = AliasList.BuilderList.create();
        this.processColsMeta(builder);
        this.tabColumnMetaMap = builder.build();
        this.primaryTables = this.createPrimaryTables();
        this.cloudClient = getCloudClient();
    }

    public static Integer CloudClientTypeCode = ITisCloudClientFactory.REAL_TIME_ROCKETMQ;

    /**
     * 根据cloudClient factory优先级的高低，取最高的一个实例创建
     *
     * @return
     */
    public static ITisCloudClient getCloudClient() {
        if (tisCloudClient != null) {
            return tisCloudClient;
        }
        ServiceLoader<ITisCloudClientFactory> clientLoader = ServiceLoader.load(ITisCloudClientFactory.class);
        ITisCloudClientFactory target = null;
        int typeCode = Integer.MIN_VALUE;
        for (ITisCloudClientFactory f : clientLoader) {
            if (f.getTypeCode() == CloudClientTypeCode) {
                target = f;
                typeCode = f.getTypeCode();
            }
        }
        if (target == null) {
            throw new IllegalStateException("have not set 'cloudClient'");
        }
        log.info("create ITisCloudClient with priority:" + typeCode + ",impl:" + target.getClass());
        return (tisCloudClient = target.create());
    }

    /**
     * 取得索引宽表主键
     *
     * @return
     */
    protected abstract DefaultPk getPk(ITable row) throws InterruptedException;

    /**
     * 取得索引主表
     *
     * @return
     */
    protected abstract Map<String, AliasList> createPrimaryTables();

    protected abstract void processColsMeta(AliasList.BuilderList builder);

    protected RateLimiter createProcessRate() {
        return RateLimiter.create(200);
    }

    public static final Logger log = LoggerFactory.getLogger(BasicRMListener.class);

    // 记录接收到的日志信息
    public static final Logger receiveLogger = LoggerFactory.getLogger("receive");

    // 传输VM级的数据
    public static final Logger vmstate = LoggerFactory.getLogger("vmstate");

    public static final int ALL_WAIT_QUEUE_LENGTH = 10000;

    private final BlockingQueue<IPojo> pojoQueue = new ArrayBlockingQueue<>(ALL_WAIT_QUEUE_LENGTH);

    private DefaultFocusTags focusTags;

    // 累计一段比较长的时间统计 引擎消费的单次消費時間用
    private long tisConsumeStartTime = System.currentTimeMillis();

    private AtomicLong tisConsumeTimeAccumulator = new AtomicLong();

    private AtomicLong tisConsumeCountAccumulator = new AtomicLong();

    // 最后一次GC 状态收集時間
    private static final AtomicLong lastGcStatusGetterTimestamp = new AtomicLong(System.currentTimeMillis());

    // /**
    // * 取得款表中主表的名称
    // */
    // public abstract String getPrimaryTableName(IPojo pojo);

    /**
     * 消费ons发送过来的增量消息
     *
     * @param event
     */
    @Override
    public boolean consume(AsyncMsg msg) {
        MDC.put(KEY_COLLECTION, this.getCollectionName());
        long now = System.currentTimeMillis();
        if (drain) {
            // 排干库存数据
            return true;
        }
        this.rateLimiter.acquire();
        DTO dto = null;
        // com.alibaba.fastjson.JSONObject content = null;
        try {
            this.shallPause();
            dto = parseDTO(msg);
            if (!getTableFocuse().contains(dto.getOrginTableName())) {
                ingorRowsCount.incrementAndGet();
                log.warn("table:" + dto.getOrginTableName() + " is not focus");
                return true;
            }
            if (DTO.EventType.DELETE == dto.getEvent()) {
                return true;
            }
            ITable table = getTable(dto);
            IPk pk = getPk(table);
            tabConsumeCount.incrementAndGet();
            if (pk != null) {
                if (receiveLogger.isDebugEnabled()) {
                    receiveLogger.debug(table.getTableName() + ",pk" + pk.getValue() + "," + table.getEventType());
                }
                pushPojo2Queue(pk, table);
            }
        } catch (Throwable e) {
            // if (content != null) {
            log.error("msgid:" + msg.getMsgID(), e);
            // }
            increaseConsumeErrorCount(e);
            throw new RuntimeException(e);
        } finally {
            MDC.remove(KEY_COLLECTION);
        }
        return true;
    }

    protected DTO parseDTO(AsyncMsg msg) {
        try {
            // (com.alibaba.fastjson.JSONObject)
            // com.alibaba.fastjson.JSONObject content = msg.getContent();
            // TypeUtils.castToJavaBean(content, DTO.class);
            DTO dto = msg.getContent();
            // dto.setRowContent(content);
            return dto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected AtomicBoolean pauseFlag = new AtomicBoolean(false);

    /**
     * 重新啟動增量消息
     */
    public void resumeConsume() {
        synchronized (this.pauseFlag) {
            if (this.pauseFlag.compareAndSet(true, false)) {
                this.pauseFlag.notifyAll();
                log.info(this.getCollectionName() + " get resume command");
            }
        }
    }

    /**
     * 增量任务是否暂停中
     *
     * @return
     */
    public final boolean isPaused() {
        return this.pauseFlag.get();
    }

    /**
     * 停止增量接收消息
     */
    public void pauseConsume() {
        if (this.pauseFlag.compareAndSet(false, true)) {
            log.info(this.getCollectionName() + " get pause command");
        }
    }

    public void shallPause() throws Exception {
        if (pauseFlag.get()) {
            synchronized (pauseFlag) {
                if (pauseFlag.get()) {
                    pauseFlag.wait();
                }
            }
        }
    }

    // public static SolrZkClient getZookeeper() {
    // return cloudClient.getZkClient();
    // }
    // public ITISCoordinator cloudCoordinator;
    // public ITISCoordinator getTisZkClient() {
    // return this.cloudClient.getCoordinator();
    // }
    // public static void addOnReconnect(OnReconnect onReconnectEvent) {
    // cloudClient.addOnReconnect(onReconnectEvent);
    // }
    private ConcurrentHashMap<IPk, IPojo> pojoMap = new ConcurrentHashMap<>();

    private static final int NUM_CONSUME_NUM = 3;

    private final AtomicLong ingorRowsCount = new AtomicLong();

    /**
     * 向solr中插入的记录条数
     */
    private final AtomicLong consumeErrorCount = new AtomicLong();

    private final ConcurrentHashMap<String, IIncreaseCounter> // count
            tableUpdateCount = new ConcurrentHashMap<>();

    private AtomicLong tabConsumeCount = new AtomicLong(0);

    // private long current = 0;
    private final ExecutorService executor = Executors.newCachedThreadPool((r) -> {
        Thread t = new Thread(r, "consume-thread");
        t.setUncaughtExceptionHandler((thread, e) -> {
            log.error(e.getMessage(), e);
        });
        return t;
    });

    private TableCluster tableCluster;

    @Override
    public Table getTableProcessor(String name) {
        return tableCluster.getTable(name);
    }

    public SolrFieldsParser.ParseResult getSchemaFieldMeta() {
        return tableCluster.getSchemaFieldMeta();
    }

    // public void increaseConsumeIncreaseCount() {
    // this.consumeIncreaseCount.incrementAndGet();
    // }
    @Override
    public long getIgnoreRowsCount() {
        return this.ingorRowsCount.get();
    }

    /**
     * jmx prop
     */
    public long getConsumeErrorCount() {
        return this.consumeErrorCount.get();
    }

    public void increaseConsumeErrorCount(Throwable e) {
        this.consumeErrorCount.incrementAndGet();
        // 控制发送频率最多10分钟分一次
        SendSMSUtils.send(StringUtils.substringAfter(this.getCollectionName(), "search4") + "," + e.getMessage(), SendSMSUtils.BAISUI_PHONE);
    }

    /**
     * jmx prop
     */
    @Override
    public String getTableUpdateCount() {
        JSONArray array = new JSONArray();
        JSONObject json = null;
        for (Map.Entry<String, IIncreaseCounter> // table
                etry : tableUpdateCount.entrySet()) {
            json = new JSONObject();
            json.put(etry.getKey(), etry.getValue().getAccumulation());
            array.put(json);
        }
        return array.toString(1);
    }

    public Set<Map.Entry<String, IIncreaseCounter>> getUpdateStatic() {
        return this.tableUpdateCount.entrySet();
    }

    /**
     * jmx prop
     */
    @Override
    public int getBufferQueueRemainingCapacity() {
        return pojoQueue.remainingCapacity();
    }

    /**
     * 缓存队列已使用的隊列長度
     *
     * @return
     */
    public int getBufferQueueUsedSize() {
        return pojoQueue.size();
    }

    /**
     * jmx prop
     */
    public long getConsumeIncreaseCount() {
        // consumeIncreaseCount.get();
        return 0;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MDC.put("app", this.getCollectionName());
        try {
            this.tableCluster = TableCluster.getTableRowProcessor(this.getCollectionName());
            if (this.tableCluster == null) {
                throw new IllegalStateException("tableCluster can not be null");
            }
            log.info("start init");
            for (int i = 0; i < getConsumeNum(); i++) {
                BasicPojoConsumer consumer = createPojoConsumer();
                consumer.setName("consume" + i);
                consumer.setSolrClient(cloudClient);
                executor.execute(consumer);
            }
            log.info("has initial " + getConsumeNum() + " consumer");
            this.focusTags = new DefaultFocusTags();
            this.focusTags.setCollection(this.getCollectionName());
            this.focusTags.setTags(this.getTableFocuse());
            this.focusTags.setTopic(StringUtils.EMPTY);
            // 限流一分钟
            // scheduler.schedule(new Runnable() {
            // @Override
            // public void run() {
            // try {
            // setCollectionName();
            //
            // log.info("source_limit_cancel");
            // } catch (Throwable e) {
            // log.error(e.getMessage(), e);
            // }
            // }
            // }, 180, TimeUnit.SECONDS);
        } finally {
            MDC.remove("app");
        }
    }

    /**
     * @return
     */
    protected abstract BasicPojoConsumer createPojoConsumer();

    protected ITable getTable(DTO dto) {
        String tableName = dto.getTargetTable();
        DefaultTable table = createTable(tableName);
        table.setEventType((dto.getEvent()));
        for (Map.Entry<String, String> entry : dto.getAfter().entrySet()) {
            table.addColumn(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : dto.getBefore().entrySet()) {
            table.addBeforeColumn(entry.getKey(), entry.getValue());
        }
        table.setRowDto(dto);
        table.validateTable();
        return table;
    }

    protected DefaultTable createTable(String tableName) {
        return new DefaultTable(tableName, this.getTableProcessor(tableName));
    }

    /**
     * 向solr引擎中添加一次更新操作的計數
     *
     * @return
     */
    // public long addSolrConsume() {
    // return this.addTableCount(IIncreaseCounter.SOLR_CONSUME_COUNT);
    // }

    /**
     * 操作增量
     *
     * @return
     */
    public long getSolrConsumeIncrease() {
        return this.getTableCount(IIncreaseCounter.SOLR_CONSUME_COUNT).getIncreasePastLast();
    }

    /**
     * 清空累加器
     */
    public void cleanLastAccumulator() {
        for (Map.Entry<String, IIncreaseCounter> /* table name */
                entry : this.tableUpdateCount.entrySet()) {
            ((ProcessCount) entry.getValue()).lastCollectCount.remove();
        }
    }

    /**
     * @param tableName
     * @return
     */
    public long addTableCount(String tableName) {
        ProcessCount upateCount = (ProcessCount) getTableCount(tableName);
        return upateCount.getAndIncrement();
    }

    /**
     * 设置单次消费时间
     *
     * @param tableName
     * @param consumeTime MS
     * @return
     */
    public long addTisConsumeTime(long consumeTime) {
        this.tisConsumeTimeAccumulator.addAndGet(consumeTime);
        this.tisConsumeCountAccumulator.incrementAndGet();
        return addTableCount(IIncreaseCounter.SOLR_CONSUME_COUNT);
    }

    protected IIncreaseCounter getTableCount(String tableName) {
        IIncreaseCounter upateCount = null;
        if ((upateCount = this.tableUpdateCount.get(tableName)) == null) {
            boolean match = false;
            for (MonitorSysTagMarker tagMarker : this.monitorSysTagMarker) {
                if (tagMarker.match(tableName)) {
                    upateCount = new ProcessCount(tagMarker);
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new IllegalStateException("metric:" + tableName + " can not find match any MonitorSysTagMarker");
            }
            IIncreaseCounter preCount = null;
            if ((preCount = this.tableUpdateCount.putIfAbsent(tableName, upateCount)) != null) {
                upateCount = preCount;
            }
        }
        return upateCount;
    }

    private static class ProcessCount implements IIncreaseCounter {

        private final AtomicLong absoluteCount;

        private final MonitorSysTagMarker tagMarker;

        private final ThreadLocal<AtomicLong> lastCollectCount = new ThreadLocal<AtomicLong>() {
        };

        // private final boolean shallCollectByMonitorSystem;
        // private final String tags;
        @Override
        public MonitorSysTagMarker getMonitorTagMarker() {
            return this.tagMarker;
        }

        @Override
        @JSONField(serialize = true)
        public long getAccumulation() {
            return absoluteCount.get();
        }

        public ProcessCount() {
            this(null);
        }

        public ProcessCount(MonitorSysTagMarker tagMarker) {
            super();
            this.absoluteCount = new AtomicLong(0);
            if (tagMarker == null) {
                throw new IllegalArgumentException("tagMarker can not be null");
            }
            this.tagMarker = tagMarker;
        }

        @JSONField(serialize = false)
        public long getAndIncrement() {
            return this.absoluteCount.getAndIncrement();
        }

        // @Override
        // public boolean shallCollectByMonitorSystem() {
        // return this.shallCollectByMonitorSystem;
        // }
        // 
        // @Override
        // public String getTags() {
        // return this.tags;
        // }
        @Override
        @JSONField(serialize = false)
        public long getIncreasePastLast() {
            long currentCount = absoluteCount.get();
            AtomicLong last = lastCollectCount.get();
            if (last == null) {
                synchronized (lastCollectCount) {
                    last = lastCollectCount.get();
                    if (last == null) {
                        lastCollectCount.set(new AtomicLong(currentCount));
                        return 0l;
                    }
                }
            }
            return currentCount - lastCollectCount.get().getAndSet(currentCount);
        }
    }

    /**
     * @param pk
     * @return
     * @throws InterruptedException
     */
    protected void pushPojo2Queue(IPk pk, ITable table) throws InterruptedException {
        IPojo pojo = pojoMap.get(pk);
        IPojo p = null;
        if (pojo == null) {
            pojo = createRowsWrapper().setPrimaryKey(pk).setCollection(getCollectionName());
            if ((p = pojoMap.putIfAbsent(pk, pojo)) == null) {
                synchronized (pojo) {
                    DefaultPojo newp = ((DefaultPojo) pojo);
                    if (!newp.setTable(table.getTableName(), table)) {
                        pushPojo2Queue(pk, table);
                        return;
                    }
                    if (!this.pojoQueue.offer(pojo)) {
                        // size 已经到上限,太累了需要休息一会儿了
                        String msg = this.getCollectionName() + "incr too tried to work , wait for a moment";
                        log.warn(msg);
                        SendSMSUtils.send(msg, SendSMSUtils.BAISUI_PHONE);
                        Thread.sleep(30000);
                        this.pojoQueue.put(pojo);
                    }
                    this.addTableCount(table.getTableName());
                }
                return;
            } else {
                pojo = p;
            }
        }
        synchronized (pojo) {
            DefaultPojo newp = ((DefaultPojo) pojo);
            if (!pojo.setTable(table.getTableName(), table)) {
                // log.info("yyyyyyyyyy:" + pk + "isX:" + isX);
                pushPojo2Queue(pk, table);
            }
        }
    }

    protected DefaultPojo createRowsWrapper() {
        return new DefaultPojo(this);
    }

    public BlockingQueue<IPojo> getPojoQueue() {
        return pojoQueue;
    }

    public ConcurrentMap<IPk, IPojo> getPojoMap() {
        return pojoMap;
    }

    public final String getCollectionName() {
        return this.collectionName;
    }

    protected int getConsumeNum() {
        return NUM_CONSUME_NUM;
    }

    @Override
    public final Set<String> getFocusedTabs() {
        return this.getTableFocuse();
    }

    public abstract Set<String> getTableFocuse();

    private String focusTabs = null;

    // private final ScheduledExecutorService scheduler =
    // Executors.newScheduledThreadPool(1);
    @Override
    public String getSubExpression() {
        setCollectionName();
        try {
            if (this.focusTags == null) {
                throw new IllegalStateException("focusTags can not be null");
            }
            final String subExpression = this.focusTags.getSubExpression();
            return subExpression;
        } finally {
            MDC.remove("app");
        }
    }

    /**
     * 收集虚拟机垃圾回收次数
     */
    // private void doGarbageCollectionUpdates() {
    // long current = System.currentTimeMillis();
    // long last = 0;
    // // 半分钟收集一次
    // if ((current > ((last = lastGcStatusGetterTimestamp.get()) + 15000))
    // && lastGcStatusGetterTimestamp.compareAndSet(last, current)) {
    // List<GarbageCollectorMXBean> gcBeans =
    // ManagementFactory.getGarbageCollectorMXBeans();
    // long count = 0;
    // long timeMillis = 0;
    // for (GarbageCollectorMXBean gcBean : gcBeans) {
    // count += gcBean.getCollectionCount();
    // }
    // garbageCollectionCounter = count;
    // }
    // }
    private static long garbageCollectionCounter;

    public static long getGarbageCollectionCount() {
        return garbageCollectionCounter;
    }

    /**
     * 30秒内
     *
     * @return
     */
    public long getTis30sAvgRT() {
        long tisConsumeCount = tisConsumeCountAccumulator.get();
        long result = tisConsumeTimeAccumulator.get() / (tisConsumeCount < 1 ? 1 : tisConsumeCount);
        long current = System.currentTimeMillis();
        if (current > (tisConsumeStartTime + 30000)) {
            tisConsumeStartTime = current;
            tisConsumeTimeAccumulator.set(0);
            tisConsumeCountAccumulator.set(0);
        }
        return result;
    }

    private void setCollectionName() {
        if (StringUtils.isBlank(getCollectionName())) {
            throw new IllegalStateException("app name can not be blank");
        }
        MDC.put("app", getCollectionName());
    }

    public long getTableConsumeCount() {
        return this.tabConsumeCount.get();
    }

    // private void printStatisReport() {
    // 
    // 
    // // current = now;
    // // }
    // }
    public static final ThreadLocal<SimpleDateFormat> formatYyyyMMddHHmmss = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    @Override
    public IIncreaseCounter getMetricCount(String metricName) {
        return this.getTableCount(metricName);
    }

    /**
     * 记录的分区键
     *
     * @return
     */
    public String getShardIdName(String tableName) {
        throw new UnsupportedOperationException();
    }

    public void lookPojoVisit(IPojo pojo, TisSolrInputDocument doc, String shareId) {
        if (this.pojoInterceptors != null) {
            this.pojoInterceptors.stream().forEach((r) -> r.haveAlook(pojo, doc, shareId));
        }
        this.pojoVisit.haveAlook(pojo, doc, shareId);
    }

    protected final void addPojoInterceptors(IPojoVisit pojoVisit) {
        synchronized (this) {
            if (this.pojoInterceptors == null) {
                this.pojoInterceptors = Lists.newArrayList();
            }
            this.pojoInterceptors.add(pojoVisit);
        }
    }

    public AliasList getTabColumnMeta(String tableName) {
        return tabColumnMetaMap.get(tableName);
    }

    /**
     * 在测试流程中用，在visit中使用断言，在最终要发送doc之前对doc中的内容进行判断
     *
     * @param pojoVisit
     */
    public final void setPojoVisit(IPojoVisit pojoVisit) {
        if (pojoVisit == null) {
            throw new NullPointerException("param pojoVisit can not be null");
        }
        this.pojoVisit = pojoVisit;
    }
}
