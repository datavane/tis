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

import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.thoughtworks.xstream.XStream;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-19
 */
public class BasicServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // public static final XStream xstream = new XStream();
    // 
    // static {
    // xstream.alias("sdomain", SnapshotDomain.class);
    // }
    protected RunContext getContext() {
        return getBeanByType(this.getServletContext(), RunContext.class);
    }

    // protected RpcCoreManage getRpcCoreManage() {
    // return WebApplicationContextUtils.getWebApplicationContext(
    // this.getServletContext()).getBean("rpcCoreManage",
    // RpcCoreManage.class);
    // }
    // static {
    // 
    // try {
    // Class.forName("com.taobao.terminator.manage.common.ActionTool");
    // } catch (ClassNotFoundException e) {
    // throw new RuntimeException(e);
    // }
    // 
    // }
    // protected BasicModule getBasicModule() {
    // return getBeanByType(BasicModule.class);
    // }
    public static <T> T getBeanByType(ServletContext servletContext, Class<T> clazz) {
        for (Object context : WebApplicationContextUtils.getWebApplicationContext(servletContext).getBeansOfType(clazz).values()) {
            return clazz.cast(context);
        }
        throw new IllegalStateException("can not find:" + clazz);
    }

    // protected ActionTool getActionTool() {
    // return getBeanByType(this.getServletContext(), ActionTool.class);
    // }
    protected void wirteXml2Client(HttpServletResponse response, Object o) throws IOException {
        response.setContentType(DownloadResource.XML_CONTENT_TYPE);
        HttpConfigFileReader.xstream.toXML(o, response.getWriter());
    }

    protected void include(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(path);
        dispatcher.include(request, response);
    }
}
