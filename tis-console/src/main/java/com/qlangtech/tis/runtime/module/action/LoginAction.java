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
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsRequestWrapper;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.Secret;
import com.qlangtech.tis.manage.common.TerminatorHttpServletRequestWrapper;
import com.qlangtech.tis.manage.common.UserUtils;

/**
 * 登录相应
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-20
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
        if (StringUtils.isBlank(encryptUserName)) {
            throw new IllegalArgumentException("encryptUserName can not be null");
        }
        try {
            return Secret.decrypt(encryptUserName, USER_TOKEN_cryptKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 退出登录
     *
     * @param
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
        getRundataInstance().redirectTo("/runtime/login.htm");
    }
}
