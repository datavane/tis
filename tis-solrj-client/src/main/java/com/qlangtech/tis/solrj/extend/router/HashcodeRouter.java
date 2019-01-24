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
package com.qlangtech.tis.solrj.extend.router;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.HashBasedRouter;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.SolrParams;

/*
 * 使用一种最普通的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HashcodeRouter extends HashBasedRouter {

    public static final String NAME = "strhash";

    @Override
    public int sliceHash(String id, SolrInputDocument sdoc, SolrParams params, DocCollection collection) {
        int collectionNum = collection.getSlices().size();
        int mod = id.hashCode() % collectionNum;
        mod = mod < 0 ? mod * -1 : mod;
        // cause the shard name start from 1 
        return mod + 1;
    }

    @Override
    protected Slice hashToSlice(int hash, DocCollection collection) {
        Slice slice = collection.getSlice(ZkStateReader.SHARD_ID_PROP + hash);
        if (slice != null) {
            return slice;
        }
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No slice servicing hash code " + hash + " in " + collection);
    }
}
