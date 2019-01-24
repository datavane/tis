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
package com.qlangtech.tis.common.timers;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 任务描述对象，提供了befor，after的扩展，还有Exception的处理器JobExceptionHandler
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class Job implements Runnable {

    /**
     * @uml.property  name="defaultHandler"
     * @uml.associationEnd
     */
    private static JobExceptionHandler defaultHandler = new DiscardJobExceptionHandler();

    /**
     * @uml.property  name="exceptionHandler"
     * @uml.associationEnd
     */
    private JobExceptionHandler exceptionHandler = defaultHandler;

    private ReentrantLock lock = new ReentrantLock();

    public Job() {
    }

    public Job(JobExceptionHandler jobExceptionHandler) {
        this.exceptionHandler = jobExceptionHandler;
    }

    @Override
    public void run() {
        if (this.isRunning()) {
            throw new RejectedExecutionException("Job is running.....");
        }
        lock.lock();
        try {
            this.beforeJob();
            this.doJob();
            this.afterJob();
        } catch (Throwable e) {
            exceptionHandler.handleException(Thread.currentThread(), e);
        } finally {
            lock.unlock();
        }
    }

    public void beforeJob() {
    }

    public abstract void doJob() throws Throwable;

    public void afterJob() {
    }

    public boolean isRunning() {
        return lock.isLocked();
    }

    /**
     * @return
     * @uml.property  name="exceptionHandler"
     */
    public JobExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * @param exceptionHandler
     * @uml.property  name="exceptionHandler"
     */
    public void setExceptionHandler(JobExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public static void setDefaultExceptionHandler(JobExceptionHandler exceptionHandler) {
        defaultHandler = exceptionHandler;
    }

    public static JobExceptionHandler getDefaultExceptionHandler() {
        return defaultHandler;
    }

    public interface JobExceptionHandler {

        public void handleException(Thread thread, Throwable e);
    }

    public static class DiscardJobExceptionHandler implements JobExceptionHandler {

        @Override
        public void handleException(Thread thread, Throwable e) {
        // do nothing
        }
    }
}
