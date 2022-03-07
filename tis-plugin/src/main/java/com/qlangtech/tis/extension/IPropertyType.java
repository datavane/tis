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

import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:11
 */
public interface IPropertyType {

    /**
     * plugin form 某一个field为集合类型，且字段内为一个javabean类型，需要填写多个字段
     */
    public class SubFormFilter {

        /**
         * 表明点击进入子表单显示
         */
        public static String PLUGIN_META_SUBFORM_DETAIL_ID_VALUE = "subformDetailIdValue";

        // 目标插件名称
        public static String PLUGIN_META_TARGET_DESCRIPTOR_NAME = "targetDescriptorName";
        public static String PLUGIN_META_SUB_FORM_FIELD = "subFormFieldName";
        private final String targetDescriptorName;
        public final String subFieldName;
        public final UploadPluginMeta uploadPluginMeta;
        // 是否显示子表单内容
        public final boolean subformDetailView;
        public final String subformDetailId;

        public boolean match(Descriptor<?> desc) {
            return StringUtils.equals(desc.getDisplayName(), this.targetDescriptorName);
        }

        public final String param(String key) {
            return uploadPluginMeta.getExtraParam(key);
        }

        /**
         * 取得子表单的宿主plugin
         *
         * @param pluginContext
         * @param <T>
         * @return
         */
        public <T> T getOwnerPlugin(IPluginContext pluginContext) {
            Optional<Object> first = this.uploadPluginMeta.getHeteroEnum().getPlugins(pluginContext, this.uploadPluginMeta).stream().findFirst();
            if (!first.isPresent()) {
                throw new IllegalStateException("can not find owner plugin:" + uploadPluginMeta.toString());
            }
            return (T) first.get();
        }

        public SubFormFilter(UploadPluginMeta uploadPluginMeta, String targetDescriptorName, String subFieldName) {
            if (StringUtils.isEmpty(targetDescriptorName)) {
                throw new IllegalArgumentException("param fieldName can not be empty");
            }
            if (StringUtils.isEmpty(subFieldName)) {
                throw new IllegalArgumentException("param subFieldName can not be empty");
            }
            this.targetDescriptorName = targetDescriptorName;
            this.subFieldName = subFieldName;
            this.uploadPluginMeta = uploadPluginMeta;
            this.subformDetailView = StringUtils.isNotEmpty(subformDetailId = uploadPluginMeta.getExtraParam(PLUGIN_META_SUBFORM_DETAIL_ID_VALUE));
        }
    }
}
