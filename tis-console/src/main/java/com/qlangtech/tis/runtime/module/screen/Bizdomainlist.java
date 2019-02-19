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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Bizdomainlist extends BasicManageScreen {

	/**
	 */
	private static final long serialVersionUID = 1L;

	@Func(PermissionConstant.APP_DEPARTMENT_LIST)
	public void execute(Context context) throws Exception {
		// disableDomainView(context);
		context.put("bizlist", getAllBizDomain());
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
		// Collections.sort(dpts, new Comparator<Department>() {
		//
		// @Override
		// public int compare(Department o1, Department o2) {
		// return o1.getFullName().compareTo(o2.getFullName());
		// }
		// });
		return dpts;
	}
}
