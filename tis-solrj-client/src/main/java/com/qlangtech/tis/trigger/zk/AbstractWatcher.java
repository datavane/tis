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
package com.qlangtech.tis.trigger.zk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractWatcher implements Watcher {

    private static final Log log = LogFactory.getLog(AbstractWatcher.class);

    public final void process(WatchedEvent event) {
        if (EventType.None.equals(event.getType())) {
            return;
        }
        try {
            final Watcher thisWatch = this;
            process(thisWatch);
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.SESSIONEXPIRED || e.code() == KeeperException.Code.CONNECTIONLOSS) {
                log.warn("ZooKeeper watch triggered, but  cannot talk to ZK");
                return;
            }
            log.error("", e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            log.warn("", e);
            return;
        }
    }

    protected abstract void process(Watcher watcher) throws KeeperException, InterruptedException;
}
