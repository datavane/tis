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
package com.qlangtech.tis.hdfs.client.router;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.Slice;
import com.qlangtech.tis.solrj.extend.router.IRouterValueGetter;
import com.qlangtech.tis.solrj.extend.router.RouterUtils;
import com.qlangtech.tis.common.config.ServiceConfig;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrCloudPainRouter extends AbstractGroupRouter {

    private static final Pattern SHARE_PATTERN = Pattern.compile("shard(\\d+)");

    @Override
    public String getGroupName(final Map<String, String> rowData) {
        DocCollection collection = getDocCollection();
        // DocRouter docRouter = collection.getRouter();
        String routerValue = RouterUtils.getRouterValue(collection, this.getShardKey(), new IRouterValueGetter() {

            @Override
            public String get(String key) {
                return rowData.get(key);
            }

            @Override
            public String docDesc() {
                StringBuffer buffer = new StringBuffer();
                for (Map.Entry<String, String> entrty : rowData.entrySet()) {
                    buffer.append("key:").append(entrty.getKey()).append(" value:").append(entrty.getValue()).append(",");
                }
                return buffer.toString();
            }
        });
        return getShardIndex(routerValue);
    }

    public String getShardIndex(String routerValue) {
        DocCollection collection = getDocCollection();
        DocRouter docRouter = collection.getRouter();
        Slice slice = docRouter.getTargetSlice(null, null, routerValue, null, collection);
        Matcher matcher = SHARE_PATTERN.matcher(slice.getName());
        if (!matcher.matches()) {
            throw new IllegalStateException("slice.getName():" + slice.getName() + " is not match the pattern:" + SHARE_PATTERN);
        }
        return String.valueOf(Integer.parseInt(matcher.group(1)) - 1);
    }

    private DocCollection docCollection;

    private long refreshTimestamp;

    public DocCollection getDocCollection() {
        if (docCollection == null || System.currentTimeMillis() > (refreshTimestamp + 60 * 1000 * 60)) {
            if (!(this.getServiceConfig() instanceof ServiceConfig)) {
                throw new IllegalStateException("type of instance must be type ServiceConfig, but now be:" + this.getServiceConfig().getClass());
            }
            this.docCollection = ((ServiceConfig) this.getServiceConfig()).getDocCollection();
            refreshTimestamp = System.currentTimeMillis();
        }
        return this.docCollection;
    }
}
