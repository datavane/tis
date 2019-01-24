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
package com.qlangtech.tis.runtime.module.screen;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Applist extends BasicManageScreen {

    /**
     */
    private static final long serialVersionUID = 1L;

    // 分页获取应用集合
    @Override
    @Func(PermissionConstant.APP_LIST)
    public void execute(Context context) throws Exception {
        // this.disableDomainView(context);
        Integer dptid = this.getInt("dptid");
        String appName = (String) context.get("appName");
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
            criteria.andProjectNameEqualTo(appName);
        }
        if (dptId != null) {
            criteria.andDptIdEqualTo(dptId);
        }
        if (recept != null && !StringUtils.isEmpty(recept)) {
            criteria.andReceptEqualTo(recept);
        }
        query.setOrderByClause("app_id desc");
        getPager().setTotalCount(this.getApplicationDAO().countByExample(query));
        if (getPager().getTotalCount() == 0) {
            this.addErrorMessage(context, "很抱歉，未能找到结果");
            return;
        }
        context.put("recept", recept);
        context.put("dptId", dptId);
        context.put("applist", this.getApplicationDAO().selectByExample(query, getPager().getCurPage(), getPager().getRowsPerPage()));
    // context.put("applist", getApplist(context, dptid, this));
    }

    public static List<Application> getApplist(final Context context, final Integer dptid, final BasicModule basicModule) {
        IAppsFetcher fetcher = basicModule.getAppsFetcher();
        return fetcher.getApps(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                if (dptid != null) {
                    context.put("bizdomain", basicModule.getDepartmentDAO().loadFromWriteDB(dptid));
                    criteria.andDptIdEqualTo(dptid);
                }
            }
        });
    // return basicModule.getApplicationDAO().selectByExample(application);
    }

    // pager
    private Pager pager;

    @SuppressWarnings("all")
    @Override
    protected StringBuffer getPagerUrl() {
        StringBuffer result = new StringBuffer(this.getRequest().getRequestURL());
        Map params = this.getRequest().getParameterMap();
        if (!params.isEmpty()) {
            result.append("?");
        }
        String[] value = null;
        boolean first = true;
        for (Object key : params.keySet()) {
            if (!StringUtils.equalsIgnoreCase(String.valueOf(key), "page")) {
                if (!first) {
                    result.append("&");
                }
                if (params.get(key) instanceof String[]) {
                    value = (String[]) params.get(key);
                } else {
                    value = new String[] { String.valueOf(params.get(key)) };
                }
                result.append(key).append("=").append(value[0]);
                first = false;
            }
        }
        // }
        return result;
    }

    public Pager getPager() {
        if (pager == null) {
            pager = this.createPager();
        }
        return pager;
    }
}
