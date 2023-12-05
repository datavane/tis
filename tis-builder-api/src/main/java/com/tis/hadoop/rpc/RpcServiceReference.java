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
package com.tis.hadoop.rpc;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-03 11:16
 */
public class RpcServiceReference {
    private final AtomicReference<ITISRpcService> ref;
    private final Runnable connect;



    public RpcServiceReference(AtomicReference<ITISRpcService> ref, Runnable connect) {
        this.ref = ref;
        this.connect = connect;
    }

    public AtomicReference<ITISRpcService> getRef() {
        return ref;
    }

    /**
     * when throw an error of  io.grpc.StatusRuntimeException,then shall execute reConnect()
     */
    public void reConnect() {
        synchronized (connect) {
            this.connect.run();
            // 唤醒 Runnable，连接继续
            this.connect.notifyAll();
        }
    }

    /**
     * default instance is type of AssembleSvcCompsite
     *
     * @param <T>
     * @return
     */
    public <T extends ITISRpcService> T get() {
        T t = (T) ref.get();
        return t;
    }
}
