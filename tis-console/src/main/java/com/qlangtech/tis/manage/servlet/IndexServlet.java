package com.qlangtech.tis.manage.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 欢迎页面
 *
 * @date 2019年2月20日
 */
public class IndexServlet extends BasicServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/runtime/index.htm");
	}

}
