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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JettyTISRunner {

    Server server;

    // FilterHolder dispatchFilter;
    String context;

    /**
     * A main class that starts jetty+solr This is useful for debugging
     */
    public static void main(String[] args) throws Exception {
        JettyTISRunner jetty = new JettyTISRunner("/tiscollect", 8080);
        jetty.start();
    }

    public JettyTISRunner(String context, int port) {
        this.init(context, port);
    }

    private void init(String context, int port) {
        this.context = context;
        server = new Server(new QueuedThreadPool(450));
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
        connector.setPort(port);
        // NetworkTrafficServerConnector healthConnector = new
        // NetworkTrafficServerConnector(server);
        // connector.setPort(8088);
        server.setConnectors(new Connector[] { // , healthConnector
        connector });
        server.setStopAtShutdown(true);
        final File webappDir = new File("webapp");
        Resource webContentResource = Resource.newResource(webappDir);
        WebAppContext webAppContext = new WebAppContext(webContentResource, context);
        webAppContext.setDescriptor("/WEB-INF/web.xml");
        webAppContext.setDisplayName("jetty");
        webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        webAppContext.setConfigurationDiscovered(true);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        server.setHandler(webAppContext);
    }

    public void addServlet(HttpServlet servlet, String pathSpec) {
    // this.rootContext.addServlet(new ServletHolder(servlet), pathSpec);
    }

    public void addFilter(FilterHolder filter, String urlpattern) {
    // this.rootContext.addFilter(filter, urlpattern,
    // EnumSet.of(DispatcherType.REQUEST));
    // FilterRegistrationBean registrationBean = new FilterRegistrationBean();
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
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
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
        // server.join();
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
