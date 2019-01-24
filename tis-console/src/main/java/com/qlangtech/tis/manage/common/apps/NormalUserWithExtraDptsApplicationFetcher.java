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
package com.qlangtech.tis.manage.common.apps;

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;

/*
 * 该用户同时具有其他部门的访问权限
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
