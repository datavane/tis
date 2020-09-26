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
package com.qlangtech.tis.manage.common.apps;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;

/**
 * 该用户同时具有其他部门的访问权限
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-5-20
 */
public class NormalUserWithExtraDptsApplicationFetcher extends NormalUserApplicationFetcher {

    public NormalUserWithExtraDptsApplicationFetcher(IUser user, Department department, RunContext context) {
        super(user, department, context);
    }

    @Override
    protected void postCreateBelongDpt(List<Department> belongDpt) {
        super.postCreateBelongDpt(belongDpt);
        // UsrDptExtraRelationCriteria extraDptQuery = new
        // UsrDptExtraRelationCriteria();
        // extraDptQuery.createCriteria().andUsrIdEqualTo(user.getId());
        belongDpt.addAll(context.getDepartmentDAO().selectByInnerJoinWithExtraDptUsrRelation(user.getId()));
    }
}
