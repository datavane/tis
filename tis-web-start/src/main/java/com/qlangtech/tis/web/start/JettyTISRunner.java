/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.web.start;

import com.qlangtech.tis.health.check.IStatusChecker;
import com.qlangtech.tis.health.check.StatusLevel;
import com.qlangtech.tis.health.check.StatusModel;
import org.eclipse.jetty.ee.webapp.WebAppClassLoader;
import org.eclipse.jetty.ee11.servlet.FilterHolder;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JettyTISRunner {

    private Server server;

    private static JettyTISRunner jetty;

    private static final Logger logger = LoggerFactory.getLogger(JettyTISRunner.class);

    public static void start(String contextPath, int port) throws Exception {
        start(contextPath, port, (c) -> {
        });
    }

    public int getPort() {
        return this.port;
    }

    /**
     * A main class that starts jetty+solr This is useful for debugging
     */
    public static void start(String contextPath, int port, IWebAppContextSetter contextSetter) throws Exception {
        if (jetty != null) {
            throw new IllegalStateException("instance jetty shall be null");
        }
        jetty = new JettyTISRunner(contextPath, port, contextSetter);
        jetty.start();
    }

    /**
     * 关闭jetty
     */
    public static void stopJetty() throws Exception {
        if (jetty == null) {
            throw new IllegalStateException("instance jetty have not been initialize");
        }
        jetty.stop();
    }

    private final ClassLoader parentLoader;
    private final int port;

    private final IWebAppContextSetter contextSetter;

    private final List<Handler> handlerList = new CopyOnWriteArrayList<>();

    private final AtomicInteger contextAddCount = new AtomicInteger();
    private List<IWebAppContextCollector> webAppContextCollector;

    JettyTISRunner(String context, int port, IWebAppContextSetter contextSetter) throws Exception {
        this(port, JettyTISRunner.class.getClassLoader(), contextSetter);
        this.addContext(context, new File("."), false, true);
    }

    JettyTISRunner(int port, IWebAppContextCollector webAppContextCollector) throws Exception {
        this(port, JettyTISRunner.class.getClassLoader(), (c) -> {
        });
        this.addContext(webAppContextCollector);
    }

    public void addContext(IWebAppContextCollector webAppContextCollector) throws IOException {
        webAppContextCollector.launchContext(this);
        if (this.webAppContextCollector != null) {
            throw new IllegalStateException("webAppContextCollector shall not be set twice");
        }
        this.webAppContextCollector = Collections.singletonList(webAppContextCollector);
    }

    JettyTISRunner(int port, ClassLoader parentLoader, IWebAppContextSetter contextSetter) {
        this.port = port;
        this.parentLoader = parentLoader;
        this.contextSetter = contextSetter;
    }

    /**
     * 启动子应用
     */
    public void addContext(File contextDir) throws Exception {
        this.addContext("/" + contextDir.getName(), contextDir, true, true);
    }

    public void addContext(WebAppContext webAppContext) throws Exception {
        if (webAppContext instanceof WebAppContext) {
            contextSetter.process(webAppContext);
        }
        handlerList.add(webAppContext);
        contextAddCount.incrementAndGet();
    }


    public void addContext(final String context, File contextDir, boolean addDirJars, boolean checkWebXmlExist) throws Exception {
        final File webappDir = getWebapp(contextDir).getCanonicalFile();
        if (!(webappDir.exists() && webappDir.isDirectory() //
                && (!checkWebXmlExist || (new File(webappDir, TisApp.PATH_WEB_XML)).exists()))) {
            logger.warn("dir is not webapp,skip:{}", webappDir.getAbsolutePath());
            return;
        }
        //  webappDir.toURI()

        // org.eclipse.jetty.util.resource.Resource.
        WebAppContext webAppContext = new WebAppContext();
        ResourceFactory resourceFactory = ResourceFactory.of(webAppContext);
        webAppContext.setBaseResource(resourceFactory.newResource(webappDir.toURI()));
        webAppContext.setContextPath(context);
        if (addDirJars) {
            final File libsDir = new File(contextDir, "lib");
            if (!(libsDir.exists() && libsDir.isDirectory())) {
                throw new IllegalStateException("libs is illegal:" + libsDir.getAbsolutePath());
            }
            List<URL> jarfiles = new ArrayList<>();
            List<String> resNames = new ArrayList<>();
            for (String path : libsDir.list()) {
                resNames.add(path);
                jarfiles.add((new File(libsDir, path)).toURI().toURL());
            }
            File confDir = new File(contextDir, "conf");
            if (!confDir.exists()) {
                throw new IllegalStateException("web context:" + context + " dir not exist:" + confDir.getAbsolutePath());
            }
            resNames.add(confDir.getName());
            jarfiles.add(confDir.toURI().toURL());
            TISAppClassLoader contextCloassLoader = new TISAppClassLoader(context, this.parentLoader,
                    jarfiles.toArray(new URL[jarfiles.size()]));
            logger.info("context:" + context + " start with customer classLoader,resCount:" + jarfiles.size() + "," + "enums:" + String.join(",", resNames));
            webAppContext.setClassLoader(contextCloassLoader);
        } else {
            logger.info("context:" + context + " start with system classloader");
            WebAppClassLoader clazzLoader = new WebAppClassLoader(this.getClass().getClassLoader(), webAppContext);
            clazzLoader.addClassPath(resourceFactory.newResource(new File(contextDir, "target/classes").toPath()));
            webAppContext.setClassLoader(clazzLoader);
        }
        webAppContext.setDescriptor(new File(webappDir, TisApp.PATH_WEB_XML).getAbsolutePath());
        webAppContext.setDisplayName(context);
        webAppContext.setConfigurationDiscovered(true);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setThrowUnavailableOnStartupException(true);
        webAppContext.addServlet(CheckHealth.class, "/check_health");

        this.addContext(webAppContext);
    }

    public File getWebapp(File contextDir) {
        return new File(contextDir, "webapp");
    }

    private void init() {
        if (validateContextHandler()) {
            throw new IllegalStateException("handlers can not small than 1");
        }
        server = new Server(new QueuedThreadPool(450));

        HttpConfiguration configuration = new HttpConfiguration();
        HttpConnectionFactory h1 = new HttpConnectionFactory(configuration);
        HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(configuration);
        ServerConnector connector = new ServerConnector(server, h1, h2c);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setStopAtShutdown(true);

        Handler.Sequence sequence = new Handler.Sequence(handlerList);
        server.setHandler(sequence);
    }

    public boolean validateContextHandler() {
        return contextAddCount.get() < 1 || this.handlerList.size() < 1;
    }

    public interface IWebAppContextSetter {

        void process(WebAppContext context);
    }

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
    }

    // ------------------------------------------------------------------------------------------------
    public void start() throws Exception {
        start(true);
    }

    private static final String KEY_DATA_DIR = "data.dir";

    private static File getDataDir() {
        File dir = new File(System.getProperty(KEY_DATA_DIR, "/opt/data/tis"));
        if (!(dir.isDirectory() && dir.exists())) {
            throw new IllegalStateException("dir:" + dir.getAbsolutePath() + " is invalid DATA DIR");
        }
        return dir;
    }

    private void start(boolean waitForSolr) throws Exception {
        this.init();
        if (!server.isRunning()) {
            server.start();
            if (this.webAppContextCollector != null) {
                for (IWebAppContextCollector c : this.webAppContextCollector) {
                    c.afterLaunchContext();
                }
            }
            server.join();
        }
    }

    private void stop() throws Exception {
        if (server.isRunning()) {
            server.stop();
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
