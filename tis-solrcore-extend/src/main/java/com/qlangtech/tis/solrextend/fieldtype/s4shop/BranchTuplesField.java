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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.util.List;
import java.util.Map;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BranchTuplesField extends TuplesField {

    // private SchemaField branch_eids_terms_count;
    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    // TrieIntField intType = (TrieIntField)
    // schema.getFieldTypes().get("int");
    // if (intType == null) {
    // StringBuffer typesDesc = new StringBuffer();
    // for (Map.Entry<String, org.apache.solr.schema.FieldType> entry :
    // schema.getFieldTypes().entrySet()) {
    // typesDesc.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
    // }
    // throw new IllegalStateException("can not get file type:int in
    // schema:" + typesDesc.toString());
    // }
    // this.branch_eids_terms_count = new
    // SchemaField("branch_eids_terms_count", intType,
    // OMIT_TF_POSITIONS | DOC_VALUES | STORED, "");
    }

    @Override
    protected void createTermsCountField(List<IndexableField> fields, int size) {
        NumericDocValuesField termsSize = new NumericDocValuesField("branch_eids_terms_count", size);
        fields.add(termsSize);
    // fields.add(branch_eids_terms_count.createField(size, 1 /* 必须为1 */));
    }
}
