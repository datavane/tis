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
package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.impl.RcDeployment;
import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.trigger.jst.ILogListener;

/**
 * 增量调用远端客户端接口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRCController {

    /**
     * 发布增量实例
     *
     * @param collection
     * @param incrSpec   增量实例规格
     * @param timestamp
     * @throws Exception
     */
    void deploy(TargetResName collection, ReplicasSpec incrSpec, long timestamp) throws Exception;

    /**
     * 删除 增量实例
     *
     * @param collection
     * @throws Exception
     */
    void removeInstance(TargetResName collection) throws Exception;

    /**
     * 重启增量节点
     *
     * @param collection
     */
    void relaunch(TargetResName collection, String... targetPod);

    /**
     * 取得增量实例
     *
     * @param collection
     * @return
     */
    IDeploymentDetail getRCDeployment(TargetResName collection);

    /**
     * 开始增量监听
     *
     * @param collection
     * @param listener
     */
    WatchPodLog listPodAndWatchLog(TargetResName collection, String podName, ILogListener listener);
}
