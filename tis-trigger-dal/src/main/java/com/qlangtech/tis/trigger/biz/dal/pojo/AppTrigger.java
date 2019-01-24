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
package com.qlangtech.tis.trigger.biz.dal.pojo;

import java.util.ArrayList;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppTrigger {

    private final com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob fullTrigger;

    private final com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob incTrigger;

    public AppTrigger(com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob fullTrigger, com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob incTrigger) {
        super();
        this.fullTrigger = fullTrigger;
        this.incTrigger = incTrigger;
    }

    public com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob getFullTrigger() {
        return fullTrigger;
    }

    public com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob getIncTrigger() {
        return incTrigger;
    }

    /**
     * dump是否是停止的状态
     *
     * @return
     */
    public boolean isPause() {
        if (fullTrigger != null && !fullTrigger.isStop()) {
            return false;
        }
        if (incTrigger != null && !incTrigger.isStop()) {
            return false;
        }
        return true;
    }

    public List<Long> getJobsId() {
        final List<Long> jobs = new ArrayList<Long>();
        if (this.getFullTrigger() != null) {
            jobs.add(this.getFullTrigger().getJobId());
        }
        if (this.getIncTrigger() != null) {
            jobs.add(this.getIncTrigger().getJobId());
        }
        return jobs;
    }
}
