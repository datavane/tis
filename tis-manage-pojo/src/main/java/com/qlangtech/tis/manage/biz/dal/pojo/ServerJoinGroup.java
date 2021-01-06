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
package com.qlangtech.tis.manage.biz.dal.pojo;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-4-13
 */
public class ServerJoinGroup extends Server {

    private Integer appId;

    private Short runtEnvironment;

    private Short groupIndex;

    private int port;

    private boolean leader;

    // 是否被选中
    private boolean checked;

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Short getRuntEnvironment() {
        return runtEnvironment;
    }

    public void setRuntEnvironment(Short runtEnvironment) {
        this.runtEnvironment = runtEnvironment;
    }

    public Short getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Short groupIndex) {
        this.groupIndex = groupIndex;
    }


}
