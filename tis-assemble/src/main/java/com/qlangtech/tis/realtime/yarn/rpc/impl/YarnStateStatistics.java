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
package com.qlangtech.tis.realtime.yarn.rpc.impl;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class YarnStateStatistics {

    private long tbTPS;

    private long sorlTPS;

    private long queueRC;

    private String from;

    private boolean paused;

    private long tis30sAvgRT;

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public long getTbTPS() {
        return tbTPS;
    }

    public void setTbTPS(long tbTPS) {
        this.tbTPS = tbTPS;
    }

    public long getSorlTPS() {
        return sorlTPS;
    }

    public void setSorlTPS(long sorlTPS) {
        this.sorlTPS = sorlTPS;
    }

    public long getQueueRC() {
        return queueRC;
    }

    public void setQueueRC(long queueRC) {
        this.queueRC = queueRC;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getTis30sAvgRT() {
        return tis30sAvgRT;
    }

    public void setTis30sAvgRT(long tis30sAvgRT) {
        this.tis30sAvgRT = tis30sAvgRT;
    }
}
