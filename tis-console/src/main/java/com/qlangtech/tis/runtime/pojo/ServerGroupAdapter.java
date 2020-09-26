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
package com.qlangtech.tis.runtime.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-22
 */
public class ServerGroupAdapter {

    private final ServerGroup group;

    private final Snapshot snapshot;

    public Integer getPublishSnapshotId() {
        return group.getPublishSnapshotId();
    }

    private int maxSnapshotId;

    public RunEnvironment getEnvironment() {
        return RunEnvironment.getEnum(group.getRuntEnvironment());
    }

    public short getRuntEnvironment() {
        return group.getRuntEnvironment();
    }

    private final List<Server> serverList = new ArrayList<Server>();

    public ServerGroupAdapter(ServerGroup group, Snapshot snapshot) {
        super();
        this.group = group;
        this.snapshot = snapshot;
    }

    public Integer getGid() {
        return getGroup().getGid();
    }

    public ServerGroup getGroup() {
        return group;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void addServer(Server server) {
        serverList.add(server);
    }

    public void addServer(List<Server> serverList) {
        if (serverList == null) {
            return;
        }
        this.serverList.addAll(serverList);
    }

    public final int getServerCount() {
        return this.serverList.size();
    }

    public List<Server> getServerList() {
        return Collections.unmodifiableList(serverList);
    }

    public int getMaxSnapshotId() {
        return maxSnapshotId;
    }

    public void setMaxSnapshotId(int maxSnapshotId) {
        this.maxSnapshotId = maxSnapshotId;
    }

    /**
     * 当前snapshot是否和当前最新的snapshotid相等
     *
     * @return
     */
    public boolean isCurrentSnapshotEqual2Neweast() {
        return this.maxSnapshotId == this.getPublishSnapshotId();
    }
}
