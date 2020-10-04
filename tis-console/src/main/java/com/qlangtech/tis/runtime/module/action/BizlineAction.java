/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 业务线控制類
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月8日
 */
public class BizlineAction extends BasicModule {

  private static final long serialVersionUID = 1L;

  @Func(value = PermissionConstant.APP_DEPARTMENT_LIST, sideEffect = false)
  public void doBizData(Context context) {
    this.setBizResult(context, getAllBizDomain(true));
  }

  @Func(value = PermissionConstant.APP_DEPARTMENT_LIST, sideEffect = false)
  public void doGetBizline(Context context) {
    this.setBizResult(context, getAllBizDomain(false));
  }

  @Func(value = PermissionConstant.APP_DEPARTMENT_MANAGE)
  public void doAddBizline(Context context) {
    this.errorsPageShow(context);
    final String name = this.getString("name");
    if (StringUtils.isBlank(name)) {
      this.addErrorMessage(context, "请填写业务线名称");
      return;
    }
    DepartmentCriteria criteria = new DepartmentCriteria();
    criteria.createCriteria().andIsLeaf(false).andNameEqualTo(name).andParentIdEqualTo(-1);

    if (this.getDepartmentDAO().countByExample(criteria) > 0) {
      this.addErrorMessage(context, "业务线:''" + name + "' 已经存在，不能重复添加");
      return;
    }
    Department dpt = new Department();
    dpt.setLeaf(false);
    dpt.setName(name);
    dpt.setFullName(name);
    dpt.setGmtModified(new Date());
    dpt.setGmtCreate(new Date());
    dpt.setParentId(-1);
    dpt.setDptId(this.getDepartmentDAO().insertSelective(dpt));
    this.setBizResult(context, dpt);
    this.addActionMessage(context, "业务线:'" + name + "' 添加成功");
    //this.setBizResult(context, getAllBizDomain(false));
  }

  /**
   * 取得所有的业务线实体
   *
   * @return
   */
  protected final List<Department> getAllBizDomain(boolean leaf) {
    DepartmentCriteria q = new DepartmentCriteria();
    q.createCriteria().andIsLeaf(leaf);
    q.setOrderByClause("dpt_id desc");
    List<Department> dpts = this.getDepartmentDAO().selectByExample(q, 1, 200);
    return dpts;
  }
}
