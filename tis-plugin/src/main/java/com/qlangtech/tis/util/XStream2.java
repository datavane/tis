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
package com.qlangtech.tis.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.extension.impl.PluginManifest;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.annotation.ITmpFileStore;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class XStream2 extends XStream {

    private static final Logger logger = LoggerFactory.getLogger(XStream2.class);
    private final XppDriver xppDruver;

    public XStream2(XppDriver xppDruver) {
        super(null, null, xppDruver);
        this.xppDruver = xppDruver;
        this.addPermission(AnyTypePermission.ANY);
    }

    private RobustReflectionConverter reflectionConverter;

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, Object root, DataHolder dataHolder) {
        TIS h = TIS.get();
        if (h != null && h.pluginManager != null && h.pluginManager.uberClassLoader != null) {
            setClassLoader(h.pluginManager.uberClassLoader);
        }
        Object o = super.unmarshal(reader, root, dataHolder);
        return o;
    }

    @Override
    protected void setupConverters() {
        reflectionConverter = new RobustReflectionConverter(getMapper(), createReflectionProvider(), new PluginClassOwnership());
        this.registerConverter(reflectionConverter, PRIORITY_VERY_LOW);
        this.registerConverter(new TempFileConvert(this.getMapper(), this.getReflectionProvider()), PRIORITY_VERY_HIGH);
        super.setupConverters();
    }

    public ReflectionProvider createReflectionProvider() {
        return JVM.newReflectionProvider();
    }

    @Override
    public void registerConverter(Converter converter, int priority) {
        if (converter instanceof ReflectionConverter) {
            return;
        }
        super.registerConverter(converter, priority);
    }

    public HierarchicalStreamWriter createHierarchicalStreamWriter(AtomicFileWriter w) {
        return this.xppDruver.createWriter(w);
    }

    /**
     * Marks serialized classes as being owned by particular components.
     */
    interface ClassOwnership {

        /**
         * Looks up the owner of a class, if any.
         *
         * @param clazz a class which might be from a plugin
         * @return an identifier such as plugin name, or null
         */
        String ownerOf(Class<?> clazz);
    }

    class PluginClassOwnership implements ClassOwnership {

        private PluginManager pm;

        @SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        // classOwnership checked for null so why does FB complain?
        @Override
        public String ownerOf(Class<?> clazz) {
            if (pm == null) {
                TIS j = TIS.get();
                if (j != null) {
                    pm = j.getPluginManager();
                }
            }
            if (pm == null) {
                return null;
            }
            // TODO: possibly recursively scan super class to discover dependencies
            PluginWrapper p = pm.whichPlugin(clazz);
            return p != null ? p.getDesc().toString() : null;
        }
    }

    public static class PluginMeta {

        public static final String NAME_VER_SPLIT = "@";

        private final String name;

        public final String ver;

        protected Long lastModifyTimeStamp;

        public String getPluginPackageName() {
            return this.name + PluginManager.PACAKGE_TPI_EXTENSION;
        }

        public File getPluginPackageFile() {
            return new File(TIS.pluginDirRoot, this.getPluginPackageName());
        }

        public PluginMeta(String name, String ver) {
            this(name, ver, null);
        }

        public PluginMeta(String name, String ver, Long lastModifyTimeStamp) {
            this.name = name;
            this.ver = ver;
            this.lastModifyTimeStamp = lastModifyTimeStamp;
        }

        @Override
        public boolean equals(Object o) {
            return this.hashCode() == o.hashCode();
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer(getKey());
            if (lastModifyTimeStamp != null) {
                buffer.append(NAME_VER_SPLIT).append(this.lastModifyTimeStamp);
            }
            return buffer.toString();
        }

        public String getKey() {
            return (new StringBuffer(name + NAME_VER_SPLIT + ver)).toString();
        }

        public String getPluginName() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, ver);
        }

        public static List<PluginMeta> parse(String attribute) {
            List<PluginMeta> result = Lists.newArrayList();
            String[] metaArray = StringUtils.split(attribute, ",");
            for (String meta : metaArray) {
                String[] verinfo = StringUtils.split(meta, NAME_VER_SPLIT);
                if (verinfo.length == 2) {
                    result.add(new PluginMeta(verinfo[0], verinfo[1]));
                } else if (verinfo.length == 3) {
                    result.add(new PluginMeta(verinfo[0], verinfo[1], Long.parseLong(verinfo[2])) {
                        @Override
                        public long getLastModifyTimeStamp() {
                            return lastModifyTimeStamp;
                        }
                    });
                } else {
                    throw new IllegalArgumentException("attri is invalid:" + attribute);
                }

            }
            return result;
        }

        /**
         * plugin的最终打包时间
         *
         * @return
         */
        public long getLastModifyTimeStamp() {
            if (lastModifyTimeStamp != null) {
                return this.lastModifyTimeStamp;
            }
            return this.lastModifyTimeStamp = processJarManifest((mfst) ->
                    (mfst == null) ? -1 : mfst.getLastModfiyTime()// Long.parseLong(.getMainAttributes().getValue(PluginStrategy.KEY_LAST_MODIFY_TIME))
            );
        }

        public boolean isLastModifyTimeStampNull() {
            return lastModifyTimeStamp == null;
        }

        public List<PluginMeta> getMetaDependencies() {
            return processJarManifest((mfst) -> {
                if (mfst == null) {
                    // throw new IllegalStateException("plugin:" + PluginMeta.this.toString() + " relevant manifest can not be null");
                    return Collections.emptyList();
                }
                ClassicPluginStrategy.DependencyMeta dpts = mfst.getDependencyMeta();// ClassicPluginStrategy.getDependencyMeta(mfst.getMainAttributes());
                return dpts.dependencies.stream().map((d) -> new PluginMeta(d.shortName, d.version)).collect(Collectors.toList());
            });
        }

        private <R> R processJarManifest(Function<PluginManifest, R> manProcess) {
            File f = getPluginPackageFile();
//            if (!f.exists()) {
//                // throw new IllegalStateException("file:" + f.getPath() + " is not exist");
//                return manProcess.apply(null);
//            }
            PluginManifest manifest = PluginManifest.create(f);
            if (manifest == null) {
                return manProcess.apply(null);
            }

            return manProcess.apply(manifest);

//            try (JarInputStream tpiFIle = new JarInputStream(FileUtils.openInputStream(f), false)) {
//                Manifest mfst = tpiFIle.getManifest();
//                return manProcess.apply(mfst);
//            } catch (Exception e) {
//                throw new RuntimeException("tpi path:" + f.getAbsolutePath(), e);
//            }
        }

        public static void main(String[] args) throws Exception {
            File f = new File("/Users/mozhenghua/j2ee_solution/project/plugins/tis-datax/tis-datax-hudi-plugin/target/tis-datax-hudi-plugin.tpi");
            try (JarFile tpiFIle = new JarFile(f, false)) {
                tpiFIle.stream().forEach((e) -> System.out.println(e.getName()));
            }
        }


        public boolean copyFromRemote() {
            return copyFromRemote(Lists.newArrayList());
        }

        public boolean copyFromRemote(List<File> pluginFileCollector) {
            return copyFromRemote(pluginFileCollector, false, false);
        }

        /**
         * 将远端插件拷贝到本地
         */
        public boolean copyFromRemote(List<File> pluginFileCollector, boolean ignoreDependencies, boolean directDownload) {
            final URL url = CenterResource.getPathURL(Config.SUB_DIR_LIBS + "/" + TIS.KEY_TIS_PLUGIN_ROOT + "/" + this.getPluginPackageName());
            final File local = getPluginPackageFile();
            boolean updated = CenterResource.copyFromRemote2Local(url, local, directDownload);
            if (!ignoreDependencies && updated) {
                for (XStream2.PluginMeta d : this.getMetaDependencies()) {
                    d.copyFromRemote(pluginFileCollector);
                }
                pluginFileCollector.add(local);
            }
            return updated;
        }

