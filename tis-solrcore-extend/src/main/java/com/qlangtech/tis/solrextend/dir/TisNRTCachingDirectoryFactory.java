/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.dir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.apache.solr.common.StringUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.NRTCachingDirectoryFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisNRTCachingDirectoryFactory extends NRTCachingDirectoryFactory {

    // private ITisCoreContext coreDesc;
    private SolrParams params;

    private static final Logger log = LoggerFactory.getLogger(TisNRTCachingDirectoryFactory.class);

    private double maxMergeSizeMB;

    private double maxCachedMB;

    @Override
    @SuppressWarnings("all")
    public void init(NamedList args) {
        super.init(args);
        this.params = SolrParams.toSolrParams(args);
        maxMergeSizeMB = params.getDouble("maxMergeSizeMB", DEFAULT_MAX_MERGE_SIZE_MB);
        if (maxMergeSizeMB <= 0) {
            throw new IllegalArgumentException("maxMergeSizeMB must be greater than 0");
        }
        maxCachedMB = params.getDouble("maxCachedMB", DEFAULT_MAX_CACHED_MB);
        if (maxCachedMB <= 0) {
            throw new IllegalArgumentException("maxCachedMB must be greater than 0");
        }
    // this.coreDesc = (ITisCoreContext) args.get(TisSolrConfig.TIS_CORE_DESC);
    // if (this.coreDesc == null) {
    // throw new IllegalStateException("this.coreDesc can not be null");
    // }
    }
}
