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
package com.qlangtech.tis.realtime.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicIncrStatusReport implements Runnable {

    private boolean closed = false;

    protected final Collection<IOnsListenerStatus> incrChannels;

    private static final Logger logger = LoggerFactory.getLogger(BasicIncrStatusReport.class);

    public BasicIncrStatusReport(Collection<IOnsListenerStatus> incrChannels) {
        this.incrChannels = incrChannels;
    }

    public void setClose() {
        this.closed = true;
    }

    protected boolean isClosed() {
        return closed;
    }

    protected abstract void processSnapshot() throws Exception;

    @Override
    public final void run() {
        try {
            while (!isClosed()) {
                try {
                    processSnapshot();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            // 清空计数器
            for (IOnsListenerStatus l : incrChannels) {
                l.cleanLastAccumulator();
            }
            logger.info("server push realtime update session has been terminated");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
