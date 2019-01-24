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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.qlangtech.tis.common.ServiceType;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.protocol.SearchService;
import com.qlangtech.tis.hdfs.util.Assert;

/*
 * 实时查询客户端
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RealtimeTerminatorSearcher extends BasicTerminatorSearcher {

    // private static final Log logger = LogFactory
    // .getLog(RealtimeTerminatorSearcher.class);
    public Map<String, SearchService> searchServices;

    // 
    @Override
    protected SearchService getSearchService(ServiceType serviceType, String serviceName, int group) {
        Assert.assertNotNull("searchServices can not be null", this.searchServices);
        return this.searchServices.get(String.valueOf(group));
    }

    @Override
    protected void subscribeSearcherService(IServiceConfig config) {
    // Set<String> groupNumberSet = this.getServiceConfig().keySet();
    // 
    // Map<String, SearchService> tmpSearchServices = new HashMap<String, SearchService>();
    // for (String number : groupNumberSet) {
    // String version = this.getServiceName() + "-" + number + "-readr";
    // Object obj = this.subscribeHSF(SearchService.class.getName(),
    // version);
    // tmpSearchServices.put(number, (SearchService) obj);
    // }
    // 
    // // 这玩意儿概率太低了，不同步了
    // this.searchServices = Collections.unmodifiableMap(tmpSearchServices);
    }
}
