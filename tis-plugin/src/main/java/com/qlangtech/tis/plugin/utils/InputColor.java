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
package com.qlangtech.tis.plugin.utils;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 表单颜色属性的值载体，对应 {@link com.qlangtech.tis.plugin.annotation.FormFieldType#Color}。
 * <p>
 * 值在前后端之间始终以颜色字符串（如 {@code #1890ff}）往返，这里是服务端的 POJO 包装：
 * 用对象而不是裸 String 是为了让插件作者一眼看出该属性走的是取色器而不是普通文本输入。
 * <p>
 * 无参构造器与 setter 是必须的：属性随插件实例经 XStream 落盘 / 回读，
 * 反射反序列化不走带参构造器。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/8
 */
public class InputColor implements Serializable {
    private static final long serialVersionUID = 1L;

    private String value;

    public InputColor() {
    }

    public InputColor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return StringUtils.trimToEmpty(value);
    }
}
