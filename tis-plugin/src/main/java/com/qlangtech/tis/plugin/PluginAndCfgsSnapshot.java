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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-04-02 09:15
 **/
public class PluginAndCfgsSnapshot {

    private static PluginAndCfgsSnapshot pluginAndCfgsSnapshot;

    public static PluginAndCfgsSnapshot setLocalPluginAndCfgsSnapshot(PluginAndCfgsSnapshot snapshot) {
        return pluginAndCfgsSnapshot = snapshot;
    }

    private final TargetResName collection;

    /**
     * key:fileName val:lastModifyTimestamp
     */
    public final Map<String, Long> globalPluginStoreLastModify;

    public final Set<XStream2.PluginMeta> pluginMetas;

    /**
     * 应用相关配置目录的最后更新时间
     */
    public final Long appLastModifyTimestamp;

    public PluginAndCfgsSnapshot(TargetResName collection, Map<String, Long> globalPluginStoreLastModify
            , Set<XStream2.PluginMeta> pluginMetas, Long appLastModifyTimestamp) {
        this.globalPluginStoreLastModify = globalPluginStoreLastModify;
        this.pluginMetas = pluginMetas;
        this.appLastModifyTimestamp = appLastModifyTimestamp;
        this.collection = collection;
    }

    public Set<String> getPluginNames() {
        return pluginMetas.stream().map((m) -> m.name).collect(Collectors.toSet());
    }


    /**
     * 通过将远程仓库中的plugin tpi的最近更新时间和本地tpi的最新更新时间经过对比，计算出需要更新的插件集合
     *
     * @param localSnaphsot
     * @return
     */
    public Set<XStream2.PluginMeta> shallBeUpdateTpis(PluginAndCfgsSnapshot localSnaphsot) {
        Set<XStream2.PluginMeta> result = Sets.newHashSet();
        Map<String, XStream2.PluginMeta> locals = localSnaphsot.pluginMetas.stream().collect(Collectors.toMap((m) -> m.name, (m) -> m));
        XStream2.PluginMeta m = null;
        for (XStream2.PluginMeta meta : pluginMetas) {
            m = locals.get(meta.name);
            if (m == null || meta.getLastModifyTimeStamp() > m.getLastModifyTimeStamp()) {
                result.add(meta);
            }
        }
        return result;
    }

    public TargetResName getAppName() {
        return this.collection;
    }

    /**
     * snapshot 从manifest中反序列化出来
     *
     * @param app
     * @param manifest
     * @return
     */
    public static PluginAndCfgsSnapshot deserializePluginAndCfgsSnapshot(TargetResName app, Manifest manifest) {
        Map<String, Long> globalPluginStoreLastModify = Maps.newHashMap();
        //  Long appLastModifyTimestamp;
        Attributes pluginMetas = manifest.getAttributes(Config.KEY_PLUGIN_METAS);
        String[] globalPluginStoreSeri = StringUtils.split(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE), ",");
        String[] file2timestamp = null;
        for (String p : globalPluginStoreSeri) {
            file2timestamp = StringUtils.split(p, XStream2.PluginMeta.NAME_VER_SPLIT);
            if (file2timestamp.length != 2) {
                throw new IllegalStateException("file2timestamp length must be 2,val:" + p);
            }
            globalPluginStoreLastModify.put(file2timestamp[0], Long.parseLong(file2timestamp[1]));
        }
        String metas = pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_PLUGIN_META);

        return new PluginAndCfgsSnapshot(app, globalPluginStoreLastModify
                , Sets.newHashSet(XStream2.PluginMeta.parse(metas))
                , Long.parseLong(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP)));
    }

    public static PluginAndCfgsSnapshot getLocalPluginAndCfgsSnapshot(TargetResName collection) {

        ExtensionList<HeteroEnum> hlist = TIS.get().getExtensionList(HeteroEnum.class);
        List<IRepositoryResource> keyedPluginStores = hlist.stream()
                .filter((e) -> !e.isAppNameAware())
                .map((e) -> e.getPluginStore(null, null))
                .collect(Collectors.toList());
        ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
        Set<XStream2.PluginMeta> globalPluginMetas = dataxComponentMeta.loadPluginMeta();
        Map<String, Long> gPluginStoreLastModify = ComponentMeta.getGlobalPluginStoreLastModifyTimestamp(dataxComponentMeta);


        // 本次任务相关插件元信息
        KeyedPluginStore.PluginMetas pluginMetas = KeyedPluginStore.getAppAwarePluginMetas(false, collection.getName());

        return new PluginAndCfgsSnapshot(
                collection, gPluginStoreLastModify, Sets.union(pluginMetas.metas, globalPluginMetas), pluginMetas.lastModifyTimestamp);
    }

    public void attachPluginCfgSnapshot2Manifest(Manifest manifest) {
        Map<String, Attributes> entries = manifest.getEntries();
        // ExtensionList<HeteroEnum> hlist = TIS.get().getExtensionList(HeteroEnum.class);
//        List<IRepositoryResource> keyedPluginStores = hlist.stream()
//                .filter((e) -> !e.isAppNameAware())
//                .map((e) -> e.getPluginStore(null, null))
//                .collect(Collectors.toList());
//        ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
        //Set<XStream2.PluginMeta> globalPluginMetas = dataxComponentMeta.loadPluginMeta();
        //Map<String, Long> gPluginStoreLastModify = ComponentMeta.getGlobalPluginStoreLastModifyTimestamp(dataxComponentMeta);

        StringBuffer globalPluginStore = new StringBuffer();
        for (Map.Entry<String, Long> e : globalPluginStoreLastModify.entrySet()) {
            globalPluginStore.append(e.getKey())
                    .append(XStream2.PluginMeta.NAME_VER_SPLIT).append(e.getValue()).append(",");
        }

        final Attributes pmetas = new Attributes();
        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE), globalPluginStore);
        // 本次任务相关插件元信息
        //KeyedPluginStore.PluginMetas pluginMetas = KeyedPluginStore.getAppAwarePluginMetas(false, collection.getName());
        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_PLUGIN_META)
                , this.pluginMetas.stream().map((meta) -> {
                    meta.getLastModifyTimeStamp();
                    return meta.toString();
                }).collect(Collectors.joining(",")));

        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP)
                , String.valueOf(this.appLastModifyTimestamp));

        entries.put(Config.KEY_PLUGIN_METAS, pmetas);
    }
}
