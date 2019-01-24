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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;

/*
 * 服务器组管理<br>
 * /runtime/jarcontent/grouplist.htm
 * @param <ServerGroupCriteria>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Grouplist extends BasicScreen {

    private static final long serialVersionUID = 1L;

    @Func(PermissionConstant.APP_SERVER_GROUP_SET)
    public void execute(Context context) throws Exception {
        this.enableChangeDomain(context);
        AppDomainInfo domain = this.getAppDomain();
        ServerGroupCriteria query = new ServerGroupCriteria();
        query.createCriteria().andAppIdEqualTo(domain.getAppid()).andRuntEnvironmentEqualTo(domain.getRunEnvironment().getId());
        List<ServerGroup> groupList = this.getServerGroupDAO().selectByExample(query);
        // 取得可以选择的snapshot
        SnapshotCriteria criteria = new SnapshotCriteria();
        criteria.createCriteria().andAppidEqualTo(domain.getAppid());
        criteria.setOrderByClause("sn_id desc");
        context.put("candidatesnapshotlist", this.getSnapshotDAO().selectByExample(criteria, 1, 20));
        // 设置grouplist
        context.put("groupList", groupList);
    }
}
