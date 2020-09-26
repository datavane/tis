/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
