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
public interface LaunchReportInfoEntryOrBuilder extends // @@protoc_insertion_point(interface_extends:rpc.LaunchReportInfoEntry)
com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * topic
     * </pre>
     *
     * <code>string topicName = 1;</code>
     */
    java.lang.String getTopicName();

    /**
     * <pre>
     * topic
     * </pre>
     *
     * <code>string topicName = 1;</code>
     */
    com.google.protobuf.ByteString getTopicNameBytes();

    /**
     * <pre>
     * tags
     * </pre>
     *
     * <code>repeated string tagName = 2;</code>
     */
    java.util.List<java.lang.String> getTagNameList();

    /**
     * <pre>
     * tags
     * </pre>
     *
     * <code>repeated string tagName = 2;</code>
     */
    int getTagNameCount();

    /**
     * <pre>
     * tags
     * </pre>
     *
     * <code>repeated string tagName = 2;</code>
     */
    java.lang.String getTagName(int index);

    /**
     * <pre>
     * tags
     * </pre>
     *
     * <code>repeated string tagName = 2;</code>
     */
    com.google.protobuf.ByteString getTagNameBytes(int index);
}
