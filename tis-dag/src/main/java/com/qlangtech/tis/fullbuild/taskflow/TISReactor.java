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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.IExecChainContext;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TISReactor {

    private static final Logger logger = LoggerFactory.getLogger(TISReactor.class);
    private final Map<String, TaskAndMilestone> taskMap;

    private final IExecChainContext execContext;

    public TISReactor(IExecChainContext execContext, Map<String, TaskAndMilestone> taskMap) {
        super();
        this.taskMap = taskMap;
        this.execContext = execContext;
    }

    public void execute(Executor executor, Reactor s, ReactorListener... addedListeners) throws Exception {
        execContext.rebindLoggingMDCParams();
        //StringWriter sw = new StringWriter();
        // System.out.println("----");
        ReactorListener listener = null;

        if (addedListeners.length > 0) {
            List<ReactorListener> listeners = Arrays.stream(addedListeners).collect(Collectors.toList());
            //listeners.add(0, listener);
            listener = new ReactorListener.Aggregator(listeners);
        } else {
            throw new IllegalStateException("param addedListeners length can not small than 1");
        }
        try {
            s.execute(executor, listener);
        } catch (InterruptedException e) {
            logger.warn("task:" + execContext.getTaskId() + " has been cancel", e);
        }
        //return sw.toString();
    }

    Pattern PatternNode = Pattern.compile("[\\S]+");

    public Reactor buildSession(CharSequence spec) throws Exception {
        Collection<TaskImpl> tasks = new ArrayList<>();

        Matcher matcher = PatternNode.matcher(spec);
        while (matcher.find()) {

            tasks.add(new TaskImpl(matcher.group(), taskMap));
        }

//        for (String node : spec.split(" ")) {
//
//        }
        return new Reactor(TaskBuilder.fromTasks(tasks));
    }

    public static class TaskImpl implements Task {

        final Collection<Milestone> requires;

        final Collection<Milestone> attains;

        final Map<String, TaskAndMilestone> taskMap;

        private final DataflowTask work;

        private final String id;

        TaskImpl(String idd, Map<String, TaskAndMilestone> taskMap) {
            String[] tokens = idd.split("->");
            this.id = Objects.requireNonNull(tokens[1], "relevant task id is null in taskMap,param idd:" + idd);
            this.work = Objects.requireNonNull(taskMap.get(this.id), "nodeId:" + this.id
                    + " can not find Task in " + String.join(",", taskMap.keySet())).task;

            this.taskMap = taskMap;
            // tricky handling necessary due to inconsistency in how split works
            this.requires = adapt(tokens[0].length() == 0 ? Collections.emptyList() : Arrays.asList(tokens[0].split(",")));
            // this.requires = adapt(tokens[0].length() == 0 ? Collections.emptyList() :
            // Arrays.asList(this.id));
            this.attains = adapt(tokens.length < 3 ? Arrays.asList(this.id) : Arrays.asList(tokens[2].split(",")));
        }

        public FullbuildPhase getPhase() {
            return this.work.phase();
        }

        public String getIdentityName() {
            return work.getIdentityName();
        }

        private Collection<Milestone> adapt(List<String> strings) {
            List<Milestone> r = new ArrayList<>();
            TaskAndMilestone w = null;
            for (String s : strings) {
                w = taskMap.get(s);
                if (w == null) {
                    throw new IllegalStateException("relevant task:" + s + " is null in taskMap");
                }
                r.add(w.milestone);
            }
            return r;
        }

        public Collection<Milestone> requires() {
            return requires;
        }

        public Collection<Milestone> attains() {
            return attains;
        }

        public String getDisplayName() {
            //  return this.id;
            return work.getIdentityName();
        }

        @Override
        public void run(Reactor reactor) throws Exception {
            work.run();
        }

        public boolean failureIsFatal() {
            return false;
        }
    }
}
