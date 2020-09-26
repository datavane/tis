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
package com.qlangtech.tis.runtime.module.action.jarcontent;

import com.alibaba.citrus.turbine.Context;
import junit.framework.Assert;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.GroupChangeSnapshotForm;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-16
 */
public class GroupChangeSnapshotAction extends BasicModule implements ModelDriven<GroupChangeSnapshotForm> {

    private static final long serialVersionUID = 1L;

    private final GroupChangeSnapshotForm form = new GroupChangeSnapshotForm();

    @Override
    public GroupChangeSnapshotForm getModel() {
        return this.form;
    }

    @Func(PermissionConstant.CONFIG_SNAPSHOT_CHANGE)
    public // Navigator nav,
    void doChange(Context context) throws Exception {
        Assert.assertNotNull("form can not be null", form);
        Assert.assertNotNull("form.getGroupSnapshot() can not be null", form.getGroupSnapshot());
        Assert.assertNotNull("form.getSnapshotId() can not be null", form.getSnapshotId());
        ServerGroupCriteria criteria = new ServerGroupCriteria();
        criteria.createCriteria().andGidEqualTo(form.getGroupId()).andRuntEnvironmentEqualTo(this.getAppDomain().getRunEnvironment().getId());
        ServerGroup group = new ServerGroup();
        group.setPublishSnapshotId(form.getSnapshotId());
        if (this.getServerGroupDAO().updateByExampleSelective(group, criteria) < 1) {
            throw new IllegalArgumentException("has not update success");
        }
        this.addActionMessage(context, "已经将第" + this.getServerGroupDAO().selectByPrimaryKey(form.getGroupId()).getGroupIndex() + "组，发布快照切换成：snapshot" + form.getSnapshotId());
    }
}
