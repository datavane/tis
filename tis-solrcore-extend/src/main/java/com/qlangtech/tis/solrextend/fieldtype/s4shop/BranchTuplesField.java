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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.util.List;
import java.util.Map;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;

/**
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
