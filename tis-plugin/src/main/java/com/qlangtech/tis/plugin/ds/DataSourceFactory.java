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

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
public abstract class DataSourceFactory implements Describable<DataSourceFactory>, IdentityName, DataSourceMeta {

    public static final String DS_TYPE_MYSQL = "MySQL";

//    public static List<DataSourceFactory> all() {
//        return TIS.get().getExtensionList(DataSourceFactory.class);
//    }


    /**
     * DataSource like TiSpark has store format as RDD shall skip the phrase of data dump
     *
     * @return
     */
    public boolean skipDumpPhrase() {
        return false;
    }

    /**
     * Get all the dump
     *
     * @return
     */
    public abstract DataDumpers getDataDumpers(TISTable table);

    @Override
    public final Descriptor<DataSourceFactory> getDescriptor() {
        Descriptor<DataSourceFactory> descriptor = TIS.get().getDescriptor(this.getClass());
        if (!(descriptor instanceof BaseDataSourceFactoryDescriptor)) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " must implement the Descriptor of "
                    + BaseDataSourceFactoryDescriptor.class.getSimpleName());
        }
        return descriptor;
    }


    public abstract static class BaseDataSourceFactoryDescriptor extends Descriptor<DataSourceFactory> {
        @Override
        public final String getDisplayName() {
            return this.getDataSourceName();
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = new HashMap<>();
            eprops.put("supportFacade", this.supportFacade());
            eprops.put("facadeSourceTypes", this.facadeSourceTypes());
            return eprops;
        }

        /**
         * Get DB name
         *
         * @return
         */
        protected abstract String getDataSourceName();

        /**
         * Support facade datasource for incr process
         *
         * @return
         */
        protected abstract boolean supportFacade();

        protected abstract List<String> facadeSourceTypes();

    }
}
