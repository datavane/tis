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

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.google.common.collect.Maps;
import com.qlangtech.tis.util.DescribableJSON;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 11:43
 * @see TargetColType
 **/
public class TargetColTypeSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.write(serialize((TargetColType) object));
    }


    private Map<String, Object> serialize(TargetColType colType) {
        try {
            Map<String, Object> result = Maps.newHashMap();
            DescribableJSON target = new DescribableJSON(Objects.requireNonNull(colType.target, "target can not be null"));
            JSONObject itemJson = target.getItemJson();
            itemJson.put("literia", colType.target.getLiteria());

            result.put("name", itemJson);
            result.put("type", colType.getType());
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
