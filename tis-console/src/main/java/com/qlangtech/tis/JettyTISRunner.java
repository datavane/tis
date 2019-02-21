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
package com.qlangtech.tis;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
import com.qlangtech.tis.manage.servlet.TISErrorHandler;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JettyTISRunner {

	private static final Logger logger = LoggerFactory.getLogger(JettyTISRunner.class);

	Server server;

	// FilterHolder dispatchFilter;
	String context;

	/**
	 * A main class that starts jetty+solr This is useful for debugging
	 */
	public static void main(String[] args) throws Exception {
		// System.setProperty("solr.solr.home", "/home/solr");
		// System.setProperty("solr.solr.home", "/opt/data/solrhome");
		// System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH",
		// "true");
		JettyTISRunner jetty = new JettyTISRunner("/", 8080);
		// jetty.addServlet(new VersionServlet(), "/version");
		//
		// FilterHolder filter = new FilterHolder(TisSolrDispatchFilter.class);
		// filter.setInitParameter("excludePatterns",
		// "/css/.+,/js/.+,/img/.+,/tpl/.+");
		// filter.setName("SolrRequestFilter");
		// jetty.addFilter(filter, "/*");
		jetty.start();
	}

	public JettyTISRunner(String context, int port) {
		try {
			this.init(context, port);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// public JettyTISRunner(String context, int port, String
	// solrConfigFilename) {
	// this.init(context, port);
	// if (solrConfigFilename != null)
	// dispatchFilter.setInitParameter("solrconfig-filename",
	// solrConfigFilename);
	// }
	// public JettySolrRunner( String context, String home, String dataDir, int
	// port, boolean log )
	// {
	// if(!log) {
	// System.setProperty("org.mortbay.log.class", NoLog.class.getName() );
	// System.setProperty("java.util.logging.config.file",
	// home+"/conf/logging.properties");
	// NoLog noLogger = new NoLog();
	// org.mortbay.log.Log.setLog(noLogger);
	// }
	//
	// // Initalize JNDI
	// Config.setInstanceDir(home);
	// new SolrCore(dataDir, new IndexSchema(home+"/conf/schema.xml"));
	// this.init( context, port );
	// }
	// private ServletContextHandler rootContext;
	// private void init(String context, int port) {
	// this.context = context;
	// server = new Server(port);
	// if (System.getProperty("jetty.testMode") != null) {
	// // SelectChannelConnector connector = new SelectChannelConnector();
	// // Normal SocketConnector is what solr's example server uses by
	// // default
	// SocketConnector connector = new SocketConnector();
	// connector.setPort(port);
	// connector.setReuseAddress(true);
	// server.setConnectors(new Connector[] { connector });
	// server.setSessionIdManager(new HashSessionIdManager(new Random()));
	// }
	// server.setStopAtShutdown(true);
	//
	// // server.setThreadPool(new QueuedThreadPool(100));
	//
	// // Initialize the servlets
	// this.rootContext = new Context(server, context, Context.SESSIONS);
	// // for some reason, there must be a servlet for this to get applied
	// rootContext.addServlet(Servlet404.class, "/*");
	// dispatchFilter = rootContext.addFilter(InnerFilter.class, "*",
	// Handler.REQUEST);
	// }
	private void init(String context, int port) throws Exception {
		this.context = context;
		server = new Server(new QueuedThreadPool(450));
		// Configuration.ClassList classlist =
		// Configuration.ClassList.setServerDefault(server);
		// classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
		// "org.eclipse.jetty.annotations.AnnotationConfiguration");
		NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
		connector.setPort(port);
		// NetworkTrafficServerConnector healthConnector = new
		// NetworkTrafficServerConnector(server);
		// connector.setPort(8088);
		server.setConnectors(new Connector[] { // , healthConnector
				connector });
		server.setStopAtShutdown(true);

		// URL url =
		// server.getClass().getResource("/org/eclipse/jetty/webapp/WebAppContext.class");
		// System.out.println(url);
		// ServletContextHandler
		final File webappDir = new File("webapp");
		Assert.assertTrue("file is illegal:" + webappDir.getAbsolutePath(),
				webappDir.exists() && webappDir.isDirectory());
		Resource webContentResource = Resource.newResource(webappDir);
		WebAppContext webAppContext = new WebAppContext(webContentResource, context);
		webAppContext.setDescriptor("/WEB-INF/web.xml");
		webAppContext.setDisplayName("jetty");
		webAppContext.setWelcomeFiles(new String[] { "index.htm" });
		webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		webAppContext.setConfigurationDiscovered(true);
		webAppContext.setParentLoaderPriority(true);
		webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
		
		webAppContext.setErrorHandler(new TISErrorHandler());
		// webAppContext.addServlet(servlet, pathSpec);
		// webAppContext.setServletHandler(servletHandler);

		webAppContext.setThrowUnavailableOnStartupException(true);
		server.setHandler(webAppContext);
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

	public static class InnerFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
		}
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public void start() throws Exception {
		start(true);
	}

	public void start(boolean waitForSolr) throws Exception {
		if (!server.isRunning()) {
			server.start();
			logger.info("jetty server launch successful");
			server.join();
		}
		// if (waitForSolr)
		// waitForSolr(context);
	}

	public void stop() throws Exception {
		if (server.isRunning()) {
			server.stop();
			server.join();
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
