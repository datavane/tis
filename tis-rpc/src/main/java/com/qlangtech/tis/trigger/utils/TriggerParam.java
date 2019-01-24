/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.utils;

import java.util.HashMap;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerParam extends HashMap<String, String> {

    // 增量执行组，前期将应用分组貌似也没有啥用，不过在后期全平台自动化部署的时候应该会派上用场
    public static final String GROUP_NAME = "test-group";

    public static final String TRIGGER_SERVER_PORT = "8199";

    public static final int COMMON_FEDDBACK_PORT = 9997;

    private static final long serialVersionUID = 1L;

    public static TriggerParam deserialize(String value) {
       throw new UnsupportedOperationException(" JsonUtil.deserialize(value) 需要废弃掉");
    	//  return JsonUtil.deserialize(value);
    }

    public TriggerParam() {
        super();
        this.put(DUMP_TYPE, DUMP_TYPE_HDFS);
    }

    public String serialize() {
       // return JsonUtil.serialize(this);
        
        throw new UnsupportedOperationException(" JsonUtil.serialize(value) 需要废弃掉");
    }

    // ▼▼▼ dump type start
    public static final String DUMP_TYPE = "dumptype";

    public static final String DUMP_TYPE_CLOUD = "cloud";

    public static final String DUMP_TYPE_HDFS = "hdfs";

    // ▲▲▲ dump type end
    private static final String TIME = "time";

    // private static final String YUNTI_HOST = "yuntihost";
    private static final String TUNTI_PATH = "yuntipath";

    // public static final String DATE_PATTERN = "datepattern";
    private static final String YUNTI_USER_TOKEN = "yuntiusertoken";

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

    private static final String TASK_ID = "taskid";

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

    // public void setYuntiHost(String value) {
    // this.put(YUNTI_HOST, value);
    // }
    // 
    // public String getYuntiHost() {
    // return this.get(YUNTI_HOST);
    // }
    public void setYuntiPath(String value) {
        this.put(TUNTI_PATH, value);
    }

    public String getYuntiPath() {
        return this.get(TUNTI_PATH);
    }

    // public void setDatePattern(String value) {
    // this.put(DATE_PATTERN, value);
    // }
    // 
    // public String getDatePattern() {
    // return this.get(DATE_PATTERN);
    // }
    // 
    public void setYuntiUserToken(String value) {
        this.put(YUNTI_USER_TOKEN, value);
    }

    public String getYuntiUserToken() {
        return this.get(YUNTI_USER_TOKEN);
    }
}
