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
package com.qlangtech.tis.manage.servlet;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-30 09:16
 */
public class TopicTagStatus {

    @JSONField(serialize = true)
    public String getKey() {
        return this.topic + "." + this.tag;
    }

    /**
     * tab名称
     *
     * @return
     */
    public String getTag() {
        return this.tag;
    }

    private final String topic;

    private final String tag;

    private long count;

    private long incr;

    private long lastUpdateTime;

    public TopicTagStatus(String topic, String tag, int count, long lastUpdate) {
        super();
        this.topic = topic;
        this.tag = tag;
        this.count = count;
        this.lastUpdateTime = lastUpdate;
    }

    public void merge(TopicTagStatus n) {
        if (!StringUtils.equals(this.getKey(), n.getKey())) {
            throw new IllegalArgumentException("key1:" + this.getKey() + ",key2:" + n.getKey() + " is not equal");
        }
        // this.setCount(this.count + n.count);
        if (n.lastUpdateTime > this.lastUpdateTime) {
            this.lastUpdateTime = n.lastUpdateTime;
        }
    }

    public void setCount(long count) {
        if (this.count > 0 && count > this.count) {
            this.incr = count - this.count;
        } else {
            this.incr = 0;
        }
        this.count = count;
    }

    @JSONField(serialize = true)
    public long getIncr() {
        return this.incr;
    }

    @JSONField(serialize = true)
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    void clean() {
        this.count = 0;
        this.incr = 0;
    }

    @Override
    public String toString() {
        return "topic:" + this.topic + ",tag:" + this.tag + ",count:" + this.count + ",incr:" + this.incr + ",lastUpdate:" + this.lastUpdateTime;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdateTime = lastUpdate;
    }
}
