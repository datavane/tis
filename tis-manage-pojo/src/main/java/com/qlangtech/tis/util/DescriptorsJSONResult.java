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

package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-25 12:41
 * @seee DescriptorsJSON
 **/
@JSONType(serializer = DescriptorsJSONResultSerializer.class)
public class DescriptorsJSONResult {
    public static final ThreadLocal<Object> rootDescriptorLocal = new ThreadLocal<Object>();

    public static <T> T getRootDescInstance() {
        return (T) Objects.requireNonNull(rootDescriptorLocal.get(), "rootDescriptorLocal element can not be null");
    }

    Map<String, Pair<JSONObject, Object>> descs = Maps.newHashMap();
    /**
     * 由于describe 可以嵌套，此标志位可以判断 是否是根元素
     */
    final boolean rootDesc;

    /**
     *
     * @param rootDesc
     */
    public DescriptorsJSONResult(boolean rootDesc) {
        this.rootDesc = rootDesc;
    }

    /**
     *
     * @param id       插件的实现类className值
     * @param descJson
     * @param desc
     */
    public void addDesc(String id, JSONObject descJson, Object desc) {
        descs.put(id, Pair.of(descJson, desc));
    }


    public JSONObject getJSONObject(String descId) {
        return Objects.requireNonNull(descs.get(descId)
                , "descId:" + descId + " relevant desc can not be null").getLeft();
    }

    public Map<String, JSONObject> getDescriptorsResult() {
        return this.descs.entrySet().stream()
                .collect(Collectors.toMap(
                        (e) -> e.getKey(), (e) -> e.getValue().getLeft()));
    }


}
