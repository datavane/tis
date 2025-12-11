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

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return new DftIdentityName(value);
    }

    public static IdentityName create(final IdentityName value) {
        return new DftIdentityName(value.identityValue());
    }

    /**
     * 创建一个新的实例id名称
     *
     * @param idPrefix
     * @param existOpts
     * @param <T>
     * @return
     */
    static <T extends IdentityName> IdentityName createNewPrimaryFieldValue(final String idPrefix, List<T> existOpts) {
        if (StringUtils.isEmpty(idPrefix) || !ValidatorCommons.pattern_identity.matcher(idPrefix).matches()) {
            throw new IllegalArgumentException("param idPrefix:" + idPrefix + " is not illegal");
        }
        final String descName = StringUtils.replace(StringUtils.lowerCase(idPrefix), "-", "_");
        Pattern pattern = Pattern.compile(descName + "_?(\\d+)");
        Matcher matcher = null;
        int maxSufix = 1;
        for (IdentityName opt : existOpts) {
            matcher = pattern.matcher(StringUtils.lowerCase(opt.identityValue()));
            if (matcher.matches()) {
                int curr;
                if ((curr = Integer.valueOf(matcher.group(1))) >= maxSufix) {
                    maxSufix = curr + 1;
                }
            }
        }
        return create(descName + "_" + maxSufix);
    }

    default boolean equalWithId(IdentityName identity) {
        return StringUtils.equals(identityValue(),
                Objects.requireNonNull(identity, "identity can not be null").identityValue());
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
