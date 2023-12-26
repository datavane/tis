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

package com.qlangtech.tis.datax.job;

/**
 * powerjob 在启动云服务资源过程中，由于关键路径上的资源没有能够正常启动，导致启动失败
 * <ol>
 * <li> server启动之后注册新app过程，由于新应用已经存在，但是密码不正确，导致失败 IRegisterApp</li>
 * <li> 由于cpu、内存资源不够导致对应资源无法正常启动</li>
 * </ol>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/18
 */
public class PowerjobOrchestrateException extends Exception {
    public PowerjobOrchestrateException(String message) {
        super(message);
    }
}
