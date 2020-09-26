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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatus;
import com.qlangtech.tis.fullbuild.phasestatus.IPhaseStatus;
import com.qlangtech.tis.manage.common.Config;
import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public abstract class BasicPhaseStatus<T extends IChildProcessStatus> implements IPhaseStatus<T> {

    private final int taskid;

    // 是否有将状态写入到本地磁盘
    private boolean hasFlush2Local = false;

    public static transient IFlush2Local statusWriter = null;

    public static File getFullBuildPhaseLocalFile(int taskid, FullbuildPhase phase) {
        return new File(Config.getMetaCfgDir(), "df-logs/" + taskid + "/" + phase.getName());
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

    public BasicPhaseStatus(int taskid) {
        this.taskid = taskid;
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
                return writeStatus2Local();
            }
            if (!ts.isComplete()) {
                return false;
            }
        }
        return writeStatus2Local();
    }

    /**
     * 将本阶段的状态写入本地文件系统
     *
     * @return
     */
    protected boolean writeStatus2Local() {
        if (!this.hasFlush2Local) {
            if (statusWriter != null) {
                synchronized (this) {
                    File localFile = getFullBuildPhaseLocalFile(this.taskid, this.getPhase());
                    this.hasFlush2Local = true;
                    try {
                        if (!localFile.exists()) {
                            // 写入本地文件系统中，后续查看状态可以直接从本地文件中拿到
                            statusWriter.write(localFile, this);
                        }
                    } catch (Exception e) {
                        this.hasFlush2Local = false;
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return true;
    }

    public interface IFlush2Local {

        public void write(File localFile, BasicPhaseStatus status) throws Exception;

        public BasicPhaseStatus loadPhase(File localFile) throws Exception;
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
