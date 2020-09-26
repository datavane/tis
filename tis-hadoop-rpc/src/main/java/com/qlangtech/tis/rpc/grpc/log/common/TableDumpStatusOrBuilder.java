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
public interface TableDumpStatusOrBuilder extends // @@protoc_insertion_point(interface_extends:TableDumpStatus)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string tableName = 1;</code>
     */
    java.lang.String getTableName();

    /**
     * <code>string tableName = 1;</code>
     */
    com.google.protobuf.ByteString getTableNameBytes();

    /**
     * <code>uint32 taskid = 2;</code>
     */
    int getTaskid();

    /**
     * <pre>
     * 全部的记录数
     * </pre>
     *
     * <code>uint32 allRows = 3;</code>
     */
    int getAllRows();

    /**
     * <pre>
     * 已经读取的记录数
     * </pre>
     *
     * <code>uint32 readRows = 4;</code>
     */
    int getReadRows();

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
