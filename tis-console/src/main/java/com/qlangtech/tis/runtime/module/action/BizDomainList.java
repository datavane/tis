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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BizDomainList extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;
    // @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
    // public void doDeleteBizDomain(Context context) {
    // 
    // final Integer dptid = this.getInt("dptid");
    // 
    // // BizDomain domain = new BizDomain();
    // // domain.setDeleteFlag((int) ManageUtils.DELETE);
    // //
    // // BizDomainCriteria criteria = new BizDomainCriteria();
    // // criteria.createCriteria().andBizIdEqualTo(bizId);
    // // this.getBizDomainDAO().updateByExampleSelective(domain, criteria);
    // final AppsFetcher fetcher = AppsFetcher
    // .create(this.getApplicationDAO());
    // 
    // int appcount = fetcher.count(new CriteriaSetter() {
    // @Override
    // public void set(Criteria criteria) {
    // criteria.andDptIdEqualTo(dptid);
    // }
    // });
    // if (appcount > 0) {
    // this.addErrorMessage(context, "该业务下有" + appcount + "个应用与之关联，不能删除");
    // return;
    // }
    // // this.getBizDomainDAO().deleteByPrimaryKey(bizId);
    // DepartmentCriteria dptCriteria = new DepartmentCriteria();
    // dptCriteria.createCriteria().andParentIdEqualTo(dptid);
    // int childCount = this.getDepartmentDAO().countByExample(dptCriteria);
    // if (childCount > 0) {
    // this.addErrorMessage(context, "该部门下有" + childCount + "个子部门，不能删除");
    // return;
    // }
    // 
    // this.getDepartmentDAO().deleteByPrimaryKey(dptid);
    // 
    // this.addActionMessage(context, "业务线被成功删除");
    // 
    // }
    // /**
    // * 取消删除状态
    // *
    // * @param context
    // */
    // @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
    // public void doRecovery(Context context) {
    // 
    // Integer bizId = this.getInt("bizid");
    // 
    // BizDomain domain = new BizDomain();
    // domain.setDeleteFlag((int) ManageUtils.UN_DELETE);
    // 
    // BizDomainCriteria criteria = new BizDomainCriteria();
    // criteria.createCriteria().andBizIdEqualTo(bizId);
    // this.getBizDomainDAO().updateByExampleSelective(domain, criteria);
    // 
    // }
    // @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
    // public void doUpdate(Context context) {
    // Integer bizId = this.getInt("bizid");
    // Assert.assertNotNull(bizId);
    // 
    // String bizline = this.getString("bizline");
    // String bizName = this.getString("bizname");
    // 
    // if (StringUtils.isBlank(bizline)) {
    // this.addErrorMessage(context, "请填写业务线名称");
    // return;
    // }
    // 
    // if (StringUtils.isBlank(bizName)) {
    // this.addErrorMessage(context, "请填写业务名称");
    // return;
    // }
    // 
    // BizDomainCriteria criteria = new BizDomainCriteria();
    // criteria.createCriteria().andBizIdEqualTo(bizId);
    // 
    // BizDomain domain = new BizDomain();
    // domain.setBizLine(bizline);
    // domain.setName(bizName);
    // 
    // this.getBizDomainDAO().updateByExampleSelective(domain, criteria);
    // this.addActionMessage(context, "业务名称更新成功");
    // }
}
