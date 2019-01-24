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
package com.qlangtech.tis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestMultiThread extends TestCase {

    public void test() throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        final Object lock = new Object();
        // exec.execute();
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Object o = new Object();
                System.err.println("isInterrupted:" + Thread.interrupted() + "isAlive:" + Thread.currentThread().isAlive());
                while (!Thread.interrupted()) {
                }
                System.err.println("isInterrupted:" + Thread.interrupted() + "isAlive:" + Thread.currentThread().isAlive());
            }
        });
        t.start();
        Thread.sleep(200);
        System.out.println("start to shutdown");
        // exec.shutdownNow();
        // exec.awaitTermination(5, TimeUnit.SECONDS);
        t.interrupt();
        synchronized (lock) {
            lock.wait();
        }
    }
}
