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
package com.qlangtech.tis.manage.impl;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.er.*;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.io.File;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-31 11:20
 */
public class DataFlowAppSource implements IAppSource {
    public static final String NAME_APP_DIR = "ap";
    public static final File parent = new File(Config.getMetaCfgDir(), NAME_APP_DIR);
    private final String dataflowName;

    public static void save(String collection, IAppSource appSource) {
        KeyedPluginStore<IAppSource> pluginStore = new KeyedPluginStore(new AppKey(collection));
        Optional<Context> context = Optional.empty();
        pluginStore.setPlugins(null, context, Collections.singletonList(new Descriptor.ParseDescribable(appSource)));
    }

    public static IAppSource load(String collection) {
        KeyedPluginStore<IAppSource> pluginStore = new KeyedPluginStore(new AppKey(collection));
        return pluginStore.getPlugin();
    }

    public static class AppKey extends KeyedPluginStore.Key<IAppSource> {
        public AppKey(String collection) {
            super(NAME_APP_DIR, collection, IAppSource.class);
        }

//    @Override
//    public int hashCode() {
//      return Objects.hash(this.keyVal, this.dbScope, pluginClass);
//    }
    }

    @Override
    public IPrimaryTabFinder getPrimaryTabFinder() {
        Optional<ERRules> erRule = ERRules.getErRule(dataflowName);
        IPrimaryTabFinder pTabFinder = null;
        if (!erRule.isPresent()) {
            pTabFinder = new DftTabFinder();
        } else {
            pTabFinder = erRule.get();
        }
        return pTabFinder;
    }


    private static class DftTabFinder implements IPrimaryTabFinder {
        @Override
        public Optional<TableMeta> getPrimaryTab(IDumpTable entityName) {
            return Optional.empty();
        }

        @Override
        public final Map<EntityName, TabFieldProcessor> getTabFieldProcessorMap() {
            //throw new UnsupportedOperationException();
            return Collections.emptyMap();
        }
    }

    @Override
    public EntityName getTargetEntity() {

        try {
            SqlTaskNodeMeta.SqlDataFlowTopology workflowDetail = SqlTaskNodeMeta.getSqlDataFlowTopology(dataflowName);
            Objects.requireNonNull(workflowDetail, "workflowDetail can not be null");
            EntityName targetEntity = null;
            if (workflowDetail.isSingleTableModel()) {
                DependencyNode dumpNode = workflowDetail.getDumpNodes().get(0);
                targetEntity = dumpNode.parseEntityName();
            } else {
                SqlTaskNodeMeta finalN = workflowDetail.getFinalNode();
                targetEntity = EntityName.parse(finalN.getExportName());
            }
            return targetEntity;
        } catch (Exception e) {
            throw new RuntimeException(dataflowName, e);
        }
    }

    public DataFlowAppSource(String dataflowName) {
        this.dataflowName = dataflowName;
    }

    @Override
    public List<ColumnMetaData> reflectCols() {
        try {
            SqlTaskNodeMeta.SqlDataFlowTopology dfTopology = SqlTaskNodeMeta.getSqlDataFlowTopology(dataflowName);
            return dfTopology.getFinalTaskNodeCols();
        } catch (Exception e) {
            throw new RuntimeException("dataflowName:" + dataflowName, e);
        }
    }


    @Override
    public boolean triggerFullIndexSwapeValidate(IMessageHandler module, Context context) {
        try {
            SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(dataflowName);
            Objects.requireNonNull(topology, "topology:" + dataflowName + " relevant topology can not be be null");

            Optional<ERRules> erRule = ERRules.getErRule(dataflowName);// module.getErRules(dataflowName);
            if (!topology.isSingleTableModel()) {
                if (!erRule.isPresent()) {
                    module.addErrorMessage(context, "请为数据流:[" + dataflowName + "]定义ER Rule");
                    return false;
                } else {
                    ERRules erRules = erRule.get();
                    List<PrimaryTableMeta> pTabs = erRules.getPrimaryTabs();
                    Optional<PrimaryTableMeta> prTableMeta = pTabs.stream().findFirst();
                    if (!TableMeta.hasValidPrimayTableSharedKey(prTableMeta.isPresent() ? Optional.of(prTableMeta.get()) : Optional.empty())) {
                        module.addErrorMessage(context, "请为数据流:[" + dataflowName + "]定义ERRule 选择主表并且设置分区键");
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(this.dataflowName, e);
        }
    }
}
