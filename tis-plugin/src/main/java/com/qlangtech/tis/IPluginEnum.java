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

package com.qlangtech.tis;

import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-29 14:59
 * @see com.qlangtech.tis.util.HeteroEnum impl
 **/
public interface IPluginEnum<T extends Describable<T>> extends IdentityName {
    static final Map<HeteroEnum, Function<Descriptor, List<Option>>> existItemsGetter = Maps.newHashMap();

    static void registeExistItemsGettor(HeteroEnum hetero, Function<Descriptor, List<Option>> itemsGetter) {
        existItemsGetter.put(hetero, itemsGetter);
    }

    public Class<T> getExtensionPoint();

    public String getIdentity();

    @Override
    default String identityValue() {
        return getIdentity();
    }

    public String getCaption();

    public Selectable getSelectable();

    public <T> List<T> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta);


    default <T> T findPlugin(PluginExtraProps.CandidatePlugin candidatePlugin, IdentityName identity) {
        throw new UnsupportedOperationException("hetero:"
                + this.getExtensionPoint().getSimpleName()
                + "is not support for findPlugin with param id:" + identity.identityValue());
    }


    public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta);

    public <T extends Describable<T>> List<Descriptor<T>> descriptors(UploadPluginMeta.TargetDesc targetDesc,
                                                                      List<T> items, boolean justGetItemRelevant);

    public <T extends Describable<T>> List<Descriptor<T>> descriptors();

    public boolean isIdentityUnique();

    public boolean isAppNameAware();

    /**
     * 例如DataSource实例，需要找已经存在的实例列表
     *
     * @param pluginDesc
     * @return
     */
    default List<Option> getExistItems(Descriptor<T> pluginDesc) {
        return Objects.requireNonNull(existItemsGetter.get(this),
                this.getIdentity() + "(" + this.getExtensionPoint().getSimpleName() + ")" + " relevant "
                        + "existItemsGetter can not be null,please invoke registeExistItemsGettor").apply(pluginDesc);
    }

    /**
     * 创建记录的token
     * @param hostPluginId
     * @param valMap
     * @return
     */
    default Pair<DataXName, UploadPluginMeta> createPKToken(Optional<String> hostPluginId, AttrValMap valMap) {
        // IdentityName.create(valMap.getPrimaryFieldVal())
        throw new UnsupportedOperationException(this.getIdentity() + "," + this.getExtensionPoint().getName());
    }
}
