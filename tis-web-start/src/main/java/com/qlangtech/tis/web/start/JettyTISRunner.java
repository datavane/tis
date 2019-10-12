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
package com.qlangtech.tis.web.start;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.health.check.IStatusChecker;
import com.qlangtech.tis.health.check.StatusLevel;
import com.qlangtech.tis.health.check.StatusModel;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JettyTISRunner {

	private Server server;
	// FilterHolder dispatchFilter;
	String context;

	private static JettyTISRunner jetty;
	private static final Logger logger = LoggerFactory.getLogger(JettyTISRunner.class);

	public static void start(String contextPath, int port) throws Exception {
		start(contextPath, port, (c) -> {
		});
	}

	/**
	 * A main class that starts jetty+solr This is useful for debugging
	 */
	public static void start(String contextPath, int port, IWebAppContextSetter contextSetter) throws Exception {
		// System.setProperty("solr.solr.home", "/home/solr");
		// System.setProperty("solr.solr.home", "/opt/data/solrhome");
		// System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH",
		// "true");
		if (jetty != null) {
			throw new IllegalStateException("instance jetty shall be null");
		}
		jetty = new JettyTISRunner(contextPath, port, contextSetter);

		jetty.start();
	}

	/**
	 * 关闭jetty
	 * 
	 * @throws Exception
	 */
	public static void stopJetty() throws Exception {
		if (jetty == null) {
			throw new IllegalStateException("instance jetty have not been initialize");
		}
		jetty.stop();
	}

	private JettyTISRunner(String context, int port, IWebAppContextSetter contextSetter) {
		this.init(context, port, contextSetter);
	}

	private void init(String context, int port, final IWebAppContextSetter contextSetter) {
		this.context = context;
		server = new Server(new QueuedThreadPool(450));

		NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
		connector.setPort(port);
		//connector.setIdleTimeout(idleTimeout);
		// NetworkTrafficServerConnector healthConnector = new
		// NetworkTrafficServerConnector(server);
		// connector.setPort(8088);
		server.setConnectors(new Connector[] { // , healthConnector
				connector });
		server.setStopAtShutdown(true);

		final File webappDir = new File("webapp");
		Assert.assertTrue("file is illegal:" + webappDir.getAbsolutePath(),
				webappDir.exists() && webappDir.isDirectory());
		Resource webContentResource = Resource.newResource(webappDir);

		WebAppContext webAppContext = new WebAppContext(webContentResource, context);
		webAppContext.setDescriptor("/WEB-INF/web.xml");
		webAppContext.setDisplayName("jetty");
		webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		webAppContext.setConfigurationDiscovered(true);
		webAppContext.setParentLoaderPriority(true);
		webAppContext.setThrowUnavailableOnStartupException(true);
		webAppContext.addServlet(CheckHealth.class, "/check_health");
//		webAppContext.addServlet(StopServlet.class, "/stop");

		contextSetter.process(webAppContext);

		server.setHandler(webAppContext);
	}

	public interface IWebAppContextSetter {
		void process(WebAppContext context);
	}

//	public static class StopServlet extends HttpServlet {
//
//		private static final long serialVersionUID = 1L;
//		private final ScheduledExecutorService stopThread = Executors.newSingleThreadScheduledExecutor();
//
//		@Override
//		public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
//			try {
//				if ("true".equals(req.getParameter("stop"))) {
//
//					stopThread.schedule(() -> {
//						try {
//							JettyTISRunner.stopJetty();
//							logger.info("trigger jetty stop process");
//						} catch (Exception e) {
//							logger.error(e.getMessage(), e);
//						}
//					}, 3, TimeUnit.SECONDS);
//
//					res.getWriter().write("stop_success");
//					return;
//				}
//			} catch (Exception e) {
//				new IOException(e);
//			}
//
//			throw new IllegalArgumentException("invalid command");
//		}
//	}

	public static class CheckHealth extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private List<IStatusChecker> checks;

		@Override
		public void init() throws ServletException {

			this.checks = new ArrayList<>();
			ServiceLoader.load(IStatusChecker.class).forEach((r) -> {
				if (r instanceof IServletContextAware) {
					((IServletContextAware) r).setServletContext(getServletContext());
				}
				checks.add(r);
			});
		}

		@Override
		public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {

			for (IStatusChecker check : checks) {
				StatusModel model = check.check();
				if (model.level != StatusLevel.OK) {
					res.getWriter().print("Check[" + check.getClass() + "] fail:" + model.message);
					return;
				}
			}
			res.getWriter().write("ok");
		}
	}

	public void addServlet(HttpServlet servlet, String pathSpec) {
		// this.rootContext.addServlet(new ServletHolder(servlet), pathSpec);
	}

	public void addFilter(FilterHolder filter, String urlpattern) {
		// this.rootContext.addFilter(filter, urlpattern,
		// EnumSet.of(DispatcherType.REQUEST));
		// FilterRegistrationBean registrationBean = new
		// FilterRegistrationBean();
		// registrationBean.setFilter(new TisSolrDispatchFilter());
		// registrationBean.addUrlPatterns("/*");
		// registrationBean.addInitParameter("excludePatterns",
		// "/css/.+,/js/.+,/img/.+,/tpl/.+");
		// registrationBean.setName("SolrRequestFilter");
		// registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		// return registrationBean;
	}

	// public static class InnerFilter implements Filter {
	//
	// @Override
	// public void init(FilterConfig filterConfig) throws ServletException {
	// }
	//
	// @Override
	// public void doFilter(ServletRequest request, ServletResponse response,
	// FilterChain chain)
	// throws IOException, ServletException {
	// chain.doFilter(request, response);
	// }
	//
	// @Override
	// public void destroy() {
	// }
	// }

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	private void start() throws Exception {
		start(true);
	}

	private void start(boolean waitForSolr) throws Exception {
		if (!server.isRunning()) {
			server.start();
			// server.join();
		}
		// if (waitForSolr)
		// waitForSolr(context);
	}

	private void stop() throws Exception {
		if (server.isRunning()) {
			server.stop();
			// server.join();
		}
	}

	/**
	 * This is a stupid hack to give jetty something to attach to
	 */
	public static class Servlet404 extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
			res.sendError(404, "Can not find: " + req.getRequestURI());
		}
	}
}
