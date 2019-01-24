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
import com.qlangtech.tis.hdfs.client.bean.searcher.CommonTerminatorSearcher;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl.ServiceConfigChangeListener;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.router.ModGroupRouter;
import com.qlangtech.tis.trigger.util.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherQueryFactory implements InitializingBean, FactoryBean {

    private CommonTerminatorSearcher searcher;

    private GroupRouter groupRouter;

    private TSearcherQueryContextImpl queryContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        searcher = new CommonTerminatorSearcher();
        Assert.assertNotNull(queryContext);
        if (groupRouter != null) {
            queryContext.addCoreConfigChangeListener(new ServiceConfigChangeListener() {

                @Override
                public void onChange(IServiceConfig config) {
                    if (groupRouter instanceof ModGroupRouter) {
                        ((ModGroupRouter) groupRouter).setServiceConfig(queryContext.getServiceConfig());
                    }
                }
            });
            queryContext.setGroupRouter(groupRouter);
        }
        searcher.setQueryContext(queryContext);
        searcher.afterPropertiesSet();
        queryContext.fireServiceConfigChange();
    }

    @Override
    public Object getObject() throws Exception {
        Assert.assertNotNull("searcher can not be null", searcher);
        return searcher;
    }

    @Override
    public Class<CommonTerminatorSearcher> getObjectType() {
        return CommonTerminatorSearcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public GroupRouter getGroupRouter() {
        return groupRouter;
    }

    public void setGroupRouter(GroupRouter groupRouter) {
        this.groupRouter = groupRouter;
    }

    public TSearcherQueryContextImpl getQueryContext() {
        return queryContext;
    }

    public void setQueryContext(TSearcherQueryContextImpl queryContext) {
        this.queryContext = queryContext;
    }
}
