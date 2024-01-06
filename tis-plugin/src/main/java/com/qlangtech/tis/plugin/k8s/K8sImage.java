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
package com.qlangtech.tis.plugin.k8s;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.k8s.IK8sContext;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.util.HeteroEnum;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 18:07
 */
@Public
public abstract class K8sImage implements Describable<K8sImage>, IdentityName {

//    public static final String DEFAULT_DESC_NAME = "dft-image";
//    public static final String DEFAULT_POWERJOB_DESC_NAME = "powerjob-image";

    /**
     * 获取Plugin存储器
     *
     * @param imageCategory
     * @return
     */
    public static IPluginStore getPluginStore(K8sImage.ImageCategory imageCategory) {
//        UploadPluginMeta pluginMeta = UploadPluginMeta.parse(
//                HeteroEnum.K8S_IMAGES.identity + ":" + UploadPluginMeta.KEY_REQUIRE + "," + KEY_TARGET_PLUGIN_DESC + ATTR_KEY_VALUE_SPLIT + imageCategory.token);
//        return pluginMeta.getHeteroEnum().getPluginStore(null, pluginMeta);
        return imageCategory.getPluginStore();
    }


    public static enum ImageCategory {
        DEFAULT_DESC_NAME("dft-image", () -> HeteroEnum.K8S_DEFAULT_IMAGES.getPluginStore(null, null)) //
        , DEFAULT_POWERJOB_DESC_NAME("powerjob-image", () -> HeteroEnum.K8S_POWERJOB_IMAGES.getPluginStore(null, null)) //
        , DEFAULT_FLINK_DESC_NAME("flink-image", () -> HeteroEnum.K8S_FLINK_IMAGES.getPluginStore(null, null));
        public final String token;
        private final Supplier<IPluginStore<K8sImage>> pluginStoreGetter;

        ImageCategory(String token, Supplier<IPluginStore<K8sImage>> pluginStoreGetter) {
            this.token = token;
            this.pluginStoreGetter = pluginStoreGetter;
        }

        public IPluginStore<K8sImage> getPluginStore() {
            return pluginStoreGetter.get();
        }

        public static ImageCategory parse(String token) {

            for (ImageCategory c : ImageCategory.values()) {
                if (StringUtils.equals(c.token, token)) {
                    return c;
                }
            }

            throw new IllegalStateException("illegal token:" + token);
        }
    }


    protected abstract String getK8SName();

    public abstract String getNamespace();

    public abstract String getImagePath();

    public abstract List<HostAlias> getHostAliases();

//    public static class HostAliases extends ArrayList<HostAlias> {
//    }

    /**
     * ParamsConfig.createConfigInstance(): io.kubernetes.client.openapi.ApiClient
     *
     * @param
     * @return
     */
    public <T> T createApiClient() {
        ParamsConfig cfg = (ParamsConfig) getK8SCfg();
        return cfg.createConfigInstance();
    }

    public IK8sContext getK8SCfg() {
        return ParamsConfig.getItem(this.getK8SName(), IK8sContext.KEY_DISPLAY_NAME);
    }

    @Override
    public final Descriptor<K8sImage> getDescriptor() {
        Descriptor<K8sImage> desc = TIS.get().getDescriptor(this.getClass());
        if (!(desc instanceof BasicDesc)) {
            throw new IllegalStateException("desc:" + desc.getClass().getName() + " must be subtype type of " + BasicDesc.class.getName());
        }
        return desc;
    }


    protected static abstract class BasicDesc extends Descriptor<K8sImage> {
        public BasicDesc() {
        }

        @Override
        public final String getDisplayName() {
            return getImageCategory().token;
        }

        protected abstract ImageCategory getImageCategory();
    }
}
