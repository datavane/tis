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
package com.qlangtech.tis.rpc.grpc.log.stream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface PIndexBackFlowPhaseStatusOrBuilder extends // @@protoc_insertion_point(interface_extends:stream.PIndexBackFlowPhaseStatus)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;string, .NodeBackflowStatus&gt; nodesStatus = 1;</code>
     */
    int getNodesStatusCount();

    /**
     * <code>map&lt;string, .NodeBackflowStatus&gt; nodesStatus = 1;</code>
     */
    boolean containsNodesStatus(java.lang.String key);

    /**
     * Use {@link #getNodesStatusMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, com.qlangtech.tis.rpc.grpc.log.common.NodeBackflowStatus> getNodesStatus();

    /**
     * <code>map&lt;string, .NodeBackflowStatus&gt; nodesStatus = 1;</code>
     */
    java.util.Map<java.lang.String, com.qlangtech.tis.rpc.grpc.log.common.NodeBackflowStatus> getNodesStatusMap();

    /**
     * <code>map&lt;string, .NodeBackflowStatus&gt; nodesStatus = 1;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.common.NodeBackflowStatus getNodesStatusOrDefault(java.lang.String key, com.qlangtech.tis.rpc.grpc.log.common.NodeBackflowStatus defaultValue);

    /**
     * <code>map&lt;string, .NodeBackflowStatus&gt; nodesStatus = 1;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.common.NodeBackflowStatus getNodesStatusOrThrow(java.lang.String key);
}
