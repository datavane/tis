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
public interface MasterJobOrBuilder extends // @@protoc_insertion_point(interface_extends:rpc.MasterJob)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.rpc.MasterJob.JobType jobType = 1;</code>
     */
    int getJobTypeValue();

    /**
     * <code>.rpc.MasterJob.JobType jobType = 1;</code>
     */
    com.qlangtech.tis.grpc.MasterJob.JobType getJobType();

    /**
     * <code>bool stop = 2;</code>
     */
    boolean getStop();

    /**
     * <code>string indexName = 3;</code>
     */
    java.lang.String getIndexName();

    /**
     * <code>string indexName = 3;</code>
     */
    com.google.protobuf.ByteString getIndexNameBytes();

    /**
     * <code>string uuid = 4;</code>
     */
    java.lang.String getUuid();

    /**
     * <code>string uuid = 4;</code>
     */
    com.google.protobuf.ByteString getUuidBytes();

    /**
     * <code>uint64 createTime = 5;</code>
     */
    long getCreateTime();
}
