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
public interface PPhaseStatusCollectionOrBuilder extends // @@protoc_insertion_point(interface_extends:stream.PPhaseStatusCollection)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.stream.PDumpPhaseStatus dumpPhase = 1;</code>
     */
    boolean hasDumpPhase();

    /**
     * <code>.stream.PDumpPhaseStatus dumpPhase = 1;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PDumpPhaseStatus getDumpPhase();

    /**
     * <code>.stream.PDumpPhaseStatus dumpPhase = 1;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PDumpPhaseStatusOrBuilder getDumpPhaseOrBuilder();

    /**
     * <code>.stream.PJoinPhaseStatus joinPhase = 2;</code>
     */
    boolean hasJoinPhase();

    /**
     * <code>.stream.PJoinPhaseStatus joinPhase = 2;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PJoinPhaseStatus getJoinPhase();

    /**
     * <code>.stream.PJoinPhaseStatus joinPhase = 2;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PJoinPhaseStatusOrBuilder getJoinPhaseOrBuilder();

    /**
     * <code>.stream.PBuildPhaseStatus buildPhase = 3;</code>
     */
    boolean hasBuildPhase();

    /**
     * <code>.stream.PBuildPhaseStatus buildPhase = 3;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PBuildPhaseStatus getBuildPhase();

    /**
     * <code>.stream.PBuildPhaseStatus buildPhase = 3;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PBuildPhaseStatusOrBuilder getBuildPhaseOrBuilder();

    /**
     * <code>.stream.PIndexBackFlowPhaseStatus indexBackFlowPhaseStatus = 4;</code>
     */
    boolean hasIndexBackFlowPhaseStatus();

    /**
     * <code>.stream.PIndexBackFlowPhaseStatus indexBackFlowPhaseStatus = 4;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PIndexBackFlowPhaseStatus getIndexBackFlowPhaseStatus();

    /**
     * <code>.stream.PIndexBackFlowPhaseStatus indexBackFlowPhaseStatus = 4;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PIndexBackFlowPhaseStatusOrBuilder getIndexBackFlowPhaseStatusOrBuilder();

    /**
     * <code>uint32 taskId = 5;</code>
     */
    int getTaskId();
}
