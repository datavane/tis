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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
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
        this.setBizResult(context, getAllBizDomain());
    }

    /**
     * 取得所有的业务线实体
     *
     * @return
     */
    protected final List<Department> getAllBizDomain() {
        DepartmentCriteria q = new DepartmentCriteria();
        q.createCriteria().andIsLeaf();
        q.setOrderByClause("dpt_id desc");
        List<Department> dpts = this.getDepartmentDAO().selectByExample(q, 1, 200);
        return dpts;
    }
}
