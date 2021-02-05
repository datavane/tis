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
package com.qlangtech.tis;

import com.google.common.collect.Lists;
import com.qlangtech.tis.component.GlobalComponent;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.*;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.extension.init.InitMilestone;
import com.qlangtech.tis.extension.init.InitReactorRunner;
import com.qlangtech.tis.extension.init.InitStrategy;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.DSKey;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.util.*;
import org.jvnet.hudson.reactor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.qlangtech.tis.extension.init.InitMilestone.PLUGINS_PREPARED;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TIS {

    private static final Logger logger = LoggerFactory.getLogger(TIS.class);
    private static final String DB_GROUP_NAME = "db";
    public static final String KEY_TIS_PLUGIN_CONFIG = "tis_plugin_config";

    public static final String KEY_TIS_PLUGIN_ROOT = "plugins";

    // public static final String KEY_TIS_INCR_COMPONENT_CONFIG_FILE = "incr_config.xml";
    public static final String KEY_TIE_GLOBAL_COMPONENT_CONFIG_FILE = "global_config.xml";

    // 
    // public static final String KEY_TIS_ENGINE_COMPONENT_CONFIG_FILE = "engine_config.xml";

    /**
     * All {@link DescriptorExtensionList} keyed by their {@link DescriptorExtensionList}.
     */
    private static final transient Memoizer<Class<? extends Describable>, PluginStore> globalPluginStore = new Memoizer<Class<? extends Describable>, PluginStore>() {

        public PluginStore compute(Class<? extends Describable> key) {
            return new PluginStore(key);
        }
    };

    private static final transient Memoizer<KeyedPluginStore.Key, KeyedPluginStore> collectionPluginStore
            = new Memoizer<KeyedPluginStore.Key, KeyedPluginStore>() {
        public KeyedPluginStore compute(KeyedPluginStore.Key key) {
            return new KeyedPluginStore(key);
        }
    };

    private static final transient Memoizer<DSKey, DataSourceFactoryPluginStore> databasePluginStore
            = new Memoizer<DSKey, DataSourceFactoryPluginStore>() {
        @Override
        public DataSourceFactoryPluginStore compute(DSKey key) {
            if (key.isFacadeType()) {
                // shall not maintance record in DB
                return new DataSourceFactoryPluginStore(key, false);
//                {
//                    @Override
//                    public void saveTable(String tableName) throws Exception {
//                        throw new UnsupportedOperationException("tableName:" + tableName);
//                    }
//                };
            } else {
                return new DataSourceFactoryPluginStore(key, true);
            }
        }
    };

    public static DataSourceFactoryPluginStore getDataBasePluginStore(PostedDSProp dsProp) {
        DataSourceFactoryPluginStore pluginStore
                = databasePluginStore.get(new DSKey(DB_GROUP_NAME
                , dsProp.getDbType(), dsProp.getDbname(), DataSourceFactory.class));
        return pluginStore;
    }

    public static void deleteDB(String dbName, DbScope dbScope) {
        try {
            if (dbScope == DbScope.DETAILED) {
                DataSourceFactoryPluginStore dsPluginStore = getDataBasePluginStore(new PostedDSProp(dbName, DbScope.DETAILED));
                dsPluginStore.deleteDB();
                databasePluginStore.clear(dsPluginStore.getDSKey());
            }

            DataSourceFactoryPluginStore facetDsPluginStore = getDataBasePluginStore(new PostedDSProp(dbName, DbScope.FACADE));
            facetDsPluginStore.deleteDB();
            databasePluginStore.clear(facetDsPluginStore.getDSKey());

        } catch (Exception e) {
            throw new RuntimeException(dbName, e);
        }
    }


    /**
     * Get the index relevant plugin configuration
     *
     * @param collection
     * @param key
     * @param <T>
     * @return
     */
//    public static <T extends Describable> PluginStore<T> getPluginStore(String collection, Class<T> key) {
//        return getPluginStore(null, collection, key);
//    }
    public static <T extends Describable> PluginStore<T> getPluginStore(String collection, Class<T> key) {
        PluginStore<T> pluginStore = collectionPluginStore.get(new KeyedPluginStore.Key("collection", collection, key));
        if (pluginStore == null) {
            // 如果和collection自身绑定的plugin没有找到，就尝试找全局plugin
            return getPluginStore(key);
        } else {
            return pluginStore;
        }
    }

    public static <T extends Describable> PluginStore<T> getPluginStore(Class<T> key) {
        return globalPluginStore.get(key);
    }

    private final transient Memoizer<Class, ExtensionList> extensionLists = new Memoizer<Class, ExtensionList>() {

        public ExtensionList compute(Class key) {
            return ExtensionList.create(TIS.this, key);
        }
    };

    /**
     * All {@link DescriptorExtensionList} keyed by their {@link DescriptorExtensionList}.
     */
    private final transient Memoizer<Class, DescriptorExtensionList> descriptorLists = new Memoizer<Class, DescriptorExtensionList>() {

        public DescriptorExtensionList compute(Class key) {
            return DescriptorExtensionList.createDescriptorList(TIS.this, key);
        }
    };

    public final transient PluginManager pluginManager;

    public static final File pluginCfgRoot = new File(Config.getMetaCfgDir(), KEY_TIS_PLUGIN_CONFIG);

    public static final File pluginDirRoot = new File(Config.getLibDir(), KEY_TIS_PLUGIN_ROOT);

    private static TIS tis;

    // 插件运行系统是否已经初始化
    public static boolean initialized = false;

    // 允许初始化，防止在非console组件中初始化过程中，插件还没有下载好，TIS已经完成初始化
    public static boolean permitInitialize = true;

    private TIS() {
        final long start = System.currentTimeMillis();
        try {
            this.pluginManager = new PluginManager(pluginDirRoot);
            final InitStrategy is = InitStrategy.get(Thread.currentThread().getContextClassLoader());
            executeReactor(// loading and preparing plugins
                    is, // load jobs
                    pluginManager.initTasks(is), // forced ordering among key milestones
                    loadTasks(), InitMilestone.ordering());
            logger.info("tis plugin have been initialized,consume: {}ms.", System.currentTimeMillis() - start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            initialized = true;
        }
    }

    private synchronized TaskBuilder loadTasks() {
        TaskGraphBuilder g = new TaskGraphBuilder();
        return g;
    }

    public static TIS get() {
        if (permitInitialize && tis == null) {
            synchronized (TIS.class) {
                if (permitInitialize && tis == null) {
                    tis = new TIS();
                }
            }
        }
        return tis;
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public Descriptor getDescriptorOrDir(Class<? extends Describable> type) {
        Descriptor d = getDescriptor(type);
        if (d == null)
            throw new AssertionError(type + " is missing its descriptor");
        return d;
    }

    /**
     * Executes a reactor.
     *
     * @param is If non-null, this can be consulted for ignoring some tasks. Only used during the initialization of Jenkins.
     */
    private void executeReactor(final InitStrategy is, TaskBuilder... builders) throws IOException, InterruptedException, ReactorException {
        Reactor reactor = new Reactor(builders) {

            /**
             * Sets the thread name to the task for better diagnostics.
             */
            @Override
            protected void runTask(Task task) throws Exception {
                if (is != null && is.skipInitTask(task))
                    return;
                String taskName = task.getDisplayName();
                Thread t = Thread.currentThread();
                String name = t.getName();
                if (taskName != null)
                    t.setName(taskName);
                try {
                    super.runTask(task);
                } finally {
                    t.setName(name);
                    // SecurityContextHolder.clearContext();
                }
            }
        };
        new InitReactorRunner() {

            @Override
            protected void onInitMilestoneAttained(InitMilestone milestone) {
                // initLevel = milestone;
                if (milestone == PLUGINS_PREPARED) {
                    // set up Guice to enable injection as early as possible
                    // before this milestone, ExtensionList.ensureLoaded() won't actually try to locate instances
                    // ExtensionList.lookup(ExtensionFinder.class).getComponents();
                }
            }
        }.run(reactor);
    }

    // public File getRootDir() {
    // return this.root;
    // }

    /**
     * Exposes {@link Descriptor} by its name to URL.
     * <p>
     * After doing all the {@code getXXX(shortClassName)} methods, I finally realized that
     * this just doesn't scale.
     *
     * @param id Either {@link Descriptor#getId()} (recommended) or the short name of a {@link Describable} subtype (for compatibility)
     * @throws IllegalArgumentException if a short name was passed which matches multiple IDs (fail fast)
     */
    // too late to fix
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Descriptor getDescriptor(String id) {
        // legacy descriptors that are reigstered manually doesn't show up in getExtensionList, so check them explicitly.
        Iterable<Descriptor> descriptors = getExtensionList(Descriptor.class);
        for (Descriptor d : descriptors) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        Descriptor candidate = null;
        for (Descriptor d : descriptors) {
            String name = d.getId();
            if (name.substring(name.lastIndexOf('.') + 1).equals(id)) {
                if (candidate == null) {
                    candidate = d;
                } else {
                    throw new IllegalArgumentException(id + " is ambiguous; matches both " + name + " and " + candidate.getId());
                }
            }
        }
        return candidate;
    }

    /**
     * Gets the {@link Descriptor} that corresponds to the given {@link Describable} type.
     * <p>
     * If you have an instance of {@code type} and call {@link Describable#getDescriptor()},
     * you'll get the same instance that this method returns.
     */
    public Descriptor getDescriptor(Class<? extends Describable> type) {
        for (Descriptor d : getExtensionList(Descriptor.class))
            if (d.clazz == type)
                return d;
        return null;
    }

    /**
     * Gets the {@link Descriptor} instance in the current Jenkins by its type.
     */
    public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
        for (Descriptor d : getExtensionList(Descriptor.class))
            if (d.getClass() == type)
                return type.cast(d);
        return null;
    }

    public <T> ExtensionList<T> getExtensionList(Class<T> extensionType) {
        return extensionLists.get(extensionType);
    }

    /**
     * Returns {@link ExtensionList} that retains the discovered {@link Descriptor} instances for the given
     * kind of {@link Describable}.
     *
     * @return Can be an empty list but never null.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(Class<T> type) {
        return descriptorLists.get(type);
    }

    private GlobalComponent globalComponent;

    public GlobalComponent loadGlobalComponent() {
        if (globalComponent == null) {
            try {
                File globalConfig = getGlobalConfigFile();
                if (!globalConfig.exists()) {
                    // 不存在的话
                    return new GlobalComponent();
                }
                globalComponent = (GlobalComponent) (new XmlFile(globalConfig).read());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return globalComponent;
    }

    public void saveComponent(GlobalComponent gloablComponent) {
        try {
            File gloabl = getGlobalConfigFile();
            (new XmlFile(gloabl)).write(gloablComponent, Collections.emptySet());
            this.globalComponent = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // /**
    // * 加载增量组件
    // *
    // * @param collection
    // * @return
    // */
    // public IncrComponent loadIncrComponent(String collection) {
    // try {
    // File incrConfig = getIncrConfigFile(collection);
    // if (!incrConfig.exists()) {
    // // 不存在的话
    // return new IncrComponent(collection);
    // }
    // return (IncrComponent) (new XmlFile(incrConfig).read());
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }

    /**
     * 取得增量模块需要用到的plugin名称
     *
     * @param incrPluginConfigSet 增量相关的插件配置集合
     * @return
     */
    public static Set<XStream2.PluginMeta> loadIncrComponentUsedPlugin(String collection, List<File> incrPluginConfigSet, boolean clearThreadholder) {
        try {
            synchronized (RobustReflectionConverter.usedPluginInfo) {
                if (clearThreadholder) {
                    RobustReflectionConverter.usedPluginInfo.remove();
                }
                for (File incrConfig : incrPluginConfigSet) {
                    if (!incrConfig.exists()) {
                        throw new IllegalStateException("file not exist,path:" + incrConfig.getAbsolutePath());
                    }
                    XmlFile xmlFile = new XmlFile(new XStream2PluginInfoReader(XmlFile.DEFAULT_DRIVER), incrConfig);
                    xmlFile.read();
                }
                return RobustReflectionConverter.usedPluginInfo.get();
            }
        } catch (IOException e) {
            throw new RuntimeException("collection:" + collection + " relevant configs:"
                    + incrPluginConfigSet.stream().map((f) -> f.getAbsolutePath()).collect(Collectors.joining(",")), e);
        }
    }

    public static ComponentMeta getDumpAndIndexBuilderComponent(List<IRepositoryResource> resources) {
        checkNotInitialized();
        permitInitialize = false;
        resources.add(getPluginStore(ParamsConfig.class));
        resources.add(getPluginStore(TableDumpFactory.class));
        resources.add(getPluginStore(IndexBuilderTriggerFactory.class));
        return new ComponentMeta(resources);
    }

    /**
     * 取得solrcore 启动相关的插件资源
     *
     * @param resources
     * @return
     */
    public static ComponentMeta getCoreComponent(List<IRepositoryResource> resources) {
        checkNotInitialized();
        permitInitialize = false;
        resources.add(getPluginStore(IndexBuilderTriggerFactory.class));
        return new ComponentMeta(resources);
    }

    public static ComponentMeta getDumpAndIndexBuilderComponent(IRepositoryResource... extractRes) {

        List<IRepositoryResource> resources = Lists.newArrayList();
        for (IRepositoryResource r : extractRes) {
            resources.add(r);
        }
        return getDumpAndIndexBuilderComponent(resources);
    }

    private static void checkNotInitialized() {
        if (initialized) {
            throw new IllegalStateException("TIS plugins has initialized");
        }
    }

    public static ComponentMeta getAssembleComponent() {
        checkNotInitialized();
        permitInitialize = false;

        List<IRepositoryResource> resources = Lists.newArrayList();
        resources.add(TIS.getPluginStore(HeteroEnum.INDEX_BUILD_CONTAINER.extensionPoint));
        resources.add(TIS.getPluginStore(FlatTableBuilder.class));
        resources.add(TIS.getPluginStore(TableDumpFactory.class));
        resources.add(TIS.getPluginStore(ParamsConfig.class));
        return new ComponentMeta(resources);
    }

    // public File getIncrConfigFile(String collection) {
    // return new File(this.pluginCfgRoot, collection + File.separator + KEY_TIS_INCR_COMPONENT_CONFIG_FILE);
    // }
    private File getGlobalConfigFile() {
        return new File(pluginCfgRoot, "global" + File.separator + KEY_TIE_GLOBAL_COMPONENT_CONFIG_FILE);
    }
    // public void saveComponent(String collection, IncrComponent incrComponent) {
    // try {
    // File incrConfig = getIncrConfigFile(collection);
    // (new XmlFile(incrConfig)).write(incrComponent, Collections.emptySet());
    // } catch (IOException e) {
    // throw new RuntimeException("collection:" + collection, e);
    // }
    // }
}
