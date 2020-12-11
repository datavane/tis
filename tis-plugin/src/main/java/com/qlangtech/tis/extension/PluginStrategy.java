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
package com.qlangtech.tis.extension;

import com.qlangtech.tis.TIS;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface PluginStrategy {

    String FILE_NAME_timestamp2 = ".timestamp2";

    <T> List<ExtensionComponent<T>> findComponents(Class<T> extensionType, TIS tis);

    void updateDependency(PluginWrapper depender, PluginWrapper dependee);

    /**
     * Creates a plugin wrapper, which provides a management interface for the plugin
     *
     * @param archive Either a directory that points to a pre-exploded plugin, or an jpi file, or an jpl file.
     */
    PluginWrapper createPluginWrapper(File archive) throws IOException;

    /**
     * Finds the plugin name without actually unpacking anything {@link #createPluginWrapper} would.
     */
    String getShortName(File archive) throws IOException;

    /**
     * Loads the plugin and starts it.
     *
     * <p>
     * This should be done after all the classloaders are constructed for all
     * the plugins, so that dependencies can be properly loaded by plugins.
     */
    void load(PluginWrapper wrapper) throws IOException;

    /**
     * Optionally start services provided by the plugin. Should be called
     * when all plugins are loaded.
     *
     * @param plugin
     */
    void initializeComponents(PluginWrapper plugin);
}
