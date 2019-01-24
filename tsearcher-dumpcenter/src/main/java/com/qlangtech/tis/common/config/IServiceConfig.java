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
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IServiceConfig {

    public void setHolder(SolrCoreStatusHolder holder);

    /**
     * group 的数量是否大于0 baisui add
     *
     * @return
     */
    public abstract boolean hasAnyGroup();

    public abstract int getGroupSize();

    public ZkStateReader getZkStateReader();

    /**
     * 获取该搜索服务对应的所有的分组名称
     *
     * @return
     */
    public abstract Set<String> getGroupNameSet();

    /**
     * 获取该搜索服务的所有机器节点的IP
     *
     * @return
     */
    public abstract Set<String> getAllNodeIps();

    public abstract Map<String, List<String>> getAllCoreIpMap();

    /**
     * 获取单索引的coreName
     *
     * @return
     */
    public abstract String getSingleCoreName();

    /**
     * 获取该搜索服务的搜有coreName
     *
     * @return
     */
    public abstract Set<String> getCoreNameSet();

    public abstract GroupConfig getGroupConfig(String groupName);

    /**
     * @return
     * @uml.property name="serviceName"
     */
    public abstract String getServiceName();

    public abstract int getGroupNum();

    public abstract void checkBySelf();
}
