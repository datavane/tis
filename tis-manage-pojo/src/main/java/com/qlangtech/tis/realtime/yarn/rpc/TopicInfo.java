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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月28日
 */
public class TopicInfo {

    Map<String, Set<String>> /* tags */
    topicWithTags = new HashMap<>();

    public void addTag(String topic, Set<String> tgs) {
        this.getTagSet(topic).addAll(tgs);
    }

    public void addTag(String topic, String tag) {
        this.getTagSet(topic).add(tag);
    }

    private Set<String> getTagSet(String topic) {
        Set<String> tags = topicWithTags.get(topic);
        if (tags == null) {
            synchronized (this) {
                tags = topicWithTags.get(topic);
                if (tags == null) {
                    tags = new HashSet<>();
                    topicWithTags.put(topic, tags);
                }
            }
        }
        return tags;
    }

    public Map<String, Set<String>> getTopicWithTags() {
        return this.topicWithTags;
    }

    public void setTopicWithTags(Map<String, Set<String>> topicWithTags) {
        this.topicWithTags = topicWithTags;
    }
}
