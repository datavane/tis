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
package com.qlangtech.tis.servlet;

import java.io.IOException;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.qlangtech.tis.checkhealth.TlogFileStatusChecker;
import com.dihuo.app.common.monitor.enums.StatusLevel;
import com.dihuo.app.common.monitor.model.StatusModel;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CheckHealthJettyRunner {

    Server server;

    FilterHolder dispatchFilter;

    String context;

    /**
     * A main class that starts jetty+solr This is useful for debugging
     */
    public static void main(String[] args) {
    // try {
    // JettyTISRunner jetty = new JettyTISRunner("/", 3456);
    // jetty.addServlet(new RealtimeReportServlet(), "/trigger");
    // jetty.start();
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    }

    public CheckHealthJettyRunner(String context, int port) {
        this.init(context, port);
    }

    public CheckHealthJettyRunner(String context, int port, String solrConfigFilename) {
        this.init(context, port);
        if (solrConfigFilename != null)
            dispatchFilter.setInitParameter("solrconfig-filename", solrConfigFilename);
    }

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
    private ServletContextHandler rootContext;

    private void init(String context, int port) {
        this.context = context;
        server = new Server(port);
        server.setStopAtShutdown(true);
        // ServletContextHandler
        this.rootContext = new // ,ServletContextHandler.SESSIONS
        ServletContextHandler(// ,ServletContextHandler.SESSIONS
        server, // ,ServletContextHandler.SESSIONS
        context);
        // server.setThreadPool(new QueuedThreadPool(100));
        // Initialize the servlets
        // this.rootContext = new Context(server, context,
        // ServletContextHandler.SESSIONS);
        // for some reason, there must be a servlet for this to get applied
        rootContext.addServlet(CheckHealth.class, "/check_health");
        dispatchFilter = rootContext.addFilter(InnerFilter.class, "*", EnumSet.of(DispatcherType.REQUEST));
    }

    public void addServlet(HttpServlet servlet, String pathSpec) {
        this.rootContext.addServlet(new ServletHolder(servlet), pathSpec);
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

    public static class CheckHealth extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private TlogFileStatusChecker tlogFileStatusChecker = new TlogFileStatusChecker();

        @Override
        public void init() throws ServletException {
            this.tlogFileStatusChecker = new TlogFileStatusChecker();
            this.tlogFileStatusChecker.init();
        }

        @Override
        public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
            StatusModel model = tlogFileStatusChecker.check();
            if (model.level == StatusLevel.OK) {
                res.getWriter().write("ok");
            } else {
                res.getWriter().print("Check fail:" + model.message);
            }
        }
    }
}
