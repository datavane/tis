/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年9月28日 下午7:28:32
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
        return this.docCollection;
    }
}
