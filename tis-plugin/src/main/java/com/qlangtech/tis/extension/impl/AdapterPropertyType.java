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

package com.qlangtech.tis.extension.impl;

/**
 * 属性包装基类：原样复制 {@code target} 的全部定义，只留出个别方法给子类覆写。
 * <p>
 * <b>必须继承 {@link PropertyType} 而不是只实现 {@code IPropertyType}</b>：
 * {@code Descriptor.isValid} 与 {@code Descriptor.buildPluginInstance} 在遍历
 * {@code PluginFormProperties#getKVTuples()} 时都会做 {@code (PropertyType) entry.getValue()}，
 * 一个只实现接口的包装对象走到那里会抛 {@code ClassCastException}。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/18
 * @see PropertyType#PropertyType(PropertyType)
 */
public class AdapterPropertyType extends PropertyType {

    public AdapterPropertyType(PropertyType target) {
        super(target);
    }
}
