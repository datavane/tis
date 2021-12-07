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

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.search.Query;
//import org.apache.lucene.search.payloads.MaxPayloadFunction;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 在solrconfig.xml中配置一个对应的qp类型
 * <queryParser name="daterange" class="com.qlangtech.tis.solrextend.queryparse.s4supplyGoods.PayloadFilterQParserPlugin"/>
 * qp的语法如下
 * q.setQuery("{!daterange f=stock_change_log_last_create_time value=564e269101564e4482aa01e4}20161001_20161030");
 * 后面的日期可以不提供，默认搜索所有范围<br>
 */
public class PayloadFilterQParserPlugin extends QParserPlugin {
    private static final Logger log = LoggerFactory.getLogger(PayloadFilterQParserPlugin.class);
    // 日期相关，用来计算payload中的间隔天数

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params,
                                SolrQueryRequest req) {
        final String fieldName = localParams.get(CommonParams.FIELD);
        final String fieldValue = localParams.get("value");
        // fieldName字段中包含fieldValue
        Term term = new Term(fieldName, fieldValue);

        float fromDate;
        float toDate;
        String[] dates = qstr.split("_", 2);
        if (dates.length == 2) {
            fromDate = Float.parseFloat(dates[0]);
            toDate = Float.parseFloat(dates[1]);
        } else {
            fromDate = Float.MIN_VALUE;
            toDate = Float.MAX_VALUE;
        }

        final Query q = new PayloadFilterQuery(new SpanTermQuery(term), fromDate, toDate);
        return new QParser(qstr, localParams, params, req) {
            @Override
            public Query parse() throws SyntaxError {
                return q;
            }
        };
    }

    @Override
    public void init(NamedList namedList) {
    }
}
