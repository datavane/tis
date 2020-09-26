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
