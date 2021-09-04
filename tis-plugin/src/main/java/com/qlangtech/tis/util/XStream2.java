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
package com.qlangtech.tis.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class XStream2 extends XStream {

    private final XppDriver xppDruver;

    public XStream2(XppDriver xppDruver) {
        super(null, null, xppDruver);
        this.xppDruver = xppDruver;
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

        public final String name;

        public final String ver;

        public String getPluginPackageName() {
            return this.name + PluginManager.PACAKGE_TPI_EXTENSION;
        }

        public File getPluginPackageFile() {
            return new File(TIS.pluginDirRoot, this.getPluginPackageName());
        }

        public PluginMeta(String name, String ver) {
            this.name = name;
            this.ver = ver;
        }

        @Override
        public boolean equals(Object o) {
            return this.hashCode() == o.hashCode();
        }

        @Override
        public String toString() {
            return name + NAME_VER_SPLIT + ver;
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
                if (verinfo.length != 2) {
                    throw new IllegalArgumentException("attri is invalid:" + attribute);
                }
                result.add(new PluginMeta(verinfo[0], verinfo[1]));
            }
            return result;
        }

        private List<PluginMeta> getMetaDependencies() {
            File f = getPluginPackageFile();
            if (!f.exists()) {
                throw new IllegalStateException("file:" + f.getPath() + " is not exist");
            }
            try (JarFile tpiFIle = new JarFile(getPluginPackageFile(), false)) {
                Manifest mfst = tpiFIle.getManifest();
                ClassicPluginStrategy.DependencyMeta dpts = ClassicPluginStrategy.getDependencyMeta(mfst.getMainAttributes());
                return dpts.dependencies.stream().map((d) -> new PluginMeta(d.shortName, d.version)).collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 将远端插件拷贝到本地
         */
        public boolean copyFromRemote() {
            final URL url = CenterResource.getPathURL(Config.SUB_DIR_LIBS + "/" + TIS.KEY_TIS_PLUGIN_ROOT + "/" + this.getPluginPackageName());
            final File local = getPluginPackageFile();
            boolean updated = CenterResource.copyFromRemote2Local(url, local, false);
            if (updated) {
                for (XStream2.PluginMeta d : this.getMetaDependencies()) {
                    d.copyFromRemote();
                }
            }
            return updated;
        }
    }
}
