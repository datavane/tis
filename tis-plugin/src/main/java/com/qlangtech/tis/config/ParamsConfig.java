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
package com.qlangtech.tis.config;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 * @see com.qlangtech.tis.plugin.rate.IncrRateController
 */
@Public
public abstract class ParamsConfig implements Describable<ParamsConfig>, IdentityName, Predicate<UploadPluginMeta> {
    public static final String CONTEXT_PARAMS_CFG = "params-cfg";

    public static <T extends ParamsConfig> List<T> getItems(String pluginDesc) {
        return getItems(pluginDesc, Optional.empty());
    }

    public Option map2SelectOption() {
        return new Option(this.identityValue());
    }

    public static <T extends ParamsConfig> List<T> getItems(String pluginDesc, Optional<String> subpath) {
        IPluginStore<ParamsConfig> paramsCfgStore = getTargetPluginStore(CONTEXT_PARAMS_CFG, pluginDesc, subpath);
        return paramsCfgStore.getPlugins().stream().map((p) -> (T) p).collect(Collectors.toList());
    }

//    取得所有的配置项
//    public static <T> List<T> getItems(String pluginDesc) {
//        List<ParamsConfig> items = getItems(pluginDesc);
//        return items.stream().filter((r) -> type.isAssignableFrom(r.getClass())).map((r) -> (T) r).collect(Collectors.toList());
//    }

    /**
     * 判断该插件是否满足当前上下文条件（例如： IncrRateController利用 identity值进行实例隔离）
     *
     * @param uploadPluginMeta the input argument
     * @return
     */
    @Override
    public boolean test(UploadPluginMeta uploadPluginMeta) {
        return true;
    }

    public static IPluginStore<ParamsConfig> getTargetPluginStore(UploadPluginMeta.TargetDesc desc) {
        return getTargetPluginStore(CONTEXT_PARAMS_CFG, desc);
    }

    public static IPluginStore<ParamsConfig> getTargetPluginStore(String childContextDir, UploadPluginMeta.TargetDesc desc) {
        if (desc == null || StringUtils.isEmpty(desc.matchTargetPluginDescName)) {
            throw new IllegalStateException("desc param is not illegal, desc:" + ((desc == null) ? "null" : desc.toString()));
        }
        return ParamsConfig.getTargetPluginStore(childContextDir, desc.matchTargetPluginDescName, Optional.empty());
    }

    public static IPluginStore<ParamsConfig> getTargetPluginStore(String childContextDir, String targetPluginDesc, Optional<String> subpath) {
        return getTargetPluginStore(childContextDir, targetPluginDesc, subpath, true);
    }

    public static IPluginStore<ParamsConfig> getTargetPluginStore(
            String childContextDir, String targetPluginDesc, Optional<String> subpath, boolean validateExist) {
        if (StringUtils.isEmpty(targetPluginDesc)) {
            throw new IllegalStateException("param targetPluginDesc can not be null");
        }
        IPluginStore<ParamsConfig> childPluginStore = getChildPluginStore(childContextDir, targetPluginDesc, subpath);
        if (validateExist && childPluginStore == null) {
            throw new IllegalStateException("targetPluginDesc:" + targetPluginDesc + " relevant childPluginStore can not be null");
        }
        return childPluginStore;
    }

    public static IPluginStore<ParamsConfig> getChildPluginStore(String childContextDir, String childFile) {
        return getChildPluginStore(childContextDir, childFile, Optional.empty());
    }

    public static IPluginStore<ParamsConfig> getChildPluginStore(String childContextDir, String childFile, Optional<String> subpath) {
        if (StringUtils.isEmpty(childFile)) {
            throw new IllegalArgumentException("param childFile can not be empty");
        }

        return TIS.getPluginStore(new KeyedPluginStore.Key(childContextDir, new KeyedPluginStore.KeyVal(childFile, subpath), ParamsConfig.class));
    }


    public abstract <INSTANCE> INSTANCE createConfigInstance();

    public static <T extends ParamsConfig> T getItem(String identityName, String targetPluginDesc) {
        return getItem(identityName, targetPluginDesc, Optional.empty(), true);
    }

    /**
     * @param identityName
     * @param targetPluginDesc
     * @param valiateNull      是否校验为空
     * @param <T>
     * @return
     */
    public static <T extends ParamsConfig> T getItem(String identityName, String targetPluginDesc, Optional<String> subpath, boolean valiateNull) {
        if (StringUtils.isEmpty(identityName)) {
            throw new IllegalArgumentException("param identityName can not be empty");
        }
        List<T> items = getItems(targetPluginDesc, subpath);
        for (T i : items) {
            if (StringUtils.equals(i.identityValue(), identityName)) {
                return i;
            }
        }
        if (valiateNull) {
            throw new IllegalStateException("Name:" + identityName + ",type:" + targetPluginDesc + " can not find relevant config in["
                    + items.stream().map((r) -> r.identityValue()).collect(Collectors.joining(",")) + "]");
        } else {
            return null;
        }
    }


    @Override
    @JSONField(serialize = false)
    public final BasicParamsConfigDescriptor getDescriptor() {
        Descriptor<ParamsConfig> desc = TIS.get().getDescriptor(this.getClass());
        if (desc == null) {
            throw new IllegalStateException("describle class:" + this.getClass() + " relevant desc can not be null");
        }
        if (!BasicParamsConfigDescriptor.class.isAssignableFrom(desc.getClass())) {
            throw new IllegalStateException(desc.getClass().getSimpleName() + " must be child of " + BasicParamsConfigDescriptor.class.getName());
        }
        return (BasicParamsConfigDescriptor) desc;
    }

    // public static DescriptorExtensionList<ParamsConfig, Descriptor<ParamsConfig>> all() {
    // DescriptorExtensionList<ParamsConfig, Descriptor<ParamsConfig>> descriptorList
    // = TIS.get().getDescriptorList(ParamsConfig.class);
    // return descriptorList;
    // }
    public static List<Descriptor<ParamsConfig>> all(Class<?> type) {
        List<Descriptor<ParamsConfig>> desc = HeteroEnum.PARAMS_CONFIG.descriptors();
        return desc.stream().filter((r) -> type.isAssignableFrom(r.getT())).collect(Collectors.toList());
    }

    /**
     * 取得存储的路径，这样可以让用户选择是否按照 identityName 来隔离
     *
     * @return
     */
    public String getStoreGroup() {
        return this.getDescriptor().paramsConfigType();
    }


    public static abstract class BasicParamsConfigDescriptor extends Descriptor<ParamsConfig> {
        private final String paramsConfigType;

        public BasicParamsConfigDescriptor(String paramsConfigType) {
            super();
            this.paramsConfigType = paramsConfigType;
        }

        public final String paramsConfigType() {
            return this.paramsConfigType;
        }

        @Override
        public String getDisplayName() {
            return this.paramsConfigType;
        }
    }
}
