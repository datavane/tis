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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.ChangeDomainForm;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.spring.aop.OperationIgnore;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.GroupAction.SuggestCallback;
import com.qlangtech.tis.runtime.module.control.AppDomain;

import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ChangeDomainAction extends BasicModule implements ModelDriven<ChangeDomainForm> {

    private static final long serialVersionUID = 1L;

    public static final String GROUPNAME = "changedomain";

    public static final String SELECT_APP_NAME = "selectAppDomain";

    public static final String COOKIE_SELECT_APP = SELECT_APP_NAME + RunEnvironment.getSysEnvironment();

    @Override
    public boolean isAppNameAware() {
        return false;
    }

    /**
     * @param groupName
     */
    public ChangeDomainAction() {
        super(GROUPNAME);
    }

    private final ChangeDomainForm form = new ChangeDomainForm();

    @Override
    public ChangeDomainForm getModel() {
        return this.form;
    }

    /**
     * 切换当前应用
     *
     * @param form
     * @param nav
     * @throws Exception
     */
    @OperationIgnore
    public // @FormGroup(GROUPNAME) ChangeDomainForm form,
    void doChange(// Navigator nav,
    Context context) throws Exception {
        Integer appid = parseSelectedAppid(form, context);
        if (appid == null) {
            return;
        }
        final Application app = this.getApplicationDAO().selectByPrimaryKey(appid);
        setAppDomainCookie(form, app);
        return2OriginUrl(form);
    }

    /**
     * 选择运行环境
     *
     * @param context
     * @throws Exception
     */
    @OperationIgnore
    public void doChangeRuntimeAjax(Context context) throws Exception {
        // Integer appid = parseSelectedAppid(form, context);
        // if (appid == null) {
        // return;
        // }
        setAppdomainCookie(getResponse(), this.getRequest(), RunEnvironment.getEnum(this.getString("runtime")));
    // return2OriginUrl(form);
    }

    /**
     * 利用AJAX切换选择的应用
     *
     * @param context
     * @throws Exception
     */
    @OperationIgnore
    public void doChangeAppAjax(Context context) throws Exception {
        Integer appid = this.getInt("appid");
        if (appid == null) {
            appid = this.getInt("selappid");
        }
        if (appid == null) {
            this.addErrorMessage(context, "请选择应用");
            return;
        }
//        if (this.getAppDomain() instanceof Nullable) {
//            this.addErrorMessage(context, "请先选择应用环境，日常？  预发？ 线上？");
//            return;
//        }
        Application app = this.getApplicationDAO().loadFromWriteDB(appid);
        setAppdomainCookie(getResponse(), this.getRequest(), this.getAppDomain().getRunEnvironment(), app);
        this.addActionMessage(context, "已经将当前应用切换成:" + AppDomain.getAppDescribe(app));
    }

    private Integer parseSelectedAppid(ChangeDomainForm form, Context context) {
        Integer appid = this.getInt("hiddenAppnamesuggest");
        if (appid != null) {
            return appid;
        }
        if (form.getBizid() == null) {
            this.addErrorMessage(context, "请选择业务线");
            return appid;
        }
        appid = form.getAppid();
        if (appid == null) {
            this.addErrorMessage(context, "请选择应用");
            return appid;
        }
        return appid;
    }

    static final List<Application> empty = Collections.emptyList();

    /**
     * 通过远程json方式，获得daily的suggest
     *
     * @param context
     * @throws Exception
     */
    @OperationIgnore
    public void doAppNameSuggestDaily(Context context) throws Exception {
    }

    /**
     * changedomain 页面上添加了一个
     *
     * @param context
     */
    @OperationIgnore
    public void doAppNameSuggest(Context context) throws Exception {
        final String appNameFuzzy = StringUtils.trimToEmpty(this.getString("query"));
        processNameSuggest(this.getRequest(), this.getUser(), this, false, appNameFuzzy);
    }

    /**
     * @param appNameFuzzy
     * @throws JSONException
     * @throws IOException
     *             this.getRequest(), false, this.getUser(), this
     */
    public static void processNameSuggest(HttpServletRequest request, IUser user, RunContext context, boolean ismaxmatch, final String appNameFuzzy) throws JSONException, IOException {
        final List<Application> result = (!StringUtils.startsWith(appNameFuzzy, "search4") ? ChangeDomainAction.empty : getMatchApps(request, ismaxmatch, appNameFuzzy, user, context));
        GroupAction.writeSuggest2Response(appNameFuzzy, result, new SuggestCallback<Application>() {

            @Override
            public String getLiteral(Application o) {
                return o.getProjectName();
            }

            @Override
            public Object getValue(Application o) {
                return o.getAppId();
            }
        }, getResponse());
    }

    // this.getRequest(), false, this.getUser(), this
    protected static List<Application> getMatchApps(HttpServletRequest request, boolean isMaxMatch, final String appNameFuzzy, IUser user, RunContext context) {
        IAppsFetcher fetcher = null;
        fetcher = getAppsFetcher(request, isMaxMatch, user, context);
        return fetcher.getApps(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                criteria.andProjectNameLike(appNameFuzzy + "%");
            }
        });
    }

    private void setAppDomainCookie(ChangeDomainForm form, final Application app) {
        setAppdomainCookie(getResponse(), this.getRequest(), RunEnvironment.getEnum(form.getRunEnviron().shortValue()), app);
    }

    private static void setAppdomainCookie(HttpServletResponse response, HttpServletRequest request, RunEnvironment runtime, final Application app) {
        final String host = request.getHeader("Host");
        addCookie(response, COOKIE_SELECT_APP, (app != null ? app.getProjectName() : com.qlangtech.tis.manage.common.ManageUtils.getAppDomain(request).getAppName()) + "_run" + (runtime == null ? StringUtils.EMPTY : runtime.getId()), StringUtils.substringBefore(host, ":"));
    }

    private static void setAppdomainCookie(HttpServletResponse response, HttpServletRequest request, RunEnvironment runtime) {
        setAppdomainCookie(response, request, runtime, null);
    }

    private void return2OriginUrl(ChangeDomainForm form) {
        Assert.assertNotNull("parameter form can not be null", form);
        // RunEnvironment change2 = RunEnvironment.getEnum(form.getRunEnviron()
        // .shortValue());
        final String referer = form.getGobackurl();
        if (StringUtils.isNotEmpty(referer)) {
        // TurbineUtil.getTurbineRunData(this.getRequest())
        // .setRedirectLocation(referer);
        }
    // if (StringUtils.isNotEmpty(referer)) {
    // 
    // Matcher matcher = url_pattern.matcher(referer);
    // if (matcher.matches()) {
    // TurbineUtil.getTurbineRunData(this.getRequest())
    // .setRedirectLocation(
    // matcher.replaceAll("$1" + hostMap.get(change2)
    // + "$3"));
    // } else {
    // throw new IllegalStateException("url referer is not illegal:"
    // + referer);
    // }
    // 
    // }
    }
    @OperationIgnore
    public void doRuntimeChange(// Navigator nav
    Context context) throws Exception {
        // AppDomainInfo appdomain = this.getAppDomain();
        setAppDomainCookie(form, null);
        // addCookie(this.getResponse(), COOKIE_SELECT_APP,
        // appdomain.getAppName()
        // + "_run" + form.getRunEnviron());
        return2OriginUrl(form);
    // final String referer = form.getGobackurl();
    // if (StringUtils.isNotEmpty(referer)) {
    // TurbineUtil.getTurbineRunData(this.getRequest())
    // .setRedirectLocation(referer);
    // }
    }

    public static void addCookie(HttpServletResponse response, String name, String value, String domain) {
        addCookie(response, name, value, domain, 60 * 60 * 24 * 3);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, String domain, int maxAge) {
        Assert.assertNotNull(domain);
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setDomain(domain);
        cookie.setVersion(0);
        response.addCookie(cookie);
    }

    /**
     * 点击业务线select
     *
     * @param from
     * @param context
     */
    @OperationIgnore
    public void doSelectChange(Context context) {
        this.setErrorMsgInvisiable(context);
        Integer bizid = this.getInt("bizid");
        if (bizid == null) {
            return;
        }
        // this.setBizObjResult(context, getAppList(bizid));
        this.setBizResult(context, getAppList(bizid));
    // try {
    // JsonUtil.copy2writer(, getResponse().getWriter());
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // context.put("applist", );
    }
}
