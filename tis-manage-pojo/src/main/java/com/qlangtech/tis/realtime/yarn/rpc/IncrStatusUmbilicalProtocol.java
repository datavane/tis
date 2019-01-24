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
package com.qlangtech.tis.realtime.yarn.rpc;

import org.apache.hadoop.ipc.VersionedProtocol;

/*
 * 增量子节点会实时将自己的状态信息汇报给master节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IncrStatusUmbilicalProtocol extends VersionedProtocol {

    public static final long versionID = 0L;

    public PingResult ping();

    /**
     * 子节点向服务端节点发送，子节点执行状态
     *
     * @param upateCounter
     */
    public MasterJob reportStatus(UpdateCounterMap upateCounter);

    /**
     * 增量节点启动的时候，向服务端汇报本地的情况，例如:监听的topic是下的tag是什么等等
     * @param launchReportInfo
     */
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo);
    // /**
    // * 报告查询节点的状态信息
    // * @param upateCounter
    // */
    // public void reportQueryNodeStatus(UpdateCounterMap upateCounter);
}
