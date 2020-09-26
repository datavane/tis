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
public interface BuildSharedPhaseStatusOrBuilder extends // @@protoc_insertion_point(interface_extends:BuildSharedPhaseStatus)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint64 allBuildSize = 1;</code>
     */
    long getAllBuildSize();

    /**
     * <code>uint64 buildReaded = 2;</code>
     */
    long getBuildReaded();

    /**
     * <code>uint32 taskid = 3;</code>
     */
    int getTaskid();

    /**
     * <pre>
     * 分组名称
     * </pre>
     *
     * <code>string sharedName = 4;</code>
     */
    java.lang.String getSharedName();

    /**
     * <pre>
     * 分组名称
     * </pre>
     *
     * <code>string sharedName = 4;</code>
     */
    com.google.protobuf.ByteString getSharedNameBytes();

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
