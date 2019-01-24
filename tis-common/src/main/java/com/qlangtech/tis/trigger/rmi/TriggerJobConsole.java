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
package com.qlangtech.tis.trigger.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface TriggerJobConsole extends Remote {

    public List<JobDesc> getAllJobsInServer() throws RemoteException;

    public List<JobDesc> getJob(String indexName, Long jobid) throws RemoteException;

    public boolean isServing(String coreName) throws RemoteException;

    /**
     * 停止执行
     *
     * @param jobids
     * @throws RemoteException
     */
    public void pause(String coreName) throws RemoteException;

    /**
     * core是否是任务终止状态
     *
     * @param coreName
     * @return
     * @throws RemoteException
     */
    public boolean isPause(String coreName) throws RemoteException;

    /**
     * 重新启动
     *
     * @param coreName
     * @throws RemoteException
     */
    public void resume(String coreName) throws RemoteException;
}
