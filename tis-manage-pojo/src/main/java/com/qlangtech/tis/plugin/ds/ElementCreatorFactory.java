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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;

import java.util.function.BiConsumer;

public interface ElementCreatorFactory<T extends IMultiElement> {


    /**
     * 多选列前置校验
     *
     * @param msgHandler
     * @param context
     * @param keyColsMeta
     * @param targetCols
     * @return
     */
    public CMeta.ParsePostMCols<T> parsePostMCols(IPropertyType propertyType,
                                                  IControlMsgHandler msgHandler, Context context, String keyColsMeta,
                                                  JSONArray targetCols);

    default ViewContent getViewContentType() {
        return ViewContent.MongoCols;
    }

    default T createDefault() {
        return this.createDefault(new JSONObject());
    }

    // CMeta
    T createDefault(JSONObject targetCol);

    default T create(JSONObject targetCol) {
        return create(targetCol, (key, errMsg) -> {
            throw new IllegalStateException("key:" + key + " ,errMsg:" + errMsg + " shall not occur");
        });
    }

    T create(JSONObject targetCol, BiConsumer<String, String> errorProcess);

    /**
     * 向客户端传输的json内容中额外添加自动移属性
     *
     * @param biz
     */
    default void appendExternalJsonProp(IPropertyType propertyType, JSONObject biz) {

    }
}
