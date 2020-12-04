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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DataSourceFactoryPluginStore extends KeyedPluginStore<DataSourceFactory> {
    private final boolean shallUpdateDB;

    public DataSourceFactoryPluginStore(Key key, boolean shallUpdateDB) {
        super(key);
        this.shallUpdateDB = shallUpdateDB;
    }

    @Override
    public void copyConfigFromRemote() {
        List<String> subFiles
                = CenterResource.getSubFiles(
                        TIS.KEY_TIS_PLUGIN_CONFIG + File.separator + this.key.getSubDirPath(), false, true);
        for (String f : subFiles) {
            CenterResource.copyFromRemote2Local(
                    TIS.KEY_TIS_PLUGIN_CONFIG + File.separator + this.key.getSubDirPath() + File.separator+f, true);
        }
    }

    @Override
    public synchronized boolean setPlugins(Optional<Context> context, List<Descriptor.ParseDescribable<DataSourceFactory>> dlist, boolean update) {
        if (!context.isPresent()) {
            throw new IllegalArgumentException("Context shall exist");
        }

        if (shallUpdateDB && !update) {
            // shall skip in update model
            Context ctx = context.get();
            String dbName = this.key.keyVal;
            // 对数据库记录进行添加
            pluginContext.addDb(dbName, ctx);
            if (ctx.hasErrors()) {
                return false;
            }
        }

        return super.setPlugins(context, dlist, update);
    }


    /**
     * Save the table metadata info which will be used in dataflow define process
     *
     * @param tableName
     * @throws Exception
     */
    public void saveTable(String tableName) throws Exception {
        List<ColumnMetaData> colMetas = this.getPlugin().getTableMetadata(tableName);
        StringBuffer extractSQL = ColumnMetaData.buildExtractSQL(tableName, colMetas);
        XmlFile configFile = getTableReflectSerializer(tableName);

        TableReflect reflectTab = new TableReflect();
        reflectTab.setSql(extractSQL.toString());
        reflectTab.setCols(colMetas);
        configFile.write(reflectTab, Collections.emptySet());
    }

    private XmlFile getTableReflectSerializer(String tableName) {
        String dbRoot = StringUtils.substringBeforeLast(this.getSerializeFileName(), File.separator);
        return Descriptor.getConfigFile(dbRoot + File.separator + tableName);
    }

    public TISTable loadTableMeta(String tableName) {
        try {
            TISTable tisTable = new TISTable();
            XmlFile tableReflectSerializer = this.getTableReflectSerializer(tableName);
            if (!tableReflectSerializer.exists()) {
                throw new IllegalStateException("file is not exist:" + tableReflectSerializer.getFile());
            }
            TableReflect tableMeta = (TableReflect) tableReflectSerializer.read();
            tisTable.setReflectCols(tableMeta.getCols());
            tisTable.setSelectSql(tableMeta.getSql());
            tisTable.setTableName(tableName);
            tisTable.setTableLogicName(tableName);
            tisTable.setDbName(this.key.keyVal);
            return tisTable;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
