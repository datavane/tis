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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.impl.TrackableExecuteInterceptor;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import org.jvnet.hudson.reactor.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TISReactor {

    public static final String MILESTONE_PREFIX = "milestone_";

    private final Map<String, TaskAndMilestone> taskMap;

    private final IExecChainContext execContext;

    public TISReactor(IExecChainContext execContext, Map<String, TaskAndMilestone> taskMap) {
        super();
        this.taskMap = taskMap;
        this.execContext = execContext;
    }

    public String execute(ExecutorService executor, Reactor s, ReactorListener... addedListeners) throws Exception {
        StringWriter sw = new StringWriter();
        // System.out.println("----");
        final PrintWriter w = new PrintWriter(sw, true);
        ReactorListener listener = new ReactorListener() {
            // TODO: Does it really needs handlers to be synchronized?
            @Override
            public synchronized void onTaskStarted(Task t) {
                w.println("Started " + t.getDisplayName());
            }

            @Override
            public synchronized void onTaskCompleted(Task t) {
                w.println("Ended " + t.getDisplayName());
                processTaskResult((TaskImpl) t, new ITaskResultProcessor() {
                    @Override
                    public void process(DumpPhaseStatus dumpPhase, TaskImpl task) {
                    }

                    @Override
                    public void process(JoinPhaseStatus joinPhase, TaskImpl task) {
                    }
                });
            }

            @Override
            public synchronized void onTaskFailed(Task t, Throwable err, boolean fatal) {
                w.println("Failed " + t.getDisplayName() + " with " + err);
                processTaskResult((TaskImpl) t, new ITaskResultProcessor() {

                    @Override
                    public void process(DumpPhaseStatus dumpPhase, TaskImpl task) {
                        IncrStatusUmbilicalProtocolImpl statReceiver = IncrStatusUmbilicalProtocolImpl.getInstance();
                        statReceiver.reportDumpTableStatusError(execContext.getTaskId(), task.getIdentityName());
                    }

                    @Override
                    public void process(JoinPhaseStatus joinPhase, TaskImpl task) {
                        JoinPhaseStatus.JoinTaskStatus stat = joinPhase.getTaskStatus(task.getIdentityName());
                        // statReceiver.reportBuildIndexStatErr(execContext.getTaskId(),task.getIdentityName());
                        stat.setWaiting(false);
                        stat.setFaild(true);
                        stat.setComplete(true);
                    }
                });
            }

            @Override
            public synchronized void onAttained(Milestone milestone) {
                w.println("Attained " + milestone);
            }
        };
        if (addedListeners.length > 0) {
            List<ReactorListener> listeners = Arrays.stream(addedListeners).collect(Collectors.toList());
            listeners.add(0, listener);
            listener = new ReactorListener.Aggregator(listeners);
        }
        s.execute(executor, listener);
        return sw.toString();
    }

    private void processTaskResult(TaskImpl t, ITaskResultProcessor resultProcessor) {
        TaskImpl task = t;
        PhaseStatusCollection pstats = TrackableExecuteInterceptor.taskPhaseReference.get(execContext.getTaskId());
        if (pstats != null) {
            switch(task.getPhase()) {
                case FullDump:
                    // pstats.getDumpPhase()
                    // IncrStatusUmbilicalProtocolImpl statReceiver = IncrStatusUmbilicalProtocolImpl.getInstance();
                    // statReceiver.reportDumpTableStatusError(execContext.getTaskId(), task.getIdentityName());
                    pstats.getDumpPhase().isComplete();
                    resultProcessor.process(pstats.getDumpPhase(), task);
                    return;
                case JOIN:
                    // JoinPhaseStatus.JoinTaskStatus stat
                    // = pstats.getJoinPhase().getTaskStatus(task.getIdentityName());
                    // //statReceiver.reportBuildIndexStatErr(execContext.getTaskId(),task.getIdentityName());
                    // stat.setWaiting(false);
                    // stat.setFaild(true);
                    // stat.setComplete(true);
                    pstats.getJoinPhase().isComplete();
                    resultProcessor.process(pstats.getJoinPhase(), task);
                    return;
                default:
                    throw new IllegalStateException("taskphase:" + task.getPhase() + " is illegal");
            }
        }
    }

    interface ITaskResultProcessor {

        void process(DumpPhaseStatus dumpPhase, TaskImpl task);

        void process(JoinPhaseStatus joinPhase, TaskImpl task);
    }

    public Reactor buildSession(String spec) throws Exception {
        Collection<TaskImpl> tasks = new ArrayList<>();
        for (String node : spec.split(" ")) {
            tasks.add(new TaskImpl(node, taskMap));
        }
        return new Reactor(TaskBuilder.fromTasks(tasks));
    }

    public static class TaskAndMilestone {

        private final DataflowTask task;

        private final MilestoneImpl milestone;

        public TaskAndMilestone(DataflowTask task) {
            super();
            this.task = task;
            this.milestone = new MilestoneImpl(MILESTONE_PREFIX + task.id);
        }
    }

    class TaskImpl implements Task {

        final Collection<Milestone> requires;

        final Collection<Milestone> attains;

        final Map<String, TaskAndMilestone> taskMap;

        private final DataflowTask work;

        private final String id;

        TaskImpl(String idd, Map<String, TaskAndMilestone> taskMap) {
            String[] tokens = idd.split("->");
            this.id = tokens[1];
            this.work = taskMap.get(this.id).task;
            if (this.id == null) {
                throw new IllegalStateException("relevant task id is null in taskMap");
            }
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
            return this.id;
        }

        @Override
        public void run(Reactor reactor) throws Exception {
            execContext.rebindLoggingMDCParams();
            work.run();
        }

        public boolean failureIsFatal() {
            return true;
        }
    }
}
