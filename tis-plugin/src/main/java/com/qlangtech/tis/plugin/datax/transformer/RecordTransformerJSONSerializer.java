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
 * @create: 2024-07-08 22:49
 * @see RecordTransformer
 **/
public class RecordTransformerJSONSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer jsonSerializer, Object object, Object fieldName, Type type, int i) throws IOException {
        RecordTransformer value = (RecordTransformer) object;
        Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");

        Map<String, Object> ser = Maps.newHashMap();
        try {
            UDFDefinition udf = value.getUdf();
            DescribableJSON itemJSON = new DescribableJSON(udf);
            JSONObject json = itemJSON.getItemJson();
            json.put("literia", udf.getLiteria());
            ser.put("udf", json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        jsonSerializer.write(ser);
    }
}
