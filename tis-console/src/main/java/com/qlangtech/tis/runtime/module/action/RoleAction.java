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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Role;
import com.qlangtech.tis.manage.biz.dal.pojo.RoleCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.Config.FuncGroup;
import com.qlangtech.tis.manage.spring.aop.Func;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RoleAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    public static final String selfuncidKEY = "selfuncid";

    // do_select_role
    /**
     * 添加功能点
     *
     * @param context
     */
    @Func(PermissionConstant.AUTHORITY_FUNC_ADD)
    public void doAddFunc(Context context) {
        final String funckey = StringUtils.trim(this.getString("funcKey"));
        final String funcName = StringUtils.trim(this.getString("funcName"));
        if (StringUtils.isBlank(funckey)) {
            this.addErrorMessage(context, "请设置Funckey");
            return;
        }
        if (StringUtils.isBlank(funcName)) {
            this.addErrorMessage(context, "请设置FuncName");
            return;
        }
        FuncCriteria fcriteria = new FuncCriteria();
        fcriteria.createCriteria().andFunKeyEqualTo(funckey);
        fcriteria.or(fcriteria.createCriteria().andFuncNameEqualTo(funcName));
        if (this.getFuncDAO().countByExample(fcriteria) > 0) {
            this.addErrorMessage(context, "不同重复添加相同的Funckey或者FuncName");
            return;
        }
        Integer groupType = this.getInt("groupType");
        FuncGroup selType = null;
        for (FuncGroup funcGroup : Config.getFuncGroup()) {
            if (funcGroup.getKey() == groupType) {
                selType = funcGroup;
                break;
            }
        }
        if (selType == null) {
            this.addErrorMessage(context, "请选择功能类型");
            return;
        }
        com.qlangtech.tis.manage.biz.dal.pojo.Func func = new com.qlangtech.tis.manage.biz.dal.pojo.Func();
        func.setFuncGroupKey(groupType);
        func.setFunKey(funckey);
        func.setFuncName(funcName);
        func.setGmtCreate(new Date());
        func.setGmtModified(new Date());
        func.setFuncGroupName(selType.getName());
        this.getFuncDAO().insertSelective(func);
        this.addActionMessage(context, "已经成功添加Func" + funcName + "(" + funckey + ")");
    }

    /**
     * 用户选择角色
     *
     * @param context
     */
    @Func(PermissionConstant.AUTHORITY_USER_ROLE_SET)
    public void doSelectRole(Context context) {
        String usrid = this.getString("usrid");
        Integer roleid = this.getInt("roleid");
        Role role = this.getRoleDAO().loadFromWriteDB(roleid);
        Assert.assertNotNull(role);
        UsrDptRelationCriteria ucriteria = new UsrDptRelationCriteria();
        ucriteria.createCriteria().andUsrIdEqualTo(usrid);
        UsrDptRelation record = new UsrDptRelation();
        record.setRoleName(role.getRoleName());
        record.setrId(role.getrId());
        this.getUsrDptRelationDAO().updateByExampleSelective(record, ucriteria);
        addActionMessage(context, "用户选择了新的角色：“" + role.getRoleName() + "”");
    }

    /**
     * 更新角色
     *
     * @param context
     */
    @Func(PermissionConstant.AUTHORITY_ROLE_UPDATE)
    public void doUpdateRole(Context context) {
        // 用户选中的func功能集合
        final List<Integer> funcids = Arrays.asList(this.getIntAry("funcid"));
        final Role role = this.getRoleDAO().loadFromWriteDB(this.getInt("roleid"));
        String roleName = this.getString("rolename");
        role.setRoleName(roleName);
        context.put("role", role);
        context.put(selfuncidKEY, Collections.unmodifiableCollection(funcids));
        if (StringUtils.isBlank(roleName)) {
            this.addErrorMessage(context, "请添写角色名称");
            return;
        }
        RoleCriteria criteria = new RoleCriteria();
        criteria.createCriteria().andRoleNameEqualTo(roleName).andRIdNotEqualTo(role.getrId());
        if (this.getRoleDAO().countByExample(criteria) > 0) {
            this.addErrorMessage(context, "角色名称：“" + roleName + "”已经创建");
            return;
        }
        if (funcids.size() < 1) {
            this.addErrorMessage(context, "请为新添加的角色设置相应的功能");
            return;
        }
        FuncRoleRelationCriteria fcriteria = new FuncRoleRelationCriteria();
        fcriteria.createCriteria().andRIdEqualTo(role.getrId());
        List<FuncRoleRelation> rellist = this.getFuncRoleRelationDAO().selectByExample(fcriteria);
        List<Integer> orignfunclist = new ArrayList<Integer>();
        for (FuncRoleRelation rel : rellist) {
            orignfunclist.add(rel.getFuncId());
        }
        List<Integer> addfuncs = new ArrayList<Integer>();
        // 需要删除的
        for (Integer funcid : funcids) {
            if (orignfunclist.contains(funcid)) {
                orignfunclist.remove(funcid);
            } else {
                addfuncs.add(funcid);
            }
        }
        for (Integer funcid : orignfunclist) {
            fcriteria = new FuncRoleRelationCriteria();
            fcriteria.createCriteria().andRIdEqualTo(role.getrId()).andFuncIdEqualTo(funcid);
            this.getFuncRoleRelationDAO().deleteByExample(fcriteria);
        }
        Role update = new Role();
        update.setRoleName(roleName);
        RoleCriteria rcriteria = new RoleCriteria();
        rcriteria.createCriteria().andRIdEqualTo(role.getrId());
        this.getRoleDAO().updateByExampleSelective(update, rcriteria);
        // 需要添加的
        createRelation(addfuncs, role);
        this.addActionMessage(context, "成功更新角色：“" + role.getRoleName() + "”");
    }

    public List<Integer> getSelfuncid() {
        return Arrays.asList(this.getIntAry("funcid"));
    }

    @Func(PermissionConstant.AUTHORITY_ROLE_ADD)
    public void doAddRole(Context context) {
        String roleName = this.getString("rolename");
        final Integer[] funcids = (Integer[]) getSelfuncid().toArray();
        if (StringUtils.isBlank(roleName)) {
            this.addErrorMessage(context, "请添写角色名称");
            return;
        }
        RoleCriteria criteria = new RoleCriteria();
        criteria.createCriteria().andRoleNameEqualTo(roleName);
        if (this.getRoleDAO().countByExample(criteria) > 0) {
            this.addErrorMessage(context, "角色名称：“" + roleName + "”已经创建");
            return;
        }
        if (funcids.length < 1) {
            this.addErrorMessage(context, "请为新添加的角色设置相应的功能");
            return;
        }
        Role role = new Role();
        role.setGmtCreate(new Date());
        role.setGmtModified(new Date());
        role.setRoleName(roleName);
        Integer newRoleId = this.getRoleDAO().insertSelective(role);
        role.setrId(newRoleId);
        createRelation(Arrays.asList(funcids), role);
        this.addActionMessage(context, "成功添加角色：“" + roleName + "”");
    }

    private void createRelation(final List<Integer> funcids, Role role) {
        FuncRoleRelation relation = null;
        com.qlangtech.tis.manage.biz.dal.pojo.Func func = null;
        
        for (Integer id : funcids) {
            func = this.getFuncDAO().loadFromWriteDB(id);
            if (func == null) {
                continue;
            }
            relation = new FuncRoleRelation();
            relation.setFuncId(func.getFunId());
            relation.setFuncKey(func.getFunKey());
            relation.setFuncName(func.getFuncName());
            relation.setGmtCreate(new Date());
            relation.setGmtModified(new Date());
            relation.setrId(role.getrId());
            relation.setRoleName(role.getRoleName());
            this.getFuncRoleRelationDAO().insertSelective(relation);
        }
    }
}
