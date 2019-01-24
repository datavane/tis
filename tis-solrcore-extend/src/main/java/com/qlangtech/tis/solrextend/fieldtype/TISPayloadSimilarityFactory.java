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
package com.qlangtech.tis.solrextend.fieldtype;

import java.io.IOException;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimWeight;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.solr.schema.SimilarityFactory;

/*
 *  进行payload进行排序，其他排序因子都要忽略 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISPayloadSimilarityFactory extends SimilarityFactory {

	private static final Similarity SIMILARITY = new Similarity() {

		@Override
		public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {

			return new SimWeight() {

				// @Override
				// public float getValueForNormalization() {
				// return 1f;
				// }
				//
				// @Override
				// public void normalize(float queryNorm, float topLevelBoost) {
				// }
			};
		}

		@Override
		public long computeNorm(FieldInvertState state) {
			throw new UnsupportedOperationException("This Similarity may only be used for searching, not indexing");
		}

		// @Override
		// public SimWeight computeWeight(float queryBoost, CollectionStatistics
		// collectionStats,
		// TermStatistics... termStats) {
		//
		// }
		@Override
		public SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
			return new SimScorer() {

				@Override
				public float score(int doc, float freq) {
					return 1f;
				}

				@Override
				public float computeSlopFactor(int distance) {
					return 1f;
				}

				@Override
				public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
					// if (payload == null) {
					return 1.0f;
					// } else {
					// return NumericUtils.prefixCodedToLong(payload);
					// }
				}
			};
		}
	};

	@Override
	public Similarity getSimilarity() {
		return SIMILARITY;
	}
}
