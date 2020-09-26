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
public interface PMonotorTargetOrBuilder extends // @@protoc_insertion_point(interface_extends:stream.PMonotorTarget)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string collection = 1;</code>
     */
    java.lang.String getCollection();

    /**
     * <code>string collection = 1;</code>
     */
    com.google.protobuf.ByteString getCollectionBytes();

    /**
     * <code>uint32 taskid = 2;</code>
     */
    int getTaskid();

    /**
     * <code>.stream.PExecuteState.LogType logtype = 3;</code>
     */
    int getLogtypeValue();

    /**
     * <code>.stream.PExecuteState.LogType logtype = 3;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PExecuteState.LogType getLogtype();
}
