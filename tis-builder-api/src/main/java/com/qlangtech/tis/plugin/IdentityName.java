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
package com.qlangtech.tis.plugin;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * The plugin global unique identity name
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@FunctionalInterface
public interface IdentityName {

    String MSG_ERROR_NAME_DUPLICATE = "名称重复";
    String PLUGIN_IDENTITY_NAME = "identityName";

    public static IdentityName create(final String value) {
        return new IdentityName() {
            @Override
            public String identityValue() {
                return value;
            }
        };
    }

    default boolean equalWithId(IdentityName identity) {
        return StringUtils.equals(identityValue()
                , Objects.requireNonNull(identity, "identity can not be null").identityValue());
    }


    /**
     * 取得唯一ID
     *
     * @returnIdentityName
     */
    //default
    String identityValue();// {
//        Describable plugin = (Describable) this;
//        Descriptor des = plugin.getDescriptor();
//        Objects.requireNonNull(des, " Descriptor of Describable instance of " + plugin.getClass().getName());
//        return des.getIdentityValue(plugin);
    //}


    default Class<?> getDescribleClass() {
        return this.getClass();
    }

}
