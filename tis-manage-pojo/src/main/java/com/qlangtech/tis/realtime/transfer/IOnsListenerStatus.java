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
package com.qlangtech.tis.realtime.transfer;

import java.util.Map;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IOnsListenerStatus {

    public long getSolrConsumeIncrease();

    /**
     * 消费执行错误
     *
     * @return
     */
    public long getConsumeErrorCount();

    /**
     * 由于增量发送过来的表的更新并不是都关心的所以部分记录需要抛弃掉
     *
     * @return
     */
    public long getIgnoreRowsCount();

    public void cleanLastAccumulator();

    public String getCollectionName();

    /**
     * 返回的是json结构的
     *
     * @return
     */
    public String getTableUpdateCount();

    public int getBufferQueueUsedSize();

    /**
     * 缓冲区剩余容量
     *
     * @return
     */
    public int getBufferQueueRemainingCapacity();

    public long getConsumeIncreaseCount();

    /**
     * 重新启动消费增量
     */
    public void resumeConsume();

    /**
     * 停止消费增量日志
     */
    public void pauseConsume();

    public Set<Map.Entry<String, IIncreaseCounter>> getUpdateStatic();

    /**
     * 取得指标统计对象
     *
     * @param metricName
     * @return
     */
    public IIncreaseCounter getMetricCount(String metricName);
}
