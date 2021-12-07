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
package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.util.mutable.MutableValue;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocListAndSet;

/**
 * 字段为 key1_value1;key_value2 式样，<br>
 * 计算结果需要将 所有命中结果的，执行一次小型map/reduce 操作
 * kindpay 字段统计<br>
 * kindpayId_count(payCount)_sum(fee)
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PairValSummary extends SearchComponent {

    public static final String COMPONENT_NAME = "structPairSummary";

    public static final String FIELD_MAP_REDUCE = "mr";

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        if (rb.req.getParams().getBool(FIELD_MAP_REDUCE, false)) {
            // rb.setNeedDocList(true);
            rb.setNeedDocSet(true);
        }
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        if (!rb.req.getParams().getBool(COMPONENT_NAME, false)) {
            return;
        }
        SolrParams params = rb.req.getParams();
        IndexSchema schema = rb.req.getSchema();
        String fieldName = params.get(FIELD_MAP_REDUCE + ".field");
        SchemaField field = schema.getField(fieldName);
        ValueSource valueSource = field.getType().getValueSource(field, null);
        DocListAndSet results = rb.getResults();
        JSONFloatPairCollector collector = new JSONFloatPairCollector(valueSource, new HashMap<Object, Object>());
        rb.req.getSearcher().search(results.docSet.getTopFilter(), collector);
        // for (Map.Entry<String, AtomicDouble> entry : collector.summary
        // .entrySet()) {
        // System.out.println(entry.getKey() + ":" + entry.getValue());
        // }
        // Map<String, Double> result = new HashMap<>();
        // for (Map.Entry<String, AtomicDouble> entry : collector.summary
        // .entrySet()) {
        // result.put(entry.getKey(), entry.getValue().get());
        // }
        rb.rsp.add(fieldName + "_mr", ResultUtils.writeList(collector.summary.values()));
    }

    public static class KindPayStatis {

        private final String kindpayId;

        private int payCount;

        private float allFee;

        public KindPayStatis(String kindpayId) {
            super();
            this.kindpayId = kindpayId;
        }

        public void addFee(int payCount, float fee) {
            this.allFee += fee;
            this.payCount += payCount;
        }

        @Override
        public String toString() {
            return ("kindpayId:\"" + kindpayId + "\",payCount:" + payCount + ",allFee:" + allFee);
        }
    }

    private static class JSONFloatPairCollector extends SimpleCollector {

        private final ValueSource groupByVS;

        private final Map<?, ?> vsContext;

        private FunctionValues.ValueFiller filler;

        private MutableValue mval;

        Map<String, KindPayStatis> /* kindpayid */
        summary = new HashMap<String, KindPayStatis>();

        /**
         */
        public JSONFloatPairCollector(ValueSource groupByVS, Map<?, ?> vsContext) {
            super();
            this.groupByVS = groupByVS;
            this.vsContext = vsContext;
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.COMPLETE_NO_SCORES;
        }

        // @Override
        // public boolean needsScores() {
        // return false;
        // }
        protected void doSetNextReader(LeafReaderContext context) throws IOException {
            FunctionValues funcValues = groupByVS.getValues(vsContext, context);
            filler = funcValues.getValueFiller();
            mval = filler.getValue();
        }

        @Override
        public void collect(int doc) throws IOException {
            filler.fillValue(doc);
            if (!mval.exists) {
                return;
            }
            String value = String.valueOf(mval.toObject());
            String[] args = StringUtils.split(value, ";");
            String[] pair = null;
            String key = null;
            KindPayStatis v = null;
            for (int i = 0; i < args.length; i++) {
                pair = StringUtils.split(args[i], "_");
                if (pair.length < 3) {
                    continue;
                }
                key = pair[0];
                if ((v = summary.get(key)) == null) {
                    v = new KindPayStatis((pair[0]));
                    summary.put(key, v);
                }
                // count _ fee
                v.addFee(Integer.parseInt(pair[1]), Float.parseFloat(pair[2]));
            }
        }
    }

    @Override
    public String getDescription() {
        return COMPONENT_NAME;
    }
}
