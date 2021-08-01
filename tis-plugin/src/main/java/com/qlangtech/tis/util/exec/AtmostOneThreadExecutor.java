/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.util.exec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class AtmostOneThreadExecutor extends AbstractExecutorService {
    private Thread worker;
    private final LinkedList<Runnable> q;
    private boolean shutdown;
    private final ThreadFactory factory;

    public AtmostOneThreadExecutor(ThreadFactory factory) {
        this.q = new LinkedList();
        this.factory = factory;
    }

    public AtmostOneThreadExecutor() {
        this(new DaemonThreadFactory());
    }

    public void shutdown() {
        synchronized (this.q) {
            this.shutdown = true;
            if (this.isAlive()) {
                this.worker.interrupt();
            }

        }
    }

    private boolean isAlive() {
        return this.worker != null && this.worker.isAlive();
    }

    @Nonnull
    public List<Runnable> shutdownNow() {
        synchronized (this.q) {
            this.shutdown = true;
            List<Runnable> r = new ArrayList(this.q);
            this.q.clear();
            return r;
        }
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public boolean isTerminated() {
        return this.shutdown && !this.isAlive();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (this.q) {
            long now = System.nanoTime();

            for (long end = now + unit.toNanos(timeout); this.isAlive() && end - now > 0L; now = System.nanoTime()) {
                this.q.wait(TimeUnit.NANOSECONDS.toMillis(end - now));
            }
        }

        return this.isTerminated();
    }

    public void execute(@Nonnull Runnable command) {
        synchronized (this.q) {
            if (this.isShutdown()) {
                throw new IllegalStateException("This executor has been shutdown.");
            } else {
                this.q.add(command);
                if (!this.isAlive()) {
                    this.worker = this.factory.newThread(new AtmostOneThreadExecutor.Worker());
                    this.worker.start();
                }

            }
        }
    }

    private class Worker implements Runnable {
        private Worker() {
        }

        public void run() {
            while (true) {
                Runnable task;
                synchronized (AtmostOneThreadExecutor.this.q) {
                    if (AtmostOneThreadExecutor.this.q.isEmpty()) {
                        AtmostOneThreadExecutor.this.worker = null;
                        return;
                    }

                    task = (Runnable) AtmostOneThreadExecutor.this.q.remove();
                }

                task.run();
            }
        }
    }
}
