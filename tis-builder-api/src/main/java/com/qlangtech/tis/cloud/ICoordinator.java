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
package com.qlangtech.tis.cloud;

/**
 * 标示是zookeeper接口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-03 10:38
 */
public interface ICoordinator {
    /**
     * 执行DataX任务时不需要连接Grpc服务端？
     */
    String KEY_DISABLE_GRPC_REMOTE_SERVER_CONNECT = "disableGrpcRemoteServerConnect";

    /**
     * default is com.qlangtech.tis.TisZkClient
     * @param <T>
     * @return
     */
    <T> T unwrap();
}
