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

package com.qlangtech.tis.plugin.ds;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-02 12:46
 **/
public abstract class ContextParamConfig {
    public static final String CONTEXT_BINDED_KEY_PREFIX = "$";

    private final String keyName;

    public ContextParamConfig(String keyName) {
        if (StringUtils.startsWith(keyName, CONTEXT_BINDED_KEY_PREFIX)) {
            throw new IllegalArgumentException("param keyName:" + keyName + " can not start with:" + CONTEXT_BINDED_KEY_PREFIX);
        }
        this.keyName = CONTEXT_BINDED_KEY_PREFIX + keyName;
    }

    //public abstract JDBCTypes getJdbcType();

    public String getKeyName() {
        return this.keyName;
    }

    /**
     * 取得执行当前上线文绑定的参数，例如，当前数据库的名称等
     *
     * @param <CONTEXT>
     * @return
     */
    public abstract <CONTEXT extends RunningContext> Function<CONTEXT, Object> valGetter();

    public abstract DataType getDataType();

}
