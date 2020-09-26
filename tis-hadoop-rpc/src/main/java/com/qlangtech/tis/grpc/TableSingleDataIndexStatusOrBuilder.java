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
package com.qlangtech.tis.grpc;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface TableSingleDataIndexStatusOrBuilder extends // @@protoc_insertion_point(interface_extends:rpc.TableSingleDataIndexStatus)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;string, uint64&gt; tableConsumeData = 1;</code>
     */
    int getTableConsumeDataCount();

    /**
     * <code>map&lt;string, uint64&gt; tableConsumeData = 1;</code>
     */
    boolean containsTableConsumeData(java.lang.String key);

    /**
     * Use {@link #getTableConsumeDataMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.Long> getTableConsumeData();

    /**
     * <code>map&lt;string, uint64&gt; tableConsumeData = 1;</code>
     */
    java.util.Map<java.lang.String, java.lang.Long> getTableConsumeDataMap();

    /**
     * <code>map&lt;string, uint64&gt; tableConsumeData = 1;</code>
     */
    long getTableConsumeDataOrDefault(java.lang.String key, long defaultValue);

    /**
     * <code>map&lt;string, uint64&gt; tableConsumeData = 1;</code>
     */
    long getTableConsumeDataOrThrow(java.lang.String key);

    /**
     * <code>uint32 bufferQueueRemainingCapacity = 2;</code>
     */
    int getBufferQueueRemainingCapacity();

    /**
     * <code>uint32 bufferQueueUsedSize = 3;</code>
     */
    int getBufferQueueUsedSize();

    /**
     * <code>uint32 consumeErrorCount = 4;</code>
     */
    int getConsumeErrorCount();

    /**
     * <code>uint32 ignoreRowsCount = 5;</code>
     */
    int getIgnoreRowsCount();

    /**
     * <code>string uuid = 6;</code>
     */
    java.lang.String getUuid();

    /**
     * <code>string uuid = 6;</code>
     */
    com.google.protobuf.ByteString getUuidBytes();

    /**
     * <code>uint64 tis30sAvgRT = 7;</code>
     */
    long getTis30SAvgRT();

    /**
     * <pre>
     * 增量任务执行是否暂停
     * </pre>
     *
     * <code>bool incrProcessPaused = 8;</code>
     */
    boolean getIncrProcessPaused();
}
