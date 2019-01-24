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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Role;
import com.qlangtech.tis.pubhook.common.Nullable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RoleUpdate extends BasicScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
    // final Role role = getRole(context, this.getInt("roleid"));
    // 
    // if (context.get("role") == null) {
    // context.put("role", role);
    // }
    }

    private static class NULL_ROLE extends Role implements Nullable {

        private static final long serialVersionUID = 1L;
    }

    private Role role = null;

    public Role getRole() {
        if (role == null) {
            Integer roleid = getRoleId();
            if (roleid == null || roleid < 1) {
                role = new NULL_ROLE();
            } else {
                role = this.getRoleDAO().loadFromWriteDB(roleid);
            }
        }
        return role;
    }

    protected Integer getRoleId() {
        return this.getInt("roleid");
    }

    private List<Integer> funcids;

    public List<Integer> getSelfuncid() {
        if (getRole() instanceof Nullable) {
            return Collections.emptyList();
        }
        if (funcids == null) {
            FuncRoleRelationCriteria criteria = new FuncRoleRelationCriteria();
            criteria.createCriteria().andRIdEqualTo(getRole().getrId());
            funcids = new ArrayList<Integer>();
            for (FuncRoleRelation relation : this.getFuncRoleRelationDAO().selectByExample(criteria)) {
                funcids.add(relation.getFuncId());
            }
        }
        return funcids;
    }

    // protected Role getRole(Context context, Integer roleid) {
    // 
    // if (roleid < 1) {
    // return new NULL_ROLE();
    // }
    // 
    // final Role role = this.getRoleDAO().loadFromWriteDB(roleid);
    // Assert.assertNotNull(role);
    // if (context.get(RoleAction.selfuncidKEY) == null) {
    // FuncRoleRelationCriteria criteria = new FuncRoleRelationCriteria();
    // criteria.createCriteria().andRIdEqualTo(role.getrId());
    // final List<Integer> funcids = new ArrayList<Integer>();
    // for (FuncRoleRelation relation : this.getFuncRoleRelationDAO()
    // .selectByExample(criteria)) {
    // funcids.add(relation.getFuncId());
    // }
    // 
    // context.put(RoleAction.selfuncidKEY, funcids);
    // }
    // return role;
    // }
    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}
