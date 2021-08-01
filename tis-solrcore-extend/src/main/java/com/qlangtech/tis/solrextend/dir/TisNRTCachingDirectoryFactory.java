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
