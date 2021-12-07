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
package com.qlangtech.tis.solrj.extend.router;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.HashBasedRouter;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.Hash;

/**
 * 使用一种最普通的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HashcodeRouter extends HashBasedRouter {

    public static final String NAME = "strhash";

    // 经过测试spark中hash 使用 seed是42 ,对应spark的函数算出来的结果是一致的 select  hash( cast( '99926497' as string) ) ;
    private static final int seed_murmurhash3 = 42;

    @Override
    public int sliceHash(String id, SolrInputDocument sdoc, SolrParams params, DocCollection collection) {
        int collectionNum = collection.getSlices().size();
        int mod = getMod(id, collectionNum);
        mod = mod < 0 ? mod * -1 : mod;
        // cause the shard name start from 1
        return mod + 1;
    }

    protected int getMod(String id, int collectionNum) {
        return Hash.murmurhash3_x86_32(id, 0, id.length(), seed_murmurhash3) % collectionNum;
    }

    @Override
    protected Slice hashToSlice(int hash, DocCollection collection) {
        Slice slice = collection.getSlice(ZkStateReader.SHARD_ID_PROP + hash);
        if (slice != null) {
            return slice;
        }
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No slice servicing hash code " + hash + " in " + collection);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
