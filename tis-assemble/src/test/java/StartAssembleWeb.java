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

import com.alibaba.datax.common.statistics.PerfTrace;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.web.start.TisApp;
import com.qlangtech.tis.web.start.TisAppLaunch;
import com.qlangtech.tis.web.start.TisSubModule;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-18 11:44
 */
public class StartAssembleWeb extends TestCase {


    static {
        // 执行TisApp的static执行块
        TisApp.setLogbackContextSelector();
        System.setProperty(NetUtils.TIS_PREFERRED_NETWORK_INTERFACE, "en0");
        System.setProperty(TisAppLaunch.KEY_LOG_DIR, "/opt/logs/tis");
        System.setProperty(Config.SYSTEM_KEY_LOGBACK_PATH_KEY, "logback-assemble.xml");
        CenterResource.setNotFetchFromCenterRepository();
    }

    public void testStart() throws Exception {

        System.out.println(this.getClass().getClassLoader().loadClass("com.alibaba.datax.common.statistics.PerfTrace"));
        PerfTrace.getInstance(false, -1111, -1111, 0, false);
        // System.setProperty(com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger.KEY_DELTA_STREM_DEBUG, "true");
        System.setProperty(DataxUtils.EXEC_TIMESTAMP, String.valueOf(TimeFormat.getCurrentTimeStamp()));
        String[] args = new String[]{};
        TisAppLaunch.setTest(true);
        TisApp app = new TisApp(TisSubModule.TIS_ASSEMBLE, (context) -> {
            context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            context.setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "true");
        });
        System.out.println("start");
        app.start(args);
    }


}
