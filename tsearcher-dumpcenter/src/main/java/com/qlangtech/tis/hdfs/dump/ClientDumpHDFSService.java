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
package com.qlangtech.tis.hdfs.dump;

/*
 * @description 终搜服务端定义的Master Outer服务的接口定义
 *  客户端将导入情况通知给服务端
 *  如果导入成功，服务端将触发任务拉取HDFS集群的增量全量数据<br>
 *  如果导入不成功，服务端将记录失败，不触发任务<br>
 * @since 2011-9-13 下午06:54:36
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface ClientDumpHDFSService {

    /**
     * Client 报告终搜服务端HDFS增量任务导入状况
     * @param incr
     * @param timePoint
     */
    public void reportIncrTaskStatus(boolean suc, String msg, String uspoint);

    /**
     * Client 报告终搜服务端HDFS全量导入任务状况
     */
    public void reportFullTaskStatus(boolean suc, String msg, String uspoint);
}
