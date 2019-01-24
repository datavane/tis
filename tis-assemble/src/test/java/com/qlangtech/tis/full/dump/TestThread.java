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
package com.qlangtech.tis.full.dump;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestThread extends TestCase {

    private static ExecutorService service;

    static void shutdownAndAwaitTermination(ExecutorService pool) {
        System.out.println("shutdownAndAwaitTermination");
        // Disable new tasks from being submitted
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public void test() throws Exception {
        service = Executors.newCachedThreadPool(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println("uncaughtException execute");
                        shutdownAndAwaitTermination(service);
                    // try {
                    // service.shutdown();
                    // System.out.println("service.shutdown()");
                    // } catch (Exception e1) {
                    // e1.printStackTrace();
                    // }
                    // e.printStackTrace();
                    }
                });
                return t;
            }
        });
        // ExecutorCompletionService<Object> completionService = new
        // ExecutorCompletionService<Object>(
        // service);
        Future<String> xxxx = service.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Future<String> f = service.submit(new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        while (true) {
                            System.out.println("inner execute");
                            Thread.sleep(1000);
                        }
                    }
                });
                try {
                    f.get();
                } catch (Exception e) {
                    f.cancel(true);
                    e.printStackTrace(System.err);
                }
                System.out.println("i inner was be cancel");
                // }
                return "xxxxx";
            }
        });
        Future<String> bbb = service.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                int i = 0;
                boolean isInterrupted = false;
                while (true && !(isInterrupted = Thread.currentThread().isInterrupted())) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("receive excpeiton InterruptedException ");
                        e.printStackTrace();
                        return "aaaaa-over";
                    }
                    System.out.println("isInterrupted():" + isInterrupted);
                // System.out.println("i am bbbb working i:" + i++);
                // if (i > 15) {
                // System.out.println("throw a exception");
                // throw new RuntimeException("xxxxxx");
                // }
                }
                return "aaaaa";
            }
        });
        Thread.sleep(6000);
        System.out.println("cancel");
        System.out.println(xxxx.cancel(true));
        System.out.println(bbb.cancel(true));
        // Future<Object> o = completionService.take();
        // System.out.println("f.get():" + o.get() + ",f.isDone():" +
        // o.isDone());
        // shutdownAndAwaitTermination(service);
        Thread.sleep(9999999999l);
    }
}
