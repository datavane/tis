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
package com.qlangtech.tis.extension;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:11
 */
public interface IPropertyType {
    String KEY_UNIT = "unit";
    int CONST_UNIT_INTEGER_FIELD = 12;

    /**
     * 对应的property 是否是集合属性
     *
     * @return
     */
    boolean isCollectionType();

    /**
     * 是否是主键
     *
     * @return
     */
    boolean isIdentity();

    /**
     * 字段成员名称说明
     *
     * @return
     */
    String propertyName();
}
