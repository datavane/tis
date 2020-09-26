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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月9日
 */
public interface IIncreaseCounter {

    String SOLR_CONSUME_COUNT = "solrConsume";

    String TABLE_CONSUME_COUNT = "tableConsumeCount";

    MonitorSysTagMarker getMonitorTagMarker();

    // /**
    // * 是否需要被监控系统收集
    // *
    // * @return
    // */
    // boolean shallCollectByMonitorSystem();
    /**
     * 可用于監控項目打標籤 ,格式例如:<br>
     * "tags": "idc=lg,loc=beijing",
     *
     * @return
     */
    // String getTags();
    long getIncreasePastLast();

    /**
     * 历史累加值
     *
     * @return
     */
    long getAccumulation();
}
