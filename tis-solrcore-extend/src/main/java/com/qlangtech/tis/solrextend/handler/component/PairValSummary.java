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
package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.util.mutable.MutableValue;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocListAndSet;

/*
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
        public boolean needsScores() {
            return false;
        }

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
