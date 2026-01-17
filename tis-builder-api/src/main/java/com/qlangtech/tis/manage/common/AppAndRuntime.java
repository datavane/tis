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

package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class AppAndRuntime {

    private static ThreadLocal<AppAndRuntime> appAndRuntimeLocal = new ThreadLocal<AppAndRuntime>();
    private DataXName appName;

    private RunEnvironment runtime;

    public static Consumer<AppAndRuntime> newAppAndRuntimeConsumer;

    public static AppAndRuntime getAppAndRuntime() {
        return appAndRuntimeLocal.get();
    }

    public static void setAppAndRuntime(AppAndRuntime appAndRuntime) {
        appAndRuntimeLocal.set(appAndRuntime);
        if (newAppAndRuntimeConsumer != null) {
            newAppAndRuntimeConsumer.accept(appAndRuntime);
        }
    }

    private final Map<String, String[]> httpParams;

    public AppAndRuntime(Map<String, String[]> httpParams) {
        this.httpParams = httpParams;
    }

    public String[] getHttpParams(String key) {
        return this.httpParams.get(key);
    }

    public String getHttpParam(String key) {
        String[] vals = getHttpParams(key);
        if (vals != null) {
            for (String val : vals) {
                return val;
            }
        }
        return null;
    }

    public void setAppName(DataXName appName) {
        this.appName = appName;
    }

    public void setRuntime(RunEnvironment runtime) {
        this.runtime = runtime;
    }

    public DataXName getAppName() {
        return appName;
    }

    public RunEnvironment getRuntime() {
        return runtime;
    }
}