//        public void install() {
//            try {
//                if (!TIS.permitInitialize) {
//                    return;
//                }
//                logger.info("dyanc install:{} to classloader ", this.toString());
//                PluginManager pluginManager = TIS.get().getPluginManager();
//                File pluginFile = getPluginPackageFile();
//                List<PluginWrapper> plugins = Lists.newArrayList(
//                        pluginManager.getPluginStrategy().createPluginWrapper(pluginFile));
//
//                pluginManager.dynamicLoad(pluginFile, false, null);
//                pluginManager.start(plugins);
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    /**
     * TIS 前端提交的临时文件转化，将临时文件保存到plugin的save目录中去
     */
    private static class TempFileConvert extends AbstractReflectionConverter {

        public TempFileConvert(Mapper mapper, ReflectionProvider reflectionProvider) {
            super(mapper, reflectionProvider);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            ITmpFileStore tmpFileStore = (ITmpFileStore) source;
            XmlFile xmlFile = (XmlFile) context.get(XmlFile.class);
            Objects.requireNonNull(xmlFile, "xmlFile can not be null");
            tmpFileStore.save(xmlFile.getFile().getParentFile());

            doMarshal(source, writer, context);
        }

//        @Override
//        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
//
//            this.doUnmarshal(reader,context);
//            return null;
//        }


        @Override
        public Object doUnmarshal(Object result, HierarchicalStreamReader reader, UnmarshallingContext context) {
            ITmpFileStore tmpFileStore = (ITmpFileStore) super.doUnmarshal(result, reader, context);
            XmlFile xmlFile = (XmlFile) context.get(XmlFile.class);
            Objects.requireNonNull(xmlFile, "xmlFile can not be null");
            tmpFileStore.setTmpeFile((new ITmpFileStore.TmpFile(new File(xmlFile.getFile().getParentFile(), tmpFileStore.getStoreFileName()))));
            return tmpFileStore;
        }

        @Override
        public boolean canConvert(Class type) {
            return ITmpFileStore.class.isAssignableFrom(type);
        }
    }
}
