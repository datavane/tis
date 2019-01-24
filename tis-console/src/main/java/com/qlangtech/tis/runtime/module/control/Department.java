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
package com.qlangtech.tis.runtime.module.control;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Department extends BasicScreen {

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.runtime.module.screen.BasicScreen#execute(com.alibaba
	 * .citrus.turbine.Context)
	 */
    @Override
    public void execute(Context context) throws Exception {
    // setOrgList(context);
    }
    // private void setOrgList(Context context) {
    // List<OrganizationDO> orglist = new ArrayList<OrganizationDO>();
    // OrganizationDO org = null;
    // DepartmentCriteria query = new DepartmentCriteria();
    // query.createCriteria();
    // 
    // for (com.taobao.terminator.manage.biz.dal.pojo.Department dpt : this
    // .getDepartmentDAO().selectByExample(query, 1, 500)) {
    // org = new OrganizationDO();
    // 
    // org.setId(dpt.getDptId());
    // org.setName(dpt.getName());
    // org.setParentId(dpt.getParentId());
    // 
    // orglist.add(org);
    // }
    // 
    // // orglist.remove(0);
    // // orglist.remove(orglist.size() - 1);
    // context.put("orglist", orglist);
    // }
}
