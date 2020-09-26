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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-11
 */
public class AppViewAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private Pager pager;

    @Func(value = PermissionConstant.PERMISSION_INDEX_QUERY)
    public void doQueryApp(Context context) throws Exception {
        final String appNameFuzzy = StringUtils.trimToEmpty(this.getString("query"));
        final IAppsFetcher fetcher = UserUtils.getAppsFetcher(this.getRequest(), this);
        // final List<Application> appresult = (!StringUtils.startsWith(appNameFuzzy, "search4")
        // ? ChangeDomainAction.emptyAppList : fetcher.getApps(new CriteriaSetter() {
        // @Override
        // public void set(Criteria criteria) {
        // criteria.andProjectNameLike(appNameFuzzy + "%");
        // }
        // }));
        final List<Application> appresult = fetcher.getApps((criteria) -> {
            criteria.andProjectNameLike(StringUtils.startsWith(appNameFuzzy, "search4") ? (appNameFuzzy + "%") : ("%" + appNameFuzzy + "%"));
        });
        this.setBizResult(context, appresult);
    }

    // /**
    // * 在进入应用之前先更新一下lastupdate，这样在applist页面可以排序
    // *
    // * @param context
    // * @throws Exception
    // */
    // public void doGoApp(Context context) throws Exception {
    // 
    // }
    /**
     * 取得索引实例列表
     *
     * @param context
     * @throws Exception
     */
    public void doGetApps(Context context) throws Exception {
        Integer dptid = this.getInt("dptid", null);
        // (String) context.get();
        String appName = this.getString("name");
        Integer dptId = (Integer) context.get("dptId");
        String recept = (String) context.get("recept");
        // 应用集合
        ApplicationCriteria query = new ApplicationCriteria();
        Criteria criteria = query.createCriteria();
        if (dptid != null) {
            context.put("bizdomain", this.getDepartmentDAO().loadFromWriteDB(dptid));
        // criteria.andDptIdEqualTo(dptid);
        }
        if (appName != null && !appName.equals("search4")) {
            criteria.andProjectNameLike("%" + appName + "%");
        }
        if (dptId != null) {
            criteria.andDptIdEqualTo(dptId);
        }
        if (recept != null && !StringUtils.isEmpty(recept)) {
            criteria.andReceptEqualTo(recept);
        }
        query.setOrderByClause("last_process_time desc,app_id desc");
        getPager().setTotalCount(this.getApplicationDAO().countByExample(query));
        if (getPager().getTotalCount() == 0) {
            this.addErrorMessage(context, "很抱歉，未能找到结果");
            return;
        }
        context.put("recept", recept);
        context.put("dptId", dptId);
        Pager pager = getPager();
        List<Application> apps = this.getApplicationDAO().selectByExample(query, pager.getCurPage(), pager.getRowsPerPage());
        apps.forEach((app) -> {
            WorkFlow df = null;
            if (app.getWorkFlowId() != null && (df = getWorkflowDAOFacade().getWorkFlowDAO().selectByPrimaryKey(app.getWorkFlowId())) != null) {
                app.setDataflowName(df.getName());
            }
        });
        this.setBizResult(context, new PaginationResult(pager, apps));
    }

    public Pager getPager() {
        if (pager == null) {
            pager = this.createPager();
        }
        return pager;
    }
}
