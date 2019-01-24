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
package com.qlangtech.tis.common.zk;

import org.apache.zookeeper.Watcher;
import com.qlangtech.tis.common.TerminatorCommonUtils;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * @description
 * @since  2012-8-28 下午05:32:52
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoreLifeManager {

    /**
     * @uml.property  name="zkClient"
     * @uml.associationEnd
     */
    private TerminatorZkClient zkClient = null;

    public CoreLifeManager(TerminatorZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public void registerCoreSelf(String coreName) throws TerminatorZKException {
        String localIp = TerminatorCommonUtils.getLocalHostIP();
        zkClient.rcreatePath(SolrCoreZKUtils.getSolrCoreStatusPath(localIp, coreName), false);
    }

    public void unRegisterCoreSelf(String coreName) throws TerminatorZKException {
        String localIp = TerminatorCommonUtils.getLocalHostIP();
        String path = SolrCoreZKUtils.getSolrCoreStatusPath(localIp, coreName);
        if (zkClient.exists(path))
            zkClient.delete(SolrCoreZKUtils.getSolrCoreStatusPath(localIp, coreName));
    }

    public boolean isAlive(String coreName) throws TerminatorZKException {
        String path = SolrCoreZKUtils.getSolrCoreStatusPath(TerminatorCommonUtils.getLocalHostIP(), coreName);
        return zkClient.exists(path);
    }

    public boolean isAlive(String hostIP, String coreName) throws TerminatorZKException {
        String path = SolrCoreZKUtils.getSolrCoreStatusPath(hostIP, coreName);
        return zkClient.exists(path);
    }

    public boolean isAlive(String hostIP, String coreName, Watcher watcher) throws TerminatorZKException {
        String path = SolrCoreZKUtils.getSolrCoreStatusPath(hostIP, coreName);
        return zkClient.exists(path, watcher);
    }

    /**
     * @return
     * @uml.property  name="zkClient"
     */
    public TerminatorZkClient getZkClient() {
        return zkClient;
    }

    /**
     * @param zkClient
     * @uml.property  name="zkClient"
     */
    public void setZkClient(TerminatorZkClient zkClient) {
        this.zkClient = zkClient;
    }
}
