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
package com.qlangtech.tis.runtime.module.misc;

import com.google.common.collect.ImmutableMap;
import com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年1月6日下午6:31:17
 */
public enum TokenizerType {
    NULL(ReflectSchemaFieldType.STRING.literia, "无分词") //
    , IK(ReflectSchemaFieldType.IK.literia, "IK分词") //
    , LIKE(ReflectSchemaFieldType.LIKE.literia, "LIKE分词") //
    , BLANK_SPLIT(ReflectSchemaFieldType.TEXT_WS.literia, "空格分词") //
    , PINGYIN(ReflectSchemaFieldType.PINYIN.literia, "拼音分词");

    public static final Map<String, VisualType> visualTypeMap;

    static {
        ImmutableMap.Builder<String, VisualType> visualTypeMapBuilder = new ImmutableMap.Builder<>();
        visualTypeMapBuilder.put(ReflectSchemaFieldType.STRING.literia, VisualType.STRING_TYPE);
        addNumericType(visualTypeMapBuilder, ReflectSchemaFieldType.DOUBLE.literia);
        addNumericType(visualTypeMapBuilder, ReflectSchemaFieldType.INT.literia);
        addNumericType(visualTypeMapBuilder, ReflectSchemaFieldType.FLOAT.literia);
        addNumericType(visualTypeMapBuilder, ReflectSchemaFieldType.LONG.literia);
        visualTypeMap = visualTypeMapBuilder.build();
    }

    private static void addNumericType(ImmutableMap.Builder<String, VisualType> visualTypeMapBuilder, String numericType) {
        VisualType type = new VisualType(numericType, false);
        visualTypeMapBuilder.put('p' + numericType, type);
    }


    public static boolean isContain(String key) {
        return parseVisualType(key) != null;
    }

    /**
     * @param key
     * @return
     */
    public static VisualType parseVisualType(String key) {
        VisualType result = visualTypeMap.get(key);
        // NumericVisualType numericVisualType = null;
        if (result != null) {
            return result;
        }
//    else if ((numericVisualType = numericTypeMap.get(key)) != null) {
//      return numericVisualType;
//            for (VisualType type : visualTypeMap.values()) {
//                if (type.isRanageQueryAware() && type.getRangedFieldName().equals(key)) {
//                    return type;
//                }
//            }
        //}
        for (TokenizerType type : TokenizerType.values()) {
            if (StringUtils.equals(type.getKey(), key)) {
                return VisualType.STRING_TYPE;
            }
        }
        return null;
    }

    public static TokenizerType parse(String key) {
        for (TokenizerType type : TokenizerType.values()) {
            if (StringUtils.equals(type.key, key)) {
                return type;
            }
        }
        return null;
    }

    private final String key;

    private final String desc;

    /**
     * @param key
     * @param desc
     */
    private TokenizerType(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }
}
