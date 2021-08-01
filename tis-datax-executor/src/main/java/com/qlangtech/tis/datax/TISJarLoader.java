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

package com.qlangtech.tis.datax;

import com.qlangtech.tis.extension.PluginManager;

import java.net.URL;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-07-15 06:40
 **/
public class TISJarLoader extends com.alibaba.datax.core.util.container.JarLoader {
    private final PluginManager pluginManager;

    public TISJarLoader(PluginManager pluginManager) {
        super(new String[]{"."});
        this.pluginManager = pluginManager;
    }

    public URL getResource(String name) {
        URL url = pluginManager.uberClassLoader.getResource(name);
        if (url == null) {
            return super.getResource(name);
        }
        return url;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            PluginManager.UberClassLoader classLoader = pluginManager.uberClassLoader;
            return classLoader.findClass(name);
        } catch (Throwable e) {
            throw new RuntimeException("className:" + name + ",scan the plugins:"
                    + pluginManager.activePlugins.stream().map((p) -> p.getDisplayName()).collect(Collectors.joining(",")), e);
        }
    }
}
