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
package com.qlangtech.tis.fullbuild.taskflow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.qlangtech.tis.common.utils.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ForTask extends AdapterTask {

    private final int from;

    private final int to;

    private final ITask task;

    public static final ThreadPoolExecutor executor;

    static {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
        executor = new ThreadPoolExecutor(4, 4, 30, TimeUnit.MINUTES, queue, new ThreadFactory() {

            private AtomicInteger id = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "file read thread #" + id.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void exexute() {
        final CountDownLatch countdown = new CountDownLatch(to - from + 1);
        for (int i = from; i <= to; i++) {
            final int index = i;
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("currentindex", String.valueOf(index));
                        task.exexute(params);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        countdown.countDown();
                    }
                }
            });
        }
        try {
            countdown.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void executeSql(String taskname, String sql) {
        throw new UnsupportedOperationException("taskname:" + taskname + ",sql:" + sql);
    }

    public ForTask(int from, int to, ITask task) {
        super();
        this.from = from;
        this.to = to;
        this.task = task;
        Assert.assertTrue(from >= 0);
        Assert.assertTrue(to >= from);
    }

    public static void main(String[] args) {
    }
}
