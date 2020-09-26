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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-31
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
