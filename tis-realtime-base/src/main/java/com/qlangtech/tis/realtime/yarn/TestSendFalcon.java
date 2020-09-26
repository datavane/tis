/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年2月10日
 */
public class TestSendFalcon {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
    // TransferIncrContainer container = new TransferIncrContainer(Collections.emptyList());
    // 
    // final List<IOnsListenerStatus> slist = Arrays.asList(new MockOnsListenerStatus());
    // 
    // scheduler.scheduleAtFixedRate(new Runnable() {
    // @Override
    // public void run() {
    // try {
    // System.out.println((System.currentTimeMillis() % (60 * 1000)) / 1000 + ":execute");
    // container.sendStatus2Falcon(slist);
    // Thread.sleep(2000);
    // } catch (Throwable e) {
    // 
    // e.printStackTrace();
    // }
    // }
    // }, 1, 30, TimeUnit.SECONDS);
    }

    private static class MockOnsListenerStatus implements IOnsListenerStatus {

        private static long errCount = 0;

        private static long ignoreCount = 0;

        @Override
        public boolean isPaused() {
            return false;
        }

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
