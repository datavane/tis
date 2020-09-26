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
package com.qlangtech.tis.solrextend.fieldtype;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.schema.SimilarityFactory;

/**
 *  进行payload进行排序，其他排序因子都要忽略 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISPayloadSimilarityFactory extends SimilarityFactory {

    private static final Similarity SIMILARITY = new Similarity() {

        @Override
        public long computeNorm(FieldInvertState fieldInvertState) {
            return 0;
        }

        @Override
        public SimScorer scorer(float v, CollectionStatistics collectionStatistics, TermStatistics... termStatistics) {
            return null;
        }
    };

    @Override
    public Similarity getSimilarity() {
        return SIMILARITY;
    }
}
