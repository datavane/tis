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

// import com.alibaba.buc.sso.client.util.SimpleUserUtil;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.common.ManageUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class QueryAuthorityFunc extends RoleUpdate {

    private static final long serialVersionUID = 1L;

    private UsrDptRelation usrDptRelation;

    @Override
    protected Integer getRoleId() {
        return this.getUserprofile().getrId();
    }

    public boolean isHasRole() throws Exception {
        if (ManageUtils.isDevelopMode()) {
            return true;
        }
        return true;
    // SimpleSSOUser ssoUser;
    // ssoUser = SimpleUserUtil.findUser(ServletActionContext.getRequest());
    // String userId = String.valueOf(ssoUser.getId());
    // UserRoleCondition userRoleCondition = new UserRoleCondition();
    // userRoleCondition.setUserId(userId);// 接入SSO登录后可获得,
    // // simpleSSOUser.getId()
    // List<String> appNames = new ArrayList<String>();
    // appNames.add("terminatorconsole");// 在ACL后台配置的应用英文名
    // userRoleCondition.setAppNames(appNames);//
    // // 注意：如果appNames为null或空，则返回该用户在所有应用下的角色
    // List<Role> results = AclServiceProvider.getUserPermissionService()
    // .findRoleByUser(userRoleCondition);
    // return results.size() > 0;
    }

    @Override
    public void execute(Context context) throws Exception {
        if (this.getUserprofile() == null) {
            // 用户还没有设置部门
            this.forward("notsetdepartment.vm");
            return;
        }
    // context.put("userprofile", usrDptRelation);
    // this.getRole(context, usrDptRelation.getrId());
    // BizFuncAuthorityCriteria criteria = new BizFuncAuthorityCriteria();
    // criteria.createCriteria().andDptIdEqualTo(r.getDptId());
    // criteria.setOrderByClause("func_id desc");
    // 
    // List<BizFuncAuthority> authlist = this.getBizFuncAuthorityDAO()
    // .selectByExample(criteria);
    // OrganizationDO org = new OrganizationDO();
    // org.setId(r.getDptId());
    // org.setName(r.getDptName());
    // context.put("authlist", authlist);
    // context.put("org", org);
    }

    public UsrDptRelation getUserprofile() {
        if (usrDptRelation == null) {
            usrDptRelation = getUserDepartment(this);
        }
        return usrDptRelation;
    }
}
