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
public interface UpdateCounterMapOrBuilder extends // @@protoc_insertion_point(interface_extends:rpc.UpdateCounterMap)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;string, .rpc.TableSingleDataIndexStatus&gt; data = 1;</code>
     */
    int getDataCount();

    /**
     * <code>map&lt;string, .rpc.TableSingleDataIndexStatus&gt; data = 1;</code>
     */
    boolean containsData(java.lang.String key);

    /**
     * Use {@link #getDataMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, com.qlangtech.tis.grpc.TableSingleDataIndexStatus> getData();

    /**
     * <code>map&lt;string, .rpc.TableSingleDataIndexStatus&gt; data = 1;</code>
     */
    java.util.Map<java.lang.String, com.qlangtech.tis.grpc.TableSingleDataIndexStatus> getDataMap();

    /**
     * <code>map&lt;string, .rpc.TableSingleDataIndexStatus&gt; data = 1;</code>
     */
    com.qlangtech.tis.grpc.TableSingleDataIndexStatus getDataOrDefault(java.lang.String key, com.qlangtech.tis.grpc.TableSingleDataIndexStatus defaultValue);

    /**
     * <code>map&lt;string, .rpc.TableSingleDataIndexStatus&gt; data = 1;</code>
     */
    com.qlangtech.tis.grpc.TableSingleDataIndexStatus getDataOrThrow(java.lang.String key);

    /**
     * <code>uint64 gcCounter = 2;</code>
     */
    long getGcCounter();

    /**
     * <pre>
     * 从哪个地址发送过来的
     * </pre>
     *
     * <code>string from = 3;</code>
     */
    java.lang.String getFrom();

    /**
     * <pre>
     * 从哪个地址发送过来的
     * </pre>
     *
     * <code>string from = 3;</code>
     */
    com.google.protobuf.ByteString getFromBytes();

    /**
     * <code>uint64 updateTime = 4;</code>
     */
    long getUpdateTime();
}
