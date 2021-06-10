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
package com.qlangtech.tis.datax.impl;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:48
 */
public abstract class DataxWriter implements Describable<DataxWriter>, IDataxWriter {

    public static final ThreadLocal<DataxReader> dataReaderThreadlocal = new ThreadLocal<>();

    /**
     * save
     *
     * @param appname
     */
    public static KeyedPluginStore<DataxWriter> getPluginStore(IPluginContext context, String appname) {
        KeyedPluginStore<DataxWriter> pluginStore = new KeyedPluginStore(new DataxReader.AppKey(context, appname, DataxWriter.class));
        return pluginStore;
    }

    /**
     * load
     *
     * @param appName
     * @return
     */
    public static DataxWriter load(IPluginContext context, String appName) {
        DataxWriter appSource = getPluginStore(context, appName).getPlugin();
        Objects.requireNonNull(appSource, "appName:" + appName + " relevant appSource can not be null");
        return appSource;
    }


//    public static class AppKey extends KeyedPluginStore.Key<DataxWriter> {
//        public AppKey(String dataxName) {
//            super(IFullBuildContext.NAME_APP_DIR, dataxName, DataxWriter.class);
//        }
//    }


    @Override
    public Descriptor<DataxWriter> getDescriptor() {
        Descriptor<DataxWriter> descriptor = TIS.get().getDescriptor(this.getClass());
        if (!(BaseDataxWriterDescriptor.class.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of "
                    + BaseDataxWriterDescriptor.class.getName());
        }
        return descriptor;
    }


    public static abstract class BaseDataxWriterDescriptor extends Descriptor<DataxWriter> {


        /**
         * reader 中是否可以选择多个表，例如像elastic这样的writer中对于column的设置比较复杂，需要在writer plugin页面中完成，所以就不能支持在reader中选择多个表了
         *
         * @return
         */
        public boolean isSupportMultiTable() {
            return true;
        }

        /**
         * 是否可以选择多个表，像Mysql这样的 ,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public abstract boolean isRdbms();
    }
}
