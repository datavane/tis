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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-04-02 09:15
 **/
public class PluginAndCfgsSnapshot {
    public static final String TIS_APP_NAME = "tis_app_name";
    private static final Logger logger = LoggerFactory.getLogger(PluginAndCfgsSnapshot.class);
    private static PluginAndCfgsSnapshot pluginAndCfgsSnapshot;

    public static String getTaskEntryName(int taskId) {
        if (taskId < 1) {
            throw new IllegalArgumentException("taskId shall be set");
        }
        return "task" + taskId;
    }


    public static String convertCfgPropertyKey(String key, boolean serialize) {
        return serialize ?
                org.apache.commons.lang3.StringUtils.replace(key, ".", "_")
                : org.apache.commons.lang3.StringUtils.replace(key, "_", ".");
    }

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

    private final Optional<KeyedPluginStore.PluginMetas> appMetas;

    public PluginAndCfgsSnapshot(TargetResName collection, Map<String, Long> globalPluginStoreLastModify
            , Set<XStream2.PluginMeta> pluginMetas, Long appLastModifyTimestamp, KeyedPluginStore.PluginMetas appMetas) {
        this.globalPluginStoreLastModify = globalPluginStoreLastModify;

        this.pluginMetas = pluginMetas;
        this.appLastModifyTimestamp = appLastModifyTimestamp;
        this.collection = collection;
        this.appMetas = Optional.ofNullable(appMetas);
    }

    public static Manifest createManifestCfgAttrs(TargetResName collection, long timestamp) throws Exception {

        Manifest manifest = new Manifest();
        Map<String, Attributes> entries = manifest.getEntries();
        Attributes attrs = new Attributes();
        attrs.put(new Attributes.Name(collection.getName()), String.valueOf(timestamp));
        // 传递App名称
        entries.put(TIS_APP_NAME, attrs);

        final Attributes cfgAttrs = new Attributes();
        // 传递Config变量
        Config.getInstance().visitKeyValPair((e) -> {
            if (Config.KEY_TIS_HOST.equals(e.getKey())) {
                // tishost为127.0.0.1会出错
                return;
            }
            cfgAttrs.put(new Attributes.Name(convertCfgPropertyKey(e.getKey(), true)), e.getValue());
        });
        cfgAttrs.put(new Attributes.Name(
                convertCfgPropertyKey(Config.KEY_TIS_HOST, true)), NetUtils.getHost());
        entries.put(Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS, cfgAttrs);

        //=====================================================================
        if (!CenterResource.notFetchFromCenterRepository()) {
            throw new IllegalStateException("must not fetchFromCenterRepository");
        }
        //"globalPluginStore"  "pluginMetas"  "appLastModifyTimestamp"
        XStream2.PluginMeta flinkPluginMeta
                = new XStream2.PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + collection.getName()
                , Config.getMetaProps().getVersion());
        PluginAndCfgsSnapshot localSnapshot
                = getLocalPluginAndCfgsSnapshot(collection, flinkPluginMeta);

