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
package com.qlangtech.tis.extension.init;

import com.qlangtech.tis.util.exec.DaemonThreadFactory;
import org.jvnet.hudson.reactor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executes the {@link Reactor} for the purpose of bootup.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class InitReactorRunner {

    public void run(Reactor reactor) throws InterruptedException, ReactorException, IOException {
        reactor.addAll(InitMilestone.ordering().discoverTasks(reactor));
        ExecutorService es;
        // if (Jenkins.PARALLEL_LOAD)
        // es = new ThreadPoolExecutor(
        // 2, 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory());
        // else
        es = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        try {
            reactor.execute(es, buildReactorListener());
        } finally {
            // upon a successful return the executor queue should be empty. Upon an exception, we want to cancel all pending tasks
            es.shutdownNow();
        }
    }

    /**
     * Aggregates all the listeners into one and returns it.
     *
     * <p>
     * At this point plugins are not loaded yet, so we fall back to the META-INF/services look up to discover implementations.
     * As such there's no way for plugins to participate into this process.
     */
    private ReactorListener buildReactorListener() throws IOException {
        // (List) Service.loadInstances(Thread.currentThread().getContextClassLoader(), InitReactorListener.class);
        List<ReactorListener> r = new ArrayList<>();
        r.add(new ReactorListener() {

            // final Level level = Level.parse( Configuration.getStringConfigParameter("initLogLevel", "FINE") );
            public void onTaskStarted(Task t) {
                LOGGER.debug("Started " + t.getDisplayName());
            }

            public void onTaskCompleted(Task t) {
                LOGGER.debug("Completed " + t.getDisplayName());
            }

            public void onTaskFailed(Task t, Throwable err, boolean fatal) {
                LOGGER.error("Failed " + t.getDisplayName(), err);
            }

            public void onAttained(Milestone milestone) {
                // Level lv = level;
                String s = "Attained " + milestone.toString();
                if (milestone instanceof InitMilestone) {
                    // lv = Level.INFO; // noteworthy milestones --- at least while we debug problems further
                    onInitMilestoneAttained((InitMilestone) milestone);
                    s = milestone.toString();
                }
                LOGGER.info(s);
            }
        });
        return new ReactorListener.Aggregator(r);
    }

    /**
     * Called when the init milestone is attained.
     */
    protected void onInitMilestoneAttained(InitMilestone milestone) {
    }

    // private static final int TWICE_CPU_NUM = SystemProperties.getInteger(
    // InitReactorRunner.class.getName()+".concurrency",
    // Runtime.getRuntime().availableProcessors() * 2);
    private static final Logger LOGGER = LoggerFactory.getLogger(InitReactorRunner.class.getName());
}
