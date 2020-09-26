/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import com.qlangtech.tis.cloud.ITisCloudClient;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.realtime.transfer.ruledriven.AllThreadLocal;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;
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
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 消费增量数据
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月25日 下午2:37:20
 */
public abstract class BasicPojoConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BasicPojoConsumer.class);

    private static final long Time_Window = 20000;

    protected final BasicRMListener onsListener;

    protected ITisCloudClient solrClient;

    public static final Logger sendRecoredLog = LoggerFactory.getLogger("sendRecored");

    public static final String VERSION_FIELD_NAME = "_version_";

    private String consumeName;

    public BasicPojoConsumer(BasicRMListener onsListener) {
        super();
        this.onsListener = onsListener;
    // mockSend();
    }

    protected AliasList getTabColumnMeta(String tableName) {
        return onsListener.getTabColumnMeta(tableName);
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
        IPk pk = pojo.getPK();
        AllThreadLocal.pkThreadLocal.set(pk);
        // boolean processComplete = false;
        while (true) {
            // onsListener.shallPause();
            // synchronized (pojo) {
            TisSolrInputDocument addDoc = createTisDocument(pojo);
            try {
                String collection = pojo.getCollection();
                final String shareId = getShareId(pojo);
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
                if (!addDoc.isHasAnyFieldChange()) {
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
                    solrClient.add(collection, addDoc, version);
                    onsListener.lookPojoVisit(pojo, addDoc, shareId);
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
            } catch (Throwable e) {
                StringBuffer tabsDetail = getTableEnum(pojo);
                log.error("tabDesc:" + tabsDetail);
                if (log.isDebugEnabled() && oldDoc != null) {
                    log.debug("olddoc:" + oldDoc.toString());
                }
                log.error("submit to solr server error,<<<\n" + String.valueOf(addDoc.getInputDoc()) + ">>>\n", e);
                onsListener.increaseConsumeErrorCount(e);
                // 一旦发生错误需要等待5秒之后再发送,需要等待下游将问题解决再启动
                if (!Config.isTestMock() && retryCount++ < 3) {
                    Thread.sleep(5000);
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
            nowVer = BasicRMListener.formatYyyyMMddHHmmss.get().parse(String.valueOf(version));
        }
        long rectified = Long.parseLong(BasicRMListener.formatYyyyMMddHHmmss.get().format(new Date(nowVer.getTime() + 1800 * 1000)));
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
        return (SolrDocument) solrClient.getDocById(collection, pk.getValue(), shareId);
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

    protected TisSolrInputDocument createTisDocument(IPojo pojo) {
        return new TisSolrInputDocument(this.onsListener.getSchemaFieldMeta());
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
     * @param waitProcess  从开始接收到消息到最终处理所花费的时间
     */
    private void sendLog(IPojo pojo, TisSolrInputDocument addDoc, long preVersion, long version, long processStart, long waitProcess, boolean lackSolrRecord) {
        if (sendRecoredLog.isInfoEnabled()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("version:").append(version).append(",add:").append(pojo.isAdd()).append((pojo.isAdd() ? StringUtils.EMPTY : (",preVersion:" + preVersion))).append(",").append(this.consumeName).append(":").append((System.currentTimeMillis() - processStart)).append(",waitprocess:").append(waitProcess).append(",lackSolrRecord:").append(lackSolrRecord).append(",tables:");
            for (Map.Entry<String, IRowPack> entry : pojo.getRowsPack()) {
                buffer.append(entry.getKey()).append("(").append(entry.getValue().getRowSize()).append(")|");
            }
            buffer.append("\n").append(addDoc.getInputDoc());
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

    // public ITisCloudClient getSolrClient() {
    // return solrClient;
    // }
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
                            AllThreadLocal.cleanAllThreadLocalVal();
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

    public void setSolrClient(ITisCloudClient solrClient) {
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
                shardid.value = r.getColumn(this.onsListener.getShardIdName());
                return (shardid.value != null);
            });
        }
        if (shardid.value != null) {
            return String.valueOf(shardid.value);
        }
        throw new IllegalStateException("pojo has not set shareid[" + this.onsListener.getShardIdName() + "]:" + pojo.toString());
    }

    public static final class ShareId {

        public Object value;

        public String getValue() {
            return String.valueOf(value);
        }
    }

    public static final ThreadLocal<BeanUtilsBean> beanDescLocal = new ThreadLocal<BeanUtilsBean>() {

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
            DateConverter dateFormat1 = new DateConverter();
            dateFormat1.setPattern("yyyyMMdd");
            DateConverter dateFormat2 = new DateConverter();
            dateFormat2.setPattern("yyyy-MM-dd");
            beanConvert.register(new Converter() {

                @Override
                public Object convert(Class type, Object value) {
                    try {
                        return dateFormat1.convert(type, value);
                    } catch (Exception e) {
                        return dateFormat2.convert(type, value);
                    }
                }
            }, Date.class);
            BigDecimalConverter bigDecimalConverter = new BigDecimalConverter();
            bigDecimalConverter.setPattern(".###");
            beanConvert.register(bigDecimalConverter, BigDecimal.class);
            BeanUtilsBean utilBean = new BeanUtilsBean(beanConvert);
            return utilBean;
        }
    };

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
            addDoc.setField(targetTableName, UnderlineUtils.addUnderline(String.valueOf(entry.getKey())).toString(), entry.getValue());
        }
    }

    protected Map<String, String> descBean(Object bean) {
        try {
            return beanDescLocal.get().describe(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // public static void main(String[] args) {
    // System.out.println(removeUnderline("hello_world"));
    // }
}
