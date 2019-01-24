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
package com.qlangtech.tis.dump.hive;

import org.apache.hadoop.hive.ql.exec.UDF;
import com.qlangtech.tis.common.utils.MockHDFSProvider;
import com.qlangtech.tis.common.utils.RealtimeTerminatorBeanFactory;
import com.qlangtech.tis.hdfs.client.router.SolrCloudPainRouter;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SharedRouter extends UDF {

    private static SolrCloudPainRouter cloudPainRouter;

    public String evaluate(final String shardValue, final String collection, final String runtime) {
        return getRouter(collection, runtime).getShardIndex(shardValue);
    }

    private SolrCloudPainRouter getRouter(String collection, String runtime) {
        if (cloudPainRouter == null) {
            synchronized (SharedRouter.class) {
                if (cloudPainRouter == null) {
                    try {
                        RunEnvironment.setSysRuntime(RunEnvironment.getEnum(runtime));
                        RealtimeTerminatorBeanFactory beanFactory = new RealtimeTerminatorBeanFactory();
                        beanFactory.setServiceName(collection);
                        beanFactory.setJustDump(false);
                        beanFactory.setIncrDumpProvider(new MockHDFSProvider());
                        beanFactory.setFullDumpProvider(new MockHDFSProvider());
                        beanFactory.setGrouprouter(null);
                        beanFactory.afterPropertiesSet();
                        cloudPainRouter = (SolrCloudPainRouter) beanFactory.getGrouprouter();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cloudPainRouter;
    }
}
