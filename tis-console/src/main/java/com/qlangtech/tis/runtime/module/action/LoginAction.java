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

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsRequestWrapper;
// import com.alibaba.buc.sso.client.util.BucSSOClientUtil;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.Secret;
import com.qlangtech.tis.manage.common.TerminatorHttpServletRequestWrapper;
import com.qlangtech.tis.manage.common.UserUtils;

/*
 * 登录相应
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LoginAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    public static final String TERMINATOR_INDEX_PAGE_PATH = "/runtime";

    private static final String USER_TOKEN_cryptKey = "%*)&^*(";

    public void doLogin(Context context) throws Exception {
        // Map<String, String> userToken = Config.getUserToken();
        final String username = this.getString("username");
        if (StringUtils.isEmpty(username)) {
            this.addErrorMessage(context, "请填写用户名");
            return;
        }
        final String password = this.getString("password");
        if (StringUtils.isEmpty(password)) {
            this.addErrorMessage(context, "请填写密码");
            return;
        }
        UsrDptRelationCriteria usrQuery = new UsrDptRelationCriteria();
        usrQuery.createCriteria().andUserNameEqualTo(username).andPasswordEqualTo(ManageUtils.md5(password));
        if (this.getUsrDptRelationDAO().countByExample(usrQuery) < 1) {
            this.addErrorMessage(context, "非法账户");
            return;
        }
        final String host = this.getRequest().getHeader("Host");
        ChangeDomainAction.addCookie(getResponse(), UserUtils.USER_TOKEN, Secret.encrypt(username, USER_TOKEN_cryptKey), StringUtils.substringBefore(host, ":"), 60 * 60 * 24 * 365);
        this.getRundata().redirectTo(this.getRequest().getContextPath() + TERMINATOR_INDEX_PAGE_PATH);
    }

    public static String getDcodeUserName(String encryptUserName) {
        try {
            if (StringUtils.isBlank(encryptUserName)) {
                throw new IllegalArgumentException("param encryptUserName can not be null");
            }
            return Secret.decrypt(encryptUserName, USER_TOKEN_cryptKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退出登录
     *
     * @param nav
     * @param context
     * @throws Exception
     */
    public void doLogout(Context context) throws Exception {
        // this.getRequest().getSession(true);
        ServletActionContext.getRequest().getSession().removeAttribute(UserUtils.USER_TOKEN_SESSION);
        final String host = this.getRequest().getHeader("Host");
        ChangeDomainAction.addCookie(getResponse(), UserUtils.USER_TOKEN, "", StringUtils.substringBefore(host, ":"), 0);
        final TerminatorHttpServletRequestWrapper request = (TerminatorHttpServletRequestWrapper) (((StrutsRequestWrapper) this.getRequest()).getRequest());
        request.removeCookie(UserUtils.USER_TOKEN);
        // https://login.alibaba-inc.com/ssoLogout.htm?APP_NAME=taobaoterminator&BACK_URL=http%3A%2F%2Fdaily.terminator.admin.taobao.org%2Fruntime%2F&CONTEXT_PATH=%2F
        // BucSSOClientUtil.handleSSOLogout(this.getRequest(), getResponse());
        // getRundataInstance().redirectTo(Config.getSSOLogoutURL() +
        // "?APP_NAME=taobaoterminator&BACK_URL=http%3A%2F%2F"
        // + (ManageUtils.isDevelopMode() ? "daily." : StringUtils.EMPTY) +
        // "terminator.admin.taobao.org%3A"
        // + (ManageUtils.isDevelopMode() ? "8080" : "9999") +
        // "%2Fruntime%2F&CONTEXT_PATH=%2F");
        getRundataInstance().redirectTo("/runtime/login.htm");
    // nav.redirectToLocation();
    }
}
