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
package com.qlangtech.tis.plugin;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局插件持久化存储
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginStore<T extends Describable> implements IRepositoryResource, IPluginStoreSave<T> {

    private final transient Class<T> pluginClass;

    private List<T> plugins = Lists.newArrayList();

    private final transient XmlFile file;

    public PluginStore(Class<T> pluginClass) {
        this(pluginClass, Descriptor.getConfigFile(pluginClass.getName()));
    }

    public PluginStore(Class<T> pluginClass, XmlFile file) {
        this.pluginClass = pluginClass;
        this.file = file;
    }

    void cleanPlugins() {
        this.plugins.clear();
    }

    /**
     * 拷贝配置文件到本地
     */
    public void copyConfigFromRemote() {
        CenterResource.copyFromRemote2Local(
                TIS.KEY_TIS_PLUGIN_CONFIG + "/" + Descriptor.getPluginFileName(getSerializeFileName()), true);
    }

    /**
     * 目标文件
     *
     * @return
     */
    public File getTargetFile() {
        return this.file.getFile();
    }

    public List<T> getPlugins() {
        this.load();
        return plugins;
    }

    public T find(String name) {
        List<T> plugins = this.getPlugins();
        if (!IdentityName.class.isAssignableFrom(this.pluginClass)) {
            throw new IllegalStateException(this.pluginClass + " can not find by name:" + name);
        }
        for (T item : plugins) {

            if (StringUtils.equals(name, ((IdentityName) item).identityValue())) {
                return item;
            }
        }
        final String instanceName = this.pluginClass.getSimpleName();
        throw new IllegalStateException(instanceName + " has not be initialized,name:" + name + " can not find relevant '" + instanceName
                + "' in ["
                + plugins.stream().map((r) -> ((IdentityName) r).identityValue()).collect(Collectors.joining(",")) + "]");
    }

    public List<Descriptor<T>> allDescriptor() {
        return TIS.get().getDescriptorList(this.pluginClass);
    }

    public T getPlugin() {
        if (this.getPlugins().size() > 1) {
            throw new IllegalStateException("plugin size can not much than 1");
        }
        Optional<T> first = this.getPlugins().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    /**
     * 当本plugin还没有初始值的时候，可以从一个已经有的plugin把值拷贝过来<br>
     * 适用场景：全局设置了一个plugin的，collection绑定的plugin没有设置，当在设置collection绑定的plugin时候可以以全局plugin为模版，所以就有一个全局plugin向collection绑定的plugin拷贝属性的过程
     *
     * @param other
     */
    public synchronized void copyFrom(IPluginContext pluginContext, PluginStore<T> other) {
        if (this.getPlugin() != null) {
            throw new IllegalStateException("destination plugin store have saved ,can not copy from other");
        }
        if (other.getPlugin() == null) {
            throw new IllegalStateException("from plugin store have not initialized");
        }
        Descriptor.ParseDescribable<T> parseDescribable = new Descriptor.ParseDescribable<>(other.getPlugin());
        ComponentMeta cmetas = new ComponentMeta(other);
        parseDescribable.extraPluginMetas.addAll(cmetas.loadPluginMeta());
        this.setPlugins(pluginContext, Optional.empty(), Collections.singletonList(parseDescribable));
    }

    @Override
    public synchronized boolean setPlugins(IPluginContext pluginContext, Optional<Context> context, List<Descriptor.ParseDescribable<T>> dlist) {
        // as almost the process is process file shall not care of process model whether update or add,bu some times have
        // extra process like db process ,shall pass a bool flag form client
        return this.setPlugins(pluginContext, context, dlist, false);
    }

    /**
     * save the plugin config
     *
     * @param dlist
     */
    @Override
    public synchronized boolean setPlugins(IPluginContext pluginContext, Optional<Context> context, List<Descriptor.ParseDescribable<T>> dlist, boolean update) {
        try {
            Set<XStream2.PluginMeta> pluginsMeta = Sets.newHashSet();
            List<T> collect = dlist.stream().map((r) -> {
                pluginsMeta.addAll(r.extraPluginMetas);
                return r.instance;
            }).collect(Collectors.toList());
            this.plugins = collect;
            // XmlFile file = Descriptor.getConfigFile(getSerializeFileName());
            this.file.write(this, pluginsMeta);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getSerializeFileName() {
        return pluginClass.getName();
    }

    private transient boolean loaded = false;

    private synchronized void load() {
        if (this.loaded) {
            return;
        }
        try {
            copyConfigFromRemote();
            if (!file.exists()) {
                return;
            }
            file.unmarshal(this);
            this.loaded = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
