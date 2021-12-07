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

package com.qlangtech.tis.solrextend.queryparse.s4supplygoods;

import com.qlangtech.tis.solrextend.fieldtype.TagPayloadTokenizer;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermStates;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafSimScorer;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class PayloadFilterQuery extends SpanQuery {

    private final SpanQuery wrappedQuery;
    // private final PayloadFunction function;
    private final float from;
    private final float to;


    public PayloadFilterQuery(SpanQuery wrappedQuery, float from, float to) {
        this.wrappedQuery = wrappedQuery;

        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return wrappedQuery.hashCode();
    }

    @Override
    public String getField() {
        return wrappedQuery.getField();
    }

    @Override
    public String toString(String field) {
        return "PayloadSpanQuery[" + wrappedQuery.toString(field) + ";]";
    }

    @Override
    public SpanWeight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) throws IOException {
        SpanWeight innerWeight = wrappedQuery.createWeight(searcher, scoreMode, boost);
        if (scoreMode == ScoreMode.COMPLETE_NO_SCORES) {
            return innerWeight;
        }
        return new PayloadSpanWeight(searcher, innerWeight, from, to);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayloadFilterQuery)) return false;
        // if (!super.equals(o)) return false;

        PayloadFilterQuery that = (PayloadFilterQuery) o;

        return wrappedQuery.equals(that.wrappedQuery);
    }

//    @Override
//    public int hashCode() {
//        int result = this.hashCode();
//        result = 31 * result + (wrappedQuery != null ? wrappedQuery.hashCode() : 0);
//        result = 31 * result + (function != null ? function.hashCode() : 0);
//        return result;
//    }

    private class PayloadSpanWeight extends SpanWeight {

        private final SpanWeight innerWeight;
        private final float from;
        private final float to;

        public PayloadSpanWeight(IndexSearcher searcher, SpanWeight innerWeight, float from, float to) throws IOException {
            // SpanQuery query, IndexSearcher searcher, Map<Term, TermStates> termStates, float boost
            super(PayloadFilterQuery.this, searcher, null, 0);
            this.innerWeight = innerWeight;
            this.from = from;
            this.to = to;
        }

        @Override
        public void extractTermStates(Map<Term, TermStates> contexts) {
            innerWeight.extractTermStates(contexts);
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return false;
        }

        @Override
        public Spans getSpans(LeafReaderContext ctx, Postings requiredPostings) throws IOException {
            return innerWeight.getSpans(ctx, requiredPostings.atLeast(Postings.PAYLOADS));
        }

        @Override
        public SpanScorer scorer(LeafReaderContext context) throws IOException {
            Spans spans = getSpans(context, Postings.PAYLOADS);
            if (spans == null) {
                return null;
            }
            return new PayloadSpanScorer(spans, this, innerWeight.getSimScorer(context));
        }
//        @Override
//        public Scorer scorer(LeafReaderContext context) throws IOException {
//            Spans spans = getSpans(context, Postings.PAYLOADS);
//            if (spans == null)
//                return null;
//            return new PayloadSpanScorer(spans, this, innerWeight.getSimScorer(context));
//        }

        @Override
        public void extractTerms(Set<Term> terms) {
            innerWeight.extractTerms(terms);
        }

        float getFrom() {
            return from;
        }

        float getTo() {
            return PayloadFilterQuery.this.to;
        }
    }

    private class PayloadSpanScorer extends SpanScorer implements SpanCollector {

        private float payloadScore;
        private final float from;
        private final float to;

        private PayloadSpanScorer(Spans spans, PayloadSpanWeight weight, LeafSimScorer docScorer) throws IOException {
            //SpanWeight weight, Spans spans, LeafSimScorer docScorer
            super(weight, spans, docScorer);
            this.from = weight.getFrom();
            this.to = weight.getTo();
        }

        @Override
        public void collectLeaf(PostingsEnum postings, int position, Term term) throws IOException {
            BytesRef payload = postings.getPayload();
            if (payload == null) {
                return;
            }

            if (isValidPayload(TagPayloadTokenizer.decode(payload))) {
                payloadScore = 1f;
            }
        }

        protected float getPayloadScore() {
            return payloadScore;
        }


        protected float getSpanScore() throws IOException {
            return super.scoreCurrentDoc();
        }

        @Override
        protected float scoreCurrentDoc() throws IOException {
            return getSpanScore() * getPayloadScore();
        }

        @Override
        public void reset() {
            payloadScore = 0;
        }

        boolean isValidPayload(float payload) {
            return (payload >= this.from && payload <= this.to);
        }

    }


}


