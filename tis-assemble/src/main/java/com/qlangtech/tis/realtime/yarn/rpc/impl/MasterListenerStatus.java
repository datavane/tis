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
package com.qlangtech.tis.realtime.yarn.rpc.impl;

import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MasterListenerStatus implements IOnsListenerStatus {

    private final IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocol;

    private static final Logger logger = LoggerFactory.getLogger(MasterListenerStatus.class);

    private final String collectionName;

    public MasterListenerStatus(String collectionName, IncrStatusUmbilicalProtocolImpl incrStatusUmbilicalProtocol) {
        super();
        this.collectionName = collectionName;
        this.incrStatusUmbilicalProtocol = incrStatusUmbilicalProtocol;
    }

    // startService(empty);
    @Override
    public long getSolrConsumeIncrease() {
        return 0;
    // TableSingleDataIndexStatus indexStatus = incrStatusUmbilicalProtocol
    // .getIndexUpdateCounterStatus(collectionName);
    // 
    // if (indexStatus == null) {
    // return 0l;
    // }
    // IncrCounter incr =
    // indexStatus.get(IIncreaseCounter.SOLR_CONSUME_COUNT);
    // if (incr == null) {
    // return 0l;
    // }
    // 
    // if (incr.isExpire()) {
    // logger.info(IIncreaseCounter.SOLR_CONSUME_COUNT + " is expire last:"
    // + incr.getLasterTimeStamp() + ",syscurrent:" +
    // System.currentTimeMillis());
    // incrStatusUmbilicalProtocol.removeIndexUpdateCounterStatus(collectionName);
    // return 0l;
    // }
    // 
    // return incr.getValue();
    }

    @Override
    public void cleanLastAccumulator() {
    }

    @Override
    public String getCollectionName() {
        return this.collectionName;
    }

    @Override
    public String getTableUpdateCount() {
        return "0";
    // try {
    // final TableSingleDataIndexStatus indexStatus =
    // incrStatusUmbilicalProtocol
    // .getIndexUpdateCounterStatus(this.getCollectionName());
    // if (indexStatus == null) {
    // return "{}";
    // }
    // IncrCounter incrCounter = null;
    // JSONArray array = new JSONArray();
    // JSONObject json = null;
    // 
    // for (Map.Entry<String /* table name */, IncrCounter // table
    // > etry : indexStatus.entrySet()) {
    // incrCounter = etry.getValue();
    // if (incrCounter.isExpire()) {
    // continue;
    // }
    // json = new JSONObject();
    // json.put(etry.getKey(), incrCounter.getValue());
    // array.put(json);
    // }
    // 
    // return array.toString(1);
    // } catch (JSONException e) {
    // throw new RuntimeException(e);
    // }
    }

    @Override
    public int getBufferQueueUsedSize() {
        return getCounter(new GetterStrategy() {

            @Override
            public int getInt(TableSingleDataIndexStatus dto) {
                return dto.getBufferQueueUsedSize();
            }
        });
    }

    @Override
    public int getBufferQueueRemainingCapacity() {
        return getCounter(new GetterStrategy() {

            @Override
            public int getInt(TableSingleDataIndexStatus dto) {
                return dto.getBufferQueueRemainingCapacity();
            }
        });
    }

    @Override
    public long getConsumeErrorCount() {
        return getCounter(new GetterStrategy() {

            @Override
            public int getInt(TableSingleDataIndexStatus dto) {
                return dto.getConsumeErrorCount();
            }
        });
    }

    protected int getCounter(GetterStrategy getterStrategy) {
        return 0;
    // TableSingleDataIndexStatus indexStatus = incrStatusUmbilicalProtocol
    // .getIndexUpdateCounterStatus(collectionName);
    // if (indexStatus == null) {
    // return 0;
    // }
    // return getterStrategy.getInt(indexStatus);
    }

    private static interface GetterStrategy {

        int getInt(TableSingleDataIndexStatus dto);
    }

    @Override
    public long getConsumeIncreaseCount() {
        return 0;
    }

    @Override
    public long getIgnoreRowsCount() {
        return 0;
    // TableSingleDataIndexStatus indexStatus = incrStatusUmbilicalProtocol
    // .getIndexUpdateCounterStatus(collectionName);
    // if (indexStatus == null) {
    // return 0;
    // }
    // return indexStatus.getIgnoreRowsCount();
    }

    @Override
    public void resumeConsume() {
        incrStatusUmbilicalProtocol.resumeConsume(this.getCollectionName());
    }

    @Override
    public void pauseConsume() {
        incrStatusUmbilicalProtocol.pauseConsume(this.getCollectionName());
    }

    @Override
    public Set<Entry<String, IIncreaseCounter>> getUpdateStatic() {
        return null;
    // TableSingleDataIndexStatus indexStatus = incrStatusUmbilicalProtocol
    // .getIndexUpdateCounterStatus(collectionName);
    // 
    // if (indexStatus == null) {
    // return Collections.emptySet();
    // }
    // 
    // Map<String, IIncreaseCounter> result = new HashMap<String,
    // IIncreaseCounter>();
    // 
    // for (Map.Entry<String, IncrCounter> entry : indexStatus.entrySet()) {
    // result.put(entry.getKey(), entry.getValue());
    // }
    // 
    // return result.entrySet();
    }

    @Override
    public IIncreaseCounter getMetricCount(String metricName) {
        return null;
    }
}
