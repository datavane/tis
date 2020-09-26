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
package com.qlangtech.tis.collect.servlet;

import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.quartz.JobExecutionException;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.qlangtech.tis.ClusterStateCollectManager;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年5月4日上午10:04:12
 */
public class FullReportCreateServlet extends GenericServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ClusterStateCollectManager appLauncherListener = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()).getBean("clusterStateCollectManager", ClusterStateCollectManager.class);
        if (appLauncherListener == null) {
            throw new IllegalStateException("servletContext obj can not be null");
        }
        try {
            appLauncherListener.exportReport();
        } catch (JobExecutionException e) {
            throw new ServletException(e);
        }
    }
}
