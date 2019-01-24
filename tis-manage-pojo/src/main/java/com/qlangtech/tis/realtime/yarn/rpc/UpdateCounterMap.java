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
package com.qlangtech.tis.realtime.yarn.rpc;

import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.ipc.RPC;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UpdateCounterMap implements org.apache.hadoop.io.Writable {

    private HashMap<String, TableSingleDataIndexStatus> /* indexname */
    data = new HashMap<>();

    // 增量转发节点执行增量的数量
    private long gcCounter;

    // 从哪个地址发送过来的
    private String from;

    private long updateTime;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getGcCounter() {
        return gcCounter;
    }

    public void setGcCounter(long gcCounter) {
        this.gcCounter = gcCounter;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    // private static final long serialVersionUID = 1L;
    public boolean containIndex(String indexName) {
        return data.keySet().contains(indexName);
    }

    public boolean containsIndex(String indexName, String uuid) {
        TableSingleDataIndexStatus tableSingleDataIndexStatus = data.get(indexName);
        if (tableSingleDataIndexStatus == null) {
            return false;
        } else {
            return StringUtils.equals(tableSingleDataIndexStatus.getUUID(), uuid);
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(data.size());
        for (Map.Entry<String, TableSingleDataIndexStatus> /* indexname */
        entry : data.entrySet()) {
            TableSingleDataIndexStatus singleDataIndexStatus = entry.getValue();
            WritableUtils.writeString(out, entry.getKey());
            out.writeInt(singleDataIndexStatus.tableSize());
            for (Map.Entry<String, Long> /* tableName */
            tabUpdate : singleDataIndexStatus.getTableConsumeData().entrySet()) {
                WritableUtils.writeString(out, tabUpdate.getKey());
                out.writeLong(tabUpdate.getValue());
            }
            out.writeInt(singleDataIndexStatus.getBufferQueueRemainingCapacity());
            out.writeInt(singleDataIndexStatus.getBufferQueueUsedSize());
            out.writeInt(singleDataIndexStatus.getConsumeErrorCount());
            out.writeInt(singleDataIndexStatus.getIgnoreRowsCount());
            out.writeLong(singleDataIndexStatus.getTis30sAvgRT());
            WritableUtils.writeString(out, singleDataIndexStatus.getUUID());
        }
        out.writeLong(gcCounter);
        WritableUtils.writeString(out, from);
        out.writeLong(updateTime);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int indexSize = in.readInt();
        int tabsCount;
        for (int i = 0; i < indexSize; i++) {
            String indexName = WritableUtils.readString(in);
            tabsCount = in.readInt();
            TableSingleDataIndexStatus tableUpdateCounter = new TableSingleDataIndexStatus();
            for (int j = 0; j < tabsCount; j++) {
                // tableUpdateCounter.put(WritableUtils.readString(in), new IncrCounter(in.readInt()));
                tableUpdateCounter.put(WritableUtils.readString(in), in.readLong());
            }
            tableUpdateCounter.setBufferQueueRemainingCapacity(in.readInt());
            tableUpdateCounter.setBufferQueueUsedSize(in.readInt());
            tableUpdateCounter.setConsumeErrorCount(in.readInt());
            tableUpdateCounter.setIgnoreRowsCount(in.readInt());
            tableUpdateCounter.setTis30sAvgRT(in.readLong());
            tableUpdateCounter.setUUID(WritableUtils.readString(in));
            data.put(indexName, tableUpdateCounter);
        }
        this.gcCounter = in.readLong();
        this.from = WritableUtils.readString(in);
        this.updateTime = in.readLong();
    }

    public void addTableCounter(String indexName, TableSingleDataIndexStatus tableUpdateCounter) {
        this.data.put(indexName, tableUpdateCounter);
    }

    public HashMap<String, TableSingleDataIndexStatus> getData() {
        return this.data;
    }

    public static void main(String[] args) throws Exception {
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", 1234);
        IncrStatusUmbilicalProtocol incrStatusUmbilicalProtocol = RPC.getProxy(IncrStatusUmbilicalProtocol.class, IncrStatusUmbilicalProtocol.versionID, address, new Configuration());
        UpdateCounterMap updateCounterMap = new UpdateCounterMap();
        TableSingleDataIndexStatus indexStatus = new TableSingleDataIndexStatus();
        updateCounterMap.addTableCounter("index1", indexStatus);
        indexStatus.setUUID("uuid");
        indexStatus.setConsumeErrorCount(1);
        indexStatus.setBufferQueueRemainingCapacity(1);
        indexStatus.setBufferQueueUsedSize(1);
        indexStatus.setIgnoreRowsCount(1);
        indexStatus.put("tab1", 10L);
        MasterJob masterJob = incrStatusUmbilicalProtocol.reportStatus(updateCounterMap);
        System.out.println(masterJob);
    }
    // public static class TableSingleDataIndexStatus extends HashMap<String/* tableName */, ConsumeDataKeeper> {
    // private static final long serialVersionUID = 1L;
    // 
    // private int bufferQueueRemainingCapacity;
    // private int bufferQueueUsedSize;
    // private int consumeErrorCount;
    // private int ignoreRowsCount;
    // private UUID uuid;
    // private long createTime;
    // 
    // private HashMap<String /*tableName*/, LinkedList<ConsumeDataKeeper>> partitionTableDataKeeper;
    // 
    // public int getBufferQueueUsedSize() {
    // return bufferQueueUsedSize;
    // }
    // 
    // public void setBufferQueueUsedSize(int bufferQueueUsedSize) {
    // this.bufferQueueUsedSize = bufferQueueUsedSize;
    // }
    // 
    // public int getBufferQueueRemainingCapacity() {
    // return bufferQueueRemainingCapacity;
    // }
    // 
    // public void setBufferQueueRemainingCapacity(int bufferQueueRemainingCapacity) {
    // this.bufferQueueRemainingCapacity = bufferQueueRemainingCapacity;
    // }
    // 
    // public int getConsumeErrorCount() {
    // return consumeErrorCount;
    // }
    // 
    // public void setConsumeErrorCount(int consumeErrorCount) {
    // this.consumeErrorCount = consumeErrorCount;
    // }
    // 
    // public int getIgnoreRowsCount() {
    // return ignoreRowsCount;
    // }
    // 
    // public void setIgnoreRowsCount(int ignoreRowsCount) {
    // this.ignoreRowsCount = ignoreRowsCount;
    // }
    // 
    // public UUID getUUID() {
    // return uuid;
    // }
    // 
    // public void setUUID(UUID uuid) {
    // this.uuid = uuid;
    // }
    // 
    // public long getCreateTime() {
    // return createTime;
    // }
    // 
    // public void setCreateTime(long createTime) {
    // this.createTime = createTime;
    // }
    // 
    // public HashMap<String, LinkedList<ConsumeDataKeeper>> getPartitionTableDataKeeper() {
    // return partitionTableDataKeeper;
    // }
    // 
    // public void setPartitionTableDataKeeper(HashMap<String, LinkedList<ConsumeDataKeeper>> partitionTableDataKeeper) {
    // this.partitionTableDataKeeper = partitionTableDataKeeper;
    // }
    // }
}
