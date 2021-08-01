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
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-11
 */
public class AppViewAction extends BasicModule {

  private static final long serialVersionUID = 1L;

  private Pager pager;

  @Func(value = PermissionConstant.PERMISSION_INDEX_QUERY, sideEffect = false)
  public void doQueryApp(Context context) throws Exception {
    final String appNameFuzzy = StringUtils.trimToEmpty(this.getString("query"));
    final IAppsFetcher fetcher = UserUtils.getAppsFetcher(this.getRequest(), this);
    final List<Application> appresult = fetcher.getApps((criteria) -> {
      criteria.andProjectNameLike(StringUtils.startsWith(appNameFuzzy, "search4") ? (appNameFuzzy + "%") : ("%" + appNameFuzzy + "%"));
    });
    this.setBizResult(context, appresult);
  }

  /**
   * 取得索引实例列表
   *
   * @param context
   * @throws Exception
   */
  public void doGetApps(Context context) throws Exception {
    Integer dptid = this.getInt("dptid", null);
    String appName = this.getString("name");
    Integer dptId = (Integer) context.get("dptId");
    String recept = (String) context.get("recept");
    // 应用集合
    ApplicationCriteria query = new ApplicationCriteria();

    Criteria criteria = query.createCriteria();
    criteria.andAppIdNotEqualTo(SysInitializeAction.TEMPLATE_APPLICATION_DEFAULT_ID);
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
    int allRows = this.getApplicationDAO().countByExample(query);
    Pager pager = getPager();
    List<Application> apps = Collections.emptyList();
    pager.setTotalCount(allRows);
    if (allRows < 1) {
      //this.addErrorMessage(context, "很抱歉，未能找到结果");
      this.setBizResult(context, new PaginationResult(pager, apps));
      return;
    }
    context.put("recept", recept);
    context.put("dptId", dptId);

    apps = this.getApplicationDAO().selectByExample(query, pager.getCurPage(), pager.getRowsPerPage());
    apps.forEach((app) -> {
//      WorkFlow df = null;
//      if (app.getWorkFlowId() != null && (df = getWorkflowDAOFacade().getWorkFlowDAO().selectByPrimaryKey(app.getWorkFlowId())) != null) {
//        app.setDataflowName(df.getName());
//      }
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
