package com.qlangtech.tis.solrextend.queryparse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.FilterSpans;
import org.apache.lucene.search.spans.FilterSpans.AcceptStatus;
import org.apache.lucene.search.spans.SpanPositionCheckQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

/**
 * 通过SpanQuery查询,专门查询某字段 位置term字段相关的查询，起始位置和長度
 * 
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2017年12月5日
 */
public class MoblieQParserPlugin extends QParserPlugin {

	@Override
	@SuppressWarnings("all")
	public void init(NamedList args) {
	}

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		String fieldName = localParams.get("f");
		int startPos = localParams.getInt("start_pos", 2);
		final boolean not = localParams.getBool("not", false);

		SpanTermQuery tq = new SpanTermQuery(new Term(fieldName, qstr));
		final MobileSpanPositionCheckQuery fquery = new MobileSpanPositionCheckQuery(tq, startPos,
				StringUtils.length(qstr));
		return new QParser(qstr, localParams, params, req) {
			@Override
			public Query parse() throws SyntaxError {
				if (not) {
					BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
					qbuilder.add(fquery, Occur.MUST_NOT);
					return qbuilder.build();
				} else {
					return fquery;
				}
			}
		};
	}

	public static class MobileSpanPositionCheckQuery extends SpanPositionCheckQuery {

		private final int startPos;
		private final int end;

		public MobileSpanPositionCheckQuery(SpanQuery match, int startPos, int length) {
			super(match);
			this.startPos = startPos;
			this.end = startPos + length;
		}

		@Override
		public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {

			// return super.createWeight(searcher, needsScores);
			SpanWeight matchWeight = match.createWeight(searcher, false, boost);
			return new SpanPositionCheckWeight(matchWeight, searcher, needsScores ? getTermContexts(matchWeight) : null,
					boost);
		}

		@Override
		protected AcceptStatus acceptPosition(Spans spans) throws IOException {
			TermSpans termsSpan = (TermSpans) spans;
			PostingsEnum posting = termsSpan.getPostings();

			if (posting.startOffset() >= end) {
				return AcceptStatus.NO_MORE_IN_CURRENT_DOC;
			} else if (posting.startOffset() == startPos) {
				return AcceptStatus.YES;
			} else {
				return AcceptStatus.NO;
			}
		}

		@Override
		public String toString(String field) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("mobileSpanPositionCheckQuery(");
			buffer.append(match.toString(field));
			buffer.append(startPos);
			buffer.append(")");
			return buffer.toString();
		}

		public class SpanPositionCheckWeight extends SpanWeight {

			final SpanWeight matchWeight;

			public SpanPositionCheckWeight(SpanWeight matchWeight, IndexSearcher searcher, Map<Term, TermContext> terms,
					float boost) throws IOException {
				super(MobileSpanPositionCheckQuery.this, searcher, terms, boost);
				this.matchWeight = matchWeight;
			}

			@Override
			public boolean isCacheable(LeafReaderContext ctx) {
				return true;
			}

			@Override
			public void extractTerms(Set<Term> terms) {
				matchWeight.extractTerms(terms);
			}

			@Override
			public void extractTermContexts(Map<Term, TermContext> contexts) {
				matchWeight.extractTermContexts(contexts);
			}

			// @Override
			// public Scorer scorer(LeafReaderContext context) throws
			// IOException {
			// return null;
			// }

			@Override
			public Spans getSpans(final LeafReaderContext context, Postings requiredPostings) throws IOException {
				Spans matchSpans = matchWeight.getSpans(context, Postings.OFFSETS);
				if (matchSpans == null) {
					return null;
				}
				return new FilterSpans(matchSpans) {
					@Override
					protected AcceptStatus accept(Spans candidate) throws IOException {
						return acceptPosition(candidate);
					}
				};
			}

		}
	}

}
