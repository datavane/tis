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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.AppPackage;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-14
 */
public class BasicDomain {

    private BasicDomain() {
    }

    private Application application;

    // private ServerGroup group;
    private Snapshot snapshot;

    private AppPackage pack;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public final Snapshot getSnapshot() {
        return snapshot;
    }

    public final void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public AppPackage getPack() {
        return pack;
    }

    public void setPack(AppPackage pack) {
        this.pack = pack;
    }
}
