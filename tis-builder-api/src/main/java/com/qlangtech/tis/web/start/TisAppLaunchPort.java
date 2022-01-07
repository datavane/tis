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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-01-07 17:27
 **/
public class TisAppLaunchPort {
    public static final String KEY_TIS_LAUNCH_PORT = "tis.launch.port";
    private final int launchPort;

    private static final TisAppLaunchPort instance = new TisAppLaunchPort();

    private TisAppLaunchPort() {
        this.launchPort = Integer.parseInt(System.getProperty(KEY_TIS_LAUNCH_PORT, "8080"));
    }


    public static int getPort() {
        return instance.launchPort;
    }
}
