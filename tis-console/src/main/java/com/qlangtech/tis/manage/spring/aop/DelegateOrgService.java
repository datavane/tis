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
import org.springframework.beans.factory.InitializingBean;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DelegateOrgService {
    // private OrgService target = null;
    // 
    // private String version;
    // 
    // @SuppressWarnings("unchecked")
    // public List getActivityOrgListByUserId(Integer userId) {
    // return target.getActivityOrgListByUserId(userId);
    // }
    // 
    // public List<OrganizationDO> getAllActiveOrganizationList() {
    // return target.getAllActiveOrganizationList();
    // }
    // 
    // public List<OrganizationDO> getAllUndeleteOrganizationList() {
    // return target.getAllUndeleteOrganizationList();
    // }
    // 
    // public String getComapnyUriByUserId(int userid) {
    // return target.getComapnyUriByUserId(userid);
    // }
    // 
    // public CompanyDO getCompanyInfoByCompanyId(Integer companyId) {
    // return target.getCompanyInfoByCompanyId(companyId);
    // }
    // 
    // public OrganizationDO getDepartDetailInfoByUserId(Integer userId) {
    // return target.getDepartDetailInfoByUserId(userId);
    // }
    // 
    // public OrganizationDO getOrganizationById(Integer organizationId) {
    // return target.getOrganizationById(organizationId);
    // }
    // 
    // public List<OrganizationDO> getPostListByUserId(Integer userId) {
    // return target.getPostListByUserId(userId);
    // }
    // 
    // public List<AdminUserDO> getUsersByOrgId(Integer orgId) {
    // return target.getUsersByOrgId(orgId);
    // }
    // 
    // public boolean isOrgUser(Integer userId, Integer orgId) {
    // return target.isOrgUser(userId, orgId);
    // }
    // 
    // public boolean isOwnnerOrg(Integer childid, Integer parentid) {
    // return target.isOwnnerOrg(childid, parentid);
    // }
    // 
    // @Override
    // public void afterPropertiesSet() throws Exception {
    // 
    // HSFSpringConsumerBean hsfConsumeBean = new HSFSpringConsumerBean();
    // hsfConsumeBean.setInterfaceName(OrgService.class.getName());
    // hsfConsumeBean.setVersion(this.getVersion());
    // 
    // hsfConsumeBean.afterPropertiesSet();
    // 
    // target = (OrgService) hsfConsumeBean.getObject();
    // 
    // }
    // 
    // public String getVersion() {
    // return version;
    // }
    // 
    // public void setVersion(String version) {
    // this.version = version;
    // }
}
