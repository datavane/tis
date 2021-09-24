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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import java.util.Map;
import com.google.common.collect.Maps;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class TypeCast<T> {

    public static final Map<String, TypeCast<?>> castToType = Maps.newHashMap();

    static {
        castToType.put("string", new TypeCast<String>() {

            @Override
            public String cast(Object val) {
                return val == null ? null : String.valueOf(val);
            }
        });
        castToType.put("int", new TypeCast<Integer>() {

            @Override
            public Integer cast(Object val) {
                if (val == null) {
                    return 0;
                }
                if (val instanceof Integer) {
                    return (Integer) val;
                } else {
                    return Integer.parseInt(String.valueOf(val));
                }
            }
        });
    }

    public abstract T cast(Object val);

    public static TypeCast<?> getTypeCast(String type) {
        TypeCast<?> cast = castToType.get(type);
        if (cast == null) {
            throw new IllegalStateException("type:" + type + " relevant type case have not been defined");
        }
        return cast;
    }
}
