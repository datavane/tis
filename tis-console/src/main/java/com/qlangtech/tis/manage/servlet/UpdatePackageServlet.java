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
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackage;
import com.qlangtech.tis.manage.biz.dal.pojo.AppPackageCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UpdatePackageServlet extends BasicServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(IOUtils.toString(req.getInputStream()));
    // Integer pid = Integer.parseInt(req.getParameter("pid"));
    // final String username = req.getParameter("username");
    // 
    // if (StringUtils.isEmpty(username)) {
    // throw new IllegalArgumentException("username:" + username
    // + " can not be null");
    // }
    // 
    // // 更新数据库
    // AppPackageCriteria criteria = new AppPackageCriteria();
    // criteria.createCriteria().andPidEqualTo(pid);
    // AppPackage pack = new AppPackage();
    // pack.setTestStatus(SnapshotCriteria.TEST_STATE_BUILD_INDEX);
    // pack.setSuccessSnapshotId(Integer.parseInt(req
    // .getParameter("snapshotid")));
    // pack.setLastTestTime(new Date());
    // pack.setLastTestUser(username);
    // getContext().getAppPackageDAO()
    // .updateByExampleSelective(pack, criteria);
    }
}
