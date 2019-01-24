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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TopicInfo {

    final Map<String, Set<String>> /* tags */
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
}
