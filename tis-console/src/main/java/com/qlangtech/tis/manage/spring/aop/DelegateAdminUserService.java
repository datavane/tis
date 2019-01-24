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
package com.qlangtech.tis.manage.spring.aop;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthorityCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.pubhook.common.Nullable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DelegateAdminUserService {
    // private AdminUserService adminUserService;
    // 
    // // private OrgService orgService;
    // 
    // private IUsrDptRelationDAO usrDptRelationDAO;
    // 
    // private HttpServletRequest request;
    // 
    // private IBizFuncAuthorityDAO bizFuncAuthorityDAO;
    // 
    // private String version;
    // 
    // public String getVersion() {
    // return version;
    // }
    // 
    // public void setVersion(String version) {
    // this.version = version;
    // }
    // 
    // public DelegateAdminUserService() {
    // super();
    // // this.adminUserService = adminUserService;
    // }
    // 
    // public HttpServletRequest getRequest() {
    // return request;
    // }
    // 
    // @Autowired
    // public void setRequest(HttpServletRequest request) {
    // this.request = request;
    // }
    // 
    // @Override
    // public void afterPropertiesSet() throws Exception {
    // HSFSpringConsumerBean hsfBean = new HSFSpringConsumerBean();
    // hsfBean.setVersion(this.getVersion());
    // hsfBean.setInterfaceName(AdminUserService.class.getName());
    // 
    // hsfBean.afterPropertiesSet();
    // 
    // adminUserService = (AdminUserService) hsfBean.getObject();
    // 
    // if (adminUserService == null) {
    // throw new IllegalStateException("adminUserService is null");
    // }
    // }
    // 
    // public boolean hasPermission(int userId, int permissionId) {
    // // return adminUserService.hasPermission(userId, permissionId);
    // throw new UnsupportedOperationException();
    // }
    // 
    // public AdminUserDO getAgentUser(int userId) {
    // return adminUserService.getAgentUser(userId);
    // }
    // 
    // public List<RoleDO> getAllRolesByUserid(int userid) {
    // return adminUserService.getAllRolesByUserid(userid);
    // }
    // 
    // public List<AdminUserDO> getAllUsersByRoleIds(List<Integer> roleIds) {
    // return adminUserService.getAllUsersByRoleIds(roleIds);
    // }
    // 
    // public List<AdminUserDO> getFinalCheckUser(int roleId) {
    // return adminUserService.getFinalCheckUser(roleId);
    // }
    // 
    // public AdminUserDO getParentUser(Integer id) {
    // return adminUserService.getParentUser(id);
    // }
    // 
    // public AdminUserDO getUserByAgentId(Integer id) {
    // return adminUserService.getUserByAgentId(id);
    // }
    // 
    // public AdminUserDO getUserById(Integer id) {
    // return adminUserService.getUserById(id);
    // }
    // 
    // public AdminUserDO getUserByName(String userName) {
    // return adminUserService.getUserByName(userName);
    // }
    // 
    // public AdminUserDO getUserByNick(String nick) {
    // return adminUserService.getUserByNick(nick);
    // }
    // 
    // public AdminUserDO getUserByRealName(String realName) {
    // return adminUserService.getUserByRealName(realName);
    // }
    // 
    // public List<AdminUserDO> getUserListByParentId(int userid) {
    // return adminUserService.getUserListByParentId(userid);
    // }
    // 
    // public List<AdminUserDO> getUserListByPermissionName(String permissionName) {
    // return adminUserService.getUserListByPermissionName(permissionName);
    // }
    // 
    // public boolean hasPermission(int userId, String permissionName, int type) {
    // return adminUserService.hasPermission(userId, permissionName, type);
    // }
    // 
    // // final Map<PermissionKey, Boolean> authorization = new
    // // HashMap<PermissionKey, Boolean>();
    // 
    // /**
    // * 创建菜单的时候用的
    // *
    // * @param userId
    // * @param permissionName
    // * @return
    // */
    // public boolean hasPermissionUseByNaviagionBar(int userId,
    // String permissionName) {
    // return hasPermission(userId, permissionName, CheckAppDomainExistValve
    // .createNull());
    // }
    // 
    // public boolean hasPermission(int userId, String permissionName) {
    // final AppDomainInfo appdomain = CheckAppDomainExistValve
    // .getAppDomain((IApplicationDAO) null);
    // return hasPermission(userId, permissionName, appdomain);
    // }
    // 
    // private boolean hasPermission(int userId, String permissionName,
    // AppDomainInfo appdomain) {
    // if (ManageUtils.isDevelopMode()) {
    // return true;
    // }
    // 
    // final PermissionKey key = new PermissionKey(userId, permissionName,
    // appdomain.getAppid());
    // 
    // Boolean hasPermission = getSessionValue(key);
    // 
    // if (hasPermission != null) {
    // return hasPermission;
    // }
    // 
    // hasPermission = adminUserService.hasPermission(userId, permissionName);
    // if (!hasPermission) {
    // return putPermission(key, false);
    // }
    // // authorization.put(key, hasPermission);
    // 
    // // OrganizationDO org =
    // // this.getOrgService().getOrganizationById(userId);
    // 
    // // 是否选择了应用
    // if (appdomain instanceof Nullable) {
    // return putPermission(key, true);
    // }
    // 
    // // 取得用户的 部门
    // // OrganizationDO org = this.get
    // 
    // UsrDptRelationCriteria query = new UsrDptRelationCriteria();
    // query.createCriteria().andUsrIdEqualTo(userId);
    // UsrDptRelation rel = null;
    // for (UsrDptRelation dptRel : this.usrDptRelationDAO
    // .selectByExample(query)) {
    // rel = dptRel;
    // break;
    // }
    // 
    // // 用户还没有分配部门
    // if (rel == null) {
    // return putPermission(key, false);
    // }
    // // 如果该用户是就是终搜的
    // if (rel.getDptId().equals(Config.getDptTerminatorId())) {
    // return putPermission(key, true);
    // }
    // 
    // BizFuncAuthorityCriteria criteria = new BizFuncAuthorityCriteria();
    // criteria.createCriteria().andAppIdEqualTo(appdomain.getAppid())
    // .andFuncIdEqualTo(permissionName).andDptIdEqualTo(
    // rel.getDptId());
    // 
    // if (bizFuncAuthorityDAO.countByExample(criteria) < 1) {
    // // 部门没有授权
    // return putPermission(key, false);
    // }
    // 
    // return putPermission(key, true);
    // }
    // 
    // private Boolean getSessionValue(PermissionKey key) {
    // return (Boolean) request.getSession().getAttribute(key.toString());
    // }
    // 
    // private boolean putPermission(PermissionKey key, Boolean hasPermission) {
    // request.getSession().setAttribute(key.toString(), hasPermission);
    // return hasPermission;
    // }
    // 
    // private static class PermissionKey {
    // final int userId;
    // final String permissionName;
    // final int appid;
    // 
    // private PermissionKey(int userId, String permissionName, int appid) {
    // super();
    // this.userId = userId;
    // this.permissionName = permissionName;
    // this.appid = appid;
    // }
    // 
    // @Override
    // public int hashCode() {
    // return permissionName.hashCode() + userId + appid;
    // }
    // 
    // @Override
    // public String toString() {
    // return permissionName + this.userId + '_' + appid;
    // }
    // 
    // }
    // 
    // public IUsrDptRelationDAO getUsrDptRelationDAO() {
    // return usrDptRelationDAO;
    // }
    // 
    // public void setUsrDptRelationDAO(IUsrDptRelationDAO usrDptRelationDAO) {
    // this.usrDptRelationDAO = usrDptRelationDAO;
    // }
    // 
    // public List<AdminUserDO> loadAllUsers() {
    // return adminUserService.loadAllUsers();
    // }
    // 
    // // public OrgService getOrgService() {
    // // return orgService;
    // // }
    // 
    // // public void setOrgService(OrgService orgService) {
    // // this.orgService = orgService;
    // // }
    // 
    // public ValidateResult tokenValidation(String domain, Integer userId,
    // String token, String email) {
    // return adminUserService.tokenValidation(domain, userId, token, email);
    // }
    // 
    // public void setBizFuncAuthorityDAO(IBizFuncAuthorityDAO bizFuncAuthorityDAO) {
    // this.bizFuncAuthorityDAO = bizFuncAuthorityDAO;
    // }
}
