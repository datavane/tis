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
public interface PExecuteStateOrBuilder extends // @@protoc_insertion_point(interface_extends:stream.PExecuteState)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.stream.PExecuteState.InfoType infoType = 1;</code>
     */
    int getInfoTypeValue();

    /**
     * <code>.stream.PExecuteState.InfoType infoType = 1;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PExecuteState.InfoType getInfoType();

    /**
     * <code>.stream.PExecuteState.LogType logType = 2;</code>
     */
    int getLogTypeValue();

    /**
     * <code>.stream.PExecuteState.LogType logType = 2;</code>
     */
    com.qlangtech.tis.rpc.grpc.log.stream.PExecuteState.LogType getLogType();

    /**
     * <code>string msg = 3;</code>
     */
    java.lang.String getMsg();

    /**
     * <code>string msg = 3;</code>
     */
    com.google.protobuf.ByteString getMsgBytes();

    /**
     * <code>string from = 4;</code>
     */
    java.lang.String getFrom();

    /**
     * <code>string from = 4;</code>
     */
    com.google.protobuf.ByteString getFromBytes();

    /**
     * <code>uint64 jobId = 5;</code>
     */
    long getJobId();

    /**
     * <code>uint64 taskId = 6;</code>
     */
    long getTaskId();

    /**
     * <code>string serviceName = 7;</code>
     */
    java.lang.String getServiceName();

    /**
     * <code>string serviceName = 7;</code>
     */
    com.google.protobuf.ByteString getServiceNameBytes();

    /**
     * <code>string execState = 8;</code>
     */
    java.lang.String getExecState();

    /**
     * <code>string execState = 8;</code>
     */
    com.google.protobuf.ByteString getExecStateBytes();

    /**
     * <code>uint64 time = 9;</code>
     */
    long getTime();

    /**
     * <code>string component = 10;</code>
     */
    java.lang.String getComponent();

    /**
     * <code>string component = 10;</code>
     */
    com.google.protobuf.ByteString getComponentBytes();
}
