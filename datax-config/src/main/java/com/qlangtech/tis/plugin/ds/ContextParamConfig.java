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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-02 12:46
 **/
public abstract class ContextParamConfig {
    public static final String CONTEXT_BINDED_KEY_PREFIX = "$";

    private final String keyName;
    public static final String KEY_DB_CONTEXT_PARAM_NAME = "dbName";

    public static Map<String, ContextParamConfig> defaultContextParams() {
        return defaultContextParams((param) -> true);
    }


    /**
     * 系统默认的绑定Transformer 可用的绑定参数
     *
     * @return
     */
    public static Map<String, ContextParamConfig> defaultContextParams(Predicate<ContextParamConfig> paramPredicate) {
        ContextParamConfig dbName = new ContextParamConfig(KEY_DB_CONTEXT_PARAM_NAME) {
            @Override
            public ContextParamValGetter<RunningContext> valGetter() {
                return new DbNameContextParamValGetter();
            }

            @Override
            public DataType getDataType() {
                return DataType.createVarChar(50);
            }
        };

        ContextParamConfig sysTimestamp = new ContextParamConfig("timestamp") {
            @Override
            public ContextParamValGetter<RunningContext> valGetter() {
                return new SystemTimeStampContextParamValGetter();
            }

            @Override
            public DataType getDataType() {
                return DataType.getType(JDBCTypes.TIMESTAMP);
            }
        };

        ContextParamConfig tableName = new ContextParamConfig("tableName") {
            @Override
            public ContextParamValGetter<RunningContext> valGetter() {
                return new TableNameContextParamValGetter();
            }

            @Override
            public DataType getDataType() {
                return DataType.createVarChar(50);
            }
        };

        List<ContextParamConfig> contextParams = new ArrayList<>();
        contextParams.add(dbName);
        contextParams.add(tableName);
        contextParams.add(sysTimestamp);
        return contextParams
                .stream().filter(paramPredicate).collect(Collectors.toMap((cfg) -> cfg.getKeyName(), (cfg) -> cfg));
    }


    public static class DbNameContextParamValGetter implements ContextParamValGetter<RunningContext> {
        @Override
        public Object apply(RunningContext runningContext) {
            return runningContext.getDbName();
        }
    }

    public static class SystemTimeStampContextParamValGetter implements ContextParamValGetter<RunningContext> {
        @Override
        public Object apply(RunningContext runningContext) {
            return System.currentTimeMillis();
        }
    }

    public static class TableNameContextParamValGetter implements ContextParamValGetter<RunningContext> {
        @Override
        public Object apply(RunningContext runningContext) {
            return runningContext.getTable();
        }
    }


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
     * @param
     * @return
     */
    public abstract ContextParamValGetter valGetter();

    public abstract DataType getDataType();


    public interface ContextParamValGetter<CONTEXT extends RunningContext> extends Function<CONTEXT, Object>, Serializable {

    }
}
