/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.realtime.yarn.rpc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.io.WritableUtils;

/*
 * 增量监听节点启动，将本地的状态发送到服务端去
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LaunchReportInfo implements org.apache.hadoop.io.Writable {

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

    @Override
    public void write(DataOutput out) throws IOException {
        // 序列化
        // WritableUtils.writeString(out, localUUID);
        out.writeInt(collectionFocusTopicInfo.size());
        for (Map.Entry<String, TopicInfo> /* collection */
        entry : collectionFocusTopicInfo.entrySet()) {
            WritableUtils.writeString(out, entry.getKey());
            this.writeTopicInfo(out, entry.getValue());
        }
    }

    private void writeTopicInfo(DataOutput out, TopicInfo info) throws IOException {
        Map<String, Set<String>> /* tags */
        topicWithTags = info.topicWithTags;
        out.writeInt(topicWithTags.size());
        for (Map.Entry<String, Set<String>> entry : topicWithTags.entrySet()) {
            WritableUtils.writeString(out, entry.getKey());
            out.writeInt(entry.getValue().size());
            for (String tag : entry.getValue()) {
                WritableUtils.writeString(out, tag);
            }
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        // 反序列化
        // this.localUUID = WritableUtils.readString(in);
        int collectionCount = in.readInt();
        String collectionName = null;
        int tagCount = 0;
        int topicCount = 0;
        TopicInfo topicInfo = null;
        String topicName = null;
        for (int i = 0; i < collectionCount; i++) {
            collectionName = WritableUtils.readString(in);
            topicInfo = new TopicInfo();
            collectionFocusTopicInfo.put(collectionName, topicInfo);
            topicCount = in.readInt();
            for (int t = 0; t < topicCount; t++) {
                topicName = WritableUtils.readString(in);
                tagCount = in.readInt();
                for (int tagIndex = 0; tagIndex < tagCount; tagIndex++) {
                    topicInfo.addTag(topicName, WritableUtils.readString(in));
                }
            }
        }
    }
}
