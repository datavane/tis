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
package com.qlangtech.tis.trigger.jst;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.trigger.socket.LogType;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RegisterMonotorTarget extends MonotorTarget {

    private static final long serialVersionUID = 1L;

    private final boolean register;


    /**
     * LogCollectorClient.IPhaseStatusCollectionListener>>>>>>>>>>>>>>>>>
     */
    /**
     * 需要监听的实体的格式 “full”,“incrbuild:search4totalpay-1”
     *
     * @param logstype
     * @return
     */
    public static List<RegisterMonotorTarget> parseLogTypes(DataXName collectionName, int taskid, String logstype) {
        List<RegisterMonotorTarget> types = new ArrayList<>();
        for (String t : StringUtils.split(logstype, ",")) {
            String[] arg = null;
            if (StringUtils.indexOf(t, ":") > 0) {
                arg = StringUtils.split(t, ":");
                if (arg.length != 2) {
                    throw new IllegalArgumentException("arg:" + t + " is not illegal");
                }
                PayloadMonitorTarget payloadMonitor = MonotorTarget.createPayloadMonitor(collectionName, arg[1], LogType.parse(arg[0]));
                types.add(payloadMonitor);
            } else {
                types.add(MonotorTarget.createRegister(collectionName, LogType.parse(t)));
            }
        }
        types.forEach((t) -> {
            if (taskid > 0) {
                t.setTaskid(taskid);
            }
        });
        return types;
    }

    /**
     * @param collection
     * @param logType
     */
    public RegisterMonotorTarget(boolean register, DataXName collection, LogType logType) {
        super(collection, logType);
        this.register = register;
    }

    public boolean isRegister() {
        return register;
    }
}
