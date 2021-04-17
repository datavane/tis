/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        // 目标插件名称
        public static String PLUGIN_META_TARGET_DESCRIPTOR_NAME = "targetDescriptorName";
        public static String PLUGIN_META_SUB_FORM_FIELD = "subFormFieldName";
        private final String targetDescriptorName;
        public final String subFieldName;
        public final UploadPluginMeta uploadPluginMeta;

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
        }
    }
}
