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
package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.IPluginContext;

import java.io.File;
import java.util.Objects;

/**
 * @author: baisui 百岁
 * @create: 2020-11-24 16:24
 */
public class DSKey extends KeyedPluginStore.Key<DataSourceFactory> {
    private final DbScope dbScope;

    public DSKey(String groupName, DbScope dbScope, String keyVal, Class<DataSourceFactory> pluginClass, IPluginContext pluginContext) {
        super(groupName, keyVal, pluginClass, pluginContext);
        this.dbScope = dbScope;
    }

    @Override
    protected String getSerializeFileName() {
        return groupName + File.separator + keyVal + File.separator + pluginClass.getName() + dbScope.getDBType();
    }

    public boolean isFacadeType() {
        return this.dbScope == DbScope.FACADE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.keyVal, this.dbScope, pluginClass);
    }
}
