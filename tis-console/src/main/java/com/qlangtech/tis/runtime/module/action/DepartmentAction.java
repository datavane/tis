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

import java.util.Date;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;

/*
 * 部门管理ACTION
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DepartmentAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * 添加部门
     *
     * @param context
     */
    public void doAddDepartment(Context context) {
        Integer parentid = this.getInt("parentid");
        String dptName = this.getString("dptName");
        if (StringUtils.isEmpty(dptName)) {
            this.addErrorMessage(context, "部门名称不能为空");
            return;
        }
        Department dpt = new Department();
        dpt.setGmtCreate(new Date());
        dpt.setGmtModified(new Date());
        dpt.setName(dptName);
        dpt.setParentId(parentid);
        this.getDepartmentDAO().insertSelective(dpt);
        this.addActionMessage(context, "成功添加部门：" + dptName);
    }

    /**
     * 删除部门
     *
     * @param context
     */
    public void doDeleteDepartment(Context context) {
        final Integer dptid = this.getInt("dptid");
        Assert.assertNotNull(dptid);
        UsrDptRelationCriteria rcriteria = null;
        DepartmentCriteria query = null;
        // 校验是否有子部门
        query = new DepartmentCriteria();
        query.createCriteria().andParentIdEqualTo(dptid);
        if (this.getDepartmentDAO().countByExample(query) > 0) {
            this.addErrorMessage(context, "该部门有子部门，不能删除");
            return;
        }
        // 校验是否有成员关联在该部门上
        rcriteria = new UsrDptRelationCriteria();
        rcriteria.createCriteria().andDptIdEqualTo(dptid);
        if (this.getUsrDptRelationDAO().countByExample(rcriteria) > 0) {
            this.addErrorMessage(context, "有成员关联在该部门，不能删除");
            return;
        }
        // 检验是否有应用绑定在部门上
        IAppsFetcher fetcher = getAppsFetcher();
        int appsCount = fetcher.count(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                criteria.andDptIdEqualTo(dptid);
            }
        });
        if (appsCount > 0) {
            this.addErrorMessage(context, "该部门下有" + appsCount + "个应用，不能删除");
            return;
        }
        this.getDepartmentDAO().deleteByPrimaryKey(dptid);
        this.addActionMessage(context, "已经成功删除部门:" + OrgAuthorityAction.getDepartmentName(this.getDepartmentDAO(), dptid));
    }

    // /**
    // * 解除会员和部门的关系
    // *
    // * @param context
    // */
    // public void doUnbindUser(Context context) {
    // 
    // Integer duid = this.getInt("duid");
    // // Integer dptid = this.getInt("dptid");
    // 
    // // Assert.assertNotNull(userid);
    // Assert.assertNotNull(duid);
    // 
    // UsrDptRelation record = new UsrDptRelation();
    // record.setIsDeleted("Y");
    // UsrDptRelationCriteria query = new UsrDptRelationCriteria();
    // query.createCriteria().andUdIdEqualTo(duid);
    // 
    // if (this.getUsrDptRelationDAO().updateByExampleSelective(record, query) >
    // 0) {
    // this.addActionMessage(context, "已经成功解除绑定");
    // return;
    // }
    // 
    // this.addErrorMessage(context, "删除过程有错误");
    // }
    /**
     * 绑定会员
     *
     * @param context
     */
    public void doBindUser(Context context) {
    // Integer dptid = this.getInt("dptid");
    // Assert.assertNotNull(dptid);
    // 
    // Integer userid = this.getInt("userid");
    // 
    // if (userid == null) {
    // this.addErrorMessage(context, "请填写成员id");
    // return;
    // }
    // 
    // final AdminUserDO user = this.getAuthService().getUserById(userid);
    // 
    // if (user == null) {
    // this.addErrorMessage(context, "userid:" + userid + " 没有对应的用户实体存在");
    // return;
    // }
    // UsrDptRelationCriteria criteria = new UsrDptRelationCriteria();
    // criteria.createCriteria().andUsrIdEqualTo(userid);
    // 
    // if (this.getUsrDptRelationDAO().countByExample(criteria) > 0) {
    // 
    // this.addErrorMessage(context, "用户" + user.getName() + "("
    // + user.getId() + ") 已经绑定");
    // return;
    // }
    // 
    // // 绑定会员
    // OrgAuthorityAction.bindUser2Dpt(this, dptid, new SysUser() {
    // private static final long serialVersionUID = 1L;
    // @Override
    // public int getId() {
    // return user.getId();
    // }
    // @Override
    // public String getName() {
    // return user.getName();
    // }
    // 
    // });
    // 
    // this.addActionMessage(context, "用户"
    // + user.getName()
    // + "("
    // + user.getId()
    // + ") 已经绑定到，"
    // + OrgAuthorityAction.getDepartmentName(this.getDepartmentDAO(),
    // dptid));
    }
}
