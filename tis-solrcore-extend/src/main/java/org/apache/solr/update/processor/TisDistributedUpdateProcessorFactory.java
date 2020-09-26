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
package org.apache.solr.update.processor;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisDistributedUpdateProcessorFactory extends UpdateRequestProcessorFactory implements DistributingUpdateProcessorFactory {

    @Override
    public void init(NamedList args) {
    }

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new DistributedUpdateProcessor(req, rsp, next);
    }

    public static void addParamToDistributedRequestWhitelist(final SolrQueryRequest req, final String... paramNames) {
        Set<String> whitelist = (Set<String>) req.getContext().computeIfAbsent(DistributedUpdateProcessor.PARAM_WHITELIST_CTX_KEY, key -> new TreeSet<>());
        Collections.addAll(whitelist, paramNames);
    }
}
