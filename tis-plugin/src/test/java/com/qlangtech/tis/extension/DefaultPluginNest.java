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
import com.qlangtech.tis.util.DescriptorsMeta;

import java.util.function.Supplier;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-25 09:51
 **/
public class DefaultPluginNest implements Describable<DefaultPluginNest> {
    @FormField(identity = false, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer exportPort;

    public static Integer dftExportPort() {
        return ((DefaultExportPortProvider) DescriptorsMeta.getRootDescInstance()).get();
    }

    interface DefaultExportPortProvider extends Supplier<Integer> {

    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<DefaultPluginNest> {
        public DefaultDescriptor() {
            super();
        }
    }

}

