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

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-25 17:00
 */
public enum SolrJavaType {
    INT(Integer.class, 1), FLOAT(Float.class, 2), DOUBLE(Double.class, 3), LONG(Long.class, 4), TIMESTAMP(Timestamp.class, 4), DATE(Date.class, 5), STRING(String.class, 6);

    public static SolrJavaType parse(String fieldType) {
        if (isTypeMatch(fieldType, "int")) {
            return INT;
        } else if (isTypeMatch(fieldType, "float")) {
            return FLOAT;
        } else if (isTypeMatch(fieldType, "double")) {
            return DOUBLE;
        } else if (isTypeMatch(fieldType, "long")) {
            return LONG;
        } else if (isTypeMatch(fieldType, "timestamp")) {
            return TIMESTAMP;
        } else if (isTypeMatch(fieldType, "date")) {
            return DATE;
        }
        return STRING;
    }

    private static boolean isTypeMatch(String fieldType, String matchLetter) {
        return StringUtils.indexOfAny(fieldType, new String[]{matchLetter, StringUtils.capitalize(matchLetter)}) > -1;
    }

    private final Class<?> javaType;
    private final int typeCode;
    // private final Method valueof;

    private SolrJavaType(Class<?> javaType, int typeCode) {
        this.javaType = javaType;
        this.typeCode = typeCode;
//        try {
//            if (javaType == String.class) {
//                valueof = javaType.getMethod("valueOf", Object.class);
//            } else {
//                valueof = javaType.getMethod("valueOf", String.class);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public int getTypeCode() {
        return typeCode;
    }

    public String getSimpleName() {
        return StringUtils.lowerCase(javaType.getSimpleName());
    }
}
