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
package com.qlangtech.tis.common.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.solr.common.cloud.ZkStateReader;
import com.qlangtech.tis.hdfs.client.router.Group32RandRouter;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JustDumpServiceConfig implements IServiceConfig {

    private final String serviceName;

    public JustDumpServiceConfig(String serviceName) {
        super();
        this.serviceName = serviceName;
    }

    @Override
    public void setHolder(SolrCoreStatusHolder holder) {
    }

    @Override
    public boolean hasAnyGroup() {
        return true;
    }

    @Override
    public int getGroupSize() {
        return Group32RandRouter.GROUP_SIZE;
    }

    @Override
    public Set<String> getGroupNameSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAllNodeIps() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<String>> getAllCoreIpMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSingleCoreName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getCoreNameSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GroupConfig getGroupConfig(String groupName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ZkStateReader getZkStateReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public int getGroupNum() {
        return Group32RandRouter.GROUP_SIZE;
    }

    @Override
    public void checkBySelf() {
    }
}
