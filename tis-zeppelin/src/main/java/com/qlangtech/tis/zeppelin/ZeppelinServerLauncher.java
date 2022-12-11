package com.qlangtech.tis.zeppelin;

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

import com.qlangtech.tis.web.start.IWebAppContextCollector;
import com.qlangtech.tis.web.start.TisApp;
import com.qlangtech.tis.web.start.TisAppLaunch;
import com.qlangtech.tis.web.start.TisSubModule;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.notebook.repo.NotebookRepoSync;
import org.apache.zeppelin.server.ErrorData;
import org.apache.zeppelin.server.ImmediateErrorHandlerImpl;
import org.apache.zeppelin.server.ZeppelinServer;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-06 09:50
 **/
public class ZeppelinServerLauncher implements IWebAppContextCollector {
    private static final Logger LOG = LoggerFactory.getLogger(ZeppelinServerLauncher.class);
    final ZeppelinConfiguration conf = ZeppelinConfiguration.create("zeppelin-site.xml");
    private ImmediateErrorHandlerImpl handler = null;
    private ServiceLocator sharedServiceLocator;

    @Override
    public void launchContext(HandlerCollection contexts) throws IOException {
        ZeppelinServer.conf = conf;
        this.sharedServiceLocator = ServiceLocatorFactory.getInstance().create("shared-locator");
        ZeppelinServer.sharedServiceLocator = this.sharedServiceLocator;
        ServiceLocatorUtilities.enableImmediateScope(sharedServiceLocator);
        ServiceLocatorUtilities.addClasses(sharedServiceLocator,
                NotebookRepoSync.class,
                ImmediateErrorHandlerImpl.class);
        this.handler = sharedServiceLocator.getService(ImmediateErrorHandlerImpl.class);

        try {
            ZeppelinServer.addWebContext(conf, sharedServiceLocator, null, contexts, null);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void afterLaunchContext() throws IOException {
        LOG.info("Done, zeppelin server started");
        try {
            List<ErrorData> errorDatas = handler.waitForAtLeastOneConstructionError(5000);
            for (ErrorData errorData : errorDatas) {
                LOG.error("Error in Construction", errorData.getThrowable());
            }
            if (!errorDatas.isEmpty()) {
                LOG.error("{} error(s) while starting - Termination", errorDatas.size());
                System.exit(-1);
            }
        } catch (InterruptedException e) {
            // Many fast unit tests interrupt the Zeppelin server at this point
            LOG.error("Interrupt while waiting for construction errors - init shutdown", e);
            Thread t = ZeppelinServer.shutdown(conf);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) {
            }
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        ZeppelinServerLauncher zeppelinServer = new ZeppelinServerLauncher();

        TisAppLaunch.setTest(true);
        TisApp app = new TisApp(TisSubModule.ZEPPELIN, zeppelinServer);
        System.out.println("start");
        app.start(args);

    }

}
