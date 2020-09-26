/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
