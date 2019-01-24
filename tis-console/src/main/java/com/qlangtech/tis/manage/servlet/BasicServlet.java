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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qlangtech.tis.manage.common.RunContext;
import com.thoughtworks.xstream.XStream;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BasicServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final XStream xstream = new XStream();

	protected RunContext getContext() {
		return getBeanByType(this.getServletContext(), RunContext.class);
	}

	public static <T> T getBeanByType(ServletContext servletContext, Class<T> clazz) {
		for (Object context : WebApplicationContextUtils.getWebApplicationContext(servletContext).getBeansOfType(clazz)
				.values()) {
			return clazz.cast(context);
		}
		throw new IllegalStateException("can not find:" + clazz);
	}

	// protected ActionTool getActionTool() {
	// return getBeanByType(this.getServletContext(), ActionTool.class);
	// }
	protected void wirteXml2Client(HttpServletResponse response, Object o) throws IOException {
		response.setContentType(DownloadResource.XML_CONTENT_TYPE);
		xstream.toXML(o, response.getWriter());
	}

	protected void include(HttpServletRequest request, HttpServletResponse response, String path)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(path);
		dispatcher.include(request, response);
	}
}
