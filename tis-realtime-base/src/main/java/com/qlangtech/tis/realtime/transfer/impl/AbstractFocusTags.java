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
package com.qlangtech.tis.realtime.transfer.impl;

import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.qlangtech.tis.realtime.transfer.IFocusTags;

/**
 * 关注的MQ信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年5月3日
 */
public abstract class AbstractFocusTags implements IFocusTags {

    private Set<String> tags;

    private String topic = StringUtils.EMPTY;

    private static final Logger logger = LoggerFactory.getLogger(AbstractFocusTags.class);

    private String collection;

    @Override
    public String getCollectionName() {
        return this.collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        // 由于二维火这边 topic和tags是分离的 这个方法暂时不用
        return topic;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public Set<String> getFocusTags() {
        return this.tags;
    }

    private String focusTabs = null;

    // private final ScheduledExecutorService scheduler =
    // Executors.newScheduledThreadPool(1);
    public String getSubExpression() {
        try {
            if (focusTabs == null) {
                StringBuffer result = new StringBuffer();
                Set<String> focus = getFocusTags();
                int tabSize = focus.size();
                int count = 0;
                for (String tab : focus) {
                    result.append(tab);
                    if (++count < tabSize) {
                        result.append(" || ");
                    }
                }
                focusTabs = StringUtils.trim(result.toString());
                logger.info("focus tables:" + focusTabs);
            }
            return this.focusTabs;
        } finally {
            MDC.remove("app");
        }
    }
}
