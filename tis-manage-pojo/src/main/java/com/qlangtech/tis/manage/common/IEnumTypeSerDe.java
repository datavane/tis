/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.manage.common;

/**
 * 枚举属性值的序列化/反序列化契约。
 * <p>
 * {@link #valueToken()} 是实例方法：每个枚举常量输出自己的 token（作为前端选项 value / 存储值）；
 * 反序列化是类级别的动作（调用方手里只有枚举的 {@link Class}，没有实例），
 * 因此做成静态方法，并且按 {@link #valueToken()} 的输出反查常量，保证正反两个方向始终自洽。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/20
 */
public interface IEnumTypeSerDe {

    /**
     * 枚举常量 -> 前端选项 value / 存储 token
     */
    public String valueToken();

    /**
     * token -> 枚举常量，按 {@link #valueToken()} 的输出反查
     *
     * @param enumClazz 实现了本接口的枚举类型
     * @param token     由 {@link #valueToken()} 产出的值
     * @return token 为空时返回 null；查不到对应的常量则抛出异常
     */
    public static Enum<?> deserialize(Class<?> enumClazz, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Object[] enumConstants = enumClazz.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalStateException("clazz:" + enumClazz.getName() + " is not a enum type");
        }
        for (Object constant : enumConstants) {
            if (token.equals(((IEnumTypeSerDe) constant).valueToken())) {
                return (Enum<?>) constant;
            }
        }
        throw new IllegalArgumentException("illegal token:'" + token + "' for enum " + enumClazz.getName());
    }
}
