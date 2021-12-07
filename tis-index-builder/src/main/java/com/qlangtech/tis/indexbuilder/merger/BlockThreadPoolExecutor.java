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
package com.qlangtech.tis.indexbuilder.merger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * waitingQueue 如果满的话，新提交的任务需要阻塞住
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年9月25日
 */
public class BlockThreadPoolExecutor extends ThreadPoolExecutor {

    private final BlockingQueue<Runnable> waitingQueue;

    private Object lock = new Object();

    private static final Logger logger = LoggerFactory.getLogger(BlockThreadPoolExecutor.class);

    public BlockThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.waitingQueue = workQueue;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (this.waitingQueue.size() < 2) {
            super.execute(command);
        } else {
            try {
                synchronized (lock) {
                    final long start = System.currentTimeMillis();
                    lock.wait();
                    logger.info("merge thread have wait for submit:{}ms", (System.currentTimeMillis() - start));
                    execute(command);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
