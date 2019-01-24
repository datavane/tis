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
package com.qlangtech.tis.trigger.socket;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.common.utils.Assert;


/*
 * 业务端执行状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExecuteState<T> implements Serializable {

    private static final long serialVersionUID = -2958243389261902244L;

    // private final InfoType infoType;
    private final LogType logType;

    private final T msg;

    // 代表这条消息是从哪里传过来的
    private InetAddress from;

    private Long jobId;

    private Long taskId;

    private String serviceName;

    private String execState;

    private Long phrase;

    private long time;

    public LogType getLogType() {
        return logType;
    }

    // public void setLogType(LogType logType) {
    // this.logType = logType;
    // }
    /**
     * 事件发生的地点
     */
    private String component;

    private ExecuteState(LogType infoType, T msg) {
        super();
        Assert.assertNotNull("infotype can not be null", infoType);
        Assert.assertNotNull("param msg can not be null", msg);
        this.logType = infoType;
        this.msg = msg;
    }

    public String getExecState() {
        return execState;
    }

    public void setExecState(String execState) {
        this.execState = execState;
    }

    public Long getPhrase() {
        return phrase;
    }

    public void setPhrase(Long phrase) {
        this.phrase = phrase;
    }

    public String getCollectionName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setFrom(InetAddress from) {
        this.from = from;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static <T> ExecuteState<T> create(LogType infoType, T msg) {
        try {
            ExecuteState<T> state = new ExecuteState<T>(infoType, msg);
            state.from = InetAddress.getLocalHost();
            // 当前时间
            state.time = System.currentTimeMillis();
            return state;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public long getTime() {
        return time;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public InetAddress getFrom() {
        return from;
    }

    public String serializeJSON() {
        return JSON.toJSONString(new JSONPojo<T>(this.logType, this.msg), true);
    }

    // private T getMsg() {
    // return msg;
    // }
    // public InfoType getInfoType() {
    // return infoType;
    // }
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public static class JSONPojo<T> {

        private LogType logtype;

        private T data;

        public JSONPojo(LogType logtype, T data) {
            super();
            this.logtype = logtype;
            this.data = data;
        }

        public String getLogtype() {
            return logtype.getValue();
        }

        public void setLogtype(LogType logtype) {
            this.logtype = logtype;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
