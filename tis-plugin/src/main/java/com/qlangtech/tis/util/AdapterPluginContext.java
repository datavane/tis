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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-07-11 08:03
 **/
public abstract class AdapterPluginContext implements IPluginContext {
    private final IPluginContext pluginContext;

    public AdapterPluginContext(IPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String getExecId() {
        return pluginContext.getExecId();
    }

    @Override
    public boolean isCollectionAware() {
        return pluginContext.isCollectionAware();
    }

    @Override
    public boolean isDataSourceAware() {
        return pluginContext.isDataSourceAware();
    }

    @Override
    public void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context, boolean shallUpdateDB) {
        pluginContext.addDb(dbDesc, dbName, context, shallUpdateDB);
    }

    @Override
    public String getCollectionName() {
        return pluginContext.getCollectionName();
    }

    @Override
    public void errorsPageShow(Context context) {
        pluginContext.errorsPageShow(context);
    }

    @Override
    public void addActionMessage(Context context, String msg) {
        pluginContext.addActionMessage(context, msg);
    }

    @Override
    public void setBizResult(Context context, Object result) {
        pluginContext.setBizResult(context, result);
    }

    @Override
    public void addErrorMessage(Context context, String msg) {
        pluginContext.addErrorMessage(context, msg);
    }

    @Override
    public String getRequestHeader(String key) {
        return pluginContext.getRequestHeader(key);
    }
}
