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
package com.qlangtech.tis.common.perfutil;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PerfTracer {

    private AtomicInteger counter;

    private long startTime;

    private long intervalTime = 1 * 60 * 1000;

    private Log logger;

    private String name;

    public PerfTracer(String name, Log logger) {
        this.logger = logger;
        this.name = name;
        this.startTime = System.currentTimeMillis();
        this.counter = new AtomicInteger(0);
    }

    public void increment() {
        counter.incrementAndGet();
        if ((System.currentTimeMillis() - startTime) >= intervalTime) {
            this.onTime();
        }
    }

    public void reset() {
        counter.set(0);
        this.startTime = System.currentTimeMillis();
    }

    protected String exportLog() {
        return "[" + name + "-statistics] " + (counter.get() / (intervalTime / 1000)) + "-tps";
    }

    protected void onTime() {
        logger.warn(this.exportLog());
        this.reset();
    }
}
