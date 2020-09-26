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
package com.qlangtech.tis.runtime.module.misc;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Maps;
import com.qlangtech.tis.runtime.module.action.SchemaAction.NumericVisualType;
import com.qlangtech.tis.runtime.module.action.SchemaAction.VisualType;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年1月6日下午6:31:17
 */
public enum TokenizerType {

    PAODING("paoding", "庖丁分词"), LIKE("like", "LIKE分词"), BLANK_SPLIT("text_ws", "空格分词"), NULL("string", "无分词"), PINGYIN("pinyin", "拼音分词");

    // public static final Set<TokenizerType> tokenerTypes;
    // 
    // static {
    // tokenerTypes = new HashSet<TokenizerType>();
    // tokenerTypes.add(BLANK_SPLIT);
    // tokenerTypes.add(LIKE);
    // tokenerTypes.add(PAODING);
    // tokenerTypes.add(NULL);
    // }
    public static final Map<String, VisualType> visualTypeMap;

    public static final Map<String, NumericVisualType> numericTypeMap;

    private static final TokenizerString splitableTokenType = new TokenizerString();

    static {
        visualTypeMap = new HashMap<String, VisualType>();
        numericTypeMap = Maps.newHashMap();
        visualTypeMap.put("string", splitableTokenType);
        addNumericType("double");
        addNumericType("int");
        addNumericType("float");
        addNumericType("long");
    // visualTypeMap.put("int", new VisualType("int", true, false));
    // visualTypeMap.put("long", new VisualType("long", true, false));
    // visualTypeMap.put("float", new VisualType("float", true, false));
    // visualTypeMap = Collections.unmodifiableMap(visualTypes);
    }

    protected static void addNumericType(String numericType) {
        VisualType type = new VisualType(numericType, true, /* ranageQueryAware */
        false);
        visualTypeMap.put(numericType, type);
        numericTypeMap.put(numericType, NumericVisualType.create(type, false));
        numericTypeMap.put('t' + numericType, NumericVisualType.create(type, true));
    }

    public static class TokenizerString extends VisualType {

        public TokenizerString() {
            super("string", false, true);
        }

        public TokenizerType[] getTokenerTypes() {
            return TokenizerType.values();
        }
    }

    // <option value="paoding">庖丁分词</option>
    // <option value="like">like查询分词</option>
    // <option value="text_ws">空格分词</option>
    // <option value="regular">正则分词</option>
    public static boolean isContain(String key) {
        return parseVisualType(key) != null;
    }

    /**
     * @param key
     * @return
     */
    public static VisualType parseVisualType(String key) {
        VisualType result = visualTypeMap.get(key);
        if (result != null) {
            return result;
        } else {
            for (VisualType type : visualTypeMap.values()) {
                if (type.isRanageQueryAware() && type.getRangedFieldName().equals(key)) {
                    return type;
                }
            }
        }
        for (TokenizerType type : TokenizerType.values()) {
            if (StringUtils.equals(type.getKey(), key)) {
                return splitableTokenType;
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
    // if (PAODING.key.equalsIgnoreCase(key)) {
    // return PAODING;
    // } else if (LIKE.key.equalsIgnoreCase(key)) {
    // return LIKE;
    // } else if (BLANK_SPLIT.key.equalsIgnoreCase(key)) {
    // return BLANK_SPLIT;
    // // } else if (REGULAR.key.equals(key)) {
    // // return REGULAR;
    // } else {
    // return NULL;
    // }
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
