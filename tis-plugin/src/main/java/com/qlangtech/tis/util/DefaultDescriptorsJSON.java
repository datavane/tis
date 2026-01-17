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
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropertyType;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/1/3
 */
public class DefaultDescriptorsJSON<T extends Describable<T>> extends DescriptorsJSON<T,
        DefaultDescriptorsJSON.JSONAttrVal> {

    public static final class JSONAttrValSerializer implements ObjectSerializer {

        @Override
        public void write(JSONSerializer jsonSerializer, Object object, Object fieldName, Type fieldType,
                          int features) throws IOException {
            JSONAttrVal attrVal = (JSONAttrVal) object;
            jsonSerializer.write(attrVal.attrVal);
        }
    }

    /**
     * @see DescriptorsJSONResultSerializer
     */
    @JSONType(serializer = JSONAttrValSerializer.class)
    public static class JSONAttrVal extends AttrVal {
        private final JSONObject attrVal;

        public JSONAttrVal(JSONObject attrVal) {
            this.attrVal = attrVal;
        }

        @Override
        public void put(String key, Object val) {
            if (StringUtils.isEmpty(key)) {
                throw new IllegalArgumentException("param key can not be empty");
            }
            attrVal.put(key, val);
        }

        @Override
        public void putDescriptors(DescriptorsJSON des2Json) {
            attrVal.put("descriptors", des2Json.getDescriptorsJSON());
        }
    }


    public DefaultDescriptorsJSON(Collection<Descriptor<T>> descriptors) {
        super(descriptors);
    }

    public DefaultDescriptorsJSON(Descriptor<T> descriptor) {
        super(descriptor);
    }

    public DefaultDescriptorsJSON(Collection<Descriptor<T>> descriptors, boolean rootDesc) {
        super(descriptors, rootDesc);
    }

    public DefaultDescriptorsJSON(Descriptor<T> descriptor, boolean rootDesc) {
        super(descriptor, rootDesc);
    }

    @Override
    protected JSONAttrVal createAttrVal(String key, PropertyType val) {
        JSONObject attrVal;
        attrVal = new JSONObject();
        attrVal.put("key", key);
        // 是否是主键
        attrVal.put("pk", val.isIdentity());
        attrVal.put("describable", val.isDescribable());
        attrVal.put("type", val.typeIdentity());
        attrVal.put("required", val.isInputRequired());
        attrVal.put("ord", val.ordinal());
        // 是否是高级组
        if (val.advance()) {
            attrVal.put(DescriptorsJSON.KEY_ADVANCE, true);
        }
        return new JSONAttrVal(attrVal);
    }

    @Override
    protected DescriptorsJSON<T, JSONAttrVal> createInnerDescrible(List<? extends Descriptor> descriptors) {
        return new DefaultDescriptorsJSON(descriptors, false);
    }
}