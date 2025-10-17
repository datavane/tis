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

package com.qlangtech.tis.plugin.credentials;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.ParamsConfig.BasicParamsConfigDescriptor;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.plugin.SetPluginsResult;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qlangtech.tis.util.UploadPluginMeta.KEY_REQUIRE;
import static com.qlangtech.tis.util.UploadPluginMeta.KEY_TARGET_PLUGIN_DESC;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-12-07 17:48
 **/
public class ParamsConfigPluginStore implements IPluginStore<ParamsConfig> {

    private final File paramsCfgDir;
    private final String childContextDir;
    private final UploadPluginMeta pluginMeta;

    public static UploadPluginMeta createParamsConfigUserIsolation(String paramPluginCategory) {
        return createParamsConfig(HeteroEnum.PARAMS_CONFIG_USER_ISOLATION, paramPluginCategory);
    }


    public static UploadPluginMeta createParamsConfig(IPluginEnum hetero, PluginExtraProps.CandidatePlugin candidatePlugin) {
        // getTargetItemDesc()
        return createParamsConfig(hetero, candidatePlugin.getTargetItemDesc());
    }

    public static UploadPluginMeta createParamsConfig(IPluginEnum hetero, String paramPluginCategory) {
        UploadPluginMeta pluginMeta
                = UploadPluginMeta.parse(hetero.identityValue() + ":" + KEY_REQUIRE
                + "," + KEY_TARGET_PLUGIN_DESC + "_" + paramPluginCategory);
        return pluginMeta;
    }


    public ParamsConfigPluginStore(UploadPluginMeta pluginMeta) {
        this(pluginMeta, Optional.empty());
    }

    public ParamsConfigPluginStore(UploadPluginMeta pluginMeta, Optional<ILoginUser> loginUser) {
        if (pluginMeta == null) {
            throw new IllegalArgumentException("param pluginMeta can not be null");
        }
        this.pluginMeta = pluginMeta;
        try {
            this.childContextDir = ParamsConfig.CONTEXT_PARAMS_CFG + loginUser.map((user) -> "-user-isolation" + File.separator + user.getName()).orElse(StringUtils.EMPTY);
            paramsCfgDir = new File(TIS.pluginCfgRoot
                    , this.childContextDir);
            FileUtils.forceMkdir(paramsCfgDir);
        } catch (IOException e) {
            throw new RuntimeException("can not create dir:" + ParamsConfig.CONTEXT_PARAMS_CFG, e);
        }
    }


    @Override
    public List<IRepositoryResource> getAll() {
        ExtensionList<Descriptor<ParamsConfig>> descs
                = TIS.get().getDescriptorList(ParamsConfig.class);
        return descs.stream()
                .map((desc) -> ParamsConfig.getTargetPluginStore(
                        ParamsConfig.CONTEXT_PARAMS_CFG, desc.getDisplayName(), Optional.empty(), false))
                .filter((rr) -> rr != null && rr.getTargetFile().exists()).collect(Collectors.toList());
    }

    @Override
    public List<ParamsConfig> getPlugins() {
        List<ParamsConfig> plugins = Lists.newArrayList();
        UploadPluginMeta.TargetDesc targetDesc = this.pluginMeta.getTargetDesc();

        if (StringUtils.isEmpty(targetDesc.matchTargetPluginDescName)) {
            visitAllPluginStore((store) -> {
                List<ParamsConfig> childs = store.getRight().getPlugins();
                add2Childs(childs, plugins);
                return null;
            });
        } else {
            IPluginStore<ParamsConfig> pluginStore = ParamsConfig.getChildPluginStore(this.childContextDir, targetDesc.pluginStoreGroupPath(this.pluginMeta));
            List<ParamsConfig> childs = pluginStore.getPlugins();
            add2Childs(childs, plugins);
        }
        return plugins;
    }

    private void add2Childs(List<ParamsConfig> childs, List<ParamsConfig> plugins) {
        childs.forEach((c) -> {
            if (c.test(this.pluginMeta)) {
                plugins.add(c);
            }
        });
    }


