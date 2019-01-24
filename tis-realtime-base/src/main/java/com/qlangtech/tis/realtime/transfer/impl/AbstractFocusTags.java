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
package com.qlangtech.tis.realtime.transfer.impl;

import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import com.qlangtech.tis.realtime.transfer.IFocusTags;

/*
 * 关注的MQ信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractFocusTags implements IFocusTags {

    private Set<String> tags;

    private String topic = StringUtils.EMPTY;

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
            }
            return this.focusTabs;
        } finally {
            MDC.remove("app");
        }
    }
}
