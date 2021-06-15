/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solrdao;

import com.alibaba.fastjson.JSONObject;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年5月8日
 */
public interface ISchemaField {

    String KEY_FIELD_TYPE = "fieldtype";

    String KEY_NAME = "name";
    String KEY_TYPE = "type";
    String KEY_ANALYZER = "analyzer";
    String KEY_INDEX = "index";
    String KEY_ARRAY = "array";
    String KEY_DOC_VALUES = "doc_values";
    String KEY_STORE = "store";


    String getName();

    /**
     * 字段类型名称，不是全路径
     *
     * @return
     */
    String getTisFieldTypeName();

    String getTokenizerType();


    boolean isIndexed();

    boolean isStored();

    boolean isDocValue();

    boolean isRequired();

    // 是否是多值
    boolean isMultiValue();

    boolean isDynamic();

    /**
     * 默认值
     *
     * @return
     */
    String getDefaultValue();


    void serialVisualType2Json(JSONObject f);
}
