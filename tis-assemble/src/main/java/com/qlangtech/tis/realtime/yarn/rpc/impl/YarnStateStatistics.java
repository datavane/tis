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
package com.qlangtech.tis.realtime.yarn.rpc.impl;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2017/2/14.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class YarnStateStatistics {

    private long tbTPS;

    private long sorlTPS;

    private long queueRC;

    private String from;

    private long tis30sAvgRT;

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
