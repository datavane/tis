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
package com.qlangtech.tis.manage;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-13
 */
public class SearchConfirm {

    private Integer pid;

    private String queryvalue;

    private Integer snapshotid;

    public Integer getSnapshotid() {
        return snapshotid;
    }

    public void setSnapshotid(Integer snapshotid) {
        this.snapshotid = snapshotid;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getQueryvalue() {
        return queryvalue;
    }

    public void setQueryvalue(String queryvalue) {
        this.queryvalue = queryvalue;
    }
}
