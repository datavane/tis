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
package com.qlangtech.tis.realtime.yarn;

import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestSendFalcon {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        TransferIncrContainer container = new TransferIncrContainer(Collections.emptyList());
        final List<IOnsListenerStatus> slist = Arrays.asList(new MockOnsListenerStatus());
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println((System.currentTimeMillis() % (60 * 1000)) / 1000 + ":execute");
                    container.sendStatus2Falcon(slist);
                    Thread.sleep(2000);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 1, 30, TimeUnit.SECONDS);
    }

    private static class MockOnsListenerStatus implements IOnsListenerStatus {

        private static long errCount = 0;

        private static long ignoreCount = 0;

        @Override
        public long getSolrConsumeIncrease() {
            return 0;
        }

        @Override
        public long getConsumeErrorCount() {
            return errCount += (int) (Math.random() * 10);
        }

        @Override
        public long getIgnoreRowsCount() {
            return ignoreCount += (int) (Math.random() * 5);
        }

        @Override
        public void cleanLastAccumulator() {
        }

        @Override
        public String getCollectionName() {
            return "search4supply";
        }

        @Override
        public String getTableUpdateCount() {
            return null;
        }

        @Override
        public int getBufferQueueUsedSize() {
            return 0;
        }

        @Override
        public int getBufferQueueRemainingCapacity() {
            return (int) (Math.random() * 1000);
        }

        @Override
        public long getConsumeIncreaseCount() {
            return 0;
        }

        @Override
        public void resumeConsume() {
        }

        @Override
        public void pauseConsume() {
        }

        @Override
        public Set<Entry<String, IIncreaseCounter>> getUpdateStatic() {
            return Collections.emptySet();
        }

        @Override
        public IIncreaseCounter getMetricCount(String metricName) {
            return null;
        }
    }
}
