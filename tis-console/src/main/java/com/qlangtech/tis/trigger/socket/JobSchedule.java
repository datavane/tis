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

/*
 * 任务执行计划
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JobSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long jobid;

    // 执行任务
    private final String crobexp;

    private final String indexName;

    /**
     * 是否是暂停状态？
     */
    private final boolean paused;

    public JobSchedule(// , boolean
    String indexName, // , boolean
    Long jobid, // , boolean
    String crobexp) // isStop
    {
        super();
        this.jobid = jobid;
        this.indexName = indexName;
        this.crobexp = crobexp;
        this.paused = false;
    }

    public Long getJobid() {
        return jobid;
    }

    public boolean isPaused() {
        return paused;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getCrobexp() {
        return crobexp;
    }
}
