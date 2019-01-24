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
package com.qlangtech.tis.runtime.module.action.jarcontent;

import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.GroupChangeSnapshotForm;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GroupChangeSnapshotAction extends BasicModule implements ModelDriven<GroupChangeSnapshotForm> {

    private static final long serialVersionUID = 1L;

    private final GroupChangeSnapshotForm form = new GroupChangeSnapshotForm();

    @Override
    public GroupChangeSnapshotForm getModel() {
        return this.form;
    }

    @Func(PermissionConstant.CONFIG_SNAPSHOT_CHANGE)
    public void doChange(// Navigator nav,
    Context context) throws Exception {
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
