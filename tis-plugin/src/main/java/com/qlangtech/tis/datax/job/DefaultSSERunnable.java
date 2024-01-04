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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;
import com.qlangtech.tis.trigger.socket.InfoType;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-24 08:27
 **/
public class DefaultSSERunnable implements SSERunnable {


    private final PrintWriter httpClientWriter;
    private final Runnable runnable;
    private Optional<ServerLaunchToken> launchToken = Optional.empty();
    private final List<ExecuteStep> executeSteps;

    // boolean inAttach2RunningProcessor = false;
//  public DefaultSSERunnable(HttpServletResponse response, DataXJobWorker dataxJobWorker, Runnable runnable) {
//    this(response, dataxJobWorker, dataxJobWorker.getExecuteSteps(), runnable);
//  }

//  public DefaultSSERunnable(HttpServletResponse response, DataXJobWorker dataxJobWorker, List<ExecuteStep> executeSteps, Runnable runnable) {
//
//      this(response.getWriter(),dataxJobWorker);
//  }

    public static k8SLaunching execute(DefaultSSERunnable launchProcess, boolean inService
            , ServerLaunchToken launchToken, Runnable runnable) {
        // ServerLaunchToken launchToken = dataxJobWorker.getProcessTokenFile();


        k8SLaunching k8SLaunching = launchProcess.hasLaunchingToken(launchProcess.executeSteps, launchToken);


        launchProcess.writeExecuteSteps(k8SLaunching.getExecuteSteps());
        if (inService) {
            // throw new IllegalStateException("dataxJobWorker is in serivce ,can not launch repeat");
            for (SubJobLog subJobLog : k8SLaunching.getLogs()) {
                //public void writeMessage(InfoType logLevel, long timestamp, String msg)
                launchProcess.writeHistoryLog(subJobLog);
            }
            return k8SLaunching;
        }
        if (k8SLaunching.isLaunching()) {

            if (k8SLaunching.isFaild()) {
//        k8SLaunching.getExecuteSteps();
//        k8SLaunching.getMilestones();

                // launchProcess.writeExecuteSteps(k8SLaunching.getExecuteSteps());

                for (SubJobLog subJobLog : k8SLaunching.getLogs()) {
                    //public void writeMessage(InfoType logLevel, long timestamp, String msg)
                    launchProcess.writeHistoryLog(subJobLog);
                }
                return k8SLaunching;
            } else if (launchToken.hasWriteOwner()) {

                // 说明启动流程正在执行，attach 到执行流程
                launchToken.addObserver(k8SLaunching);
                //  k8SLaunching.attach2RunningProcessor();
                return k8SLaunching;
            }

        }

        try {
            launchProcess.setLaunchToken(launchToken);
            launchProcess.startLaunch();

            runnable.run();
            // dataxJobWorker.executeLaunchService(launchProcess);
            return null;
        } finally {
            launchProcess.terminate();
        }
    }

    public DefaultSSERunnable(PrintWriter clientWriter, DataXJobWorker dataxJobWorker, List<ExecuteStep> executeSteps, Runnable runnable) {
        SSERunnable.setLocalThread(this);
        this.httpClientWriter = clientWriter;
        this.runnable = runnable;

        //  this.launchToken = dataxJobWorker.getServerLaunchTokenFile();
        this.executeSteps = executeSteps;// dataxJobWorker.getExecuteSteps();
        if (CollectionUtils.isEmpty(this.executeSteps)) {
            throw new IllegalStateException("executeSteps can not be empty,dataxJobWorker:" + dataxJobWorker.getClass().getName());
        }
    }

