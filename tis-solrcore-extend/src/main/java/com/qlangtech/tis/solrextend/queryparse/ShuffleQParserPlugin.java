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

package com.qlangtech.tis.solrextend.queryparse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.util.PriorityQueue;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.MergeStrategy;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;

/**
 * 需要将结果集打散,现有预购项目中單個品牌的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年7月17日
 */
public class ShuffleQParserPlugin extends QParserPlugin {

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new ShuffleQParser(qstr, localParams, params, req);
    }

    private class ShuffleQParser extends QParser {

        public ShuffleQParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
            super(query, localParams, params, req);
        }

        @Override
        public Query parse() throws SyntaxError {
            int mod = this.localParams.getInt("mod", 1);
            float multi = this.localParams.getFloat("multi", 1.0f);
            // QParser reRankParser = QParser.getParser(this.qstr, null, req);
            // Query originQuery = reRankParser.parse();

            return new ShuffleQuery(mod, multi);
        }
    }

    private static Query defaultQuery = new MatchAllDocsQuery();

    private final class ShuffleQuery extends RankQuery {
        private Query mainQuery;

        public int hashCode() {
            return 31 * mainQuery.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
//        public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
//            return mainQuery.createWeight(searcher, needsScores);
//        }

        private final int mod;
        private final float multi;

        public ShuffleQuery(int mod, float multi) {
            // this.originQuery = originQuery;
            this.mod = mod;
            this.multi = multi;
        }

        public RankQuery wrap(Query _mainQuery) {
            if (_mainQuery != null) {
                this.mainQuery = _mainQuery;
            }
            return this;
        }

        @Override
        public Query rewrite(IndexReader reader) throws IOException {

            return super.rewrite(reader);
        }

        public MergeStrategy getMergeStrategy() {
            return null;
        }

        @Override
        public TopDocsCollector<ScoreDoc> getTopDocsCollector(int len, QueryCommand cmd, IndexSearcher searcher)
                throws IOException {

            PriorityQueue<ScoreDoc> queue = TISHitQueue.create(len);
            return new ShufferCollector(queue, cmd, searcher);
        }

        private class ShufferCollector extends TopDocsCollector<ScoreDoc> {
            ScoreDoc pqTop;
            // private Query originQuery;
            private IndexSearcher searcher;

            @Override
            public ScoreMode scoreMode() {
                return ScoreMode.COMPLETE;
            }

            public ShufferCollector(PriorityQueue<ScoreDoc> queue, // Query reRankQuery,
                                    QueryCommand cmd, IndexSearcher searcher) throws IOException {
                super(queue);
                this.searcher = searcher;
                this.pqTop = pq.top();
            }

            @Override
            public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
                final int docBase = context.docBase;
                return new ScorerLeafCollector() {

                    @Override
                    public void collect(int doc) throws IOException {

                        // 这样可以让score产生随机性，理论上来说 可以打乱结果
                        float score = scorer.score() + (doc % mod) * multi;

                        // This collector cannot handle these scores:
                        assert score != Float.NEGATIVE_INFINITY;
                        assert !Float.isNaN(score);

                        totalHits++;
                        if (score <= pqTop.score) {
                            return;
                        }
                        pqTop.doc = doc + docBase;
                        pqTop.score = score;
                        pqTop = pq.updateTop();
                    }

                };
            }

        }
    }

    abstract static class ScorerLeafCollector implements LeafCollector {
        Scorable scorer;

        @Override
        public void setScorer(Scorable scorer) throws IOException {
            this.scorer = scorer;
        }

    }
}