    private <TT> TT visitAllPluginStore(Function<Pair<String, IPluginStore<ParamsConfig>>, TT> func) {
        final String[] childFiles = paramsCfgDir.list();

        TT result = null;
        for (String childFile : childFiles) {
            IPluginStore<ParamsConfig> pluginStore = ParamsConfig.getChildPluginStore(this.childContextDir, childFile);
            result = func.apply(Pair.of(childFile, pluginStore));
            if (result != null) {
                return result;
            }
        }
        return null;
    }


    @Override
    public ParamsConfig getPlugin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanPlugins() {
        visitAllPluginStore((ps) -> {
            ps.getRight().cleanPlugins();
            return null;
        });
    }

    @Override
    public List<Descriptor<ParamsConfig>> allDescriptor() {

        IPluginStore<ParamsConfig> childPluginStore = ParamsConfig.getTargetPluginStore(this.childContextDir, pluginMeta.getTargetDesc());
        List<Descriptor<ParamsConfig>> descriptors = childPluginStore.allDescriptor();

        List<Descriptor<ParamsConfig>> descs = Lists.newArrayList();
        for (Descriptor<ParamsConfig> desc : descriptors) {
            if (StringUtils.equals(((BasicParamsConfigDescriptor) desc).paramsConfigType(), pluginMeta.getTargetDesc().matchTargetPluginDescName)) {
                descs.add(desc);
            }
        }
        return descs;
    }

    @Override
    public ParamsConfig find(String name, boolean throwNotFoundErr) {
        ParamsConfig cfg = visitAllPluginStore((ps) -> {
            return ps.getRight().find(name, throwNotFoundErr);
        });
        return cfg;
    }

    @Override
    public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context
            , List<Descriptor.ParseDescribable<ParamsConfig>> dlist, boolean update) {

        Map<String, List<Descriptor.ParseDescribable<ParamsConfig>>> desc2Plugin = Maps.newHashMap();
        String storeGroup = null;
        ParamsConfig paramCfg = null;
        List<Descriptor.ParseDescribable<ParamsConfig>> plugins = null;
        boolean cfgChanged = false;
        for (Descriptor.ParseDescribable<ParamsConfig> p : dlist) {
            paramCfg = p.getInstance();
//            paramCfg.identityValue();
            storeGroup = paramCfg.getStoreGroup();
//            descName = paramCfg.getDescriptor().getDisplayName();
            plugins = desc2Plugin.get(storeGroup);
            if (plugins == null) {
                plugins = Lists.newArrayList();
                desc2Plugin.put(storeGroup, plugins);
            }
            plugins.add(p);
        }

        for (Map.Entry<String, List<Descriptor.ParseDescribable<ParamsConfig>>> entry : desc2Plugin.entrySet()) {
            IPluginStore<ParamsConfig> childPluginStore = ParamsConfig.getChildPluginStore(this.childContextDir, entry.getKey());
            if (childPluginStore.setPlugins(pluginContext, context, entry.getValue(), true).cfgChanged) {
                cfgChanged = true;
            }
        }

        return new SetPluginsResult(true, cfgChanged);
    }

    @Override
    public void copyConfigFromRemote() {
        // UploadPluginMeta.TargetDesc desc = getTargetDesc();
        IPluginStore<ParamsConfig> childPluginStore = ParamsConfig.getTargetPluginStore(pluginMeta.getTargetDesc());
        childPluginStore.copyConfigFromRemote();
    }

    @Override
    public XmlFile getTargetFile() {
        // UploadPluginMeta.TargetDesc desc = getTargetDesc();
        IPluginStore<ParamsConfig> childPluginStore = ParamsConfig.getTargetPluginStore(pluginMeta.getTargetDesc());
        return childPluginStore.getTargetFile();
    }

//    private UploadPluginMeta.TargetDesc getTargetDesc() {
//        UploadPluginMeta.TargetDesc desc = pluginMeta.getTargetDesc();
//        if (desc == null || StringUtils.isEmpty(desc.matchTargetPluginDescName)) {
//            throw new IllegalStateException("desc param is not illegal, desc:" + ((desc == null) ? "null" : desc.toString()));
//        }
//        return desc;
//    }

    @Override
    public long getWriteLastModifyTimeStamp() {
        // UploadPluginMeta.TargetDesc desc = getTargetDesc();
        IPluginStore<ParamsConfig> childPluginStore = ParamsConfig.getTargetPluginStore(pluginMeta.getTargetDesc());
        return childPluginStore.getWriteLastModifyTimeStamp();
    }

}