    public k8SLaunching hasLaunchingToken(List<ExecuteStep> executeSteps, ServerLaunchToken launchToken) {
        ServerLaunchLog launchWALLog = launchToken.buildWALLog(executeSteps);
        k8SLaunching k8SLaunching = new k8SLaunching(launchWALLog);
        // k8SLaunching.setExecuteSteps(executeSteps);
        if (!k8SLaunching.launchWALLog.isLaunchingTokenExist()) {
            return k8SLaunching;
        }

        //   try {
        //  LineIterator lines = FileUtils.lineIterator(launchToken.getLaunchingToken(), TisUTF8.getName());
//      String[] line = null;
//      SSEEventType event;
//      String data;

//            Map<String, SubJobMilestone> milestones = Maps.newHashMap();
//
//            LoopQueue<SubJobLog> loggerQueue = new LoopQueue<>(new SubJobLog[100]);
//            //   final JSONArray[] subJobExecStepsJSONArray = new JSONArray[1];
//            while (lines.hasNext()) {
//                processLaunchWALLine(lines.nextLine(), new LaunchWALLineVisitor() {
//
//                    @Override
//                    public void process(SubJobLog jobLog) {
//                        loggerQueue.write(jobLog);
//                    }
//
//                    @Override
//                    public void process(SubJobMilestone stone) {
//                        milestones.put(stone.getName(), stone);
//                    }
//                });
//            }
//
//            k8SLaunching.setMilestones(Lists.newArrayList(milestones.values()));
//            k8SLaunching.setExecuteSteps(SubJobMilestone.readSubJobJSONArray(executeSteps, (subJobName) -> milestones.get(subJobName)));
//            k8SLaunching.setLogs(loggerQueue.readBuffer());
//        line = StringUtils.split(lines.nextLine(), splitChar);
//        event = SSEEventType.parse(line[0]);
//        data = line[1];
//


//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        return k8SLaunching;
    }


