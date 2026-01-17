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
package com.qlangtech.tis.manage.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Option implements IdentityName {

    public static final String KEY_VALUE = "val";
    public static final String KEY_LABEL = "label";
    public static final String keyChecked = "checked";

    public static Option create(JSONObject option) {
        return new Option(Objects.requireNonNull(option, "option can not be null").getString(Option.KEY_LABEL),
                option.getString(Option.KEY_VALUE));
    }

    public static JSONArray toJson(List<?> options) {
        JSONArray enums = new JSONArray();
        if (options != null) {
            Stream<Option> optStream = options.stream().map(new Function<Object, Option>() {
                @Override
                public Option apply(Object o) {
                    if (o instanceof Option) {
                        return (Option) o;
                    } else if (o instanceof IdentityName) {
                        return new Option(((IdentityName) o).identityValue());
                    } else {
                        throw new IllegalStateException("illegal type:" + o.getClass());
                    }
                }
            });
            optStream.forEach((opt) -> {
                JSONObject o = new JSONObject();
                o.put(KEY_LABEL, opt.getName());
                o.put(KEY_VALUE, opt.getValue());
                if (opt.isChecked() != null) {
                    o.put(keyChecked, opt.isChecked());
                }
                enums.add(o);
            });
        }
        return enums;
    }

    private final String name;

    private final Object value;

    private Boolean checked;

    /**
     * @param name  label
     * @param value
     */
    public Option(String name, Object value) {
        super();
        this.name = name;
        this.value = value;
    }

    public Option(String val) {
        this(val, val);
    }

    @Override
    public String identityValue() {
        return String.valueOf(value);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @JSONField(serialize = false)
    public Boolean isChecked() {
        return checked;
    }

    public Option setChecked(Boolean checked) {
        this.checked = checked;
        return this;
    }
}
