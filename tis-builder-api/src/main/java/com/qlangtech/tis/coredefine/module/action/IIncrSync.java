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
package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.trigger.jst.ILogListener;

/**
 * 增量调用远端客户端接口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IIncrSync {

    /**
     * 发布增量实例
     *
     * @param collection
     * @param incrSpec   增量实例规格
     * @param timestamp
     * @throws Exception
     */
    void deploy(String collection, ReplicasSpec incrSpec, long timestamp) throws Exception;

    /**
     * 删除 增量实例
     *
     * @param collection
     * @throws Exception
     */
    void removeInstance(String collection) throws Exception;

    /**
     * 重启增量节点
     *
     * @param collection
     */
    void relaunch(String collection);

    /**
     * 取得增量实例
     *
     * @param collection
     * @return
     */
    IncrDeployment getRCDeployment(String collection);

    /**
     * 开始增量监听
     *
     * @param collection
     * @param listener
     */
    WatchPodLog listPodAndWatchLog(String collection, String podName, ILogListener listener);
}
