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
package com.qlangtech.tis.realtime.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月8日
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
