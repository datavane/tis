/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.cloud;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * ZK的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ITISCoordinator extends ICoordinator {

    /**
     * 是否应该连接Assemble日志收集服务，单元测试过程中需要返回false
     *
     * @return
     */
    boolean shallConnect2RemoteIncrStatusServer();

    List<String> getChildren(String zkPath, Watcher watcher, boolean b);

    void addOnReconnect(IOnReconnect onReconnect);

    byte[] getData(String s, Watcher o, Stat stat, boolean b);

    void create(String path, byte[] data, boolean persistent, boolean sequential);

    boolean exists(String path, boolean watch);

    public interface IOnReconnect {

        public void command();
    }
}
