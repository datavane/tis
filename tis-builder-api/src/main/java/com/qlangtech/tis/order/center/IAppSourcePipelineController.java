/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.order.center;

/**
 * 可以控制DataX执行器，数据增量同步管道等的停止
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-01 15:51
 **/
public interface IAppSourcePipelineController {
    String DATAX_FULL_PIPELINE = "dataX_full_pipeline_";

    /**
     * dataX全量会直接把进程关闭，作用于增量管道只是停止不继续消费数据（进程不会kill掉），客户可以调用
     * resume() 方法继续运行
     *
     * @param appName
     */
    public boolean stop(String appName);

    public boolean resume(String appName);

    public void registerAppSubExecNodeMetrixStatus(String appName, String subExecNodeId);
}
