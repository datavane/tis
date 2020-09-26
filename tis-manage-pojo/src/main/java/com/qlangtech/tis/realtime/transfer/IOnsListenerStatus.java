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
package com.qlangtech.tis.realtime.transfer;

import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月23日 上午11:28:16
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
     * 增量任务是否暂停中
     *
     * @return
     */
    public boolean isPaused();

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
