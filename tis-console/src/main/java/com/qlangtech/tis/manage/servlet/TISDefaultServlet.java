package com.qlangtech.tis.manage.servlet;

import org.eclipse.jetty.servlet.DefaultServlet;

/**
 * @date 2019年2月20日
 */
public class TISDefaultServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public String getWelcomeFile(String pathInContext) {
		return "/index.htm";
	}
}
