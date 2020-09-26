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
