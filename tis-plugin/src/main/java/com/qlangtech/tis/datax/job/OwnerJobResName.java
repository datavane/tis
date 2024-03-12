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

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-11 13:18
 **/
public abstract class OwnerJobResName<T, RESULT> extends JobResName<T> {

    private final OwnerJobExec<T, RESULT> jobExec;

    public OwnerJobResName(String name, OwnerJobExec<T, RESULT> jobExec) {
        super(name);
        this.jobExec = jobExec;
    }

    @Override
    protected final void execute(SSERunnable sse, T t) throws Exception {
        RESULT result = jobExec.accept(t);
        if (result != null) {
            sse.setContextAttr(SSEExecuteOwner.class, new SSEExecuteOwner(result));
        } else {
            sse.cleanContextAttr(SSEExecuteOwner.class);
        }
    }

    public static SSEExecuteOwner getSSEExecuteOwner() {
        SSERunnable sse = SSERunnable.getLocal();
        SSEExecuteOwner contextAttr = Objects.requireNonNull(sse.getContextAttr(SSEExecuteOwner.class)
                , "SSEExecuteOwner must be present");
        return contextAttr;
    }

    public static class SSEExecuteOwner {
        public final Object owner;

        public SSEExecuteOwner(Object owner) {
            this.owner = owner;
        }
    }


}
