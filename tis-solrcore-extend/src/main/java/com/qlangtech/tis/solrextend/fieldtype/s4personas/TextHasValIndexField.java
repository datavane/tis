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
package com.qlangtech.tis.solrextend.fieldtype.s4personas;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.StringUtils;
import org.apache.solr.schema.TextField;

/*
 * store的值不变，被索引的值根据field字段空或者不空来设置f或者t
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TextHasValIndexField extends TextField {
	
	@Override
	protected IndexableField createField(String name, String val, org.apache.lucene.index.IndexableFieldType type) {
		// @Override
		// protected IndexableField createField(String name, final String val,
		// org.apache.lucene.document.FieldType type) {
		Field f = new Field(name, val, type);
		//f.setBoost(boost);
		AtomicBoolean incrable = new AtomicBoolean(true);
		f.setTokenStream(new TokenStream() {

			private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

			@Override
			public boolean incrementToken() throws IOException {
				this.clearAttributes();
				termAtt.setEmpty().append(StringUtils.isEmpty(val) ? 'f' : 'T');
				return incrable.getAndSet(false);
			}
		});
		return f;
	}
}
