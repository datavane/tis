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
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * 基于allmenu字段作聚合查询
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TripleValueMapReduceComponent extends SearchComponent {

    public static final String COMPONENT_NAME = "TripleValueMapReduceComponent";

    private static final String FIELD_MAP_REDUCE = "triplemr";

    // private static final Logger logger = LoggerFactory
    // .getLogger(TripleValueMapReduceComponent.class);
    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        if (rb.req.getParams().getBool(getComponentName(), false)) {
            rb.setNeedDocSet(true);
        }
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        if (!rb.req.getParams().getBool(getComponentName(), false)) {
            return;
        }
        SolrParams params = rb.req.getParams();
        IndexSchema schema = rb.req.getSchema();
        boolean kindmenuInclude = Boolean.parseBoolean(params.get(getComponentName() + ".kindmenu.include", "false"));
        String kindmenuIncludeValue = null;
        if (kindmenuInclude) {
            kindmenuIncludeValue = params.get(getComponentName() + ".kindmenu.include.value");
        }
        String fieldName = params.get(getComponentName() + ".field");
        SchemaField field = schema.getField(fieldName);
        ValueSource valueSource = field.getType().getValueSource(field, null);
        DocListAndSet results = rb.getResults();
        String groupByKey = params.get("groupby.key");
        if (groupByKey == null) {
            throw new IllegalArgumentException("param groupby.key can not be null");
        }
        InstanceCollector collector = createCollector(params, valueSource, groupByKey, kindmenuIncludeValue);
        rb.req.getSearcher().search(results.docSet.getTopFilter(), collector);
        rb.rsp.add(fieldName + "_" + getComponentName(), ResultUtils.writeMap(collector.getStatiResult()));
    }

    /**
     * @param valueSource
     * @param groupByKey
     * @return
     */
    protected InstanceCollector createCollector(SolrParams params, ValueSource valueSource, String groupByKey, String kindmenuIncludeValue) {
        return new JSONFloatPairCollector(groupByKey, valueSource, new HashMap<Object, Object>(), kindmenuIncludeValue);
    }

    public abstract static class InstanceCollector extends SimpleCollector {

        private final ValueSource groupByVS;

        private final Map<?, ?> vsContext;

        private FunctionValues.ValueFiller filler;

        private MutableValue mval;

        Map<String, CountAndFee> summary = new HashMap<String, CountAndFee>();

        private final String kindmenuIncludeValue;

        public InstanceCollector(ValueSource groupByVS, Map<?, ?> vsContext, String kindmenuIncludeValue) {
            super();
            this.groupByVS = groupByVS;
            this.vsContext = vsContext;
            this.kindmenuIncludeValue = kindmenuIncludeValue;
        }

        public abstract Map<String, ?> getStatiResult();

        @Override
        public final boolean needsScores() {
            return false;
        }

        protected final void doSetNextReader(LeafReaderContext context) throws IOException {
            FunctionValues funcValues = groupByVS.getValues(vsContext, context);
            filler = funcValues.getValueFiller();
            mval = filler.getValue();
        }

        @Override
        public final void collect(int doc) throws IOException {
            filler.fillValue(doc);
            if (!mval.exists) {
                return;
            }
            String value = String.valueOf(mval.toObject());
            String[] args = StringUtils.split(value, ";");
            AllMenuRow menuRow = null;
            for (int i = 0; i < args.length; i++) {
                try {
                    menuRow = parseRow(args[i]);
                    if (menuRow == null) {
                        continue;
                    }
                    if (kindmenuIncludeValue != null) {
                        if (!StringUtils.equals(kindmenuIncludeValue, menuRow.getKindMenuid())) {
                            continue;
                        }
                    }
                    processInstance(menuRow);
                } catch (Exception e) {
                    throw new RuntimeException("all_menu:" + args[i], e);
                }
            }
        }

        protected abstract void processInstance(AllMenuRow menuRow);
    }

    private static class JSONFloatPairCollector extends InstanceCollector {

        private String groupByKey;

        Map<String, CountAndFee> summary = new HashMap<String, CountAndFee>();

        @Override
        public Map<String, ?> getStatiResult() {
            return this.summary;
        }

        public JSONFloatPairCollector(String groupByKey, ValueSource groupByVS, Map<?, ?> vsContext, String kindmenuIncludeValue) {
            super(groupByVS, vsContext, kindmenuIncludeValue);
            this.groupByKey = groupByKey;
        }

        /**
         * @param menuRow
         * @return
         */
        protected void processInstance(AllMenuRow menuRow) {
            String key;
            CountAndFee v;
            key = getGroupKey(menuRow);
            if ((v = summary.get(key)) == null) {
                summary.put(key, createCountAndFee(this.groupByKey, menuRow));
            } else {
                v.add(menuRow);
            }
        }

        /**
         * @param pair
         * @param key
         * @return
         */
        protected String getGroupKey(AllMenuRow menuRow) {
            String key = null;
            if (GROUP_KEY_NAME.equals(groupByKey)) {
                // pair[1];
                key = menuRow.getMenuId();
                if ("0".equals(key)) {
                    key = menuRow.getName();
                }
            } else if (GROUP_KEY_KIND.equals(groupByKey)) {
                key = menuRow.getKindMenuid();
            } else {
                throw new IllegalArgumentException("param groupByKey:" + groupByKey + " is illegal");
            }
            return key;
        }
    }

    private static final String GROUP_KEY_NAME = "name";

    public static final String GROUP_KEY_KIND = "kind";

    // 一共10个
    private static final Pattern PATTERN_TUPLES = Pattern.compile("(.*?)_(.*?)_(.*?)_(.*?)_(.*?)_(.*?)_(.*?)_(.*?)_(.*?)");

    private static final int TUPLE_COUNT = 9;

    public static String[] getAllMenuTuple(String i) {
        String[] tuple = null;
        Matcher m = PATTERN_TUPLES.matcher(i);
        if (m.matches()) {
            tuple = new String[TUPLE_COUNT];
            for (int j = 0; j < TUPLE_COUNT; j++) {
                tuple[j] = m.group(j + 1);
            }
        }
        return tuple;
    }

    /**
     * @param pair
     * @return
     */
    public static CountAndFeeGroupByMenuName createCountAndFee(String groupByKey, AllMenuRow menuRow) {
        if (GROUP_KEY_NAME.equals(groupByKey)) {
            return new CountAndFeeGroupByMenuName(menuRow);
        } else if (GROUP_KEY_KIND.equals(groupByKey)) {
            return new CountAndFeeGroupByMenuName(menuRow);
        } else {
            throw new IllegalArgumentException("param groupByKey:" + groupByKey + " is illegal");
        }
    }

    // 经过无数次试验使用以下参数，最后计算出来的sum结果会和pg数据库的结果一致
    public static final MathContext BigDecimalContext = MathContext.UNLIMITED;

    // new MathContext(8,
    // RoundingMode.CEILING);
    public static AllMenuRow parseRow(String strPair) {
        String[] pair = getAllMenuTuple(strPair);
        if (pair == null) {
            return null;
        }
        // logger.warn("baisui-" + pair[2] + "-" + pair[5]);
        final TisMoney ratioFee = parseFloat(pair[5]);
        final TisMoney fee = parseFloat(pair[4]);
        return new AllMenuRow(parseFloat(pair[3]), /* num */
        pair[6], parseFloat(pair[7]), /* accountNum */
        pair[8], fee, ratioFee, pair[0], /* menu_id */
        pair[1], /* name */
        pair[2]);
    }

    private static TisMoney parseFloat(String val) {
        try {
            // new BigDecimal(val,
            return TisMoney.create(val);
        // BigDecimalContext);//
        // Float.parseFloat(val);
        } catch (Throwable e) {
        }
        return TisMoney.create();
    }

    public static void main(String[] arg) {
    // System.out.println(Float.parseFloat(null));
    // Matcher m = PATTERN_TUPLES.matcher(
    // c817f92b3e_鱼羊鲜_00034204508e749e015098c80cff2aff_1.0_38.0_38.0_份_1.0_份
    // "1123123123_清水面_00034204508e749e015098c84e742cea_1.0_8.0_8.0_份_1.0");
    // System.out.println(m.matches());
    }

    public static class AllMenuRow {

        // 订购份额数 如果是鱼的话就是num条鱼
        private final TisMoney num;

        private final String unit;

        // 点菜单位一条鱼0.5斤为一个单位，如果是两斤那就是4个单位
        private final TisMoney accountNum;

        private final String accountUnit;

        // 花费费用
        private final TisMoney fee;

        /**
         * 折后费用
         */
        private final TisMoney ratioFee;

        private final String menuId;

        private final String name;

        private final String kindMenuid;

        /**
         * @param num
         * @param unit
         * @param accountNum
         * @param accountUnit
         * @param fee
         * @param ratioFee
         */
        public AllMenuRow(TisMoney num, String unit, TisMoney accountNum, String accountUnit, TisMoney fee, TisMoney ratioFee, String menuId, String name, String kindMenuid) {
            super();
            this.num = num;
            this.unit = unit;
            this.accountNum = accountNum;
            this.accountUnit = accountUnit;
            this.fee = fee;
            this.ratioFee = ratioFee;
            this.menuId = menuId;
            this.name = name;
            this.kindMenuid = kindMenuid;
        }

        public TisMoney getNum() {
            return num;
        }

        public String getUnit() {
            return unit;
        }

        public TisMoney getAccountNum() {
            return accountNum;
        }

        public String getAccountUnit() {
            return accountUnit;
        }

        public TisMoney getFee() {
            return this.fee;
        }

        public TisMoney getRatioFee() {
            return this.ratioFee;
        }

        public String getMenuId() {
            return menuId;
        }

        public String getName() {
            return name;
        }

        public String getKindMenuid() {
            return kindMenuid;
        }
    }

    /**
     * group by menukind
     *
     * @date 2015年12月5日 下午2:23:47
     */
    public static class CountAndFee {

        // 订购份额数
        protected TisMoney num;

        // 花费费用
        protected TisMoney fee;

        /**
         * 折后费用
         */
        private TisMoney ratioFee;

        public CountAndFee(AllMenuRow menuRow) {
            super();
            this.num = menuRow.getNum();
            // this.num = menuRow.getAccountNum();
            this.fee = menuRow.getFee();
            this.ratioFee = menuRow.getRatioFee();
        }

        public TisMoney getCount() {
            return this.num;
        }

        public TisMoney getFee() {
            return this.fee;
        }

        public TisMoney getRatioFee() {
            return this.ratioFee;
        }

        public void add(AllMenuRow menuRow) {
            // = this.num.add();
            this.num.add(menuRow.getNum());
            // this.num += menuRow.getAccountNum();
            // = this.fee.add();
            this.fee.add(menuRow.getFee());
            // = this.ratioFee.add();
            this.ratioFee.add(menuRow.getRatioFee());
        }

        @Override
        public String toString() {
            return "num:" + num + ",fee:" + this.fee.format() + ",ratioFee:" + this.ratioFee.format();
        }
    }

    public static class CountAndFeeGroupByMenuName extends CountAndFee {

        // 2(unm)份（unit）/1.7(accountNum)斤（account_unit）
        private String unit;

        private String accountUnit;

        private TisMoney accountNum;

        private final String menuName;

        /**
         * @param count
         * @param fee
         * @param ratiofee
         */
        public CountAndFeeGroupByMenuName(AllMenuRow menuRow) {
            super(menuRow);
            this.unit = menuRow.getUnit();
            this.accountUnit = menuRow.getAccountUnit();
            this.accountNum = menuRow.getAccountNum();
            this.menuName = menuRow.getName();
        }

        /**
         * 业务处理需要这里需乘上一个系数
         *
         * @param value
         */
        public void addCoefficient(BigDecimal value) {
            // =
            this.accountNum.addCoefficient(value);
            // this.accountNum.multiply(value);//
            // *= value;
            // = this.num.multiply(value);//
            this.num.addCoefficient(value);
            // value;
            // = this.fee.multiply(value);
            this.fee.addCoefficient(value);
        }

        @Override
        public void add(AllMenuRow menuRow) {
            super.add(menuRow);
            if (StringUtils.isEmpty(unit)) {
                this.unit = menuRow.getUnit();
            }
            if (StringUtils.isEmpty(accountUnit)) {
                this.accountUnit = menuRow.getAccountUnit();
            }
            // =
            this.accountNum.add(menuRow.getAccountNum());
        // this.accountNum.add(menuRow.getAccountNum());
        }

        public TisMoney getAccountNum() {
            return accountNum;
        }

        @Override
        public String toString() {
            return super.toString() + ",unit:\"" + unit + "\",accountUnit:\"" + accountUnit + "\",accountNum:" + accountNum + ",menuName:\"" + this.menuName + "\"";
        }
    }

    @Override
    public String getDescription() {
        return COMPONENT_NAME;
    }

    protected String getComponentName() {
        return FIELD_MAP_REDUCE;
    }
}
