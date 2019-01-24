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
package com.qlangtech.tis.solrextend.queryparse;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.BitSet;
import java.io.IOException;
import java.util.Set;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BitQuery extends Query {

	private final DocIdSetIterator docIdSetIterator;
	private final BitDocIdSet docIdSet;

	public BitQuery(BitSet bitSet) {
		super();
		this.docIdSet = new BitDocIdSet(bitSet);
		this.docIdSetIterator = docIdSet.iterator();
	}

	protected float getScore(int docid) {
		return 1;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		return new BitQueryWeight(this);
	}

	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return docIdSet.hashCode();
	}

	@Override
	public String toString(String field) {
		return field;
	}

	final class BitQueryWeight extends Weight {

		private BitQueryWeight(Query query) {
			super(query);
		}

		@Override
		public void extractTerms(Set<Term> terms) {
		}

		@Override
		public Explanation explain(LeafReaderContext context, int doc) throws IOException {
			return Explanation.noMatch("no avalible");
		}

		@Override
		public boolean isCacheable(LeafReaderContext ctx) {
			return true;
		}

		@Override
		public Scorer scorer(LeafReaderContext context) throws IOException {
			return new BitScorer(this, context);
		}

		@Override
		public String toString() {
			return "weight(" + BitQueryWeight.this + ")";
		}
	}

	final class BitScorer extends Scorer {

		// 对应该段的maxDoc
		final int maxDoc;

		final int docBase;

		@Override
		public DocIdSetIterator iterator() {
			return new DocIdSetIterator() {

				@Override
				public int docID() {
					return doc;
				}

				@Override
				public int nextDoc() throws IOException {
					doc = advance(doc + 1);
					return doc;
				}

				/**
				 * 返回第一个大于等于target的docId
				 */
				@Override
				public int advance(int target) throws IOException {
					if (target >= maxDoc) {
						doc = NO_MORE_DOCS;
						return NO_MORE_DOCS;
					}
					doc = docIdSetIterator.advance(target + docBase) - docBase;
					if (doc >= maxDoc) {
						doc = NO_MORE_DOCS;
						return NO_MORE_DOCS;
					} else {
						return doc;
					}
				}

				@Override
				public long cost() {
					return 0;
				}
			};
		}

		BitScorer(Weight weight, LeafReaderContext context) {
			super(weight);
			docBase = context.docBase;
			maxDoc = context.reader().maxDoc();
		}

		private int doc = -1;

		@Override
		public int docID() {
			return doc;
		}

		@Override
		public float score() {
			return getScore(docID());
		}

		/**
		 * Returns a string representation of this <code>TermScorer</code>.
		 */
		@Override
		public String toString() {
			return "scorer(" + weight + ")";
		}
	}
}
