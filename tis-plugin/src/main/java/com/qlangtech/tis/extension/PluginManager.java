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

import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.extension.impl.MissingDependencyException;
import com.qlangtech.tis.extension.init.InitStrategy;
import com.qlangtech.tis.extension.util.ClassLoaderReflectionToolkit;
import com.qlangtech.tis.extension.util.CyclicGraphDetector;
import com.qlangtech.tis.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.LogFactory;
import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.qlangtech.tis.extension.init.InitMilestone.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginManager {

    public static final String PACAKGE_TPI_EXTENSION = ".tpi";

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private static final Logger LOGGER = logger;

    private final PluginStrategy strategy;

    private final File workDir;

    public final File rootDir;

    // private static final Logger logger = LoggerFactory.getLogger(TIS.class.getName());
    public final PluginManager.PluginInstanceStore pluginInstanceStore = new PluginManager.PluginInstanceStore();

    public final UberClassLoader uberClassLoader = new UberClassLoader();

    public File getWorkDir() {
        return workDir;
    }

    boolean pluginListed = false;

    /**
     * All discovered plugins.
     */
    protected final List<PluginWrapper> plugins = new ArrayList<PluginWrapper>();

    /**
     * All active plugins, topologically sorted so that when X depends on Y, Y appears in the list before X does.
     */
    protected final List<PluginWrapper> activePlugins = new CopyOnWriteArrayList<PluginWrapper>();

    protected final List<FailedPlugin> failedPlugins = new ArrayList<FailedPlugin>();

    public PluginManager(File rootDir) {
        try {
            if (!rootDir.exists()) {
                FileUtils.forceMkdir(rootDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.workDir = rootDir;
        this.rootDir = rootDir;
        this.strategy = this.createPluginStrategy();
    }

    protected PluginStrategy createPluginStrategy() {
        // default and fallback
        return new ClassicPluginStrategy(this);
    }

    public PluginStrategy getPluginStrategy() {
        return strategy;
    }

    public TaskBuilder initTasks(final InitStrategy initStrategy) {
        TaskBuilder builder;
        if (!pluginListed) {
            builder = new TaskGraphBuilder() {

                List<File> archives;

                Collection<String> bundledPlugins = Collections.emptyList();

                {
                    // Handle loadBundledPlugins = add("Loading bundled plugins", new Executable() {
                    // public void run(Reactor session) throws Exception {
                    // bundledPlugins = loadBundledPlugins();
                    // }
                    // });
                    Handle listUpPlugins = add("Listing up plugins", new Executable() {

                        public void run(Reactor session) throws Exception {
                            archives = initStrategy.listPluginArchives(PluginManager.this);
                        }
                    });
                    requires(listUpPlugins).attains(PLUGINS_LISTED).add("Preparing plugins", new Executable() {

                        public void run(Reactor session) throws Exception {
                            // once we've listed plugins, we can fill in the reactor with plugin-specific initialization tasks
                            TaskGraphBuilder g = new TaskGraphBuilder();
                            final Map<String, File> inspectedShortNames = new HashMap<String, File>();
                            for (final File arc : archives) {
                                g.followedBy().notFatal().attains(PLUGINS_LISTED).add("Inspecting plugin " + arc, new Executable() {

                                    public void run(Reactor session1) throws Exception {
                                        try {
                                            PluginWrapper p = strategy.createPluginWrapper(arc);
                                            if (isDuplicate(p))
                                                return;
                                            p.isBundled = containsHpiJpi(bundledPlugins, arc.getName());
                                            plugins.add(p);
                                        } catch (IOException e) {
                                            failedPlugins.add(new FailedPlugin(arc.getName(), e));
                                            throw e;
                                        }
                                    }

                                    /**
                                     * Inspects duplication. this happens when you run hpi:run on a bundled plugin,
                                     * as well as putting numbered jpi files, like "cobertura-1.0.jpi" and "cobertura-1.1.jpi"
                                     */
                                    private boolean isDuplicate(PluginWrapper p) {
                                        String shortName = p.getShortName();
                                        if (inspectedShortNames.containsKey(shortName)) {
                                            LOGGER.info("Ignoring " + arc + " because " + inspectedShortNames.get(shortName) + " is already loaded");
                                            return true;
                                        }
                                        inspectedShortNames.put(shortName, arc);
                                        return false;
                                    }
                                });
                            }
                            g.followedBy().attains(PLUGINS_LISTED).add("Checking cyclic dependencies", new Executable() {

                                /**
                                 * Makes sure there's no cycle in dependencies.
                                 */
                                public void run(Reactor reactor) throws Exception {
                                    try {
                                        CyclicGraphDetector<PluginWrapper> cgd = new CyclicGraphDetector<PluginWrapper>() {

                                            @Override
                                            protected List<PluginWrapper> getEdges(PluginWrapper p) {
                                                List<PluginWrapper> next = new ArrayList<PluginWrapper>();
                                                addTo(p.getDependencies(), next);
                                                addTo(p.getOptionalDependencies(), next);
                                                return next;
                                            }

                                            private void addTo(List<PluginWrapper.Dependency> dependencies, List<PluginWrapper> r) {
                                                for (PluginWrapper.Dependency d : dependencies) {
                                                    PluginWrapper p = getPlugin(d.shortName);
                                                    if (p != null)
                                                        r.add(p);
                                                }
                                            }

                                            @Override
                                            protected void reactOnCycle(PluginWrapper q, List<PluginWrapper> cycle) throws CyclicGraphDetector.CycleDetectedException {
                                                LOGGER.info("found cycle in plugin dependencies: (root=" + q + ", deactivating all involved) " + Util.join(cycle, " -> "));
                                                for (PluginWrapper pluginWrapper : cycle) {
                                                    pluginWrapper.setHasCycleDependency(true);
                                                    failedPlugins.add(new FailedPlugin(pluginWrapper.getShortName(), new CycleDetectedException(cycle)));
                                                }
                                            }
                                        };
                                        cgd.run(getPlugins());
                                        // obtain topologically sorted list and overwrite the list
                                        ListIterator<PluginWrapper> litr = getPlugins().listIterator();
                                        for (PluginWrapper p : cgd.getSorted()) {
                                            litr.next();
                                            litr.set(p);
                                            if (p.isActive()) {
                                                activePlugins.add(p);
                                            }
                                        }
                                    } catch (CyclicGraphDetector.CycleDetectedException e) {
                                        // disable all plugins since classloading from them can lead to StackOverflow
                                        stop();
                                        // let Hudson fail
                                        throw e;
                                    }
                                }
                            });
                            // Let's see for a while until we open this functionality up to plugins
                            // g.followedBy().attains(PLUGINS_LISTED).add("Load compatibility rules", new Executable() {
                            // public void run(Reactor reactor) throws Exception {
                            // compatibilityTransformer.loadRules(uberClassLoader);
                            // }
                            // });
                            session.addAll(g.discoverTasks(session));
                            // technically speaking this is still too early, as at this point tasks are merely scheduled, not necessarily executed.
                            pluginListed = true;
                        }
                    });
                }
            };
        } else {
            builder = TaskBuilder.EMPTY_BUILDER;
        }
        // lists up initialization tasks about loading plugins.
        return TaskBuilder.union(builder, new TaskGraphBuilder() {

            {
                requires(PLUGINS_LISTED).attains(PLUGINS_PREPARED).add("Loading plugins", new Executable() {

                    /**
                     * Once the plugins are listed, schedule their initialization.
                     */
                    public void run(Reactor session) throws Exception {
                        // Jenkins.getInstance().lookup.set(PluginInstanceStore.class, new PluginInstanceStore());
                        TaskGraphBuilder g = new TaskGraphBuilder();
                        // schedule execution of loading plugins
                        for (final PluginWrapper pluginWrapper : activePlugins.toArray(new PluginWrapper[activePlugins.size()])) {
                            g.followedBy().notFatal().attains(PLUGINS_PREPARED).add("Loading plugin " + pluginWrapper.getShortName(), new Executable() {

                                public void run(Reactor session) throws Exception {
                                    try {
                                        pluginWrapper.resolvePluginDependencies();
                                        strategy.load(pluginWrapper);
                                    } catch (MissingDependencyException e) {
                                        failedPlugins.add(new FailedPlugin(pluginWrapper.getShortName(), e));
                                        activePlugins.remove(pluginWrapper);
                                        plugins.remove(pluginWrapper);
                                        LOGGER.error("Failed to install {0}: {1}", new Object[]{pluginWrapper.getShortName(), e.getMessage()});
                                        return;
                                    } catch (IOException e) {
                                        failedPlugins.add(new FailedPlugin(pluginWrapper.getShortName(), e));
                                        activePlugins.remove(pluginWrapper);
                                        plugins.remove(pluginWrapper);
                                        throw e;
                                    }
                                }
                            });
                        }
                        // schedule execution of initializing plugins
                        for (final PluginWrapper p : activePlugins.toArray(new PluginWrapper[activePlugins.size()])) {
                            g.followedBy().notFatal().attains(PLUGINS_STARTED).add("Initializing plugin " + p.getShortName(), new Executable() {

                                public void run(Reactor session) throws Exception {
                                    if (!activePlugins.contains(p)) {
                                        return;
                                    }
                                    try {
                                        p.getPlugin().postInitialize();
                                    } catch (Exception e) {
                                        failedPlugins.add(new FailedPlugin(p.getShortName(), e));
                                        activePlugins.remove(p);
                                        plugins.remove(p);
                                        throw e;
                                    }
                                }
                            });
                        }
                        // g.followedBy().attains(PLUGINS_STARTED).add("Discovering plugin initialization tasks", new Executable() {
                        // public void run(Reactor reactor) throws Exception {
                        // // rescan to find plugin-contributed @Initializer
                        // reactor.addAll(initializerFinder.discoverTasks(reactor));
                        // }
                        // });
                        // register them all
                        session.addAll(g.discoverTasks(session));
                    }
                });
                // All plugins are loaded. Now we can figure out who depends on who.
                requires(PLUGINS_PREPARED).attains(COMPLETED).add("Resolving Dependant Plugins Graph", new Executable() {

                    @Override
                    public void run(Reactor reactor) throws Exception {
                        resolveDependantPlugins();
                    }
                });
            }
        });
    }

    public synchronized void resolveDependantPlugins() {
        for (PluginWrapper plugin : plugins) {
            Set<String> dependants = new HashSet<>();
            for (PluginWrapper possibleDependant : plugins) {
                // count as a dependant.
                if (possibleDependant.isDeleted()) {
                    continue;
                }
                List<PluginWrapper.Dependency> dependencies = possibleDependant.getDependencies();
                for (PluginWrapper.Dependency dependency : dependencies) {
                    if (dependency.shortName.equals(plugin.getShortName())) {
                        dependants.add(possibleDependant.getShortName());
                    }
                }
            }
            plugin.setDependants(dependants);
        }
    }

    /**
     * Orderly terminates all the plugins.
     */
    public void stop() {
        for (PluginWrapper p : activePlugins) {
            p.stop();
            p.releaseClassLoader();
        }
        activePlugins.clear();
        // Work around a bug in commons-logging.
        // See http://www.szegedi.org/articles/memleak.html
        LogFactory.release(uberClassLoader);
    }

    protected Collection<String> loadBundledPlugins() throws Exception {
        try {
            return loadPluginsFromWar("/WEB-INF/plugins", null);
        } finally {
            loadDetachedPlugins();
        }
    }

    /**
     * Stores {@link Plugin} instances.
     */
    static final class PluginInstanceStore {

        final Map<PluginWrapper, Plugin> store = new Hashtable<PluginWrapper, Plugin>();
    }

    protected void loadDetachedPlugins() {
    }

    protected Set<String> loadPluginsFromWar(String fromPath, FilenameFilter filter) {
        Set<String> names = new HashSet();
        return names;
    }

    /**
     * Return the {@link PluginWrapper} that loaded the given class 'c'.
     *
     * @since 1.402.
     */
    public PluginWrapper whichPlugin(Class c) {
        PluginWrapper oneAndOnly = null;
        ClassLoader cl = c.getClassLoader();
        for (PluginWrapper p : activePlugins) {
            if (p.classLoader == cl) {
                if (oneAndOnly != null)
                    // ambigious
                    return null;
                oneAndOnly = p;
            }
        }
        return oneAndOnly;
    }

    /*
     * contains operation that considers xxx.hpi and xxx.jpi as equal
     * this is necessary since the bundled plugins are still called *.hpi
     */
    private boolean containsHpiJpi(Collection<String> bundledPlugins, String name) {
        return bundledPlugins.contains(name.replaceAll("\\.hpi", PACAKGE_TPI_EXTENSION)) || bundledPlugins.contains(name.replaceAll("\\" + PACAKGE_TPI_EXTENSION, ".hpi"));
    }

    /**
     * Get the plugin instance with the given short name.
     *
     * @param shortName the short name of the plugin
     * @return The plugin singleton or <code>null</code> if a plugin with the given short name does not exist.
     */
    public PluginWrapper getPlugin(String shortName) {
        for (PluginWrapper p : getPlugins()) {
            if (p.getShortName().equals(shortName))
                return p;
        }
        return null;
    }

    public List<PluginWrapper> getPlugins() {
        List<PluginWrapper> out = new ArrayList<PluginWrapper>(plugins.size());
        out.addAll(plugins);
        return out;
    }

    /**
     * Remembers why a plugin failed to deploy.
     */
    public static final class FailedPlugin {

        public final String name;

        public final Exception cause;

        public FailedPlugin(String name, Exception cause) {
            this.name = name;
            this.cause = cause;
        }

        public String getExceptionString() {
            return ExceptionUtils.getFullStackTrace(cause);
        }
    }

    // !SystemProperties.getBoolean(PluginManager.class.getName()+".noFastLookup");
    public static boolean FAST_LOOKUP = true;

    /**
     * {@link ClassLoader} that can see all plugins.
     */
    public final class UberClassLoader extends ClassLoader {

        /**
         * Make generated types visible.
         * Keyed by the generated class name.
         */
        private ConcurrentMap<String, WeakReference<Class>> generatedClasses = new ConcurrentHashMap<String, WeakReference<Class>>();

        /**
         * Cache of loaded, or known to be unloadable, classes.
         */
        private final Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();

        public UberClassLoader() {
            super(PluginManager.class.getClassLoader());
        }

        public void addNamedClass(String className, Class c) {
            generatedClasses.put(className, new WeakReference<Class>(c));
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            WeakReference<Class> wc = generatedClasses.get(name);
            if (wc != null) {
                Class c = wc.get();
                if (c != null)
                    return c;
                else
                    generatedClasses.remove(name, wc);
            }
            if (name.startsWith("SimpleTemplateScript")) {
                // cf. groovy.text.SimpleTemplateEngine
                throw new ClassNotFoundException("ignoring " + name);
            }
            synchronized (loaded) {
                if (loaded.containsKey(name)) {
                    Class<?> c = loaded.get(name);
                    if (c != null) {
                        return c;
                    } else {
                        throw new ClassNotFoundException("cached miss for " + name);
                    }
                }
            }
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    try {
                        Class<?> c = ClassLoaderReflectionToolkit._findLoadedClass(p.classLoader, name);
                        if (c != null) {
                            synchronized (loaded) {
                                loaded.put(name, c);
                            }
                            return c;
                        }
                        // calling findClass twice appears to cause LinkageError: duplicate class def
                        c = ClassLoaderReflectionToolkit._findClass(p.classLoader, name);
                        synchronized (loaded) {
                            loaded.put(name, c);
                        }
                        return c;
                    } catch (ClassNotFoundException e) {
                        // not found. try next
                    }
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    try {
                        return p.classLoader.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // not found. try next
                    }
                }
            }
            synchronized (loaded) {
                loaded.put(name, null);
            }
            // not found in any of the classloader. delegate.
            throw new ClassNotFoundException(name);
        }

        @Override
        protected URL findResource(String name) {
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    URL url = ClassLoaderReflectionToolkit._findResource(p.classLoader, name);
                    if (url != null)
                        return url;
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    URL url = p.classLoader.getResource(name);
                    if (url != null)
                        return url;
                }
            }
            return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            List<URL> resources = new ArrayList<URL>();
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    resources.addAll(Collections.list(ClassLoaderReflectionToolkit._findResources(p.classLoader, name)));
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    resources.addAll(Collections.list(p.classLoader.getResources(name)));
                }
            }
            return Collections.enumeration(resources);
        }

        @Override
        public String toString() {
            // only for debugging purpose
            return "classLoader " + getClass().getName();
        }
    }
}
