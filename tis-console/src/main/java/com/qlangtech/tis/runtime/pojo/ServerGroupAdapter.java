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
package com.qlangtech.tis.runtime.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServerGroupAdapter {

    private final ServerGroup group;

    private final Snapshot snapshot;

    public Integer getPublishSnapshotId() {
        return group.getPublishSnapshotId();
    }

    private int maxSnapshotId;

    // public RunEnvironment getEnvironment() {
    // return RunEnvironment.getEnum(group.getRuntEnvironment());
    // }
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
