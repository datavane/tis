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

import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * 在 RequiredPasswordPlugin.json 配置文件中 将require disable掉
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/19
 */
public class RequiredPasswordPlugin implements Describable<RequiredPasswordPlugin> {
    @FormField(ordinal = 7, type = FormFieldType.PASSWORD, validate = {Validator.none_blank, Validator.require})
    public String password;

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<DefaultPlugin> {
        public DefaultDescriptor() {
        }
    }
}
