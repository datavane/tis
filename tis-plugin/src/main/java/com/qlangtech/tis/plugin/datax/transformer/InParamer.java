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

package com.qlangtech.tis.plugin.datax.transformer;

import com.qlangtech.tis.plugin.ds.ContextParamConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * UDF 入参数
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-31 23:32
 **/
public abstract class InParamer {

    private final String paramKey;

    public InParamer(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getKey() {
        return this.paramKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InParamer inParamer = (InParamer) o;
        return Objects.equals(paramKey, inParamer.paramKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.paramKey);
    }

    /**
     * 是否是上下文绑定参数
     *
     * @return
     */
    public abstract boolean isContextParams();

    public static InParamer create(String paramKey) {
        if (StringUtils.isEmpty(paramKey)) {
            throw new IllegalArgumentException("param param key can not be empty");
        }
        if (StringUtils.startsWith(paramKey, ContextParamConfig.CONTEXT_BINDED_KEY_PREFIX)) {
            return new ContextBindInParamer(paramKey);
        } else {
            return new RecordColRelevantInParamer(paramKey);
        }
    }

    private static final class ContextBindInParamer extends InParamer {
        public ContextBindInParamer(String paramKey) {
            super(paramKey);
        }

        @Override
        public boolean isContextParams() {
            return true;
        }
    }

    /**
     * 源于记录相关列的入参数
     *
     * @see com.alibaba.datax.common.element.Record
     */
    public static final class RecordColRelevantInParamer extends InParamer {
        public RecordColRelevantInParamer(String paramKey) {
            super(paramKey);
        }

        @Override
        public boolean isContextParams() {
            return false;
        }
    }
}
