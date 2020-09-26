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
package com.qlangtech.tis.rpc.grpc.log.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface JoinTaskStatusOrBuilder extends // @@protoc_insertion_point(interface_extends:JoinTaskStatus)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string joinTaskName = 1;</code>
     */
    java.lang.String getJoinTaskName();

    /**
     * <code>string joinTaskName = 1;</code>
     */
    com.google.protobuf.ByteString getJoinTaskNameBytes();

    /**
     * <code>map&lt;uint32, .JobLog&gt; jobStatus = 2;</code>
     */
    int getJobStatusCount();

    /**
     * <code>map&lt;uint32, .JobLog&gt; jobStatus = 2;</code>
     */
    boolean containsJobStatus(int key);

    /**
     * Use {@link #getJobStatusMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.Integer, com.qlangtech.tis.rpc.grpc.log.common.JobLog> getJobStatus();

    /**
     * <code>map&lt;uint32, .JobLog&gt; jobStatus = 2;</code>
     */
    java.util.Map<java.lang.Integer, com.qlangtech.tis.rpc.grpc.log.common.JobLog> getJobStatusMap();

    /**
     * <code>map&lt;uint32, .JobLog&gt; jobStatus = 2;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.common.JobLog getJobStatusOrDefault(int key, com.qlangtech.tis.rpc.grpc.log.common.JobLog defaultValue);

    /**
     * <code>map&lt;uint32, .JobLog&gt; jobStatus = 2;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.common.JobLog getJobStatusOrThrow(int key);

    /**
     * <code>bool faild = 5;</code>
     */
    boolean getFaild();

    /**
     * <code>bool complete = 6;</code>
     */
    boolean getComplete();

    /**
     * <code>bool waiting = 7;</code>
     */
    boolean getWaiting();
}
