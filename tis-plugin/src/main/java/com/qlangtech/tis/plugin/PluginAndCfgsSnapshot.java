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

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.IFlinkColCreator;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.config.flink.IFlinkCluster;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.StoreResourceTypeConstants;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.impl.PluginManifest;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.maven.plugins.tpi.PluginClassifier;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.plugin.license.TISLicense;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PluginMeta;
import com.qlangtech.tis.util.RobustReflectionConverter2;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.web.start.TisSubModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
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

    public static String getTaskEntryName() {
        //        if (taskId < 1) {
        //            throw new IllegalArgumentException("taskId shall be set");
        //        }
        //  return "task_xxxx" + taskId;
        return "task_xxxx";
    }

    public static PluginAndCfgsSnapshot getRepositoryCfgsSnapshot(
            String resName, StoreResourceType resourceType, InputStream manifestJar) throws IOException {
        return getRepositoryCfgsSnapshot(resName, Objects.requireNonNull(resourceType), manifestJar, true);
    }

    /**
     * 远程传输过来的资源快照信息
     *
     * @param resName
     * @param manifestJar
     * @return
     * @throws IOException
     */
    public static PluginAndCfgsSnapshot getRepositoryCfgsSnapshot(
            String resName, StoreResourceType resourceType, InputStream manifestJar,
            boolean resetConfigWithSysProps) throws IOException {

        PluginAndCfgsSnapshot pluginAndCfgsSnapshot = null;
        String appName = null;
        // for (URL lib : libraryURLs) {
        //try (
        JarInputStream jarReader = new JarInputStream(manifestJar);
        //) {
        Manifest manifest = jarReader.getManifest();
        appName = getRepositoryCfgsSnapshot(resName, manifest);

        // KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE;
        //Attributes pluginMetas = manifest.getAttributes(Config.KEY_PLUGIN_METAS);
        // processPluginMetas(pluginMetas);

        pluginAndCfgsSnapshot =
                PluginAndCfgsSnapshot.setLocalPluginAndCfgsSnapshot(
                        PluginAndCfgsSnapshot.deserializePluginAndCfgsSnapshot(new TargetResName(appName), resourceType, manifest));
        Attributes sysProps = manifest.getAttributes(Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS);
        if (resetConfigWithSysProps) {
            Config.setConfig(null);
            System.setProperty(Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS, String.valueOf(true));
            StringBuffer sysPropsDesc = new StringBuffer();
            for (Map.Entry<Object, Object> pluginDesc : sysProps.entrySet()) {
                Attributes.Name name = (Attributes.Name) pluginDesc.getKey();
                String val = (String) pluginDesc.getValue();
                String key = PluginAndCfgsSnapshot.convertCfgPropertyKey(name.toString(), false);
                System.setProperty(key, val);
                sysPropsDesc.append("\n").append(key).append("->").append(val);
            }
            logger.info("sysProps details:" + sysPropsDesc.toString());
        }

        // @see TISFlinkCDCStreamFactory 在这个类中进行配置信息的加载

        // shall not have any exception here.
        TisSubModule.TIS_CONSOLE.getLaunchPort();
        Config.getInstance();

        //}
        if (pluginAndCfgsSnapshot == null) {
            throw new IllegalStateException("param appName can not be null,in res name:" + resName);
        }
        //  }

        return pluginAndCfgsSnapshot;
    }

    //    private static void processPluginMetas(Attributes pluginMetas) {
    //        pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE);
    //        pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_PLUGIN_META);
    //        pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP);
    //    }

    public static String getRepositoryCfgsSnapshot(String resName, Manifest manifest) {
        Attributes tisAppName = manifest.getAttributes(PluginAndCfgsSnapshot.TIS_APP_NAME);
        String appName = null;
        //  Attributes pluginInventory = manifest.getAttributes("plugin_inventory");
        if (tisAppName == null) {
            throw new IllegalStateException("tisAppName can not be empty in lib:" + resName);
        }

        aa:
        for (Map.Entry<Object, Object> pluginDesc : tisAppName.entrySet()) {
            Attributes.Name name = (Attributes.Name) pluginDesc.getKey();
            String val = (String) pluginDesc.getValue();
            appName = name.toString();
            break aa;
            //  pluginManager.dynamicLoadPlugin(String.valueOf(pluginDesc.getKey()));
        }
        return appName;
    }


    public static String convertCfgPropertyKey(String key, boolean serialize) {
        return serialize ? org.apache.commons.lang3.StringUtils.replace(key, ".", "_") :
                org.apache.commons.lang3.StringUtils.replace(key, "_", ".");
    }

    public static PluginAndCfgsSnapshot setLocalPluginAndCfgsSnapshot(PluginAndCfgsSnapshot snapshot) {
        return pluginAndCfgsSnapshot = snapshot;
    }

    private final TargetResName collection;
    private final StoreResourceType resType;

    /**
     * key:fileName val:lastModifyTimestamp
     */
    public final Map<String, Long> globalPluginStoreLastModify;

    public final Set<PluginMeta> pluginMetas;
    private final Set<IRepositoryResource> repoRes;

    /**
     * 应用相关配置目录的最后更新时间
     */
    public final Long appLastModifyTimestamp;

    private final Optional<KeyedPluginStore.PluginMetas> appMetas;

    public PluginAndCfgsSnapshot(TargetResName collection, StoreResourceType resType, Map<String, Long> globalPluginStoreLastModify,
                                 IPluginMetasInfo metasInfo, Long appLastModifyTimestamp,
                                 KeyedPluginStore.PluginMetas appMetas) {
        this.globalPluginStoreLastModify = globalPluginStoreLastModify;

        this.pluginMetas = metasInfo.getMetas();
        this.repoRes = metasInfo.getRepoResources();
        this.appLastModifyTimestamp = appLastModifyTimestamp;
        this.collection = collection;
        this.resType = Objects.requireNonNull(resType, "resType can not be null");
        this.appMetas = Optional.ofNullable(appMetas);
    }

    public static void createManifestCfgAttrs2File(File manifestJar, StoreResourceType resourceType, TargetResName collection, long timestamp,
                                                   Optional<Predicate<PluginMeta>> pluginMetasFilter) throws Exception {
        createManifestCfgAttrs2File(manifestJar, resourceType, collection, timestamp, pluginMetasFilter, Collections.emptyMap());
    }

    public static Manifest createDataBatchJobManifestCfgAttrs(TargetResName collection) throws Exception {
        IDataxProcessor processor = DataxProcessor.load(null, StoreResourceType.DataApp, collection.getName());
        return createDataBatchJobManifestCfgAttrs(processor);
    }


    public static Manifest createDataBatchJobManifestCfgAttrs(IDataxProcessor processor) throws Exception {
        Map<String, String> extraEnvProps = Collections.emptyMap();
        Optional<Predicate<PluginMeta>> pluginMetasFilter = Optional.empty();
        return createDataBatchJobManifestCfgAttrs(processor, extraEnvProps, pluginMetasFilter);

    }

    /**
     * 通过运行时遍历的方式取得 DataX 批量任务对应的Manifest
     *
     * @param processor
     * @return
     * @throws Exception
     */
    public static Manifest createDataBatchJobManifestCfgAttrs(IDataxProcessor processor
            , Map<String, String> extraEnvProps, Optional<Predicate<PluginMeta>> pluginMetasFilter) throws Exception {

        if (processor.getResType() != StoreResourceType.DataApp
                && processor.getResType() != StoreResourceType.DataFlow) {
            throw new IllegalArgumentException("resType must be "
                    + StoreResourceType.DataApp
                    + " or "
                    + StoreResourceType.DataFlow + " but now is " + processor.getResType());
        }

        RobustReflectionConverter2.PluginMetas pluginMetas =
                RobustReflectionConverter2.PluginMetas.collectMetas((metas) -> {
                    TISLicense.load(false);
                    // 先收集plugmeta，特别是通过dataXWriter的dataSource关联的元数据
                    IDataxProcessor dataxProcessor = DataxProcessor.load(
                            null, processor.getResType(), processor.identityValue());
                    dataxProcessor.getReaders(null).forEach((reader) -> {
                        //  reader.getSelectedTabs().forEach((tab) -> tab.getCols());
                        reader.startScanDependency();

                        RecordTransformerRules.contextParamValsGetterMapper(
                                processor, IPluginContext.namedContext(new DataXName(processor.identityValue(), processor.getResType()))
                                , reader, reader.getSelectedTabs());
                    });
                    dataxProcessor.getWriter(null).startScanDependency();
                });


        TargetResName resName = new TargetResName(processor.identityValue());
//        return createManifestCfgAttrs(resName, System.currentTimeMillis(), Collections.emptyMap(), () -> {
//            PluginAndCfgsSnapshot localSnapshot = getLocalPluginAndCfgsSnapshot(processor.getResType(), resName, Optional.empty(), pluginMetas);
//            return localSnapshot;
//        }).getRight();


        return createManifestCfgAttrs(resName, processor.getResType(), System.currentTimeMillis()
                , extraEnvProps, () -> {

                    KeyedPluginStore.PluginMetas metas
                            = KeyedPluginStore.getAppAwarePluginMetas(processor.getResType(), resName.getName(), false);

                    ComponentMeta dataxComponentMeta = new ComponentMeta(Lists.newArrayList(pluginMetas.getRepoResources()));

                    Map<String, Long> globalPluginStoreLastModify = ComponentMeta.getGlobalPluginStoreLastModifyTimestamp(dataxComponentMeta);

                    PluginMetaSet collector = new PluginMetaSet(Optional.empty());
                    for (PluginMeta meta : pluginMetas.getMetas()) {
                        collectAllPluginMeta(meta, collector);
                    }

                    return new PluginAndCfgsSnapshot(resName, processor.getResType(), globalPluginStoreLastModify
                            , collector //
                            , metas.lastModifyTimestamp, metas);

                }).getValue();
    }

    /**
     * 通过运行时遍历的方式取得到Manifest
     *
     * @param collection
     * @param timestamp
     * @return
     * @throws Exception
     */
    public static Manifest createFlinkIncrJobManifestCfgAttrs(TargetResName collection, long timestamp) throws Exception {
        // Manifest manifest = null;
        StoreResourceType resourceType = StoreResourceType.DataApp;
        RobustReflectionConverter2.PluginMetas pluginMetas =
                RobustReflectionConverter2.PluginMetas.collectMetas((metas) -> {
                    TISLicense.load(false);
                    MQListenerFactory sourceFactory = HeteroEnum.getIncrSourceListenerFactory(DataXName.createDataXPipeline(collection.getName()));
                    sourceFactory.create();

                    // 先收集plugmeta，特别是通过dataXWriter的dataSource关联的元数据
                    IDataxProcessor processor = DataxProcessor.load(null, resourceType, collection.getName());
                    TISSinkFactory incrSinKFactory = TISSinkFactory.getIncrSinKFactory(new DataXName(collection.getName(), resourceType));
                    incrSinKFactory.createSinkFunction(processor, new IFlinkColCreator() {
                        @Override
                        public Object build(IColMetaGetter meta, int colIndex) {
                            return null;
                        }
                    });
                });
        return createFlinkIncrJobManifestCfgAttrs(resourceType, collection, timestamp, pluginMetas);
    }

    public static Pair<PluginAndCfgsSnapshot, Manifest> createManifestCfgAttrs2File(File manifestJar, StoreResourceType resourceType,
                                                                                    TargetResName collection,
                                                                                    long timestamp,
                                                                                    Optional<Predicate<PluginMeta>> pluginMetasFilter, Map<String, String> extraEnvProps) throws Exception {
        Pair<PluginAndCfgsSnapshot, Manifest> manifestCfgAttrs = createManifestCfgAttrs(resourceType, collection, timestamp,
                extraEnvProps, pluginMetasFilter, new IPluginMetasInfo() {
                });
        PluginAndCfgsSnapshotUtils.writeManifest2Jar(manifestJar, manifestCfgAttrs.getRight());
        return manifestCfgAttrs;
    }

    public static Manifest createFlinkIncrJobManifestCfgAttrs(StoreResourceType resourceType, TargetResName collection, long timestamp,
                                                              IPluginMetasInfo appendPluginMeta) throws Exception {


        return createManifestCfgAttrs(resourceType, collection, timestamp, Optional.empty(), new IPluginMetasInfo() {
            @Override
            public Set<PluginMeta> getMetas() {
                return Sets.union(appendPluginMeta.getMetas(),
                        Collections.singleton(new PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + collection.getName(),
                                Config.getMetaProps().getVersion(), Optional.empty())));
            }

            @Override
            public Set<IRepositoryResource> getRepoResources() {
                return appendPluginMeta.getRepoResources();
            }
        });
    }


    public static Manifest createManifestCfgAttrs(StoreResourceType resourceType, TargetResName collection, long timestamp,
                                                  Optional<Predicate<PluginMeta>> pluginMetasFilter,
                                                  IPluginMetasInfo appendPluginMeta) throws Exception {
        return createManifestCfgAttrs(resourceType, collection, timestamp, Collections.emptyMap(), pluginMetasFilter, appendPluginMeta).getRight();
    }

    public static Pair<PluginAndCfgsSnapshot, Manifest> //
    createManifestCfgAttrs(StoreResourceType resourceType, TargetResName collection,
                           long timestamp,
                           Map<String, String> extraEnvProps,
                           Optional<Predicate<PluginMeta>> pluginMetasFilter, IPluginMetasInfo appendPluginMeta) throws Exception {

        return createManifestCfgAttrs(collection, resourceType, timestamp, extraEnvProps, () -> {
            PluginAndCfgsSnapshot localSnapshot = getLocalPluginAndCfgsSnapshot(resourceType, collection, pluginMetasFilter, appendPluginMeta);
            return localSnapshot;
        });

//        //=====================================================================
//        if (!CenterResource.notFetchFromCenterRepository()) {
//            throw new IllegalStateException("must not fetchFromCenterRepository");
//        }
//
//        Manifest manifest = new Manifest();
//        Map<String, Attributes> entries = manifest.getEntries();
//        Attributes attrs = new Attributes();
//        attrs.put(new Attributes.Name(collection.getName()), String.valueOf(timestamp));
//        // 传递App名称
//        entries.put(TIS_APP_NAME, attrs);
//
//        final Attributes cfgAttrs = new Attributes();
//        // 传递Config变量
//        Config.getInstance().visitKeyValPair((e) -> {
//            if (Config.KEY_TIS_HOST.equals(e.getKey())) {
//                // tishost为127.0.0.1会出错
//                return;
//            }
//            addCfgAttrs(cfgAttrs, e);
//        });
//        for (Map.Entry<String, String> e : extraEnvProps.entrySet()) {
//            addCfgAttrs(cfgAttrs, e);
//        }
//        cfgAttrs.put(new Attributes.Name(convertCfgPropertyKey(Config.KEY_TIS_HOST, true)), Config.getTisHost());
//        entries.put(Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS, cfgAttrs);
//
//
//        //"globalPluginStore"  "pluginMetas"  "appLastModifyTimestamp"
//
//
//        PluginAndCfgsSnapshot localSnapshot = getLocalPluginAndCfgsSnapshot(resourceType, collection, pluginMetasFilter,
//                appendPluginMeta);
//
//        localSnapshot.attachPluginCfgSnapshot2Manifest(manifest);
//        return ImmutablePair.of(localSnapshot, manifest);
    }


    public static Pair<PluginAndCfgsSnapshot, Manifest> //
    createManifestCfgAttrs(TargetResName collection,
                           StoreResourceType resourceType,
                           long timestamp,
                           Map<String, String> extraEnvProps,
                           Supplier<PluginAndCfgsSnapshot> localPluginAndCfgsSnapshotCreator) throws Exception {

        //=====================================================================
        if (!CenterResource.notFetchFromCenterRepository()) {
            throw new IllegalStateException("must not fetchFromCenterRepository");
        }

        Manifest manifest = new Manifest();
        Map<String, Attributes> entries = manifest.getEntries();
        Attributes attrs = new Attributes();
        attrs.put(new Attributes.Name(collection.getName()), String.valueOf(timestamp));
        attrs.put(new Attributes.Name(StoreResourceType.KEY_STORE_RESOURCE_TYPE)
                , Objects.requireNonNull(resourceType, "resourceType can not be null").getType());
        // 传递App名称
        entries.put(TIS_APP_NAME, attrs);

        final Attributes cfgAttrs = new Attributes();
        // 传递Config变量
        Config.getInstance().visitKeyValPair((e) -> {
            if (Config.KEY_TIS_HOST.equals(e.getKey())) {
                // tishost为127.0.0.1会出错
                return;
            }
            addCfgAttrs(cfgAttrs, e);
        });
        for (Map.Entry<String, String> e : extraEnvProps.entrySet()) {
            addCfgAttrs(cfgAttrs, e);
        }
        cfgAttrs.put(new Attributes.Name(convertCfgPropertyKey(Config.KEY_TIS_HOST, true)), Config.getTisHost());
        entries.put(Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS, cfgAttrs);

        PluginAndCfgsSnapshot localSnapshot = localPluginAndCfgsSnapshotCreator.get();
        localSnapshot.attachPluginCfgSnapshot2Manifest(manifest);
        return ImmutablePair.of(localSnapshot, manifest);
    }


    private static void addCfgAttrs(Attributes cfgAttrs, Map.Entry<String, String> e) {
        cfgAttrs.put(new Attributes.Name(convertCfgPropertyKey(e.getKey(), true)), e.getValue());
    }

    private static void collectAllPluginMeta(PluginMeta meta, PluginMetaSet collector) {
        // meta.getLastModifyTimeStamp();
        collector.add(meta);
        List<PluginMeta> dpts = meta.getMetaDependencies();
        collector.addAll(dpts);
        for (PluginMeta m : dpts) {
            collectAllPluginMeta(m, collector);
        }
    }

    public Set<String> getPluginNames() {
        return pluginMetas.stream().map((m) -> m.getPluginName()).collect(Collectors.toSet());
    }

    /**
     * Flink 远端 会启多个VM，一个JM，多个TM，问题是多个VM在运行时 本地插件/配置的目录对应的是一个，为了避免当其中一个VM更新之后，其他VM就中的pluginManager->UberClassLoader就
     * 不更新了，所以本地需要有两个配置快照的副本，一个是localSnaphsot，另外一个 cacheSnaphsot
     *
     * @param localSnaphsot 每次从本地文件系统中去load
     * @param cacheSnaphsot 缓存在VM内存中的，用来和最新的远端快照做对比
     * @throws Exception
     */
    public void synchronizTpisAndConfs(PluginAndCfgsSnapshot localSnaphsot,
                                       Optional<PluginAndCfgsSnapshot> cacheSnaphsot) throws Exception {
        synchronized (TIS.class) {
            logger.info("start synchronizTpisAndConfs,cacheSnaphsot.isPresent():" + cacheSnaphsot.isPresent() + ",TIS.initialized:" + TIS.initialized);
            this.synchronizTpisAndConfs(localSnaphsot);
            if (cacheSnaphsot.isPresent() && TIS.initialized /** 必须要TIS 已经初始化 完成，启动时 cacheSnaphsot
             内的依赖plugin为空致使启动报错*/) {
                this.updatePluginManager(cacheSnaphsot.get());
            }
        }
    }


    /**
     * 通过将远程仓库中的plugin tpi的最近更新时间和本地tpi的最新更新时间经过对比，计算出需要更新的插件集合
     *
     * @param localSnaphsot
     * @return
     */
    private void synchronizTpisAndConfs(PluginAndCfgsSnapshot localSnaphsot) throws Exception {
        if (!localSnaphsot.appMetas.isPresent()) {
            throw new IllegalArgumentException("localSnaphsot.appMetas must be present");
        }

        StringBuffer updateTpisLogger = new StringBuffer("\nplugin synchronizTpisAndConfs" +
                "------------------------------\n");

        Long localTimestamp;
        Pair<Boolean, File> cfg = null;
        //boolean cfgChanged = false;
        // URL globalCfg = null;
        updateTpisLogger.append(">>global cfg compare:\n");
        for (Map.Entry<String, Long> entry : this.globalPluginStoreLastModify.entrySet()) {
            localTimestamp = localSnaphsot.globalPluginStoreLastModify.get(entry.getKey());
            if (localTimestamp == null || entry.getValue() > localTimestamp) {
                // 更新本地配置文件
                //globalCfg = CenterResource.getPathURL(Config.SUB_DIR_CFG_REPO, TIS.KEY_TIS_PLUGIN_CONFIG + "/" +
                // entry.getKey());
                cfg = CenterResource.copyFromRemote2Local(StoreResourceTypeConstants.KEY_TIS_PLUGIN_CONFIG + "/" + entry.getKey(), true);
                FileUtils.writeStringToFile(PluginStore.getLastModifyTimeStampFile(cfg.getRight()),
                        String.valueOf(entry.getValue()), TisUTF8.get());
                // cfgChanged = true;
                updateTpisLogger.append(entry.getKey()).append(localTimestamp == null
                                ? "[" + entry.getValue() + "] " + "local is none"
                                : " center ver:" + entry.getValue() + " > " + "local ver:" + localTimestamp)
                        .append(cfg.getKey() ? (",copy to " + cfg.getValue().getAbsolutePath()) : " skip to copy").append("\n");
            }
        }


        updateTpisLogger.append(">>app cfg compare:\n");
        updateTpisLogger.append("center:").append(this.appLastModifyTimestamp)
                .append(this.appLastModifyTimestamp > localSnaphsot.appLastModifyTimestamp ? " > " : " <= ")
                .append("local:").append(localSnaphsot.appLastModifyTimestamp).append("\n");
        if (this.appLastModifyTimestamp > localSnaphsot.appLastModifyTimestamp) {
            // 更新app相关配置,下载并更新本地配置
            KeyedPluginStore.AppKey appKey = new KeyedPluginStore.AppKey(
                    null, this.resType,
                    this.collection.getName(), (KeyedPluginStore.PluginClassCategory) null);
            URL appCfgUrl = CenterResource.getPathURL(Config.SUB_DIR_CFG_REPO,
                    StoreResourceTypeConstants.KEY_TIS_PLUGIN_CONFIG + "/" + appKey.getSubDirPath());

            updateTpisLogger.append("app cfg url:" + appCfgUrl + " config file enum:");
            KeyedPluginStore.PluginMetas appMetas = localSnaphsot.appMetas.get();
            HttpUtils.get(appCfgUrl, new ConfigFileContext.StreamProcess<Void>() {
                @Override
                public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    try {
                        FileUtils.deleteQuietly(appMetas.appDir);
                        ZipInputStream zipInput = new ZipInputStream(stream);
                        ZipEntry entry = null;
                        while ((entry = zipInput.getNextEntry()) != null) {
                            try (OutputStream output = FileUtils.openOutputStream(new File(appMetas.appDir,
                                    entry.getName()))) {
                                updateTpisLogger.append(entry.getName()).append(",");
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
            updateTpisLogger.append("\n");
            // cfgChanged = true;
        }

        Set<PluginMeta> result = getShallUpdatePluginMeta(localSnaphsot, updateTpisLogger);

        int copyCount = 0;
        for (PluginMeta update : result) {
            if (update.copyFromRemote(Collections.emptyList(), true, true)) {
                copyCount++;
            }
        }

        updateTpisLogger.append("success synchronized files count:" + copyCount).append("\n");


        // TIS tis = TIS.get();
        //        PluginManager pluginManager = tis.getPluginManager();
        //        Set<PluginMeta> loaded = Sets.newHashSet();
        //        PluginWrapperList batch = new PluginWrapperList();
        //        for (PluginMeta update : result) {
        //            dynamicLoad(pluginManager, update, batch, result, loaded);
        //        }
        //
        //        if (batch.size() > 0) {
        //            pluginManager.start(batch);
        //            updateTpisLogger.append("\ndynamic reload plugins:" + batch.getBatchNames());
        //        }
        //        Thread.sleep(3000l);
        //        if (cfgChanged) {
        //            TIS.cleanPluginStore();
        //            tis.cleanExtensionCache();
        //        }

        logger.info(updateTpisLogger.append("\n------------------------------").toString());
        //   return result;
    }

    /**
     * 更新本地pluginManger,激活插件
     *
     * @param localCacheSnaphsot
     * @throws Exception
     */
    private void updatePluginManager(PluginAndCfgsSnapshot localCacheSnaphsot) throws Exception {
        StringBuffer updateTpisLogger = new StringBuffer("\nplugin updatePluginManager " + "synchronize" +
                "------------------------------\n");
        Set<PluginMeta> result = getShallUpdatePluginMeta(localCacheSnaphsot, updateTpisLogger);

        //        for (PluginMeta update : result) {
        //            update.copyFromRemote(Collections.emptyList(), true, true);
        //        }
        try {
            TIS tis = TIS.get();
            PluginManager pluginManager = tis.getPluginManager();
            Set<PluginMeta> loaded = Sets.newHashSet();
            PluginWrapperList batch = new PluginWrapperList();
            for (PluginMeta update : result) {
                dynamicLoad(pluginManager, update, batch, result, loaded);
            }

            if (batch.size() > 0) {
                pluginManager.start(batch);
                updateTpisLogger.append("\ndynamic reload plugins:" + batch.getBatchNames());
                Thread.sleep(3000l);
                TIS.cleanPluginStore();
                tis.cleanExtensionCache();
            }
        } catch (Exception e) {
            logger.error(updateTpisLogger.append("\n------------------------------").toString());
            throw e;
        }


        logger.info(updateTpisLogger.append("\n------------------------------").toString());
        //   return result;
    }

    private Set<PluginMeta> getShallUpdatePluginMeta(PluginAndCfgsSnapshot localSnaphsot,
                                                     StringBuffer updateTpisLogger) {
        Set<PluginMeta> result = new HashSet<>();
        updateTpisLogger.append(">>center repository:").append(pluginMetas.stream().map((meta) -> meta.toString()).collect(Collectors.joining(",")));
        updateTpisLogger.append("\n>>local:").append(localSnaphsot.pluginMetas.stream().map((meta) -> meta.toString()).collect(Collectors.joining(","))).append("\n");
        updateTpisLogger.append(">>compare result\n");
        Map<String, PluginMeta> locals =
                localSnaphsot.pluginMetas.stream().collect(Collectors.toMap((m) -> m.getKey(), (m) -> m));
        PluginMeta m = null;
        for (PluginMeta meta : pluginMetas) {
            if (IFlinkCluster.SKIP_PLUGIN_NAMES.contains(meta.getPluginName())) {
                continue;
            }
            m = locals.get(meta.getKey());
            if (m == null || meta.getLastModifyTimeStamp() > m.getLastModifyTimeStamp()) {
                result.add(meta);
                updateTpisLogger.append(meta.getKey()).append(m == null ? " local is none" : " center repository " +
                        "ver:" + meta.getLastModifyTimeStamp() + " > local ver:" + m.getLastModifyTimeStamp()).append("\n");
            }
        }
        return result;
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

        public String getBatchNames() {
            return this.batch.stream().map(p -> p.getShortName()).collect(Collectors.joining(","));
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

    private void dynamicLoad(PluginManager pluginManager, PluginMeta update, PluginWrapperList batch,
                             Set<PluginMeta> shallUpdate, Set<PluginMeta> loaded) {
        try {
            for (PluginMeta dpt : update.getMetaDependencies()) {
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
    public static PluginAndCfgsSnapshot deserializePluginAndCfgsSnapshot(TargetResName app, StoreResourceType resourceType, Manifest manifest) {
        Map<String, Long> globalPluginStoreLastModify = Maps.newHashMap();
        //  Long appLastModifyTimestamp;
        Attributes pluginMetas = manifest.getAttributes(Config.KEY_PLUGIN_METAS);
        String[] globalPluginStoreSeri =
                StringUtils.split(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE), ",");
        String[] file2timestamp = null;
        for (String p : globalPluginStoreSeri) {
            file2timestamp = StringUtils.split(p, PluginMeta.NAME_VER_SPLIT);
            if (file2timestamp.length != 2) {
                throw new IllegalStateException("file2timestamp length must be 2,val:" + p);
            }
            globalPluginStoreLastModify.put(file2timestamp[0], Long.parseLong(file2timestamp[1]));
        }
        JSONArray ms = null;
        String metsAttr = null;
        try {
            metsAttr = pluginMetas.getValue(Config.KEY_PLUGIN_METAS);
            ms = JSONArray.parseArray(metsAttr);
        } catch (Exception e) {
            throw new RuntimeException("illegal metaAttr:" + metsAttr, e);
        }
        List<PluginMeta> metas = PluginMeta.parse(JsonUtil.toArray(String.class, ms));
        metas.forEach((meta) -> {
            if (meta.isLastModifyTimeStampNull()) {
                throw new IllegalStateException("pluginMeta:" + meta.getKey() + " relevant LastModify timestamp can " + "not be null");
            }
        });

        return new PluginAndCfgsSnapshot(app, resourceType, globalPluginStoreLastModify, new IPluginMetasInfo() {
            @Override
            public Set<PluginMeta> getMetas() {
                return Sets.newHashSet(metas);
            }

        }, Long.parseLong(pluginMetas.getValue(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP)), null);
    }

    /**
     * 远端执行点的本地快照
     *
     * @param collection
     * @param appendPluginMeta
     * @return
     */
    public static PluginAndCfgsSnapshot getWorkerPluginAndCfgsSnapshot(StoreResourceType resourceType, TargetResName collection,
                                                                       Set<PluginMeta> appendPluginMeta) {

        return getLocalPluginAndCfgsSnapshot(resourceType, collection, (pluginMetas, dataxComponentMeta) -> {
            PluginMetaSet collector = new PluginMetaSet(Optional.empty());

            File pluginDir = getPluginRootDir();
            Collection<File> tpis = FileUtils.listFiles(pluginDir,
                    new String[]{PluginClassifier.PACAKGE_TPI_EXTENSION_NAME}, false);
            tpis.forEach((tpi) -> {
                PluginManifest manifest = PluginManifest.create(tpi);
                if (manifest != null) {
                    collector.add(manifest.getPluginMeta());
                }
            });
            for (PluginMeta m : appendPluginMeta) {
                collectAllPluginMeta(m, collector);
            }
            return collector;
        });
    }

    public static File getPluginRootDir() {
        return new File(Config.getLibDir(), Config.KEY_TIS_PLUGIN_ROOT);
    }

    /**
     * @param collection
     * @param pluginMetasFilter pluginMetas 有plugin tpi 不需要同步（属于特例）
     * @param appendPluginMeta
     * @return
     */
    private static PluginAndCfgsSnapshot //
    getLocalPluginAndCfgsSnapshot(StoreResourceType resourceType, TargetResName collection,
                                  Optional<Predicate<PluginMeta>> pluginMetasFilter, IPluginMetasInfo appendPluginMeta) {
        //  ExtensionList<HeteroEnum> hlist = TIS.get().getExtensionList(HeteroEnum.class);

        return getLocalPluginAndCfgsSnapshot(resourceType, collection, (pluginMetas, dataxComponentMeta) -> {
            appendPluginMeta.getRepoResources().forEach((res) -> {
                dataxComponentMeta.addResource(res);
            });

            PluginMetaSet collector = new PluginMetaSet(pluginMetasFilter);
            for (PluginMeta m : pluginMetas.metas) {
                collectAllPluginMeta(m, collector);
            }
            Set<PluginMeta> globalPluginMetas = dataxComponentMeta.loadPluginMeta();
            for (PluginMeta m : globalPluginMetas) {
                collectAllPluginMeta(m, collector);
            }
            for (PluginMeta m : appendPluginMeta.getMetas()) {
                collectAllPluginMeta(m, collector);
            }
            return collector;
        });
    }

    private static PluginAndCfgsSnapshot getLocalPluginAndCfgsSnapshot(StoreResourceType resourceType, TargetResName collection,
                                                                       MetaSetProductor metaSetProductor) {
        // 本次任务相关插件元信息
        KeyedPluginStore.PluginMetas pluginMetas = KeyedPluginStore.getAppAwarePluginMetas(resourceType, collection.getName());
        //  Set<PluginMeta> globalPluginMetas = null;
        Map<String, Long> gPluginStoreLastModify = Collections.emptyMap();
        UploadPluginMeta upm = UploadPluginMeta.parse("x:require", true);

        TIS tis = TIS.get();
        List<IRepositoryResource> keyedPluginStores = Collections.emptyList();
        if (tis != null) {
            ExtensionList<HeteroEnum> hlist = TIS.get().getExtensionList(HeteroEnum.class);
            keyedPluginStores =
                    hlist.stream()
                            .filter((e) -> !e.isAppNameAware())
                            .flatMap((e) -> e.getPluginStore(null, upm).getAll().stream())
                            .collect(Collectors.toList());
        }


        ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
        PluginMetaSet pluginMetaSet = metaSetProductor.call(pluginMetas, dataxComponentMeta);
        gPluginStoreLastModify = ComponentMeta.getGlobalPluginStoreLastModifyTimestamp(dataxComponentMeta);

        try {
            return new PluginAndCfgsSnapshot(collection, resourceType, gPluginStoreLastModify //
                    , pluginMetaSet //
                    , pluginMetas.lastModifyTimestamp, pluginMetas);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    interface MetaSetProductor {
        PluginMetaSet call(KeyedPluginStore.PluginMetas pluginMetas, ComponentMeta dataxComponentMeta);
    }

    private static class PluginMetaSet implements IPluginMetasInfo {
        private HashSet<PluginMeta> metas = Sets.newHashSet();
        private Set<IRepositoryResource> repoRes = Sets.newHashSet();

        final Optional<Predicate<PluginMeta>> pluginMetasFilter;

        public PluginMetaSet(Optional<Predicate<PluginMeta>> pluginMetasFilter) {
            this.pluginMetasFilter = pluginMetasFilter;
        }

        @Override
        public Set<PluginMeta> getMetas() {
            return (pluginMetasFilter.isPresent() ?
                    this.metas.stream().filter(pluginMetasFilter.get()).collect(Collectors.toSet()) : this.metas);
        }

        @Override
        public Set<IRepositoryResource> getRepoResources() {
            return this.repoRes;
        }

        public boolean add(PluginMeta meta) {

            Iterator<PluginMeta> it = metas.iterator();
            PluginMeta m = null;
            while (it.hasNext()) {
                m = it.next();
                if (StringUtils.equals(meta.getPluginName(), m.getPluginName())) {
                    if (meta.ver.compareTo(m.ver) > 0) {
                        it.remove();
                        return metas.add(meta);
                        // 新加的版本高，需要将之前的版本替换
                        // System.out.println("meta.ver:" + meta.ver + ",m.ver:" + m.ver);
                    }
                    return false;
                }
            }

            return metas.add(meta);
        }

        public boolean addAll(Collection<PluginMeta> c) {
            for (PluginMeta m : c) {
                this.add(m);
            }
            return true;
        }
    }

    public void attachPluginCfgSnapshot2Manifest(Manifest manifest) {
        Map<String, Attributes> entries = manifest.getEntries();

        StringBuffer globalPluginStore = new StringBuffer();
        for (Map.Entry<String, Long> e : globalPluginStoreLastModify.entrySet()) {
            globalPluginStore.append(e.getKey()).append(PluginMeta.NAME_VER_SPLIT).append(e.getValue()).append(",");
        }

        final Attributes pmetas = new Attributes();
        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_GLOBAL_PLUGIN_STORE), String.valueOf(globalPluginStore));
        // 本次任务相关插件元信息
        //KeyedPluginStore.PluginMetas pluginMetas = KeyedPluginStore.getAppAwarePluginMetas(false, collection
        // .getName());
        PluginManager pluginManager = TIS.get().getPluginManager();

        Map<String, PluginWrapper> plugins = pluginManager.getActivePluginsMap();
        // pluginManager.
        final JSONArray jarray = new JSONArray();
        this.pluginMetas.forEach((meta) -> {
            meta.getLastModifyTimeStamp();
            PluginWrapper plugin = plugins.get(meta.getPluginName());
            if (plugin != null) {
                Optional<PluginClassifier> classifier = plugin.getClassifier();
                if (classifier.isPresent()) {
                    meta.setClassifier(classifier.get());
                }
            }
            jarray.add(meta.toString());
        });
        // KeyedPluginStore.PluginMetas.KEY_PLUGIN_META
        final String pluginMetas = jarray.toJSONString();
        logger.info("collected " + Config.KEY_PLUGIN_METAS + ":" + pluginMetas);
        pmetas.put(new Attributes.Name(Config.KEY_PLUGIN_METAS), pluginMetas);

        pmetas.put(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_APP_LAST_MODIFY_TIMESTAMP),
                String.valueOf(this.appLastModifyTimestamp));

        entries.put(Config.KEY_PLUGIN_METAS, pmetas);
    }

}
