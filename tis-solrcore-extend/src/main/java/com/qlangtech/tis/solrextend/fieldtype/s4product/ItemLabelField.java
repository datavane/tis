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
package com.qlangtech.tis.solrextend.fieldtype.s4product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import com.google.common.base.Joiner;

/**
 * item_label字段处理方式<br>
 * 1:蛋类,豆角,鱼;2:荤菜;3:重辣;4:本月新菜;5:强烈推荐<br>
 * 有一个case是在一个输入框中，输入菜名和label一起查
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ItemLabelField extends org.apache.solr.schema.TextField {

    private static final Pattern PATTERN_TUPLES = Pattern.compile("(\\d+):(([^,^;]+,)*([^,^;]+))");

    // private org.apache.solr.schema.FieldType textSemType;
    private IndexSchema schema;

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        // this.textSemType = schema.getFieldTypeByName("text_sem");
        // if (textSemType == null) {
        // throw new IllegalArgumentException("textSemType can not be null");
        // }
        this.schema = schema;
    }

    public static void main(String[] args) {
        Matcher matcher = PATTERN_TUPLES.matcher("1:蛋类,豆角,鱼;2:荤菜;3:重辣;4:本月新菜;5:强烈推荐");
        while (matcher.find()) {
            System.out.println(matcher.group(1) + "||" + matcher.group(2));
        }
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<>();
        fields.add(createField(field, value));
        Matcher matcher = PATTERN_TUPLES.matcher(String.valueOf(value));
        final List<String> codes = new ArrayList<String>();
        final List<String> literal = new ArrayList<String>();
        String tags = null;
        while (matcher.find()) {
            // code名称
            codes.add(StringUtils.lowerCase(matcher.group(1)));
            // 字面量
            tags = StringUtils.lowerCase(matcher.group(2));
            for (String tag : StringUtils.split(tags, ",")) {
                literal.add(tag);
            }
        }
        SchemaField item_label_code = this.schema.getField("item_label_code");
        SchemaField item_label_literal = this.schema.getField("item_label_literal");
        fields.add(item_label_code.createField(join(codes)));
        fields.add(item_label_literal.createField(join(literal)));
        return fields;
    }

    private static final Joiner comma_joiner = Joiner.on(",").skipNulls();

    private static String join(List<String> vals) {
        return comma_joiner.join(vals);
    }

    @Override
    public void checkSchemaField(SchemaField field) {
    // super.checkSchemaField(field);
    }

    // @Override
    // protected IndexableField createField(String name, String val, FieldType type,
    // float boost) {
    // if (StringUtils.isBlank(val)) {
    // return null;
    // }
    // Field f = new Field(name, val, type);
    // 
    // Matcher matcher = PATTERN_TUPLES.matcher(val);
    // final List<String> keys = new ArrayList<String>(4);
    // while (matcher.find()) {
    // keys.add(matcher.group(1));
    // }
    // int keysSize = keys.size();
    // if (keysSize > 0) {
    // f.setTokenStream(new TokenStream() {
    // private final CharTermAttribute termAtt = (CharTermAttribute)
    // addAttribute(CharTermAttribute.class);
    // private final PositionIncrementAttribute postIncr =
    // (PositionIncrementAttribute) addAttribute(
    // PositionIncrementAttribute.class);
    // int index = 0;
    // 
    // @Override
    // public boolean incrementToken() throws IOException {
    // clearAttributes();
    // if (index >= keys.size()) {
    // return false;
    // } else {
    // postIncr.setPositionIncrement(1);
    // termAtt.setEmpty().append(keys.get(index++));
    // return true;
    // }
    // }
    // });
    // 
    // // terms越多，排序越在下面
    // // f.setBoost((1 / keysSize) * 100);
    // }
    // 
    // return f;
    // }
    @Override
    public boolean isPolyField() {
        return true;
    }
}
