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
package com.qlangtech.tis.trigger.http;

import com.qlangtech.tis.trigger.socket.IWorkflowFeedback;
import com.qlangtech.tis.trigger.socket.Task;
import com.qlangtech.tis.trigger.socket.TaskContext;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class WorkflowFeedbackImpl implements IWorkflowFeedback {

    private final Task tsk;

    private final TaskContext context;

    public WorkflowFeedbackImpl(Task tsk, TaskContext context) {
        super();
        this.tsk = tsk;
        this.context = context;
    }

    @Override
    public TaskContext getTaskContext() {
        return this.context;
    }

    @Override
    public void sendError(String msg, Exception e) {
    // tsk.sendError(context, msg, e);
    }

    @Override
    public void sendError(Exception e) {
    // tsk.sendError(context, e);
    }

    @Override
    public void sendError(String message) {
        tsk.sendError(context, message);
    }

    @Override
    public void sendInfo(String message) {
    // tsk.sendInfo(context, message);
    }
}
