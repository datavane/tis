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

import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.TimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-29 10:01
 **/
public abstract class JobResName<T> extends TargetResName {
    // private final EXEC jobExec;
    private static final Logger logger = LoggerFactory.getLogger(JobResName.class);

    public JobResName(String name //, EXEC jobExec
    ) {
        super(name);
        // this.jobExec = jobExec;
    }


    /**
     * 创建默认
     *
     * @param jobName
     * @param exec
     * @param <T>
     * @return
     */
    public static <T> SubJobResName<T> createSubJob(String jobName, ThrowableConsumer<T> exec) {
        final SubJobResName<T> created
                = new SubJobResName<T>(jobName, new SubJobExec<T>() {
            @Override
            public void accept(T dto) throws Exception {
                exec.accept(dto);
            }
        }) {
            @Override
            protected String getResourceType() {
                return jobName;
            }
        };
        return created;
    }


    public final void execSubJob(T t) throws Exception {
        SSERunnable sse = SSERunnable.getLocal();
        boolean success = false;
        try {
            sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "〇〇 start to publish " + this.getResourceType() + "'" + this.getName() + "'");
            this.execute(sse, t);
            success = true;
            sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "✔✔ successful to publish " + this.getResourceType() + "'" + this.getName() + "'");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (!success) {
                sse.info(this.getName(), TimeFormat.getCurrentTimeStamp(), "✕✕ faild to publish " + this.getResourceType() + "'" + this.getName() + "'");
            }
            sse.writeComplete(this, success);
        }
    }

    protected abstract void execute(SSERunnable sse, T t) throws Exception;
//        if (jobExec instanceof SubJobExec) {
//            ((OwnerJobExec) jobExec).accept(t);
//        } else {
//            throw new UnsupportedOperationException("jobExec:" + jobExec.getClass());
//        }
    //}

    protected abstract String getResourceType();


    public interface OwnerJobExec<T, RESULT> {
        public RESULT accept(T t) throws Exception;
    }

    public interface SubJobExec<T> {
        public void accept(T t) throws Exception;
    }


    @FunctionalInterface
    public interface ThrowableConsumer<T> {
        void accept(T t) throws Exception;
    }
}
