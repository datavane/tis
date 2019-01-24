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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
