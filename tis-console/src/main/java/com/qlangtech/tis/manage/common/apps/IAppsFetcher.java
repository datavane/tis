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
package com.qlangtech.tis.manage.common.apps;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;

/**
 * @author 百岁（baisui@qlangtech.com）
 *  @date 2014年7月26日下午7:15:16
 */
public interface IAppsFetcher {

    public abstract boolean hasGrantAuthority(String permissionCode);

    /**
     * 取得当前用户所在部门的应用
     *
     * @param setter
     * @return
     */
    public abstract List<Application> getApps(CriteriaSetter setter);

    /**
     * 统计符合条件的应用数目
     *
     * @param setter
     * @return
     */
    public abstract int count(CriteriaSetter setter);

    /**
     * 更新应用
     *
     * @param app
     * @param setter
     * @return
     */
    public abstract int update(Application app, CriteriaSetter setter);

    public abstract List<Department> getDepartmentBelongs(RunContext runcontext);

    /**
     * 显示所有的定时任务
     *
     * @param usrDptRelationDAO
     * @return
     */
    public abstract List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO);

    public abstract List<ApplicationApply> getAppApplyList(IApplicationApplyDAO applicationApplyDAO);
}
