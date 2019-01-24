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
package com.qlangtech.tis.hdfs.client.bean.searcher;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.InitializingBean;

import com.qlangtech.tis.common.ServiceType;
import com.qlangtech.tis.common.TerminatorServiceException;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.protocol.SearchService;
import com.qlangtech.tis.common.protocol.TerminatorQueryRequest;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl.ServiceConfigChangeListener;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;

/*
 * 终搜查询客户端(现在还只能用在非实时的场景)
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicTerminatorSearcher implements SearchService, InitializingBean, TSearcherQueryContext {

    public static final String DEFAULT_SERVLET_CONTEXT = "terminator-search";

    private String servletContextName = DEFAULT_SERVLET_CONTEXT;

    static final Log logger = LogFactory.getLog(BasicTerminatorSearcher.class);

    private TSearcherQueryContext queryContext;

    public IServiceConfig getServiceConfig() {
        return queryContext.getServiceConfig();
    }

    public void addCoreConfigChangeListener(ServiceConfigChangeListener listener) {
        queryContext.addCoreConfigChangeListener(listener);
    }

    public void fireServiceConfigChange() {
        queryContext.fireServiceConfigChange();
    }

    public Set<String> getGroupNameSet() {
        return queryContext.getGroupNameSet();
    }

    protected abstract SearchService getSearchService(ServiceType serviceType, String serviceName, int group);

    @Override
    public final void afterPropertiesSet() throws Exception {
        getQueryContext().addCoreConfigChangeListener(new ServiceConfigChangeListener() {

            @Override
            public void onChange(IServiceConfig config) {
                subscribeSearcherService(config);
            }
        });
    }

    protected abstract void subscribeSearcherService(IServiceConfig config);

    /**
     * 取得负责查询的查询服务
     *
     * @param serviceType
     * @param serviceName
     * @param group
     * @return
     */
    // protected abstract SearchService getSearchService(ServiceType
    // serviceType,
    // String serviceName, int group);
   // @Override
    public QueryResponse query(TerminatorQueryRequest query) throws TerminatorServiceException {
      return null;
    // }
    }

   

    public GroupRouter getGroupRouter() {
        return queryContext.getGroupRouter();
    }

    public String getServiceName() {
        return queryContext.getServiceName();
    }

    public TerminatorZkClient getZkClient() {
        return queryContext.getZkClient();
    }

    protected final boolean isSingleGroup() {
        return getServiceConfig().getGroupSize() == 1;
    }

    public final String getServletContextName() {
        return servletContextName;
    }

    public final void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    public TSearcherQueryContext getQueryContext() {
        return queryContext;
    }

    public void setQueryContext(TSearcherQueryContext queryContext) {
        this.queryContext = queryContext;
    }

    public SolrCoreStatusHolder getHostStatusHolder() {
        return queryContext.getHostStatusHolder();
    }
}
