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

package com.qlangtech.tis.datax.job;

/**
 * 编排任务在任务在执行过程中出错，需要中断编排任务
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-29 10:01
 **/
public class JobOrchestrateException extends Exception {
    public JobOrchestrateException(String message) {
        super(message);
    }

    public JobOrchestrateException(Throwable cause) {
        super(cause);
    }
}
