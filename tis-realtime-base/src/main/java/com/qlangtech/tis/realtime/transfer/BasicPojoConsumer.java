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
package com.qlangtech.tis.realtime.transfer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.qlangtech.tis.solrj.extend.TisCloudSolrClient;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import com.qlangtech.tis.wangjubao.jingwei.TabField;
import com.qlangtech.tis.wangjubao.jingwei.Table;

/*
 * 消费增量数据
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicPojoConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BasicPojoConsumer.class);

    private static final long Time_Window = 20000;

    protected final BasicONSListener onsListener;

    protected TisCloudSolrClient solrClient;

    private final Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();

    public static final Logger sendRecoredLog = LoggerFactory.getLogger("sendRecored");

    private final SolrFieldsParser solrFieldsParser;

    protected static final String VERSION_FIELD_NAME = "_version_";

    private String consumeName;

    public BasicPojoConsumer(BasicONSListener onsListener) {
        super();
        this.onsListener = onsListener;
        this.solrFieldsParser = new SolrFieldsParser();
    // mockSend();
    }

    public final void setName(String name) {
        this.consumeName = name;
    }

    private IPojo takeFromQueue() throws InterruptedException {
        return onsListener.getPojoQueue().take();
    }

    private boolean removePojoAndGet(IPk pk, IPojo row) {
        return onsListener.getPojoMap().remove(pk, row);
    }

    protected void process(IPojo pojo, long processStart, long waitProcess) throws Exception {
        // IndexableField versionField = null;
        SolrInputDocument oldDoc = null;
        int retryCount = 0;
        while (true) {
            // onsListener.shallPause();
            // synchronized (pojo) {
            TisSolrInputDocument addDoc = createTisDocument(pojo);
            try {
                String collection = pojo.getCollection();
                IPk pk = pojo.getPK();
                final String shareId = getShareId(pojo);
                // log.debug("pk:" + pk.getValue() + ",tables:" +
                // pojo.getTables().size());
                SolrDocument doc = null;
                boolean lackSolrRecord = false;
                if (shallGetOrigin() && !pojo.isAdd()) {
                    // 更新状态
                    doc = fetchDocument(collection, pk, pojo);
                    if (doc == null) {
                        if (shallAbandonDirectly(pojo, addDoc)) {
                            return;
                        }
                        lackSolrRecord = true;
                        writeLackSolrRecord(pojo, shareId);
                        this.onsListener.addTableCount(MonitorSysTagMarker.KEY_LACK_SOLR_RECORD);
                        // 索引中应该有数据，但是没有数据
                        if (!indexIsExist(pojo, addDoc)) {
                            StringBuffer error = new StringBuffer("pk:" + pk.getValue() + " update mode ,index not exist(");
                            for (Map.Entry<String, IRowPack> entry : pojo.getRowsPack()) {
                                error.append(entry.getKey()).append(",");
                            }
                            error.append(")");
                            log.warn(error.toString());
                            return;
                        }
                        addDoc.setField(BasicPojoConsumer.VERSION_FIELD_NAME, pojo.getVersion());
                        if (log.isDebugEnabled()) {
                            log.debug("fetch  pojo from db pk:" + pk.getValue() + ",collection:" + collection);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("fetch doc from solr pk:" + pk.getValue() + ",collection:" + collection + ",shareId:" + shareId);
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    oldDoc = new SolrInputDocument();
                    if (doc != null) {
                        for (String key : doc.keySet()) {
                            oldDoc.setField(key, doc.getFirstValue(key));
                        }
                    }
                }
                // 补全数据
                if (doc != null) {
                    addDoc.merge2DocumentFields(doc);
                }
                long preVersion = getPreVersion(addDoc);
                if (shallGetOrigin() && !pojo.isAdd() && preVersion < 0) {
                    sendRecoredLog.warn("preVersion(" + preVersion + ") < 0," + (doc != null ? "find doc in tis" : StringUtils.EMPTY) + getTableEnum(pojo));
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("preVersion:" + preVersion);
                }
                if (!processPojo(pojo, addDoc)) {
                    this.printIgnoreLog(pojo, addDoc);
                    return;
                }
                if (!addDoc.hasAnyFieldChange) {
                    log.warn("update ignore2,detail:" + getTableEnum(pojo));
                    return;
                }
                long version = pojo.getVersion();
                // 如果更新记录的版本号比历史的版本好小的话，那就自动加上50秒，以防止丢失数据
                if (version <= preVersion) {
                    version = rectifiedVersion(pk, preVersion, version);
                }
                if (log.isDebugEnabled()) {
                    log.debug("new version:" + version);
                }
                addDoc.setField(VERSION_FIELD_NAME, version);
                long start = System.currentTimeMillis();
                try {
                    solrClient.add(collection, addDoc.doc, version);
                } finally {
                    this.onsListener.addTableCount(this.consumeName);
                    this.onsListener.addTisConsumeTime((System.currentTimeMillis() - start));
                }
                if (log.isDebugEnabled()) {
                    log.debug("new version:" + version);
                }
                sendLog(pojo, addDoc, preVersion, version, processStart, waitProcess, lackSolrRecord);
            // this.onsListener.addSolrConsume();
            // this.onsListener.addTableCount("solrConsume");
            } catch (Exception e) {
                StringBuffer tabsDetail = getTableEnum(pojo);
                log.error("tabDesc:" + tabsDetail);
                if (log.isDebugEnabled() && oldDoc != null) {
                    log.debug("olddoc:" + oldDoc.toString());
                }
                log.error("submit to solr server error," + addDoc.doc.toString(), e);
                onsListener.increaseConsumeErrorCount(e);
                // 一旦发生错误需要等待5秒之后再发送,需要等待下游将问题解决再启动
                Thread.sleep(5000);
                if (retryCount++ < 3) {
                    continue;
                } else {
                    return;
                }
            } finally {
            // try {
            // pojo.close();
            // } catch (IOException e) {
            // }
            }
            return;
        }
    // end while
    }

    protected void writeLackSolrRecord(IPojo pojo, final String shareId) throws Exception {
        IPk pk = pojo.getPK();
        sendRecoredLog.warn("lackSolrRecord pk:" + pk.getValue() + ",collection:" + pojo.getCollection() + ",shareId:" + shareId + getTableEnum(pojo));
    }

    /**
     * 由于binlog数据到达有先后，如果先发生的数据在一个时间窗口之后，的版本比之前的版本老，說明由於binlog傳輸的有延时 这里需要尝试一次版本修正
     *
     * @param preVersion
     * @param version
     * @return
     * @throws Exception
     */
    protected long rectifiedVersion(IPk pk, long preVersion, long version) throws Exception {
        Date nowVer = null;
        if (version < 1) {
            nowVer = new Date();
        } else {
            nowVer = BasicONSListener.formatYyyyMMddHHmmss.get().parse(String.valueOf(version));
        }
        long rectified = Long.parseLong(BasicONSListener.formatYyyyMMddHHmmss.get().format(new Date(nowVer.getTime() + 1800 * 1000)));
        // 如果版本时间相差一个小时以内，如果相差太大了，也沒有辦法挽救了
        if (rectified > preVersion) {
            version = (preVersion + 1);
        }
        return version;
    }

    protected void printIgnoreLog(IPojo pojo, TisSolrInputDocument addDoc) throws Exception {
        sendRecoredLog.warn("update ignore,detail:" + getTableEnum(pojo));
    }

    /**
     * 从搜索引擎上取得到原有的一条索引记录
     *
     * @param collection
     * @param pk
     * @param pojo
     * @return
     * @throws Exception
     */
    protected SolrDocument fetchDocument(String collection, IPk pk, IPojo pojo) throws Exception {
        final String shareId = getShareId(pojo);
        return solrClient.getById(collection, pk.getValue(), shareId);
    }

    /**
     * 取得之前老文档的_version_
     *
     * @param addDoc
     * @return
     */
    private long getPreVersion(TisSolrInputDocument addDoc) {
        long preVersion = -1;
        // IndexableField versionField;
        Object v = addDoc.getFieldValue(VERSION_FIELD_NAME);
        if (v == null) {
            return preVersion;
        }
        if (v instanceof Long) {
            preVersion = (Long) v;
        }
        // }
        return preVersion;
    }

    private StringBuffer getTableEnum(IPojo pojo) throws Exception {
        final StringBuffer tabsDetail = new StringBuffer();
        for (Map.Entry<String, IRowPack> entry : pojo.getRowsPack()) {
            entry.getValue().vistRow((r) -> {
                tabsDetail.append("\n").append(entry.getKey()).append(",pk:").append(pojo.getPK().getValue()).append(",").append(r.getEventType()).append("\n \t");
                r.desc(tabsDetail);
                return false;
            });
        }
        return tabsDetail;
    }

    protected TisSolrInputDocument createTisDocument(IPojo pojo) throws Exception {
        return new TisSolrInputDocument(getSchemaFields(pojo.getCollection()), onsListener);
    }

    /**
     * 是否应该查询原始记录，如果不需要join的话，<br>
     * 则每次查询都直接将binlog中的数据完全覆盖，也就不需要查询搜索引擎了
     *
     * @return
     */
    protected boolean shallGetOrigin() {
        return true;
    }

    /**
     * @param pojo
     * @param addDoc
     * @param preVersion
     * @param version
     * @param processStart
     * @param waitProcess
     *            从开始接收到消息到最终处理所花费的时间
     */
    private void sendLog(IPojo pojo, TisSolrInputDocument addDoc, long preVersion, long version, long processStart, long waitProcess, boolean lackSolrRecord) {
        if (sendRecoredLog.isDebugEnabled()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("version:").append(version).append(",add:").append(pojo.isAdd()).append((pojo.isAdd() ? StringUtils.EMPTY : (",preVersion:" + preVersion))).append(",").append(this.consumeName).append(":").append((System.currentTimeMillis() - processStart)).append(",waitprocess:").append(waitProcess).append(",lackSolrRecord:").append(lackSolrRecord).append(",tables:");
            for (Map.Entry<String, IRowPack> entry : pojo.getRowsPack()) {
                buffer.append(entry.getKey()).append("(").append(entry.getValue().getRowSize()).append(")|");
            }
            buffer.append("\n").append(addDoc.doc.toString());
            sendRecoredLog.info(buffer.toString());
        }
    }

    /**
     * 执行索引更新操作的时候首先会到索引中利用主键找索引记录，如果找不到会调用此操作，如果返回false，则这条更新记录会被忽略
     *
     * @param pojo
     * @param addDoc
     * @return
     * @throws Exception
     */
    public boolean indexIsExist(IPojo pojo, TisSolrInputDocument addDoc) throws Exception {
        return true;
    }

    /**
     * 更新流程中在TIS中反查没有，是否需要立即结束此次更新流程
     *
     * @param pojo
     * @param addDoc
     * @return
     */
    protected boolean shallAbandonDirectly(IPojo pojo, TisSolrInputDocument addDoc) {
        return false;
    }

    /**
     * 将POJO中的表记录内容拷贝到document中去，最终会提交到TIS中去
     *
     * @param pojo
     * @param addDoc
     * @return
     * @throws Exception
     */
    protected abstract boolean processPojo(IPojo pojo, TisSolrInputDocument addDoc) throws Exception;

    public TisCloudSolrClient getSolrClient() {
        return solrClient;
    }

    @Override
    public void run() {
        try {
            MDC.put("app", onsListener.getCollectionName());
            IPojo rowWrapper = null;
            while (true) {
                try {
                    long processStart = System.currentTimeMillis();
                    rowWrapper = takeFromQueue();
                    long timeWindow = getTimeWindow(rowWrapper);
                    long waitProcess = (rowWrapper.occurTime() + timeWindow) - System.currentTimeMillis();
                    if (waitProcess > 0) {
                        // sleep需要在synchronized (rowWrapper) 外面，不然，前面放row到pojo中会阻塞
                        Thread.sleep(waitProcess);
                    }
                    synchronized (rowWrapper) {
                        try {
                            if (rowWrapper.getPK() == null) {
                                throw new IllegalStateException("row.getPK() can not be null");
                            }
                            if (removePojoAndGet(rowWrapper.getPK(), rowWrapper)) {
                                process(rowWrapper, processStart, (waitProcess - timeWindow));
                            } else {
                                // 已经被删除了
                                log.error("pk:" + rowWrapper.getPK() + " has been consume");
                            }
                        } finally {
                            try {
                                rowWrapper.close();
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                    try {
                        if (rowWrapper != null) {
                            log.error(rowWrapper.toString(), e);
                        } else {
                            log.error(e.getMessage(), e);
                        }
                    } catch (Exception e1) {
                        log.error("", e1);
                    }
                    onsListener.increaseConsumeErrorCount(e);
                }
            }
        } finally {
            MDC.remove("app");
        }
    }

    /**
     * @return
     */
    protected final long getTimeWindow(IPojo rowWrapper) {
        IRowPack row = null;
        Long timeWindow = null;
        if (rowWrapper.careRowpackTimeWindow()) {
            for (Entry<String, IRowPack> entry : rowWrapper.getRowsPack()) {
                row = entry.getValue();
                if (row != null && (timeWindow = row.getTimeWindow()) != null) {
                    // timeWindow);
                    return timeWindow;
                }
            }
        }
        return getTimeWindow();
    }

    protected long getTimeWindow() {
        return Time_Window;
    }

    /**
     * 取得schema中的字段
     *
     * @param collection
     * @return
     * @throws Exception
     */
    protected Set<String> getSchemaFields(String collection) throws Exception {
        Set<String> fiedls = fieldsMap.get(collection);
        if (fiedls == null) {
            synchronized (fieldsMap) {
                fiedls = fieldsMap.get(collection);
                if (fiedls == null) {
                    fiedls = new HashSet<String>();
                    TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
                    SnapshotDomain domain = HttpConfigFileReader.getResource(config.getTerminatorConsoleHostAddress(), collection, config.getRuntime(), ConfigFileReader.FILE_SCHEMA);
                    ParseResult parseResult = null;
                    StringBuffer acceptKeys = new StringBuffer();
                    try (ByteArrayInputStream reader = new ByteArrayInputStream(ConfigFileReader.FILE_SCHEMA.getContent(domain))) {
                        parseResult = solrFieldsParser.parseSchema(reader, false);
                        for (PSchemaField f : parseResult.dFields) {
                            acceptKeys.append(f.getName()).append(",");
                            fiedls.add(f.getName());
                        }
                    }
                    log.info("doc acceptKeys:" + acceptKeys.toString());
                    fieldsMap.put(collection, fiedls);
                }
            }
        }
        return fiedls;
    }

    public void setSolrClient(TisCloudSolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * @param pojo
     * @return
     */
    protected String getShareId(IPojo pojo) throws Exception {
        final ShareId shardid = new ShareId();
        for (Map.Entry<String, IRowPack> entry : pojo.getRowsPack()) {
            entry.getValue().vistRow((r) -> {
                shardid.value = r.getColumn(getShardIdName());
                return (shardid.value != null);
            });
        }
        if (shardid.value != null) {
            return String.valueOf(shardid.value);
        }
        throw new IllegalStateException("pojo has not set shareid[" + getShardIdName() + "]:" + pojo.toString());
    }

    public static final class ShareId {

        public Object value;

        public String getValue() {
            return String.valueOf(value);
        }
    }

    public static final String TABLE_SHARD_ID = "entity_id";

    protected String getShardIdName() {
        return TABLE_SHARD_ID;
    }

    private static final ThreadLocal<BeanUtilsBean> beanDescLocal = new ThreadLocal<BeanUtilsBean>() {

        @Override
        protected BeanUtilsBean initialValue() {
            ConvertUtilsBean beanConvert = new ConvertUtilsBean() {

                public String convert(Object value) {
                    if (value == null) {
                        return ((String) null);
                    } else if (value.getClass().isArray()) {
                        if (Array.getLength(value) < 1) {
                            return (null);
                        }
                        value = Array.get(value, 0);
                        if (value == null) {
                            return ((String) null);
                        } else {
                            Converter converter = lookup(value.getClass());
                            return ((String) converter.convert(String.class, value));
                        }
                    } else {
                        Converter converter = lookup(value.getClass());
                        return ((String) converter.convert(String.class, value));
                    }
                }
            };
            DateConverter dateFormat = new DateConverter();
            dateFormat.setPattern("yyyyMMdd");
            beanConvert.register(dateFormat, Date.class);
            BigDecimalConverter bigDecimalConverter = new BigDecimalConverter();
            bigDecimalConverter.setPattern(".###");
            beanConvert.register(bigDecimalConverter, BigDecimal.class);
            BeanUtilsBean utilBean = new BeanUtilsBean(beanConvert);
            return utilBean;
        }
    };

    public static class TisSolrInputDocument {

        private final Set<String> acceptFields;

        public final SolrInputDocument doc;

        private final BasicONSListener onsListener;

        // 是否被合并过
        private boolean merged = false;

        public TisSolrInputDocument(Set<String> acceptFields, BasicONSListener onsListener) {
            this(acceptFields, new SolrInputDocument(), onsListener);
        }

        public TisSolrInputDocument(Set<String> acceptFields, SolrInputDocument doc, BasicONSListener onsListener) {
            this.acceptFields = acceptFields;
            this.doc = doc;
            this.onsListener = onsListener;
        }

        /**
         * 将文档类型转化为SolrDocument
         *
         * @return
         */
        public SolrDocument convertSolrDocument() {
            SolrDocument doc = new SolrDocument();
            for (String key : this.doc.keySet()) {
                doc.setField(key, this.doc.getFieldValue(key));
            }
            return doc;
        }

        // 是否有任何字段的值改變了
        boolean hasAnyFieldChange = false;

        public final void setField(String colname, Object value) {
            this.setField(colname, value, false);
        }

        /**
         * @param colname
         * @param value
         * @param merge
         *            在执行新老doc merge的时候可能会根据当前是否在做merge流程而做一流程操作
         */
        public void setField(String colname, Object value, boolean merge) {
            if (value == null || StringUtils.isBlank(String.valueOf(value))) {
                this.clearField(colname);
                return;
            }
            if (!merge && isFieldNotAccept(colname)) {
                return;
            }
            if (!this.hasAnyFieldChange) {
                Object old = this.doc.getFieldValue(colname);
                if (old != null && !StringUtils.equals(String.valueOf(old), String.valueOf(value))) {
                    this.hasAnyFieldChange = true;
                } else if (old == null) {
                    this.hasAnyFieldChange = true;
                }
            }
            this.doc.setField(colname, value);
        }

        protected boolean isFieldNotAccept(String colname) {
            return !getAcceptFields().contains(colname);
        }

        /**
         * 创建一个子document
         *
         * @return
         */
        protected TisSolrInputDocument createChild(SolrDocument doc) {
            TisSolrInputDocument child = new TisSolrInputDocument(getAcceptFields(), onsListener);
            child.merge2DocumentFields(doc);
            this.doc.addChildDocument(child.doc);
            return child;
        }

        /**
         * 將老的document的值合併到新document上
         *
         * @param oldDoc
         */
        public final TisSolrInputDocument merge2DocumentFields(SolrDocument oldDoc) {
            if (merged) {
                throw new IllegalStateException("this.doc:" + doc.toString() + " can not be merge twice");
            }
            for (String name : oldDoc.getFieldNames()) {
                this.setField(name, oldDoc.getFirstValue(name), shallForceMerge(oldDoc));
            }
            if (oldDoc.hasChildDocuments()) {
                for (SolrDocument d : oldDoc.getChildDocuments()) {
                    // .merge2DocumentFields(d);
                    this.createChild(d);
                }
            }
            merged = true;
            return this;
        }

        /**
         * 是否要强力执行merge逻辑
         *
         * @param oldDoc
         * @return
         */
        protected boolean shallForceMerge(SolrDocument oldDoc) {
            return false;
        }

        /**
         * 取得字段值
         *
         * @param fieldName
         * @return
         */
        public Object getFieldValue(String fieldName) {
            return doc.getFieldValue(fieldName);
        }

        public int getInt(String fieldName, boolean careError) {
            Object val = getFieldValue(fieldName);
            if (val == null) {
                if (careError) {
                    throw new NullPointerException("fieldname:" + fieldName + " is null");
                } else {
                    return 0;
                }
            }
            if (val instanceof Integer) {
                return (Integer) val;
            } else if (val instanceof Long) {
                return ((Long) val).intValue();
            }
            try {
                return Integer.parseInt(String.valueOf(val));
            } catch (Throwable e) {
                if (careError) {
                    throw new RuntimeException(e);
                } else {
                    return 0;
                }
            }
        }

        public long getLong(String fieldName, boolean careError) {
            Object val = getFieldValue(fieldName);
            if (val == null) {
                if (careError) {
                    throw new NullPointerException("fieldname:" + fieldName + " is null");
                } else {
                    return 0;
                }
            // throw new NullPointerException("fieldname:" + fieldName
            // + " is null");
            }
            if (val instanceof Integer) {
                return ((Integer) val).longValue();
            } else if (val instanceof Long) {
                return ((Long) val);
            }
            try {
                return Long.parseLong(String.valueOf(val));
            } catch (Throwable e) {
                if (careError) {
                    throw new RuntimeException(e);
                } else {
                    return 0;
                }
            }
        }

        public void setField(String table, String colname, Object value) {
            if (value == null || StringUtils.isBlank(String.valueOf(value))) {
                this.clearField(colname);
                return;
            }
            TabField field = null;
            Table tab = onsListener.getTable(table);
            // colname = addUnderline(colname).toString();
            if (tab != null && (field = tab.findAliasColumn(colname)) != null) {
                value = field.getAliasProcess().process(Collections.singletonMap(colname, String.valueOf(value)));
            }
            setField(colname, value);
        }

        public void clearField(String colname) {
            Object old = this.doc.getFieldValue(colname);
            if (old != null && StringUtils.isNotBlank(String.valueOf(old))) {
                this.hasAnyFieldChange = true;
            }
            this.doc.removeField(colname);
        }

        protected Set<String> getAcceptFields() {
            return acceptFields;
        }
        // public void setField(String table, String name, Object value,
        // float boost) {
        // if (!acceptFields.contains(name)) {
        // return;
        // }
        // this.doc.setField(name, value, boost);
        // }
    }

    @SuppressWarnings("all")
    protected void beanCopy(String targetTableName, Object table, TisSolrInputDocument addDoc) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanUtilsBean descUtils = beanDescLocal.get();
        Map desc = descUtils.describe(table);
        // FIXME shall add data convert
        for (Object o : desc.entrySet()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) o;
            if (StringUtils.equals("class", String.valueOf(entry.getKey())) || entry.getValue() == null) {
                continue;
            }
            addDoc.setField(targetTableName, addUnderline(String.valueOf(entry.getKey())).toString(), entry.getValue());
        }
    }

    protected Map<String, String> descBean(Object bean) {
        try {
            return beanDescLocal.get().describe(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static StringBuffer addUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        for (int i = 0; i < nameAry.length; i++) {
            if (Character.isUpperCase(nameAry[i])) {
                parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
            } else {
                parsedName.append(nameAry[i]);
            }
        }
        return parsedName;
    }

    public static StringBuffer removeUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        boolean findUnderChar = false;
        for (int i = 0; i < nameAry.length; i++) {
            if (nameAry[i] == '_') {
                findUnderChar = true;
            // parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
            } else {
                if (findUnderChar) {
                    parsedName.append(Character.toUpperCase(nameAry[i]));
                    findUnderChar = false;
                } else {
                    parsedName.append(nameAry[i]);
                }
            }
        }
        return parsedName;
    }

    public static void main(String[] args) {
        System.out.println(removeUnderline("hello_world"));
    }
}
