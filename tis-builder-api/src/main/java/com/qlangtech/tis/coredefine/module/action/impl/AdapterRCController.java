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

package com.qlangtech.tis.coredefine.module.action.impl;

import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.IRCController;
import com.qlangtech.tis.coredefine.module.action.RcDeployment;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.plugin.incr.WatchPodLog;
import com.qlangtech.tis.trigger.jst.ILogListener;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-07 22:47
 **/
public class AdapterRCController implements IRCController {
    @Override
    public void deploy(TargetResName collection, ReplicasSpec incrSpec, long timestamp) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeInstance(TargetResName collection) throws Exception {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public void relaunch(String collection) {
//        throw new UnsupportedOperationException();
//    }


    @Override
    public void relaunch(TargetResName collection, String... targetPod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RcDeployment getRCDeployment(TargetResName collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchPodLog listPodAndWatchLog(TargetResName collection, String podName, ILogListener listener) {
        throw new UnsupportedOperationException();
    }
}
