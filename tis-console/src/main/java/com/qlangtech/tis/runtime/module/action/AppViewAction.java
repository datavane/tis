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
package com.qlangtech.tis.runtime.module.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrApplyDptRecord;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.ANode;
import com.qlangtech.tis.manage.common.BizANode;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.runtime.module.screen.Bizdomainlist;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AppViewAction extends Bizdomainlist {

    /**
     */
    private static final long serialVersionUID = 1L;

    public void doGet(Context context) throws Exception {
        Integer bizId = this.getInt("bizid");
        final List<ANode> anodelist = new ArrayList<ANode>();
        if (bizId == null) {
            List<Department> bizList = this.getAllBizDomain();
            for (Department biz : bizList) {
                anodelist.add(ANode.createBizNode(biz.getDptId(), biz.getName()));
            }
        } else {
            // List<Application> applist = Applist
            // .getApplist(context, bizId, this);
            ApplicationCriteria acriteria = new ApplicationCriteria();
            acriteria.createCriteria().andDptIdEqualTo(bizId);
            List<Application> applist = this.getApplicationDAO().selectByExample(acriteria);
            for (Application app : applist) {
                anodelist.add(ANode.createAppNode(app.getAppId(), (app.getProjectName())));
            }
        }
        context.put("anodelist", anodelist);
    }

    // http://l.admin.taobao.org/runtime/app_view.ajax?resulthandler=anodes&action=app_view_action&event_submit_do_query_app=y&query=search4su
    /**
     * @param context
     * @throws Exception
     */
    public void doQueryApp(Context context) throws Exception {
        final String appNameFuzzy = StringUtils.trimToEmpty(this.getString("query"));
        // ApplicationCriteria criteria = new ApplicationCriteria();
        // criteria.createCriteria().andProjectNameLike(appNameFuzzy + "%");
        final IAppsFetcher fetcher = UserUtils.getAppsFetcher(this.getRequest(), this);
        // .create(this.getApplicationDAO());
        final List<Application> appresult = (!StringUtils.startsWith(appNameFuzzy, "search4") ? ChangeDomainAction.empty : fetcher.getApps(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                criteria.andProjectNameLike(appNameFuzzy + "%");
            }
        }));
        Map<Integer, BizANode> biznodes = new HashMap<Integer, BizANode>();
        for (Application app : appresult) {
            BizANode bizNode = biznodes.get(app.getDptId());
            if (bizNode == null) {
                bizNode = ANode.createExtBizNode(app.getDptId(), app.getDptName());
                biznodes.put(app.getDptId(), bizNode);
            }
            bizNode.addAppNode(app.getAppId(), app.getProjectName());
        }
        context.put("anodelist", new ArrayList<BizANode>(biznodes.values()));
    }

    /**
     * @param context
     * @throws Exception
     *             在应用管理列表中查询应用信息
     */
    public void doQuery(Context context) throws Exception {
        Application app = new Application();
        String appName = this.getString("appnamesuggest");
        Integer dptId = this.getInt("combDptid");
        String recept = this.getString("recept");
        if (appName.equals("search4") && dptId == null && StringUtils.isEmpty(recept)) {
            this.addErrorMessage(context, "请输入查询条件");
            return;
        }
        if (!appName.equals("search4")) {
            app.setProjectName(this.getString("appnamesuggest"));
            ApplicationCriteria criteria = new ApplicationCriteria();
            criteria.createCriteria().andProjectNameEqualTo(app.getProjectName());
            List<Application> appList = this.getApplicationDAO().selectByExample(criteria);
            Assert.assertTrue("app num could not larger than 1", appList.size() <= 1);
            UsrApplyDptRecord record = new UsrApplyDptRecord();
            if (appList.size() == 0) {
                this.addErrorMessage(context, "应用名（“" + app.getProjectName() + "”）不存在，请重新输入");
                return;
            }
        }
        context.put("appName", appName);
        context.put("dptId", dptId);
        context.put("recept", recept);
    }
}
