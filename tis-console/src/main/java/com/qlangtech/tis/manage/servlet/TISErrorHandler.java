package com.qlangtech.tis.manage.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

public class TISErrorHandler extends ErrorHandler {

	@Override
	public void handle(String target, Request baseRequest //
			, HttpServletRequest request, HttpServletResponse response) throws IOException {

		Throwable th = (Throwable) request.getAttribute(Dispatcher.ERROR_EXCEPTION);

		request.setCharacterEncoding("utf8");
		response.setCharacterEncoding("utf8");
		response.setContentType("text/html; charset=utf-8");

		// response.getWriter().write();

		StringBuffer buffer = new StringBuffer();
		buffer.append("	<html>");
		buffer.append("		 <head>");
		buffer.append("		 </head>");
		buffer.append("		 <body>");

		buffer.append("		 <center style=\"margin-top:100px;\">");
		buffer.append("		 <table>");
		buffer.append("		 <tbody><tr>");
		buffer.append("		 <td>");
		buffer.append("		 </td>");
		buffer.append("		 </tr>");
		buffer.append("		 <tr>");
		buffer.append("		 <td align=\"center\">");
		buffer.append(
				"		 <h1 style=\"margin:10px;font-size:800%;font-family:Arial Black,黑体;color:#999999\">404</h1>");
		buffer.append("		 <h2 style=\"color:blue;\">系统发生错误,请联系统管理员</h2>");
		buffer.append("\n<pre>");
		if (th != null) {
			buffer.append(ExceptionUtils.getRootCauseMessage(th));
		}
		buffer.append("\n</pre>");
		buffer.append("		 </td>");
		buffer.append("		 </tr>");
		buffer.append("		 </tbody></table>");
		buffer.append("		 </center>");
		buffer.append("\n<pre style=\"display:none\">");
		if (th != null) {
			buffer.append(ExceptionUtils.getStackTrace(th));
		}
		buffer.append("\n</pre>");
		buffer.append("		 </body>");
		buffer.append("		</html>");
		response.getWriter().write(buffer.toString());
	}
}
