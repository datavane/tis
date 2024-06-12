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

package com.qlangtech.tis.trigger.util;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 13:45
 **/
public class UnCacheStringSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type type, int i) throws IOException {
        try {
            //  SerializeWriter out = serializer.out;

            UnCacheString value = (UnCacheString) object;
            Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");

            //  out.writeString(value.getValue());

            serializer.write(value.getValue());

        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
