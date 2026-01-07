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
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 13:30
 **/
public class DescriptorsJSONResultSerializer implements ObjectSerializer {

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        boolean prettyFormat = SerializerFeature.isEnabled((int) features, SerializerFeature.PrettyFormat);
        DescriptorsMeta value = (DescriptorsMeta) object;
        Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");
        jsonWriter.writeRaw(toJSONString(value, prettyFormat));
    }

    public String toJSONString(DescriptorsMeta value, boolean prettyFormat) {
        JSONObject o = new JSONObject();
        final int fieldSize = value.descs.size();
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (prettyFormat) {
            json.append("\n");
        }
        int fieldIndex = 0;
        for (Map.Entry<String, Pair<JSONObject, Object>> entry : value.descs.entrySet()) {
            try {
                if (value.rootDesc) {
                    DescriptorsMeta.rootDescriptorLocal.set(entry.getValue().getValue());
                }
                json.append("\t\"").append(entry.getKey()).append("\":")
                        .append(JsonUtil.toString(entry.getValue().getLeft(), prettyFormat));
                if (++fieldIndex < fieldSize) {
                    json.append(",");
                }
            } catch (Exception e) {
                throw new IllegalStateException(entry.getKey() + " " + entry.getValue().getValue().getClass(), e);
            } finally {
                if (value.rootDesc) {
                    DescriptorsMeta.rootDescriptorLocal.remove();
                }
            }
        }
        if (prettyFormat) {
            json.append("\n");
        }
        json.append("}");
        return json.toString();
    }

    @Override
    public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
        throw new UnsupportedEncodingException();
    }
}
