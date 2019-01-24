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
package com.qlangtech.tis.solrextend.dir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// import org.apache.solr.common.StringUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.NRTCachingDirectoryFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisNRTCachingDirectoryFactory extends NRTCachingDirectoryFactory {

    // private ITisCoreContext coreDesc;
    private SolrParams params;

    private static final Log log = LogFactory.getLog(TisNRTCachingDirectoryFactory.class);

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
