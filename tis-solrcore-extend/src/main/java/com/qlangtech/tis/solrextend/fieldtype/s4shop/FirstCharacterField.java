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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieIntField;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FirstCharacterField extends TrieIntField {

    private static final HanyuPinyinOutputFormat outputFormat;

    /**
     * 用int的32位，分为5段，分别记录5个值，每个值分配6个bit
     */
    public static final int BitsPerValue = 6;

    public static final int NumOfValue = 5;

    private static final Log logger = LogFactory.getLog(FirstCharacterField.class);

    static {
        outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        logger.info("successful load pinyin4j");
    }

    @Override
    public IndexableField createField(SchemaField field, Object value) {
        String externalVal = (String) value;
        if (StringUtils.isBlank(externalVal)) {
            throw new IllegalStateException("name can not be null");
        }
        externalVal = StringUtils.trim(externalVal);
        int result = 0;
        int index = 0;
        for (int i = 0; i < externalVal.length(); i++) {
            char fc = externalVal.charAt(i);
            try {
                if (FirstCharacterFieldHelper.isChinese(fc)) {
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(fc, outputFormat);
                    /**
                     * 多音字暂定取第一个
                     */
                    if (pinyin == null || pinyin.length == 0) {
                        continue;
                    }
                    fc = pinyin[0].charAt(0);
                } else if (FirstCharacterFieldHelper.isCharOrNum(fc)) {
                    fc = Character.toLowerCase(fc);
                } else {
                    /**
                     * 特殊字符默认排在最后,数字无具体意义
                     */
                    fc = 126;
                }
                result = FirstCharacterFieldHelper.processInt(result, fc, index, BitsPerValue);
                index = index + 1;
                if (index >= NumOfValue) {
                    break;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                // logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return super.createField(field, result);
    }
}
