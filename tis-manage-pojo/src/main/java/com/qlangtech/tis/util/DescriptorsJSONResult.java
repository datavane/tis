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
import com.google.common.collect.Maps;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.web.start.TisAppLaunch;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-25 12:41
 * @seee DescriptorsJSON
 **/
public class DescriptorsJSONResult {
    public static final ThreadLocal<Object> rootDescriptorLocal = new ThreadLocal<Object>();

    public static <T> T getRootDescInstance() {
        return (T) Objects.requireNonNull(rootDescriptorLocal.get(), "rootDescriptorLocal element can not be null");
    }

    private Map<String, Pair<JSONObject, Object>> descs = Maps.newHashMap();
    /**
     * 由于describe 可以嵌套，此标志位可以判断 是否是根元素
     */
    private final boolean rootDesc;

    public DescriptorsJSONResult(boolean rootDesc) {
        this.rootDesc = rootDesc;
    }

    public void addDesc(String id, JSONObject descJson, Object desc) {
        descs.put(id, Pair.of(descJson, desc));
    }

    public String toJSONString() {
        JSONObject o = new JSONObject();
        final int fieldSize = descs.size();
        StringBuffer json = new StringBuffer();
        json.append("{\n");
        int fieldIndex = 0;
        for (Map.Entry<String, Pair<JSONObject, Object>> entry : descs.entrySet()) {
            try {
                if (this.rootDesc) {
                    rootDescriptorLocal.set(entry.getValue().getValue());
                }
                json.append("\t\"").append(entry.getKey()).append("\":")
                        .append(JsonUtil.toString(entry.getValue().getLeft(), TisAppLaunch.isTestMock()));
                if (++fieldIndex < fieldSize) {
                    json.append(",");
                }
            } finally {
                if (this.rootDesc) {
                    rootDescriptorLocal.remove();
                }
            }
        }
        json.append("\n}");
        return json.toString();
    }

    public JSONObject getJSONObject(String descId) {
        return Objects.requireNonNull(descs.get(descId)
                , "descId:" + descId + " relevant desc can not be null").getLeft();
    }
}
