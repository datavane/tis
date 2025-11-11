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

package com.qlangtech.tis.runtime.module.misc;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/19
 */
public enum FormVaildateType {

    FIRST_VALIDATE("first"),
    SECOND_VALIDATE("second"),
    VERIFY("verify"),
    /**
     * 最严格的校验 VERIFY + FIRST_VALIDATE 都要执行
     */
    STRICT("strict");

    private final String token;

    public static FormVaildateType parse(String token) {
        if (StringUtils.isEmpty(token) || "false".equalsIgnoreCase(token)) {
            return FormVaildateType.FIRST_VALIDATE;
        }
        for (FormVaildateType type : FormVaildateType.values()) {
            if (StringUtils.equals(type.token, token)) {
                return type;
            }
        }

        throw new IllegalStateException("illegal token:" + token);
    }

    public boolean isVerify() {
        return this == VERIFY;
    }

    private FormVaildateType(String token) {
        this.token = token;
    }

    public static FormVaildateType create(boolean verify) {
        return verify ? VERIFY : FIRST_VALIDATE;
    }
}
