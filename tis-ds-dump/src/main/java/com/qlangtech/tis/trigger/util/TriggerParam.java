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
package com.qlangtech.tis.trigger.util;

import java.util.HashMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TriggerParam extends HashMap<String, String> {
    public static final String TASK_ID = "taskid";
    public static final String TRIGGER_SERVER_PORT = "8199";

    public static final int COMMON_FEDDBACK_PORT = 9997;

    public static final String TRIGGER_SERVER_ZK_PATH = "/terminator-lock/terminator_trigger_server";

    private static final long serialVersionUID = 1L;

//    public static TriggerParam deserialize(String value) {
//        return JsonUtil.deserialize(value);
//    }

    public TriggerParam() {
        super();
        this.put(DUMP_TYPE, DUMP_TYPE_HDFS);
    }

    public String serialize() {
        return JsonUtil.serialize(this);
    }

    // ▼▼▼ dump type start
    public static final String DUMP_TYPE = "dumptype";

    public static final String DUMP_TYPE_CLOUD = "cloud";

    public static final String DUMP_TYPE_HDFS = "hdfs";

    // ▲▲▲ dump type end
    private static final String TIME = "time";


    private static final String CURRENT_USER_NAME = "currentusername";

    private static final String GROUP_SIZE = "groupsize";

    private static final String TRIGGER_DUMP_TASK_ID = "triggerDumpTaskid";

    public void setDumpType(String value) {
        if (!(DUMP_TYPE_CLOUD.equals(value) || DUMP_TYPE_HDFS.equals(value))) {
            throw new IllegalArgumentException("value:" + value + " is not valid");
        }
        this.put(DUMP_TYPE, value);
    }

    public String getDumpType() {
        return this.get(DUMP_TYPE);
    }



    public void setTriggerDumpTaskId(String triggerDumpTaskId) {
        this.put(TRIGGER_DUMP_TASK_ID, triggerDumpTaskId);
    }

    public String getTriggerDumpTaskId() {
        return this.get(TRIGGER_DUMP_TASK_ID);
    }

    public void setCurrentUserName(String value) {
        this.put(CURRENT_USER_NAME, value);
    }

    public String getCurrentUserName() {
        return this.get(CURRENT_USER_NAME);
    }

    public void setGroupSize(int value) {
        this.put(GROUP_SIZE, String.valueOf(value));
    }

    public int getGroupSize() {
        return Integer.parseInt(this.get(GROUP_SIZE));
    }

    public void setTaskId(String value) {
        this.put(TASK_ID, value);
    }

    public String getTaskId() {
        return this.get(TASK_ID);
    }

    public void setTime(String value) {
        this.put(TIME, value);
    }

    public String getTime() {
        return this.get(TIME);
    }
}