    /**
     * 执行终止
     */
    public void terminate() {
        try {
            ServerLaunchToken launchToken = this.launchToken.get();
            // 结束写入
            launchToken.setWriteOwner(null);
            launchToken.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//    //.ifPresent((lt) -> {
//
//        lt.close();

//    });
    }

    public void setLaunchToken(ServerLaunchToken launchToken) {
        this.launchToken = Optional.of(launchToken);
        launchToken.setWriteOwner(this);
    }

    interface LaunchWALLineVisitor {
        public void process(SubJobLog jobLog);

        public void process(SubJobMilestone stone);

        // public void process(JSONArray subJobExecStepsJSONArray);
    }

    /**
     * @see ServerLaunchToken as Observable
     */
    public class k8SLaunching implements Observer {
//        private final boolean isLaunchingTokenExist;
//
//        private List<SubJobMilestone> milestones;
//        private SubJobLog[] logs;
//        private List<ExecuteStep> executeSteps;

        private final ServerLaunchLog launchWALLog;
        private final List<ExecuteStep> executeSteps;

        public k8SLaunching(ServerLaunchLog launchWALLog) {
            this.launchWALLog = launchWALLog;
            this.executeSteps = launchWALLog.getExecuteSteps();
        }


        /**
         * 是否正在执行启动执行任务
         *
         * @return
         */
        public boolean isLaunching() {
            boolean complete = true;
            if (CollectionUtils.isEmpty(this.executeSteps)) {
                throw new IllegalStateException("executeSteps can not be empty");
            }
            for (ExecuteStep step : this.executeSteps) {
                if (!step.isComplete()) {
                    complete = false;
                }
            }

            return launchWALLog.isLaunchingTokenExist() && !complete;
        }

        public List<SubJobMilestone> getMilestones() {
            return launchWALLog.getMilestones();
        }

//        public void setMilestones(List<SubJobMilestone> milestones) {
//            this.milestones = milestones;
//        }

        public SubJobLog[] getLogs() {
            return this.launchWALLog.getLogs();
        }


        public List<ExecuteStep> getExecuteSteps() {
            return this.executeSteps;
        }


        /**
         * 启动是否失败了
         *
         * @return
         */
        public boolean isFaild() {
            return this.launchWALLog.isFaild();
//            for (SubJobMilestone subJobMilestone : getMilestones()) {
//                if (subJobMilestone.isFaild()) {
//                    return true;
//                }
//            }
//            return false;
        }

        /**
         * 说明启动流程正在执行，attach 到执行流程
         */
//    public void attach2RunningProcessor(ServerLaunchToken launchToken) {
//      // inAttach2RunningProcessor = true;
//      launchToken.addObserver(this);
//      //  launchToken = Optional.empty();
////      .ifPresent((lt) -> {
////        lt.addObserver(this);
////      });
//    }
        @Override
        public void update(Observable o, Object arg) {
            String line = (String) arg;

            ServerLaunchToken.processLaunchWALLine(line, new LaunchWALLineVisitor() {
                @Override
                public void process(SubJobLog jobLog) {
                    writeHistoryLog(jobLog);
                }

                @Override
                public void process(SubJobMilestone stone) {
                    writeComplete(new TargetResName(stone.getName()), stone.isSuccess());
                }

//        @Override
//        public void process(JSONArray subJobExecStepsJSONArray) {
//          throw new UnsupportedOperationException();
//        }
            });
        }
    }

    public void startLaunch() {
        // this.writeExecuteSteps(this.executeSteps);
        this.launchToken.get().touchLaunchingToken();
    }

    public void writeExecuteSteps(List<ExecuteStep> executeSteps) {

        JSONArray steps = SubJobMilestone.createSubJobJSONArray(executeSteps);

//     new JSONArray();
//    JSONObject step = null;
//    for (ExecuteStep s : executeSteps) {
//      step = SubJobMilestone.createMilestoneJson(s.getName()
//        , Optional.ofNullable(s.getDescribe()), false, null);
//      steps.add(step);
//    }
        writeMessage(SSEEventType.TASK_EXECUTE_STEPS, JsonUtil.toString(steps, false));
    }


    /**
     * 执行完成
     *
     * @param subJob
     * @param success
     */
    @Override
    public void writeComplete(TargetResName subJob, boolean success) {

        JSONObject m = SubJobMilestone.createMilestoneJson(subJob.getName(), Optional.empty(), true, success);
        writeMessage(SSEEventType.TASK_MILESTONE, JsonUtil.toString(m, false));
    }

    public void info(String serviceName, long timestamp, String msg) {
        writeMessage(InfoType.INFO, timestamp, msg);
    }

    public void writeHistoryLog(SubJobLog subJobLog) {
        if (subJobLog == null) {
            return;
        }
        writeMessage(subJobLog.getLogLevel(), subJobLog.getTimestamp(), subJobLog.getMsg());
    }

    private void writeMessage(InfoType logLevel, long timestamp, String msg) {
        JSONObject m = SubJobLog.createSubJobLog(logLevel, timestamp, msg);

        this.writeMessage(SSEEventType.TASK_LOG, JsonUtil.toString(m, false));
//    writer.println("event: message");
//    writer.println("data: " + JsonUtil.toString(m, false));
//    writer.println(); // note the additional line being written to the stream..
    }

    public static class SubJobLog {
        private final InfoType logLevel;
        private final long timestamp;
        private final String msg;

        private SubJobLog(InfoType logLevel, long timestamp, String msg) {
            this.logLevel = logLevel;
            this.timestamp = timestamp;
            this.msg = msg;
        }

        public static JSONObject createSubJobLog(InfoType logLevel, long timestamp, String msg) {
            JSONObject m = new JSONObject();
            m.put("level", logLevel.getToken());
            m.put("time", timestamp);
            m.put("msg", msg);
            return m;
        }

        public static SubJobLog readSubJobLog(String data) {
            JSONObject j = JSONObject.parseObject(data);
            return new SubJobLog(InfoType.getType(j.getString("level"))
                    , j.getLongValue("time"), j.getString("msg"));
        }

        public InfoType getLogLevel() {
            return logLevel;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getMsg() {
            return msg;
        }
    }


    private void writeMessage(SSEEventType event, Object data) {
        httpClientWriter.println("event: " + event.getEventType());
        httpClientWriter.println("data: " + data);
        httpClientWriter.println(); // note the additional line being written to the stream..
        httpClientWriter.flush();

        //  if (!inAttach2RunningProcessor) {
        launchToken.ifPresent((lt) -> {
            lt.appendLaunchingLine(event.getEventType() + splitChar + data);
        });
        //}
    }

    @Override
    public void error(String serviceName, long timestamp, String msg) {
        writeMessage(InfoType.ERROR, timestamp, msg);
    }

    @Override
    public void fatal(String serviceName, long timestamp, String msg) {
        writeMessage(InfoType.ERROR, timestamp, msg);
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
