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
package com.qlangtech.tis.indexbuilder.doc.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.qlangtech.tis.indexbuilder.doc.ReusableSolrInputDocument;
import com.qlangtech.tis.indexbuilder.map.RawDataProcessor;
import com.qlangtech.tis.indexbuilder.source.SourceReader;

/*
 * 这个要求全量文件采用union方式配置，比如，订单和会员，多对一的关系，需要多个相同会员的订单堆叠在一起，最后一条是会员记录<br>
 * 这样的文件结构方便构建nestDocument
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SequenceFileNestInputDocCreator extends AbstractInputDocCreator {

    private static final Logger logger = LoggerFactory.getLogger(SequenceFileNestInputDocCreator.class);

    private static final String KEY_DATA_TYPE = "data_type";

    private static final String KEY_CUSTOMER_REGISTER_ID = "customer_register_id";

    // '数据类型:0消费属性 1 基本属性'
    private static final String VAL_1 = "1";

    private static final String VAL_0 = "0";

    private static final String SCHEMA_TYPE_FIELD = "type";

    private static final String VAL_PARENT = "p";

    private static final String VAL_CHILD = "c";

    private static final String val_null_customer_register_id = "-1";

    private static final String NULL_VAL = "-1";

    private static final String NULL_VAL_FLOAT = "-1.0";

    // = Collections.singletonMap(KEY_DATA_TYPE, "1");
    private final Map<String, String> defaultKV;

    public SequenceFileNestInputDocCreator(RawDataProcessor rawDataProcessor, IndexSchema indexSchema, String newVersion) {
        super(rawDataProcessor, indexSchema, newVersion);
        this.defaultKV = Maps.newHashMap();
        for (SchemaField f : indexSchema.getFields().values()) {
            if (f.isRequired()) {
                defaultKV.put(f.getName(), NULL_VAL);
            }
        }
        this.defaultKV.put(KEY_DATA_TYPE, "1");
        this.defaultKV.put("nick_name", "-9");
    }

    private final AtomicInteger count1 = new AtomicInteger();

    @Override
    public SolrInputDocument createSolrInputDocument(SourceReader recordReader) throws Exception {
        Map<String, String> kv = null;
        // createDoc(defaultKV);
        SolrInputDocument parent = null;
        SolrInputDocument doc = null;
        String customer_register_id = null;
        // String isGroupLast = null;
        String dataType = null;
        int childCount = 0;
        while ((kv = recordReader.next()) != null) {
            customer_register_id = kv.get(KEY_CUSTOMER_REGISTER_ID);
            if (val_null_customer_register_id.equals(customer_register_id)) {
                printCount(parent);
                count1.incrementAndGet();
                continue;
            // return createDoc(kv);
            }
            // isGroupLast = kv.get(KEY_IS_GROUP_LAST);
            // KEY_DATA_TYPE,1:基本属性
            dataType = kv.get(KEY_DATA_TYPE);
            doc = createDoc(kv);
            // if (VAL_1.equals(isGroupLast)) {
            if (parent == null) {
                parent = createDoc(defaultKV);
            }
            if (VAL_0.equals(dataType)) {
                if (++childCount > 300) {
                // 每个父下面大于50个子的记录都去掉了
                // continue;
                // StringBuffer out = new StringBuffer("childDocNum Overhead,parent:\n");
                // int count = 0;
                // for (SolrInputDocument c : parent.getChildDocuments()) {
                // if (++count > 50) {
                // break;
                // }
                // for (String f : c.getFieldNames()) {
                // out.append(f).append(":").append(c.getFieldValue(f)).append(",");
                // }
                // out.append("\n");
                // }
                // 
                // throw new IllegalStateException(out.toString());
                }
                // 消费行为 这条parent记录需丢弃掉
                // logger.info("hasno_child,childCount:" + parent.getChildDocumentCount());
                parent.addChildDocument(doc);
            } else if (VAL_1.equals(dataType)) {
                // 基本屬性
                for (String key : doc.getFieldNames()) {
                    parent.setField(key, doc.getFieldValue(key));
                }
                return parent;
            }
        // count2.incrementAndGet();
        // }
        // count4.incrementAndGet();
        // parent.addChildDocument(doc);
        }
        printCount(parent);
        // }
        return doc;
    }

    private void printCount(SolrInputDocument parent) {
        if ((count1.incrementAndGet() % 10000) == 0) {
            System.out.print("count1:" + count1.get());
            if (parent != null) {
                System.out.print(",id:" + parent.getFieldValue(this.uniqueKeyFieldName));
            }
            System.out.println();
        }
    // if ((count.incrementAndGet() % 10000) == 0) {
    // System.out.println("count1:" + count1.get() + ",count2" + count2.get() +
    // ",count3:" + count3.get()
    // + ",count4:" + count4.get());
    // }
    }

    private SolrInputDocument createDoc(Map<String, String> kv) {
        SolrInputDocument doc = getLuceneDocument(kv);
        String dataType = kv.get(KEY_DATA_TYPE);
        doc.setField(SCHEMA_TYPE_FIELD, VAL_1.equals(dataType) ? VAL_PARENT : VAL_CHILD);
        return doc;
    }

    @Override
    protected ReusableSolrInputDocument createDocument() {
        return new DefaultReusableSolrInputDocument(indexSchema);
    }

    private static class DefaultReusableSolrInputDocument extends ReusableSolrInputDocument {

        private static final long serialVersionUID = 1L;

        public DefaultReusableSolrInputDocument(IndexSchema indexSchema) {
            super(indexSchema);
        }

        @Override
        public void setField(String name, Object value) {
            if (NULL_VAL.equals(value) || NULL_VAL_FLOAT.equals(value)) {
                return;
            }
            super.setField(name, value);
        }
    }
}
