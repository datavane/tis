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
package com.qlangtech.tis.common.utils;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.config.JustDumpServiceConfig;
import com.qlangtech.tis.common.config.ServiceConfig;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.bean.BasicTerminatorClient;
import com.qlangtech.tis.hdfs.client.bean.searcher.CommonTerminatorSearcher;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl.ServiceConfigChangeListener;
import com.qlangtech.tis.hdfs.client.data.HDFSProvider;
import com.qlangtech.tis.hdfs.client.data.MultiThreadHDFSDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProviderFactory;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.hdfs.client.router.AbstractGroupRouter;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.router.SolrCloudPainRouter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class CommonTerminatorBeanFactory implements FactoryBean, InitializingBean {

    private String serviceName;

    @SuppressWarnings("all")
    private HDFSProvider<String, String> fullDumpProvider;

    @SuppressWarnings("all")
    private HDFSProvider<String, String> incrDumpProvider;

    private BasicTerminatorClient termiantorBean;

    private BatchDataProcessor<String, String> dataprocess;

    private GroupRouter grouprouter;

    // 执行dump逻辑的时候，是否需要判断本节点是否抢夺到了锁
    // 一般情况下这个值都是true，只有在业务自动调用trigger的时候需要将这个值设置成false
    private boolean triggerLock;

    // 表示只作dump
    private boolean justDump;

    @SuppressWarnings("all")
    @Override
    public void afterPropertiesSet() throws Exception {
        termiantorBean = createTerminatorBean();
        // builder.addPropertyValue("zkAddress", TSearcherConfigFetcher
        // .getZkAddress());
        // this.addPropertyValue(element, builder, "serviceName");
        final TSearcherQueryContextImpl queryContext = new TSearcherQueryContextImpl();
        TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
        TerminatorZkClient zkClient = TerminatorZkClient.create(configFetcher.getZkAddress(), TSearcherQueryContextImpl.DEFAULT_ZK_TIMEOUT, null, true);
        queryContext.setZkClient(zkClient);
        if (this.isJustDump()) {
            queryContext.setServiceConfig(new JustDumpServiceConfig(serviceName));
        } else {
            queryContext.setServiceConfig(new ServiceConfig(serviceName, zkClient, queryContext));
        }
        GroupRouter groupRouter = this.getGrouprouter();
        if (groupRouter instanceof AbstractGroupRouter) {
            final AbstractGroupRouter r = (AbstractGroupRouter) groupRouter;
            queryContext.addCoreConfigChangeListener(new ServiceConfigChangeListener() {

                @Override
                public void onChange(IServiceConfig config) {
                    r.setServiceConfig(queryContext.getServiceConfig());
                }
            });
        }
        queryContext.setGroupRouter(groupRouter);
        queryContext.setServiceName(this.getServiceName());
        queryContext.setZkAddress(configFetcher.getZkAddress());
        queryContext.afterPropertiesSet();
        TSearcherDumpContextImpl dumpContext = new TSearcherDumpContextImpl();
        dumpContext.setQueryContext(queryContext);
        dumpContext.setFsName(configFetcher.getHdfsAddress());
        dumpContext.setDataprocessor(this.dataprocess);
        dumpContext.afterPropertiesSet();
        CommonTerminatorSearcher searcher = new CommonTerminatorSearcher();
        searcher.setQueryContext(queryContext);
        searcher.afterPropertiesSet();
        termiantorBean.setSearcher(searcher);
        SourceDataProvider datasourceProvider = null;
        initMultiThreadHdfsDataProvider(dumpContext, fullDumpProvider);
        initMultiThreadHdfsDataProvider(dumpContext, incrDumpProvider);
        // if (incrDumpProvider instanceof MultiThreadHDFSDataProvider) {
        // datasourceProvider = ((MultiThreadHDFSDataProvider) incrDumpProvider)
        // .getSourceData();
        // if (datasourceProvider != null) {
        // datasourceProvider.setDumpContext(dumpContext);
        // datasourceProvider.init();
        // }
        // }
        termiantorBean.setDumpContext(dumpContext);
        fullDumpProvider.setDumpContext(dumpContext);
        incrDumpProvider.setDumpContext(dumpContext);
        termiantorBean.setFullHdfsProvider(fullDumpProvider);
        termiantorBean.setIncrHdfsProvider(incrDumpProvider);
        termiantorBean.init();
        // 让需要serviceconfig的模块全部设置上
        queryContext.fireServiceConfigChange();
    }

    @SuppressWarnings("all")
    private void initMultiThreadHdfsDataProvider(TSearcherDumpContextImpl dumpContext, HDFSProvider<String, String> dumpProvider) throws SourceDataReadException {
        if (!(dumpProvider instanceof MultiThreadHDFSDataProvider)) {
            return;
        }
        SourceDataProviderFactory datasourceProvider = ((MultiThreadHDFSDataProvider) dumpProvider).getSourceData();
        if (datasourceProvider != null) {
            datasourceProvider.setDumpContext(dumpContext);
            datasourceProvider.init();
        }
    }

    public GroupRouter getGrouprouter() {
        if (grouprouter == null) {
            SolrCloudPainRouter router = new SolrCloudPainRouter();
            router.setShardKey("id");
            this.grouprouter = router;
        }
        return grouprouter;
    }

    public void setGrouprouter(GroupRouter grouprouter) {
        this.grouprouter = grouprouter;
    }

    @Override
    public Object getObject() throws Exception {
        return termiantorBean;
    }

    protected abstract BasicTerminatorClient createTerminatorBean();

    @Override
    public abstract Class<? extends BasicTerminatorClient> getObjectType();

    public BatchDataProcessor<String, String> getDataprocess() {
        return dataprocess;
    }

    public void setDataprocess(BatchDataProcessor<String, String> dataprocess) {
        this.dataprocess = dataprocess;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @SuppressWarnings("all")
    public HDFSProvider<String, String> getFullDumpProvider() {
        return fullDumpProvider;
    }

    @SuppressWarnings("all")
    public void setFullDumpProvider(HDFSProvider<String, String> fullDumpProvider) {
        this.fullDumpProvider = fullDumpProvider;
    }

    public void setIncrDumpProvider(HDFSProvider<String, String> incrDumpProvider) {
        this.incrDumpProvider = incrDumpProvider;
    }

    public void setTermiantorBean(BasicTerminatorClient termiantorBean) {
        this.termiantorBean = termiantorBean;
    }

    public boolean isTriggerLock() {
        return triggerLock;
    }

    public void setTriggerLock(boolean triggerLock) {
        this.triggerLock = triggerLock;
    }

    public boolean isJustDump() {
        return justDump;
    }

    public void setJustDump(boolean justDump) {
        this.justDump = justDump;
    }
}
