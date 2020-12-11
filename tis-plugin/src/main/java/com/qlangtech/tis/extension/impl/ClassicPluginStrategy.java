/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.*;
import com.qlangtech.tis.extension.util.AntClassLoader;
import com.qlangtech.tis.extension.util.ClassLoaderReflectionToolkit;
import com.qlangtech.tis.extension.util.CyclicGraphDetector;
import com.qlangtech.tis.extension.util.VersionNumber;
import com.qlangtech.tis.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.resources.MappedResourceCollection;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ClassicPluginStrategy implements PluginStrategy {

    /**
     * Filter for jar files.
     */
    private static final FilenameFilter JAR_FILTER = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };

    private PluginManager pluginManager;

    /**
     * All the plugins eventually delegate this classloader to load core, servlet APIs, and SE runtime.
     */
    private final MaskingClassLoader coreClassLoader = new MaskingClassLoader(getClass().getClassLoader());

    public ClassicPluginStrategy(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public String getShortName(File archive) throws IOException {
        Manifest manifest;
        if (isLinked(archive)) {
            manifest = loadLinkedManifest(archive);
        } else {
            JarFile jf = new JarFile(archive, false);
            try {
                manifest = jf.getManifest();
            } finally {
                jf.close();
            }
        }
        return PluginWrapper.computeShortName(manifest, archive.getName());
    }

    private static boolean isLinked(File archive) {
        return archive.getName().endsWith(".hpl") || archive.getName().endsWith(".jpl");
    }

    private static Manifest loadLinkedManifest(File archive) throws IOException {
        // resolve the .hpl file to the location of the manifest file
        try {
            // Locate the manifest
            String firstLine;
            FileInputStream manifestHeaderInput = new FileInputStream(archive);
            try {
                firstLine = IOUtils.readFirstLine(manifestHeaderInput, "UTF-8");
            } finally {
                manifestHeaderInput.close();
            }
            if (firstLine.startsWith("Manifest-Version:")) {
            // this is the manifest already
            } else {
                // indirection
                archive = resolve(archive, firstLine);
            }
            // Read the manifest
            FileInputStream manifestInput = new FileInputStream(archive);
            try {
                return new Manifest(manifestInput);
            } finally {
                manifestInput.close();
            }
        } catch (IOException e) {
            throw new IOException("Failed to load " + archive, e);
        }
    }

    @Override
    public PluginWrapper createPluginWrapper(File archive) throws IOException {
        final Manifest manifest;
        URL baseResourceURL = null;
        File expandDir = null;
        // if .hpi, this is the directory where war is expanded
        boolean isLinked = isLinked(archive);
        if (isLinked) {
            manifest = loadLinkedManifest(archive);
        } else {
            if (archive.isDirectory()) {
                // already expanded
                expandDir = archive;
            } else {
                File f = pluginManager.getWorkDir();
                expandDir = new File(f == null ? archive.getParentFile() : f, getBaseName(archive.getName()));
                explode(archive, expandDir);
            }
            File manifestFile = new File(expandDir, PluginWrapper.MANIFEST_FILENAME);
            if (!manifestFile.exists()) {
                throw new IOException("Plugin installation failed. No manifest at " + manifestFile);
            }
            FileInputStream fin = new FileInputStream(manifestFile);
            try {
                manifest = new Manifest(fin);
            } finally {
                fin.close();
            }
        }
        final Attributes atts = manifest.getMainAttributes();
        // TODO: define a mechanism to hide classes
        // String export = manifest.getMainAttributes().getValue("Export");
        List<File> paths = new ArrayList<File>();
        if (isLinked) {
            parseClassPath(manifest, archive, paths, "Libraries", ",");
            // backward compatibility
            parseClassPath(manifest, archive, paths, "Class-Path", " +");
            baseResourceURL = resolve(archive, atts.getValue("Resource-Path")).toURI().toURL();
        } else {
            File classes = new File(expandDir, "WEB-INF/classes");
            if (classes.exists())
                paths.add(classes);
            File lib = new File(expandDir, "WEB-INF/lib");
            File[] libs = lib.listFiles(JAR_FILTER);
            if (libs != null)
                paths.addAll(Arrays.asList(libs));
            baseResourceURL = expandDir.toPath().toUri().toURL();
        }
        File disableFile = new File(archive.getPath() + ".disabled");
        if (disableFile.exists()) {
            LOGGER.info("Plugin " + archive.getName() + " is disabled");
        }
        // compute dependencies
        List<PluginWrapper.Dependency> dependencies = new ArrayList<PluginWrapper.Dependency>();
        List<PluginWrapper.Dependency> optionalDependencies = new ArrayList<PluginWrapper.Dependency>();
        String v = atts.getValue("Plugin-Dependencies");
        if (v != null) {
            for (String s : v.split(",")) {
                PluginWrapper.Dependency d = new PluginWrapper.Dependency(s);
                if (d.optional) {
                    optionalDependencies.add(d);
                } else {
                    dependencies.add(d);
                }
            }
        }
        fix(atts, optionalDependencies);
        // Register global classpath mask. This is useful for hiding JavaEE APIs that you might see from the container,
        // such as database plugin for JPA support. The Mask-Classes attribute is insufficient because those classes
        // also need to be masked by all the other plugins that depend on the database plugin.
        String masked = atts.getValue("Global-Mask-Classes");
        if (masked != null) {
            for (String pkg : masked.trim().split("[ \t\r\n]+")) coreClassLoader.add(pkg);
        }
        ClassLoader dependencyLoader = new DependencyClassLoader(coreClassLoader, archive, Util.join(dependencies, optionalDependencies));
        dependencyLoader = getBaseClassLoader(atts, dependencyLoader);
        return new PluginWrapper(pluginManager, archive, manifest, baseResourceURL, createClassLoader(paths, dependencyLoader, atts), disableFile, dependencies, optionalDependencies);
    }

    private static void fix(Attributes atts, List<PluginWrapper.Dependency> optionalDependencies) {
        String pluginName = atts.getValue("Short-Name");
        String jenkinsVersion = atts.getValue("Jenkins-Version");
        if (jenkinsVersion == null)
            jenkinsVersion = atts.getValue("Hudson-Version");
    // optionalDependencies.addAll(getImpliedDependencies(pluginName, jenkinsVersion));
    }

    @Deprecated
    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent) throws IOException {
        return createClassLoader(paths, parent, null);
    }

    /**
     * Creates the classloader that can load all the specified jar files and delegate to the given parent.
     */
    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent, Attributes atts) throws IOException {
        if (atts != null) {
            String usePluginFirstClassLoader = atts.getValue("PluginFirstClassLoader");
            if (Boolean.valueOf(usePluginFirstClassLoader)) {
                PluginFirstClassLoader classLoader = new PluginFirstClassLoader();
                classLoader.setParentFirst(false);
                classLoader.setParent(parent);
                classLoader.addPathFiles(paths);
                return classLoader;
            }
        }
        AntClassLoader2 classLoader = new AntClassLoader2(parent);
        classLoader.addPathFiles(paths);
        return classLoader;
    }

    /**
     * Implicit dependencies that are known to be unnecessary and which must be cut out to prevent a dependency cycle among bundled plugins.
     */
    private static final Set<String> BREAK_CYCLES = new HashSet<String>(Arrays.asList("script-security/matrix-auth", "script-security/windows-slaves", "script-security/antisamy-markup-formatter", "script-security/matrix-project", "credentials/matrix-auth", "credentials/windows-slaves"));

    /**
     * Computes the classloader that takes the class masking into account.
     *
     * <p>
     * This mechanism allows plugins to have their own versions for libraries that core bundles.
     */
    private ClassLoader getBaseClassLoader(Attributes atts, ClassLoader base) {
        String masked = atts.getValue("Mask-Classes");
        if (masked != null)
            base = new MaskingClassLoader(base, masked.trim().split("[ \t\r\n]+"));
        return base;
    }

    public void initializeComponents(PluginWrapper plugin) {
    }

    private static final List<ExtensionFinder> finders = Collections.singletonList(new ExtensionFinder.Sezpoz());

    public <T> List<ExtensionComponent<T>> findComponents(final Class<T> type, TIS tis) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scout-loading ExtensionList: " + type);
        }
        for (ExtensionFinder finder : finders) {
            finder.scout(type, tis);
        }
        List<ExtensionComponent<T>> r = Lists.newArrayList();
        for (ExtensionFinder finder : finders) {
            try {
                r.addAll(finder.find(type, tis));
            } catch (AbstractMethodError e) {
                // backward compatibility
                for (T t : finder.findExtensions(type, tis)) r.add(new ExtensionComponent<T>(t));
            }
        }
        return r;
    }

    public void load(PluginWrapper wrapper) throws IOException {
        // override the context classloader. This no longer makes sense,
        // but it is left for the backward compatibility
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(wrapper.classLoader);
        try {
            String className = wrapper.getPluginClass();
            if (className == null) {
                // use the default dummy instance
                wrapper.setPlugin(new Plugin.DummyImpl());
            } else {
                try {
                    Class<?> clazz = wrapper.classLoader.loadClass(className);
                    Object o = clazz.newInstance();
                    if (!(o instanceof Plugin)) {
                        throw new IOException(className + " doesn't extend from hudson.Plugin");
                    }
                    wrapper.setPlugin((Plugin) o);
                } catch (LinkageError | ClassNotFoundException e) {
                    throw new IOException("Unable to load " + className + " from " + wrapper.getShortName(), e);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new IOException("Unable to create instance of " + className + " from " + wrapper.getShortName(), e);
                }
            }
            // initialize plugin
            try {
                Plugin plugin = wrapper.getPlugin();
                // plugin.setServletContext(pluginManager.context);
                startPlugin(wrapper);
            } catch (Throwable t) {
                // gracefully handle any error in plugin.
                throw new IOException("Failed to initialize", t);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void startPlugin(PluginWrapper plugin) throws Exception {
        plugin.getPlugin().start();
    }

    @Override
    public void updateDependency(PluginWrapper depender, PluginWrapper dependee) {
        DependencyClassLoader classLoader = findAncestorDependencyClassLoader(depender.classLoader);
        if (classLoader != null) {
            classLoader.updateTransientDependencies();
            LOGGER.info("Updated dependency of {}", depender.getShortName());
        }
    }

    private DependencyClassLoader findAncestorDependencyClassLoader(ClassLoader classLoader) {
        for (; classLoader != null; classLoader = classLoader.getParent()) {
            if (classLoader instanceof DependencyClassLoader) {
                return (DependencyClassLoader) classLoader;
            }
            if (classLoader instanceof AntClassLoader) {
                // AntClassLoaders hold parents not only as AntClassLoader#getParent()
                // but also as AntClassLoader#getConfiguredParent()
                DependencyClassLoader ret = findAncestorDependencyClassLoader(((AntClassLoader) classLoader).getConfiguredParent());
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    private static File resolve(File base, String relative) {
        File rel = new File(relative);
        if (rel.isAbsolute())
            return rel;
        else
            return new File(base.getParentFile(), relative);
    }

    private static void parseClassPath(Manifest manifest, File archive, List<File> paths, String attributeName, String separator) throws IOException {
        String classPath = manifest.getMainAttributes().getValue(attributeName);
        // attribute not found
        if (classPath == null)
            return;
        for (String s : classPath.split(separator)) {
            File file = resolve(archive, s);
            if (file.getName().contains("*")) {
                // handle wildcard
                FileSet fs = new FileSet();
                File dir = file.getParentFile();
                fs.setDir(dir);
                fs.setIncludes(file.getName());
                for (String included : fs.getDirectoryScanner(new Project()).getIncludedFiles()) {
                    paths.add(new File(dir, included));
                }
            } else {
                if (!file.exists())
                    throw new IOException("No such file: " + file);
                paths.add(file);
            }
        }
    }

    /**
     * Explodes the plugin into a directory, if necessary.
     */
    private static void explode(File archive, File destDir) throws IOException {
        destDir.mkdirs();
        // timestamp check
        File explodeTime = new File(destDir, FILE_NAME_timestamp2);
        if (explodeTime.exists() && explodeTime.lastModified() == archive.lastModified()) {
            // no need to expand
            return;
        }
        LOGGER.info("start to explode archive:{}", archive.getAbsolutePath());
        // delete the contents so that old files won't interfere with new files
        Util.deleteRecursive(destDir);
        try {
            Project prj = new Project();
            unzipExceptClasses(archive, destDir, prj);
            createClassJarFromWebInfClasses(archive, destDir, prj);
        } catch (BuildException x) {
            throw new IOException("Failed to expand " + archive, x);
        }
        try {
            // new FilePath(explodeTime).touch(archive.lastModified());
            FileUtils.touch(explodeTime);
            explodeTime.setLastModified(archive.lastModified());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Repackage classes directory into a jar file to make it remoting friendly.
     * The remoting layer can cache jar files but not class files.
     */
    private static void createClassJarFromWebInfClasses(File archive, File destDir, Project prj) throws IOException {
        File classesJar = new File(destDir, "WEB-INF/lib/classes.jar");
        ZipFileSet zfs = new ZipFileSet();
        zfs.setProject(prj);
        zfs.setSrc(archive);
        zfs.setIncludes("WEB-INF/classes/");
        MappedResourceCollection mapper = new MappedResourceCollection();
        mapper.add(zfs);
        GlobPatternMapper gm = new GlobPatternMapper();
        gm.setFrom("WEB-INF/classes/*");
        gm.setTo("*");
        mapper.add(gm);
        final long dirTime = archive.lastModified();
        // this ZipOutputStream is reused and not created for each directory
        final ZipOutputStream wrappedZOut = new ZipOutputStream(new NullOutputStream()) {

            @Override
            public void putNextEntry(ZipEntry ze) throws IOException {
                // roundup
                ze.setTime(dirTime + 1999);
                super.putNextEntry(ze);
            }
        };
        try {
            Zip z = new Zip() {

                /**
                 * Forces the fixed timestamp for directories to make sure
                 * classes.jar always get a consistent checksum.
                 */
                protected void zipDir(Resource dir, ZipOutputStream zOut, String vPath, int mode, ZipExtraField[] extra) throws IOException {
                    // use wrappedZOut instead of zOut
                    super.zipDir(dir, wrappedZOut, vPath, mode, extra);
                }
            };
            z.setProject(prj);
            z.setTaskType("zip");
            classesJar.getParentFile().mkdirs();
            z.setDestFile(classesJar);
            z.add(mapper);
            z.execute();
        } finally {
            wrappedZOut.close();
        }
    }

    private static void unzipExceptClasses(File archive, File destDir, Project prj) {
        Expand e = new Expand();
        e.setProject(prj);
        e.setTaskType("unzip");
        e.setSrc(archive);
        e.setDest(destDir);
        PatternSet p = new PatternSet();
        p.setExcludes("WEB-INF/classes/");
        e.addPatternset(p);
        e.execute();
    }

    /**
     * Used to load classes from dependency plugins.
     */
    final class DependencyClassLoader extends ClassLoader {

        /**
         * This classloader is created for this plugin. Useful during debugging.
         */
        private final File _for;

        private List<PluginWrapper.Dependency> dependencies;

        /**
         * Topologically sorted list of transient dependencies.
         */
        private volatile List<PluginWrapper> transientDependencies;

        public DependencyClassLoader(ClassLoader parent, File archive, List<PluginWrapper.Dependency> dependencies) {
            super(parent);
            this._for = archive;
            this.dependencies = dependencies;
        }

        private void updateTransientDependencies() {
            // This will be recalculated at the next time.
            transientDependencies = null;
        }

        private List<PluginWrapper> getTransitiveDependencies() {
            if (transientDependencies == null) {
                CyclicGraphDetector<PluginWrapper> cgd = new CyclicGraphDetector<PluginWrapper>() {

                    @Override
                    protected List<PluginWrapper> getEdges(PluginWrapper pw) {
                        List<PluginWrapper> dep = new ArrayList<PluginWrapper>();
                        for (PluginWrapper.Dependency d : pw.getDependencies()) {
                            PluginWrapper p = pluginManager.getPlugin(d.shortName);
                            if (p != null && p.isActive())
                                dep.add(p);
                        }
                        return dep;
                    }
                };
                try {
                    for (PluginWrapper.Dependency d : dependencies) {
                        PluginWrapper p = pluginManager.getPlugin(d.shortName);
                        if (p != null && p.isActive())
                            cgd.run(Collections.singleton(p));
                    }
                } catch (CyclicGraphDetector.CycleDetectedException e) {
                    // such error should have been reported earlier
                    throw new AssertionError(e);
                }
                transientDependencies = cgd.getSorted();
            }
            return transientDependencies;
        }

        // public List<PluginWrapper> getDependencyPluginWrappers() {
        // List<PluginWrapper> r = new ArrayList<PluginWrapper>();
        // for (Dependency d : dependencies) {
        // PluginWrapper w = pluginManager.getPlugin(d.shortName);
        // if (w!=null)    r.add(w);
        // }
        // return r;
        // }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    try {
                        Class<?> c = ClassLoaderReflectionToolkit._findLoadedClass(pw.classLoader, name);
                        if (c != null)
                            return c;
                        return ClassLoaderReflectionToolkit._findClass(pw.classLoader, name);
                    } catch (ClassNotFoundException e) {
                    // not found. try next
                    }
                }
            } else {
                for (PluginWrapper.Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null)
                        try {
                            return p.classLoader.loadClass(name);
                        } catch (ClassNotFoundException _) {
                        // try next
                        }
                }
            }
            throw new ClassNotFoundException(name);
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            HashSet<URL> result = new HashSet<URL>();
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    Enumeration<URL> urls = ClassLoaderReflectionToolkit._findResources(pw.classLoader, name);
                    while (urls != null && urls.hasMoreElements()) result.add(urls.nextElement());
                }
            } else {
                for (PluginWrapper.Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null) {
                        Enumeration<URL> urls = p.classLoader.getResources(name);
                        while (urls != null && urls.hasMoreElements()) result.add(urls.nextElement());
                    }
                }
            }
            return Collections.enumeration(result);
        }

        @Override
        protected URL findResource(String name) {
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    URL url = ClassLoaderReflectionToolkit._findResource(pw.classLoader, name);
                    if (url != null)
                        return url;
                }
            } else {
                for (PluginWrapper.Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null) {
                        URL url = p.classLoader.getResource(name);
                        if (url != null)
                            return url;
                    }
                }
            }
            return null;
        }
    }

    private final class AntClassLoader2 extends AntClassLoader implements Closeable {

        private final Vector pathComponents;

        private AntClassLoader2(ClassLoader parent) {
            super(parent, true);
            try {
                Field $pathComponents = AntClassLoader.class.getDeclaredField("pathComponents");
                $pathComponents.setAccessible(true);
                pathComponents = (Vector) $pathComponents.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new Error(e);
            }
        }

        public void addPathFiles(Collection<File> paths) throws IOException {
            for (File f : paths) addPathFile(f);
        }

        public void close() throws IOException {
            cleanup();
        }

        /**
         * As of 1.8.0, {@link AntClassLoader} doesn't implement {@link #findResource(String)}
         * in any meaningful way, which breaks fast lookup. Implement it properly.
         */
        @Override
        protected URL findResource(String name) {
            URL url = null;
            // try and load from this loader if the parent either didn't find
            // it or wasn't consulted.
            Enumeration e = pathComponents.elements();
            while (e.hasMoreElements() && url == null) {
                File pathComponent = (File) e.nextElement();
                url = getResourceURL(pathComponent, name);
                if (url != null) {
                    log("Resource " + name + " loaded from ant loader", Project.MSG_DEBUG);
                }
            }
            return url;
        }

        @Override
        protected Class defineClassFromData(File container, byte[] classData, String classname) throws IOException {
            // classData = pluginManager.getCompatibilityTransformer().transform(classname, classData, this);
            return super.defineClassFromData(container, classData, classname);
        }
    }

    // public static boolean useAntClassLoader = SystemProperties.getBoolean(ClassicPluginStrategy.class.getName() + ".useAntClassLoader");
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassicPluginStrategy.class.getName());
    // public static boolean DISABLE_TRANSFORMER = SystemProperties.getBoolean(ClassicPluginStrategy.class.getName() + ".noBytecodeTransformer");
}
