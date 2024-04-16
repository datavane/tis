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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatus;
import com.qlangtech.tis.fullbuild.phasestatus.IFlush2Local;
import com.qlangtech.tis.fullbuild.phasestatus.IFlush2LocalFactory;
import com.qlangtech.tis.fullbuild.phasestatus.IPhaseStatus;
import com.qlangtech.tis.manage.common.Config;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public abstract class BasicPhaseStatus<T extends IChildProcessStatus> implements IPhaseStatus<T> {

    private final int taskid;

    // 是否有将状态写入到本地磁盘
    private transient boolean hasFlush2Local = false;

    public final transient IFlush2Local statusWriter;
    /**
     * 10秒执行一次速度
     */
    private transient final RateLimiter intervalWriteLimit = RateLimiter.create(0.1);

    public static File getFullBuildPhaseLocalFile(int taskid, FullbuildPhase phase) {
        return new File(Config.getMetaCfgDir(), "df-logs/" + taskid + "/"
                + Objects.requireNonNull(phase, "param phase can not be null").getName());
    }

    protected abstract FullbuildPhase getPhase();

    protected static boolean shallOpenView(Collection<? extends AbstractChildProcessStatus> childs) {
        if (childs.size() < 1) {
            return false;
        }
        // 如果全部都complete了不能open
        // 找正在运行的
        Optional<? extends AbstractChildProcessStatus> find = childs.stream().filter((r) -> !r.isComplete() && !r.isWaiting()).findFirst();
        return (childs.size() > 0 && find.isPresent());
    }

    public boolean isHasFlush2Local() {
        return hasFlush2Local;
    }

    public void setHasFlush2Local(boolean hasFlush2Local) {
        this.hasFlush2Local = hasFlush2Local;
    }

    public BasicPhaseStatus(int taskid, IFlush2Local statusWriter) {
        this.taskid = taskid;
        this.statusWriter = statusWriter;
    }

    public BasicPhaseStatus(int taskid) {
        this.taskid = taskid;
        this.statusWriter = IFlush2LocalFactory.createNew(
                Thread.currentThread().getContextClassLoader(), getFullBuildPhaseLocalFile(taskid, this.getPhase()))
                .orElse(null);


//                new IFlush2Local() {
//            @Override
//            public void write( BasicPhaseStatus status) throws Exception {
//                XmlFile xmlFile = new XmlFile(localFile);
//                xmlFile.write(status, Collections.emptySet());
//            }
//
//            @Override
//            public BasicPhaseStatus loadPhase(File localFile) throws Exception {
//                XmlFile xmlFile = new XmlFile(localFile);
//                return (BasicPhaseStatus) xmlFile.read();
//            }
//        };
    }

    // 在客户端是否详细展示
    public abstract boolean isShallOpen();

    protected abstract Collection<T> getChildStatusNode();

    @Override
    public final int getTaskId() {
        return this.taskid;
    }

    /**
     * 是否正在执行
     */
    @Override
    public final boolean isProcessing() {
        // 判断是否都执行完成了
        if (isComplete()) {
            return false;
        }
        for (T ts : getChildStatusNode()) {
            if (!ts.isWaiting()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否失败了
     */
    @Override
    public final boolean isFaild() {
        for (T ts : getChildStatusNode()) {
            if (ts.isFaild()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean isComplete() {
        Collection<T> children = getChildStatusNode();
        if (children.isEmpty()) {
            return false;
        }
        for (T ts : children) {
            if (ts.isFaild()) {
                finalWriteStatus2Local();
                return true;
            }
            if (!ts.isComplete()) {
                return false;
            }
        }
        finalWriteStatus2Local();
        return true;
    }

    private void finalWriteStatus2Local() {
        // boolean result = false;
        if (!this.hasFlush2Local) {
            this.writeStatus2Local();
            this.hasFlush2Local = true;
        }
        //return result;
    }


    public boolean intervalWriteStatus2Local() {
        if (this.intervalWriteLimit.tryAcquire()) {
            this.writeStatus2Local();
            return true;
        }
        return false;
    }

    /**
     * 将本阶段的状态写入本地文件系统
     *
     * @return
     */
    private boolean writeStatus2Local() {

        if (statusWriter != null) {
            synchronized (this) {
                try {
                    // if (!localFile.exists()) {
                    // 写入本地文件系统中，后续查看状态可以直接从本地文件中拿到
                    statusWriter.write(this);
                    //}
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    @Override
    public final boolean isSuccess() {
        Collection<T> children = getChildStatusNode();
        if (children.isEmpty()) {
            return false;
        }
        for (T ts : children) {
            if (!ts.isSuccess()) {
                return false;
            }
        }
        return true;
    }
}