        localSnapshot.attachPluginCfgSnapshot2Manifest(manifest);
        return manifest;
    }

    private static void collectAllPluginMeta(XStream2.PluginMeta meta, Set<XStream2.PluginMeta> collector) {
        meta.getLastModifyTimeStamp();
        collector.add(meta);
        List<XStream2.PluginMeta> dpts = meta.getMetaDependencies();
        collector.addAll(dpts);
        for (XStream2.PluginMeta m : dpts) {
            collectAllPluginMeta(m, collector);
        }
    }

    public Set<String> getPluginNames() {
        return pluginMetas.stream().map((m) -> m.getPluginName()).collect(Collectors.toSet());
    }


    /**
     * 通过将远程仓库中的plugin tpi的最近更新时间和本地tpi的最新更新时间经过对比，计算出需要更新的插件集合
     *
     * @param localSnaphsot
     * @return
     */
    public void synchronizTpisAndConfs(PluginAndCfgsSnapshot localSnaphsot) throws Exception {
        if (!localSnaphsot.appMetas.isPresent()) {
            throw new IllegalArgumentException("localSnaphsot.appMetas must be present");
        }
        Set<XStream2.PluginMeta> result = Sets.newHashSet();
        StringBuffer updateTpisLogger = new StringBuffer("\nplugin synchronize------------------------------\n");

        Long localTimestamp;
        File cfg = null;
        boolean cfgChanged = false;
        // URL globalCfg = null;
        updateTpisLogger.append(">>global cfg compare:\n");
        for (Map.Entry<String, Long> entry : this.globalPluginStoreLastModify.entrySet()) {
            localTimestamp = localSnaphsot.globalPluginStoreLastModify.get(entry.getKey());
            if (localTimestamp == null || entry.getValue() > localTimestamp) {
                // 更新本地配置文件
                //globalCfg = CenterResource.getPathURL(Config.SUB_DIR_CFG_REPO, TIS.KEY_TIS_PLUGIN_CONFIG + "/" + entry.getKey());
                cfg = CenterResource.copyFromRemote2Local(Config.KEY_TIS_PLUGIN_CONFIG + "/" + entry.getKey(), true);
                FileUtils.writeStringToFile(
                        PluginStore.getLastModifyTimeStampFile(cfg), String.valueOf(entry.getValue()), TisUTF8.get());
                cfgChanged = true;
                updateTpisLogger.append(entry.getKey()).append(localTimestamp == null
                        ? "[" + entry.getValue() + "] local is none"
                        : " center ver:" + entry.getValue()
                        + " > local ver:" + localTimestamp).append("\n");
            }
        }


        updateTpisLogger.append(">>app cfg compare:\n");
        updateTpisLogger.append("center:").append(this.appLastModifyTimestamp)
                .append(this.appLastModifyTimestamp > localSnaphsot.appLastModifyTimestamp ? " > " : " <= ").append("local:").append(localSnaphsot.appLastModifyTimestamp).append("\n");
        if (this.appLastModifyTimestamp > localSnaphsot.appLastModifyTimestamp) {
            // 更新app相关配置,下载并更新本地配置
            KeyedPluginStore.AppKey appKey = new KeyedPluginStore.AppKey(null, false, this.collection.getName(), null);
            URL appCfgUrl = CenterResource.getPathURL(Config.SUB_DIR_CFG_REPO, Config.KEY_TIS_PLUGIN_CONFIG + "/" + appKey.getSubDirPath());

            KeyedPluginStore.PluginMetas appMetas = localSnaphsot.appMetas.get();
            HttpUtils.get(appCfgUrl, new ConfigFileContext.StreamProcess<Void>() {
                @Override
                public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    try {
                        FileUtils.deleteQuietly(appMetas.appDir);
                        ZipInputStream zipInput = new ZipInputStream(stream);
                        ZipEntry entry = null;
                        while ((entry = zipInput.getNextEntry()) != null) {
                            try (OutputStream output = FileUtils.openOutputStream(new File(appMetas.appDir, entry.getName()))) {
                                IOUtils.copy(zipInput, output);
                            }
                            zipInput.closeEntry();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            });
            cfgChanged = true;
        }

        updateTpisLogger.append(">>center repository:")
                .append(pluginMetas.stream().map((meta) -> meta.toString()).collect(Collectors.joining(",")));
        updateTpisLogger.append("\n>>local:")
                .append(localSnaphsot.pluginMetas.stream()
                        .map((meta) -> meta.toString())
                        .collect(Collectors.joining(","))).append("\n");
        updateTpisLogger.append(">>compare result\n");
        Map<String, XStream2.PluginMeta> locals = localSnaphsot.pluginMetas.stream()
                .collect(Collectors.toMap((m) -> m.getKey(), (m) -> m));
        XStream2.PluginMeta m = null;
        for (XStream2.PluginMeta meta : pluginMetas) {
            m = locals.get(meta.getKey());
            if (m == null || meta.getLastModifyTimeStamp() > m.getLastModifyTimeStamp()) {
                result.add(meta);
                updateTpisLogger.append(meta.getKey()).append(m == null
                        ? " local is none"
                        : " center repository ver:" + meta.getLastModifyTimeStamp()
                        + " > local ver:" + m.getLastModifyTimeStamp()).append("\n");
            }
        }

        for (XStream2.PluginMeta update : result) {
            update.copyFromRemote(Collections.emptyList(), true, true);
        }
        PluginManager pluginManager = TIS.get().getPluginManager();
        Set<XStream2.PluginMeta> loaded = Sets.newHashSet();
        PluginWrapperList batch = new PluginWrapperList();
        for (XStream2.PluginMeta update : result) {
            dynamicLoad(pluginManager, update, batch, result, loaded);
        }

        if (batch.size() > 0) {
            pluginManager.start(batch);
        }
        Thread.sleep(3000l);
        if (cfgChanged) {
            TIS.cleanPluginStore();
        }

        logger.info(updateTpisLogger.append("\n------------------------------").toString());
        //   return result;
    }

    /**
     * 为了去除batch plugin中的重复机器，用一个List包裹一下
     */
    public static class PluginWrapperList {
        List<PluginWrapper> batch = Lists.newArrayList();
        Set<String> addPluginNams = Sets.newHashSet();

        public PluginWrapperList() {
        }

        public PluginWrapperList(PluginWrapper pluginWrapper) {
            this.add(pluginWrapper);
        }

        public PluginWrapperList(List<PluginWrapper> plugins) {
            plugins.forEach((p) -> {
                add(p);
            });
        }

        public void add(PluginWrapper plugin) {
            if (addPluginNams.add(plugin.getShortName())) {
                batch.add(plugin);
            }
        }

        public List<PluginWrapper> getPlugins() {
            return this.batch;
        }

        public Map<String, PluginWrapper> getPluginsByName() {
            return batch.stream().collect(Collectors.toMap(PluginWrapper::getShortName, p -> p));
        }

        public Set<ClassLoader> getLoaders() {
            return batch.stream().map(p -> p.classLoader).collect(Collectors.toSet());
        }

        public int size() {
            return batch.size();
        }

        public boolean contains(PluginWrapper depender) {
            for (PluginWrapper wrapper : batch) {
                if (StringUtils.equals(wrapper.getShortName(), depender.getShortName())) {
                    return true;
                }
            }
            return false;
//            return batch.contains( depender);
        }
    }

    private void dynamicLoad(PluginManager pluginManager
            , XStream2.PluginMeta update, PluginWrapperList batch, Set<XStream2.PluginMeta> shallUpdate, Set<XStream2.PluginMeta> loaded) {
        try {
            for (XStream2.PluginMeta dpt : update.getMetaDependencies()) {
                this.dynamicLoad(pluginManager, dpt, batch, shallUpdate, loaded);
            }
            if (shallUpdate.contains(update) && loaded.add(update)) {
                pluginManager.dynamicLoad(update.getPluginPackageFile(), true, batch);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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

        List<XStream2.PluginMeta> metas
                = XStream2.PluginMeta.parse(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_PLUGIN_META));
        metas.forEach((meta) -> {
            if (meta.isLastModifyTimeStampNull()) {
                throw new IllegalStateException("pluginMeta:" + meta.getKey() + " relevant LastModify timestamp can not be null");
            }
        });
        return new PluginAndCfgsSnapshot(app, globalPluginStoreLastModify
                , Sets.newHashSet(metas)
                , Long.parseLong(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP)), null);
    }

//    public static PluginAndCfgsSnapshot getLocalPluginAndCfgsSnapshot(
//            TargetResName collection, XStream2.PluginMeta... appendPluginMeta) {
//        return getLocalPluginAndCfgsSnapshot(collection, true, appendPluginMeta);
//    }

    public static PluginAndCfgsSnapshot getLocalPluginAndCfgsSnapshot(
            TargetResName collection, XStream2.PluginMeta... appendPluginMeta) {

        Set<XStream2.PluginMeta> globalPluginMetas = null;
        Map<String, Long> gPluginStoreLastModify = Collections.emptyMap();
        UploadPluginMeta upm = UploadPluginMeta.parse("x:require");
        ExtensionList<HeteroEnum> hlist = TIS.get().getExtensionList(HeteroEnum.class);
        List<IRepositoryResource> keyedPluginStores = hlist.stream()
                .filter((e) -> !e.isAppNameAware())
                .flatMap((e) -> e.getPluginStore(null, upm).getAll().stream())
                .collect(Collectors.toList());
        ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
        globalPluginMetas = dataxComponentMeta.loadPluginMeta();

        // if (storeCfgAware) {
        gPluginStoreLastModify = ComponentMeta.getGlobalPluginStoreLastModifyTimestamp(dataxComponentMeta);
        //}

        // 本次任务相关插件元信息
        KeyedPluginStore.PluginMetas pluginMetas = KeyedPluginStore.getAppAwarePluginMetas(false, collection.getName());

        Set<XStream2.PluginMeta> collector = Sets.newHashSet();
        for (XStream2.PluginMeta m : pluginMetas.metas) {
            collectAllPluginMeta(m, collector);
        }
        for (XStream2.PluginMeta m : globalPluginMetas) {
            collectAllPluginMeta(m, collector);
        }
        for (XStream2.PluginMeta m : appendPluginMeta) {
            collectAllPluginMeta(m, collector);
        }
        return new PluginAndCfgsSnapshot(
                collection, gPluginStoreLastModify
                , collector, pluginMetas.lastModifyTimestamp, pluginMetas);
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
        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE), String.valueOf(globalPluginStore));
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
