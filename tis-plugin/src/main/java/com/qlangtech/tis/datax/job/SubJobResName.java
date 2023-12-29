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

import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.TimeFormat;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-29 10:01
 **/
public abstract class SubJobResName<T> extends TargetResName {
    private final SubJobExec<T> subJobExec;

    public SubJobResName(String name, SubJobExec<T> subJobExec) {
        super(name);
        this.subJobExec = subJobExec;
    }

    public final void execSubJob(T t) throws Exception {
        SSERunnable sse = SSERunnable.getLocal();
        boolean success = false;
        try {
            sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "start to publish " + this.getResourceType() + "'" + this.getName() + "'");
            subJobExec.accept(t);
            success = true;
            sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "successful to publish " + this.getResourceType() + "'" + this.getName() + "'");
        } finally {
            if (!success) {
                sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "faild to publish " + this.getResourceType() + "'" + this.getName() + "'");
            }
            sse.writeComplete(this, success);
        }
    }

    protected abstract String getResourceType();
    public interface SubJobExec<T> {
        public void accept(T t) throws Exception;
    }
}
