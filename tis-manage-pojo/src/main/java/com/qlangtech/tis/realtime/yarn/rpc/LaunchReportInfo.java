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
package com.qlangtech.tis.realtime.yarn.rpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 增量监听节点启动，将本地的状态发送到服务端去
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月28日
 */
public class LaunchReportInfo {

    // 本地节点的编号
    // private String localUUID;
    private Map<String, TopicInfo> /* collection */
    collectionFocusTopicInfo;

    public LaunchReportInfo() {
        super();
        this.collectionFocusTopicInfo = new HashMap<>();
    }

    public Map<String, /* collection */
    TopicInfo> getCollectionFocusTopicInfo() {
        return Collections.unmodifiableMap(this.collectionFocusTopicInfo);
    }

    public LaunchReportInfo(Map<String, /* collection */
    TopicInfo> collectionFocusTopicInfo) {
        super();
        // this.localUUID = localUUID;
        this.collectionFocusTopicInfo = collectionFocusTopicInfo;
        if (collectionFocusTopicInfo == null || collectionFocusTopicInfo.isEmpty()) {
            throw new IllegalArgumentException("param collectionFocusTopicInfo can not be null");
        }
    }
    // @Override
    // public void write(DataOutput out) throws IOException {
    // // 序列化
    // // WritableUtils.writeString(out, localUUID);
    // out.writeInt(collectionFocusTopicInfo.size());
    // for (Map.Entry<String, TopicInfo> /* collection */
    // entry : collectionFocusTopicInfo.entrySet()) {
    // WritableUtils.writeString(out, entry.getKey());
    // this.writeTopicInfo(out, entry.getValue());
    // }
    // }
    // private void writeTopicInfo(DataOutput out, TopicInfo info) throws IOException {
    // Map<String, Set<String>> /* tags */
    // topicWithTags = info.topicWithTags;
    // out.writeInt(topicWithTags.size());
    // for (Map.Entry<String, Set<String>> entry : topicWithTags.entrySet()) {
    // WritableUtils.writeString(out, entry.getKey());
    // out.writeInt(entry.getValue().size());
    // for (String tag : entry.getValue()) {
    // WritableUtils.writeString(out, tag);
    // }
    // }
    // }
    // @Override
    // public void readFields(DataInput in) throws IOException {
    // // 反序列化
    // // this.localUUID = WritableUtils.readString(in);
    // int collectionCount = in.readInt();
    // String collectionName = null;
    // int tagCount = 0;
    // int topicCount = 0;
    // TopicInfo topicInfo = null;
    // String topicName = null;
    // for (int i = 0; i < collectionCount; i++) {
    // collectionName = WritableUtils.readString(in);
    // topicInfo = new TopicInfo();
    // collectionFocusTopicInfo.put(collectionName, topicInfo);
    // topicCount = in.readInt();
    // for (int t = 0; t < topicCount; t++) {
    // topicName = WritableUtils.readString(in);
    // tagCount = in.readInt();
    // for (int tagIndex = 0; tagIndex < tagCount; tagIndex++) {
    // topicInfo.addTag(topicName, WritableUtils.readString(in));
    // }
    // }
    // }
    // }
}
