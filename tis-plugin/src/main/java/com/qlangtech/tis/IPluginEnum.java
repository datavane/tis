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

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-29 14:59
 **/
public interface IPluginEnum<T extends Describable<T>> {

    public Class<T> getExtensionPoint();

    public String getIdentity();

    public String getCaption();

    public Selectable getSelectable();

    public <T> List<T> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta);

    public PluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta);

    public <T extends Describable<T>> List<Descriptor<T>> descriptors();

    public boolean isIdentityUnique();
}
