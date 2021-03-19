/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.web.start;

import com.qlangtech.tis.web.start.JettyTISRunner.IWebAppContextSetter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class TisApp {

    public static final String KEY_ASSEMBLE_TASK_DIR = "assemble.task.dir";

    static {
        // System.setProperty("logback.ContextSelector", "com.qlangtech.tis.web.start.TISContextSelector");
        System.setProperty("logback.ContextSelector", "JNDI");
//${log.dir}/assemble/task
        System.setProperty(KEY_ASSEMBLE_TASK_DIR, System.getProperty("log.dir") + "/assemble/task");
    }

    public static final File getAssebleTaskDir() {
        return new File(System.getProperty(KEY_ASSEMBLE_TASK_DIR));
    }

    public static final String KEY_WEB_ROOT_DIR = "web.root.dir";

//    public static void setWebRootDir(File webRootDir) {
//        org.eclipse.jetty.client.api.Request.BeginListener b = new org.eclipse.jetty.client.api.Request.BeginListener() {
//
//            @Override
//            public void onBegin(org.eclipse.jetty.client.api.Request request) {
//                System.out.println("hahah");
//            }
//        };
//        b.onBegin(null);
//        if (!webRootDir.exists()) {
//            throw new IllegalStateException("root dir not exist:" + webRootDir.getAbsolutePath());
//        }
//        System.setProperty(KEY_WEB_ROOT_DIR, webRootDir.getAbsolutePath());
//    }

    private static Logger logger = LoggerFactory.getLogger(TisApp.class);

    private final JettyTISRunner jetty;

    public static void main(String[] args) throws Exception {
        // 启动应用使用本地8080端口
        TisApp tisApp = new TisApp(8080, (context) -> {
            context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            context.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
        });
        tisApp
        tisApp.start(args);
    }

    public TisApp(String servletContext, int port, IWebAppContextSetter contextSetter) throws Exception {
        super();
        this.jetty = new JettyTISRunner(servletContext, port, contextSetter);
    }

    public TisApp(int port, IWebAppContextSetter contextSetter) throws Exception {
        super();
        this.jetty = new JettyTISRunner(port, contextSetter);
        this.initContext();
    }

    static final String APP_CONSOLE = "root";

    static final String PATH_WEB_XML = "WEB-INF/web.xml";

    private void initContext() throws Exception {
        File root = getWebRootDir();
        logger.info("webapps context dir:{}", root.getAbsolutePath());
        File contextDir = null;
        for (String context : root.list()) {
            contextDir = new File(root, context);
            if (contextDir.isDirectory() && !"web-start".equals(context)) {
                if (APP_CONSOLE.equals(context)) {
                    continue;
                } else {
                    logger.info("load context:{}", context);
                    this.jetty.addContext(contextDir);
                }
            }
        }
        // '/' root 的handler必须要最后添加
        contextDir = new File(root, APP_CONSOLE);
        if (contextDir.exists() && contextDir.isDirectory()) {
            // root
            File webappFile = new File(this.jetty.getWebapp(contextDir), PATH_WEB_XML);
            if (!webappFile.exists()) {
                // 写入本地
                try (InputStream input = this.getClass().getResourceAsStream("/web.xml")) {
                    Objects.requireNonNull(input, "web.xml inputstram can not be null");
                    FileUtils.copyToFile(input, webappFile);
                }
            }
            this.jetty.addContext("/", contextDir, false);
        }

        if (this.jetty.validateContextHandler()) {
            throw new IllegalStateException("handlers can not small than 1,web rootDir:" + root.getAbsolutePath());
        }
    }

    private File getWebRootDir() {
        File root = new File(System.getProperty(KEY_WEB_ROOT_DIR, "."));
        if (!root.exists()) {
            throw new IllegalStateException("web.root.dir not exist:" + root.getAbsolutePath());
        }
        return root;
    }

    public TisApp(String servletContext, int port) throws Exception {
        this(servletContext, port, (r) -> {
        });
    }

    public void start(String[] args) throws Exception {
        if (TriggerStop.isStopCommand(args)) {
            int stopPort = Integer.parseInt(System.getProperty("STOP.PORT"));
            final String key = System.getProperty("STOP.KEY");
            TriggerStop.stop("127.0.0.1", stopPort, key, 5);
            return;
        }
        this.jetty.start();
    }
}
