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

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.IFieldValidator;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IFieldErrorHandler {

    String ACTION_ERROR_FIELDS = "action_error_fields";

    static String joinField(String fkey, Object... keys) {

        List<String> joins = Lists.newArrayList(fkey);
        for (Object key : keys) {
            if (key instanceof String) {
                joins.add(".");
                joins.add((String) key);
            } else if (key instanceof List) {
                joins.add((String) ((List) key).stream().map((k) -> "[" + k + "]").collect(Collectors.joining()));
            } else {
                throw new IllegalStateException("illegal type:" + key);
            }
        }

        return joins.stream().collect(Collectors.joining());
    }

    void addFieldError(final Context context, String fieldName, String msg, Object... params);

    enum BizLogic {
        VALIDATE_APP_NAME_DUPLICATE, VALIDATE_WORKFLOW_NAME_DUPLICATE
        // 创建新的数据管道
        , CREATE_DATA_PIPELINE
    }

    /**
     * 在插件中校验业务逻辑
     *
     * @param logicType
     * @param context
     * @param fieldName
     * @param value
     * @return
     */
    boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value);

    public BasicPipelineValidator getPipelineValidator(BizLogic logicType);

    abstract class BasicPipelineValidator implements IFieldValidator {

        /**
         *
         * @param namePrefix 是否使用名称前缀匹配
         * @return
         */
        public abstract <T extends IdentityName> List<T> getExistEntities(Optional<String> namePrefix);
    }
}
