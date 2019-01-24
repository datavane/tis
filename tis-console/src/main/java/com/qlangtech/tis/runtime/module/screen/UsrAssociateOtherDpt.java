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

import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;

/*
 * 用户相关了部门一览
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UsrAssociateOtherDpt extends UsrUpdate {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        super.execute(context);
        String usrid = this.getString("usrid");
        Assert.assertNotNull(usrid);
        // 取得当前已经关联的部门
        UsrDptExtraRelationCriteria criteria = new UsrDptExtraRelationCriteria();
        criteria.createCriteria().andUsrIdEqualTo(usrid);
        context.put("extraDptAssociate", this.getUsrDptExtraRelationDAO().selectByExample(criteria));
    }
}
