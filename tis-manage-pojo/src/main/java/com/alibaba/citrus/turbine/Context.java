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
package com.alibaba.citrus.turbine;

import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;

import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface Context {

    public boolean containsKey(String key);

    public Object get(String key);

    public Set<String> keySet();

    public Object put(String key, Object value);


    public void remove(String key);


    @SuppressWarnings("all")
    public default boolean hasErrors() {

        if (this.get(IFieldErrorHandler.ACTION_ERROR_FIELDS) != null && !((List<Object>) this.get(IFieldErrorHandler.ACTION_ERROR_FIELDS)).isEmpty()) {
            return true;
        }

        if (this.get(IMessageHandler.ACTION_ERROR_MSG) != null && !((List<String>) this.get(IMessageHandler.ACTION_ERROR_MSG)).isEmpty()) {
            return true;
        }

        return false;
    }

    public default <T> T getContext() {
        throw new UnsupportedOperationException();
    }

    public default <T> void setContext(T actionContext) {
        throw new UnsupportedOperationException();
    }
}
