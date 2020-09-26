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
package com.qlangtech.tis.trigger.jst;

import com.qlangtech.tis.trigger.socket.LogType;
import java.io.Serializable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MonotorTarget implements Serializable {

    // 当DF构建时，没有绑定collection，先用一个假的collection名称代替一下
    public static final String DUMP_COLLECTION = "dummpCollection";

    private static final long serialVersionUID = 1L;

    public final String collection;

    public final LogType logType;

    private Integer taskid;

    private static MonotorTarget create(String collection, LogType logtype) {
        if (logtype == null) {
            throw new IllegalArgumentException("log type can not be null");
        }
        return new MonotorTarget(collection, logtype);
    }

    public Integer getTaskid() {
        return taskid;
    }

    public void setTaskid(Integer taskid) {
        this.taskid = taskid;
    }

    public static RegisterMonotorTarget createRegister(MonotorTarget target) {
        return new RegisterMonotorTarget(true, target.collection, target.logType);
    }

    public static RegisterMonotorTarget createRegister(String collection, LogType logtype) {
        MonotorTarget target = create(collection, logtype);
        return new RegisterMonotorTarget(true, target.collection, target.logType);
    }

    public static PayloadMonitorTarget createPayloadMonitor(String collection, String payload, LogType logtype) {
        return new PayloadMonitorTarget(true, collection, payload, logtype);
    }

    public static RegisterMonotorTarget createUnregister(String collection, LogType logtype) {
        MonotorTarget target = create(collection, logtype);
        return new RegisterMonotorTarget(false, target.collection, target.logType);
    }

    public static RegisterMonotorTarget createUnregister(MonotorTarget target) {
        return new RegisterMonotorTarget(false, target.collection, target.logType);
    }

    @Override
    public int hashCode() {
        return (collection + logType.getValue()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    MonotorTarget(String collection, LogType logType) {
        super();
        this.collection = collection;
        this.logType = logType;
    }

    public String getCollection() {
        return collection;
    }

    @Override
    public String toString() {
        return "monitorTarget[collection:" + collection + ",type:" + logType + "]";
    }

    public LogType getLogType() {
        return logType;
    }

    /**
     * 是否有匹配的日志类型？
     *
     * @param testType
     * @return
     */
    public boolean testLogType(LogType... testType) {
        for (LogType type : testType) {
            if (type == this.logType) {
                return true;
            }
        }
        return false;
    }
}
