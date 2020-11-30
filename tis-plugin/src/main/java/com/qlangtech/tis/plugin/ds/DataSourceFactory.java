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

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
public abstract class DataSourceFactory implements Describable<DataSourceFactory>, IdentityName {

    public static final String DS_TYPE_MYSQL = "MySQL";

    /**
     * Get all the tables in dataBase
     *
     * @return
     */
    public abstract List<String> getTablesInDB() throws Exception;

    /**
     * Get table column metaData list
     *
     * @param table
     * @return
     */
    public abstract List<ColumnMetaData> getTableMetadata(String table);

    /**
     * Support facade datasource for incr process
     *
     * @return
     */
    public abstract boolean supportFacade();

    public abstract List<String> facadeSourceTypes();

    /**
     * Create datasource for incr process
     *
     * @return
     */
    public abstract DataSource createFacadeDataSource();

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
    public abstract Iterator<IDataSourceDumper> getDataDumpers();

    @Override
    public Descriptor<DataSourceFactory> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
}
