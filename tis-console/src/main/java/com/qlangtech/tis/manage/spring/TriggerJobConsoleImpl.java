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
package com.qlangtech.tis.manage.spring;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.trigger.rmi.JobDesc;
import com.qlangtech.tis.trigger.rmi.TriggerJobConsole;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerJobConsoleImpl extends EnvironmentBindService<TerminatorRmiProxyFactoryBean> implements TriggerJobConsole {

    @Override
    public List<JobDesc> getJob(String indexname, Long jobid) throws RemoteException {
        return null;
    }

    private ZooKeeperGetter zooKeeperGetter;

    private static final Log log = LogFactory.getLog(TriggerJobConsoleImpl.class);

    public ZooKeeperGetter getZooKeeperGetter() {
        return zooKeeperGetter;
    }

    @Override
    public boolean isServing(String coreName) {
        return true;
    }

    public void setZooKeeperGetter(ZooKeeperGetter zooKeeperGetter) {
        this.zooKeeperGetter = zooKeeperGetter;
    }

    private TriggerJobConsole getConsoleInstance() {
        return (TriggerJobConsole) this.getInstance().getObject();
    }

    @Override
    protected TerminatorRmiProxyFactoryBean createSerivce(RunEnvironment runtime) {
        TerminatorRmiProxyFactoryBean factory = new TerminatorRmiProxyFactoryBean(this.getZooKeeperGetter().getInstance(runtime));
        // factory.setServiceUrl("rmi://{0}:9999/consoleTriggerJobService");
        factory.setServiceInterface(TriggerJobConsole.class);
        factory.afterPropertiesSet();
        log.debug("runtime:" + runtime + ",service url:" + factory.getServiceUrl() + " rmi server connection has been established");
        return factory;
    }

    public TriggerJobConsole getOnlineTriggerJobConsole() {
        return (TriggerJobConsole) this.getInstance(RunEnvironment.getSysRuntime()).getObject();
    }

    @Override
    public List<JobDesc> getAllJobsInServer() throws RemoteException {
        return getConsoleInstance().getAllJobsInServer();
    }

    @Override
    public boolean isPause(String coreName) throws RemoteException {
        return getConsoleInstance().isPause(coreName);
    }

    @Override
    public void pause(String coreName) throws RemoteException {
        getConsoleInstance().pause(coreName);
    }

    @Override
    public void resume(String coreName) throws RemoteException {
        getConsoleInstance().resume(coreName);
    }
}
