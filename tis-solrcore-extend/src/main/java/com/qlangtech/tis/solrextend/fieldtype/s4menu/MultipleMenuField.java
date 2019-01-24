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
package com.qlangtech.tis.solrextend.fieldtype.s4menu;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.StrField;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * multiple_menu_info字段的结构变成:菜单id_价格(规格id_规格价格)(规格id_规格价格);菜单id_价格(规格id_规格价格)(规格id_规格价格)
 * 所有的menuId可以当做索引来搜索
 * Created by Qinjiu on 6/30/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultipleMenuField extends StrField {

    public static final Pattern MENU_INFO_PATTERN = Pattern.compile("(\\w+)_(\\d{1})_([\\d.]+)([\\-()\\w_.]*)");

    // static final Pattern MENU_SPEC_PATTERN = Pattern.compile("");
    public static final String SEPARATOR = ";";

    @Override
    protected IndexableField createField(String name, String val, org.apache.lucene.index.IndexableFieldType type) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        org.apache.lucene.document.Field f = new Field(name, val, type);
        String[] menuInfos = val.split(SEPARATOR);
        final List<String> menuIds = new ArrayList<>();
        for (String menuInfo : menuInfos) {
            Matcher matcher = MENU_INFO_PATTERN.matcher(menuInfo);
            if (matcher.matches()) {
                menuIds.add(matcher.group(1));
            }
        }
        final int length = menuIds.size();
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            int index = 0;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                if (index >= length) {
                    return false;
                } else {
                    termAtt.setEmpty().append(menuIds.get(index++));
                    return true;
                }
            }
        });
        return f;
    }

    @Override
    public boolean isPolyField() {
        return false;
    }
}
