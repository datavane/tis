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
import com.google.common.collect.Maps;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.manage.spring.aop.Func;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * 用户更新
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-1
 */
public class UserAction extends BasicModule {

  private static final long serialVersionUID = 1L;
  private static Properties tisMetaProps;

  /**
   * @param context
   */
  public void doGetUserInfo(Context context) throws Exception {
    Map<String, Object> sysInfo = Maps.newHashMap();
    sysInfo.put("usr", UserUtils.getUser(this.getRequest(), this.getDaoContext()));
    sysInfo.put("sysInitialized", SysInitializeAction.isSysInitialized());

    if (tisMetaProps == null) {
      try (InputStream reader = UserAction.class.getResourceAsStream("/tis-meta")) {
        tisMetaProps = new Properties();
        tisMetaProps.load(reader);
      }
    }
    sysInfo.put("tisMeta", tisMetaProps);
    this.setBizResult(context, sysInfo);
  }

  /**
   * 取得初始化頁面數據
   *
   * @param context
   */
  public void doGetInitData(Context context) {
    UsrDptRelationCriteria criteria = new UsrDptRelationCriteria();
    criteria.setOrderByClause("create_time desc");
    Pager pager = createPager();
    pager.setTotalCount(this.getUsrDptRelationDAO().countByExample(criteria));
    this.setBizResult(context, new PaginationResult(pager, this.getUsrDptRelationDAO().selectByExample(criteria, pager.getCurPage(), pager.getRowsPerPage())));
  }

  /**
   * get child department
   *
   * @param context
   */
  public void doGetChildDpt(Context context) {
    Integer dptid = this.getInt("pid");
    DepartmentCriteria query = new DepartmentCriteria();
    query.createCriteria().andParentIdEqualTo(dptid);
    List<Option> result = new ArrayList<Option>();
    for (Department dpt : this.getDepartmentDAO().selectByExample(query)) {
      result.add(new Option(dpt.getName(), String.valueOf(dpt.getDptId())));
    }
    context.put(AjaxValve.BIZ_RESULT, result);
  }

  /**
   * 添加用户，默认是给系统用户添加的
   *
   * @param context
   */
  public void doUsrAdd(Context context) {
    String userAccount = this.getString("userAccount");
    String realName = this.getString("realName");
    String password = this.getString("password");
    Integer dptid = this.getInt("dptid");
    if (StringUtils.isEmpty(userAccount)) {
      this.addErrorMessage(context, "请填写登陆账户名");
      return;
    }
    if (StringUtils.isEmpty(realName)) {
      this.addErrorMessage(context, "请填写真实名称");
      return;
    }
    if (StringUtils.isEmpty(password)) {
      this.addErrorMessage(context, "请填写密码");
      return;
    }
    UsrDptRelationCriteria q = new UsrDptRelationCriteria();
    q.createCriteria().andUserNameEqualTo(userAccount);
    if (this.getUsrDptRelationDAO().countByExample(q) > 0) {
      this.addErrorMessage(context, "用户“" + userAccount + "”已经存在");
      return;
    }
    UsrDptRelation user = new UsrDptRelation();
    user.setUsrId(StringUtils.remove(UUID.randomUUID().toString(), "-"));
    user.setUserName(userAccount);
    user.setRealName(realName);
    user.setPassword(ManageUtils.md5(password));
    user.setDptId(dptid);
    user.setCreateTime(new Date());
    user.setUpdateTime(new Date());
    user.setDptId(-1);
    user.setDptName("none");
    user.setrId(-1);
    this.getUsrDptRelationDAO().insertSelective(user);
    this.addActionMessage(context, "用户“" + userAccount + "”添加成功");
  }

