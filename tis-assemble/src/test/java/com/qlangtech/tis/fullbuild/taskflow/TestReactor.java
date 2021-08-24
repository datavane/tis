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
package com.qlangtech.tis.fullbuild.taskflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.fullbuild.taskflow.TISReactor.TaskAndMilestone;
import com.qlangtech.tis.order.center.TestIndexSwapTaskflowLauncher;
import junit.framework.TestCase;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorListener;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestReactor extends TestCase {

    public static final String MILESTONE_PREFIX = "milestone_";

    private static final Map<String, TaskAndMilestone> taskMap = Maps.newHashMap();

    static Map<String, Boolean> successToken = Maps.newHashMap();

    static {
        taskMap.put("a", new TaskAndMilestone(new TestDataflowTask("a") {

            @Override
            public void run() throws Exception {
                Thread.sleep(5000);
                System.out.println("task execute " + id);
                successToken.put(id, true);
            }
        }));
        taskMap.put("b", new TaskAndMilestone(new TestDataflowTask("b") {

            @Override
            public void run() throws Exception {
                System.out.println("task execute " + id);
                successToken.put(id, true);
            }
        }));
        taskMap.put("c", new TaskAndMilestone(new TestDataflowTask("c") {

            @Override
            public void run() throws Exception {
                Assert.assertTrue(successToken.get("a"));
                Assert.assertTrue(successToken.get("b"));
                successToken.put(id, true);
                System.out.println("task execute " + id);
            }
        }));
    }

    private abstract static class TestDataflowTask extends DataflowTask {

        public TestDataflowTask(String id) {
            super(id);
        }

        @Override
        protected Map<String, Boolean> getTaskWorkStatus() {
            return Collections.emptyMap();
        }

        @Override
        public FullbuildPhase phase() {
            return FullbuildPhase.FullDump;
        }

        @Override
        public String getIdentityName() {
            return this.id;
        }
    }

    public void testSequentialOrdering() throws Exception {
        DefaultChainContext chainContext = TestIndexSwapTaskflowLauncher.createDumpAndJoinChainContext();
        TISReactor tisReactor = new TISReactor(chainContext, taskMap);
        Reactor s = tisReactor.buildSession("->a ->b a,b->c");
        // Reactor s = buildSession("->t1->m1 m1->t2->m2 m2->t3->", (session, id) ->
        // System.out.println(id));
        assertEquals(3, s.size());
        String sw = tisReactor.execute(Executors.newCachedThreadPool(), s, new ReactorListener() {
        });
        System.out.println(sw);
        System.out.println("last");
        for (String taskname : Lists.newArrayList("a", "b", "c")) {
            Assert.assertNotNull("taskname:" + taskname + " shall have execute", successToken.get(taskname));
            Assert.assertTrue("taskname:" + taskname + " shall have execute", successToken.get(taskname));
        }
        // assertEqualsIgnoreNewlineStyle(
        // "Started t1\nEnded t1\nAttained m1\nStarted t2\nEnded t2\nAttained
        // m2\nStarted t3\nEnded t3\n", sw);
    }

    private static void assertEqualsIgnoreNewlineStyle(String s1, String s2) {
        assertEquals(normalizeLineEnds(s1), normalizeLineEnds(s2));
    }

    private static String normalizeLineEnds(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("\r\n", "\n").replace('\r', '\n');
    }
}
