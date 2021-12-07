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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieIntField;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
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

    private static final Logger logger = LoggerFactory.getLogger(FirstCharacterField.class);

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