  /**
   * 关联其他部门，一旦关联上其他部门之后只能操作其他部门的应用了
   *
   * @param context
   */
  public void doAssociateOtherDepartment(Context context) {
    Integer dptId = this.getInt("selecteddptid");
    if (dptId == null) {
      this.addErrorMessage(context, "请选择需要关联的部门");
      return;
    }
    final String usrId = this.getString("usrid");
    UsrDptRelation usr = this.getUsrDptRelationDAO().loadFromWriteDB(usrId);
    final Department department = this.getDepartmentDAO().loadFromWriteDB(dptId);
    Assert.assertNotNull("department can not be null", department);
    if (!department.getLeaf()) {
      this.addErrorMessage(context, "关联的部门“" + department.getFullName() + "”不能包含其他子部门");
      return;
    }
    UsrDptExtraRelationCriteria query = new UsrDptExtraRelationCriteria();
    query.createCriteria().andUsrIdEqualTo(usrId).andDptIdEqualTo(dptId);
    if (department.getDptId() == dptId) {
      this.addErrorMessage(context, "不能关联用户自己所在的部门");
      return;
    }
    if (getUsrDptExtraRelationDAO().countByExample(query) > 0) {
      this.addErrorMessage(context, "用户已经关联了部门：" + department.getFullName() + "，不能重复关联");
      return;
    }
    UsrDptExtraRelation relation = new UsrDptExtraRelation();
    relation.setGmtCreate(new Date());
    relation.setGmtModified(new Date());
    relation.setDptId(dptId);
    relation.setDptName(department.getFullName());
    relation.setUsrId(usr.getUsrId());
    relation.setUsrName(usr.getUserName());
    relation.setGmtCreate(new Date());
    relation.setGmtModified(new Date());
    getUsrDptExtraRelationDAO().insertSelective(relation);
    // 更新用户关系表上的是否有extra department的标记位
    usr = new UsrDptRelation();
    usr.setExtraDptRelation(true);
    UsrDptRelationCriteria usrRelation = new UsrDptRelationCriteria();
    usrRelation.createCriteria().andUsrIdEqualTo(usrId);
    this.getUsrDptRelationDAO().updateByExampleSelective(usr, usrRelation);
    addActionMessage(context, "已经成功将用户“" + relation.getUsrName() + "” 与部门“" + relation.getDptName() + "” 相关联");
  }

  /**
   * 解除用户和额外部门的绑定关系
   *
   * @param context
   */
  public void doDeleteUsrDptRelation(Context context) {
    // String usrid = this.getString("userid");
    Long usrDptRelationId = this.getLong("usrDptRelationId");
    final UsrDptExtraRelation usrDptRelation = this.getUsrDptExtraRelationDAO().selectByPrimaryKey(usrDptRelationId);
    if (usrDptRelation == null) {
      this.addErrorMessage(context, "已经解除绑定");
      return;
    }
    Assert.assertNotNull("usrDptRelationId can not be null", usrDptRelationId);
    this.getUsrDptExtraRelationDAO().deleteByPrimaryKey(usrDptRelationId);
    // 更新用户关系表上的是否有extra department的标记位
    UsrDptExtraRelationCriteria extraRelationQuery = new UsrDptExtraRelationCriteria();
    extraRelationQuery.createCriteria().andUsrIdEqualTo(usrDptRelation.getUsrId());
    UsrDptRelation usr = new UsrDptRelation();
    usr.setExtraDptRelation(this.getUsrDptExtraRelationDAO().countByExample(extraRelationQuery) > 0);
    UsrDptRelationCriteria usrRelation = new UsrDptRelationCriteria();
    usrRelation.createCriteria().andUsrIdEqualTo(usrDptRelation.getUsrId());
    this.getUsrDptRelationDAO().updateByExampleSelective(usr, usrRelation);
    this.addActionMessage(context, "已经解除了用户“" + usrDptRelation.getUsrName() + "”与部门“" + usrDptRelation.getDptName() + "”的绑定关系");
  }

  /**
   * 用户部门更新
   *
   * @param context
   */
  @Func(PermissionConstant.AUTHORITY_USER_MANAGE)
  public void doChangeDepartment(Context context) {
    String usrid = this.getString("usrid");
    Integer selecteddptid = this.getInt("selecteddptid");
    Assert.assertNotNull(usrid);
    Assert.assertNotNull(selecteddptid);
    UsrDptRelation usr = this.getUsrDptRelationDAO().loadFromWriteDB(usrid);
    Assert.assertNotNull(usr);
    Department department = this.getDepartmentDAO().loadFromWriteDB(selecteddptid);
    Assert.assertNotNull(department);
    UsrDptRelationCriteria criteria = new UsrDptRelationCriteria();
    criteria.createCriteria().andUsrIdEqualTo(usrid);
    UsrDptRelation record = new UsrDptRelation();
    record.setDptName(department.getFullName());
    record.setDptId(department.getDptId());
    record.setUpdateTime(new Date());
    this.getUsrDptRelationDAO().updateByExampleSelective(record, criteria);
    this.addActionMessage(context, "已经将用户“" + usr.getUserName() + "”归属到新的部门：" + department.getFullName());
  }
}
