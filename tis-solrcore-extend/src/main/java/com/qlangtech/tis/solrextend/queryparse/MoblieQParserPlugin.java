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

import java.io.IOException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.FilterSpans.AcceptStatus;
import org.apache.lucene.search.spans.SpanPositionCheckQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

/*
 * 通过SpanQuery查询,专门查询某字段 位置term字段相关的查询，起始位置和長度
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MoblieQParserPlugin extends QParserPlugin {

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        String fieldName = localParams.get("f");
        int startPos = localParams.getInt("start_pos", 2);
        int length = localParams.getInt("length", 0);
        SpanTermQuery tq = new SpanTermQuery(new Term(fieldName, qstr));
        final MobileSpanPositionCheckQuery fquery = new MobileSpanPositionCheckQuery(tq, startPos, length);
        return new QParser(qstr, localParams, params, req) {

            @Override
            public Query parse() throws SyntaxError {
                return fquery;
            }
        };
    }

    private class MobileSpanPositionCheckQuery extends SpanPositionCheckQuery {

        private final int length;

        private final int startPos;

        public MobileSpanPositionCheckQuery(SpanQuery match, int startPos, int length) {
            super(match);
            this.length = length;
            this.startPos = startPos;
        }

        @Override
        protected AcceptStatus acceptPosition(Spans spans) throws IOException {
            if (startPos == spans.startPosition() && length == spans.width()) {
                return AcceptStatus.YES;
            } else {
                return AcceptStatus.NO_MORE_IN_CURRENT_DOC;
            }
        // AcceptStatus res = (spans.startPosition() >= end) ?
        // AcceptStatus.NO_MORE_IN_CURRENT_DOC
        // : (spans.startPosition() >= start && spans.endPosition() <= end) ?
        // AcceptStatus.YES
        // : AcceptStatus.NO;
        // return res;
        }

        @Override
        public String toString(String field) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("mobileSpanPositionCheckQuery(");
            buffer.append(match.toString(field));
            buffer.append(startPos);
            buffer.append(", ");
            buffer.append(length);
            buffer.append(")");
            return buffer.toString();
        }
    }
}
