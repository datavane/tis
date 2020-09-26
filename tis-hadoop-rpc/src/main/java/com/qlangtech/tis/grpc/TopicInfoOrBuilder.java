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
public interface TopicInfoOrBuilder extends // @@protoc_insertion_point(interface_extends:rpc.TopicInfo)
com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated .rpc.LaunchReportInfoEntry topicWithTags = 1;</code>
     */
    java.util.List<com.qlangtech.tis.grpc.LaunchReportInfoEntry> getTopicWithTagsList();

    /**
     * <code>repeated .rpc.LaunchReportInfoEntry topicWithTags = 1;</code>
     */
    com.qlangtech.tis.grpc.LaunchReportInfoEntry getTopicWithTags(int index);

    /**
     * <code>repeated .rpc.LaunchReportInfoEntry topicWithTags = 1;</code>
     */
    int getTopicWithTagsCount();

    /**
     * <code>repeated .rpc.LaunchReportInfoEntry topicWithTags = 1;</code>
     */
    java.util.List<? extends com.qlangtech.tis.grpc.LaunchReportInfoEntryOrBuilder> getTopicWithTagsOrBuilderList();

    /**
     * <code>repeated .rpc.LaunchReportInfoEntry topicWithTags = 1;</code>
     */
    com.qlangtech.tis.grpc.LaunchReportInfoEntryOrBuilder getTopicWithTagsOrBuilder(int index);
}
