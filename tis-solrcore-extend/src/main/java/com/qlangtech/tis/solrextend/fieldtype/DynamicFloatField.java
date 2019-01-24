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
package com.qlangtech.tis.solrextend.fieldtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.apache.solr.schema.TrieFloatField;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DynamicFloatField extends StrField {

	private static final Log log = LogFactory.getLog(DynamicFloatField.class);

	private static final List<IndexableField> NULL = Collections.emptyList();

	private TrieFloatField indexedField;

	protected void init(IndexSchema schema, Map<String, String> args) {
		super.init(schema, args);
		indexedField = (TrieFloatField) schema.getFieldTypes().get("tfloat");
		if (indexedField == null) {
			StringBuffer typesDesc = new StringBuffer();
			for (Map.Entry<String, FieldType> entry : schema.getFieldTypes().entrySet()) {
				typesDesc.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
			}
			throw new IllegalStateException("can not get file type:tfloat in schema:" + typesDesc.toString());
		}
	}

	@Override
	public List<IndexableField> createFields(SchemaField sf, Object value) {
		String externalVal = null;
		if (value == null || StringUtils.isBlank(externalVal = String.valueOf(value))) {
			return NULL;
		}
		List<IndexableField> fields = new ArrayList<IndexableField>();
		SchemaField tfield = null;
		try {
			String[] multifloat = StringUtils.split(externalVal, ";");
			String[] pair = null;
			for (String p : multifloat) {
				pair = StringUtils.split(p, "_");
				if (pair.length < 2) {
					continue;
				}
				tfield = new SchemaField("pt_" + pair[0], indexedField,
						INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS | STORED, "");
				fields.add(indexedField.createField(tfield, getFloatValue(pair[1])));
			}
			if ((sf.getProperties() & STORED) > 0) {
				fields.add(super.createField(new SchemaField(sf.getName(), sf.getType(),
						INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS | STORED, ""), value));
			}
			return fields;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private String getFloatValue(String value) {
		try {
			return String.format("%.2f", new Object[] { Double.valueOf(Float.parseFloat(value)) });
		} catch (NumberFormatException e) {
		}
		return "0";
	}

	public boolean isPolyField() {
		return true;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(String.format("%.2f", new Object[] { Double.valueOf(0.9999999999999999D) }));
	}
}
