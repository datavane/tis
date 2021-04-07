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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.compiler.streamcode.IDBTableNamesGetter;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.taskflow.DataflowTask;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.sql.parser.DBNode;
import com.qlangtech.tis.sql.parser.er.*;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.meta.TabExtraMeta;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.IEntityNameGetter;
import com.qlangtech.tis.sql.parser.tuple.creator.IValChain;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-31 11:20
 */
public class SingleTableAppSource implements IAppSource {
    private final DatasourceDb db;
    private final Integer tabId;
    private final String tabName;

    public SingleTableAppSource(DatasourceDb db, Integer tabId, String tabName) {
        this.db = db;
        this.tabId = tabId;
        this.tabName = tabName;
    }

    @Override
    public List<ColumnMetaData> reflectCols() {

        DataSourceFactoryPluginStore dataBasePluginStore = TIS.getDataBasePluginStore(new PostedDSProp(db.getName()));
        TISTable table = dataBasePluginStore.loadTableMeta(tabName);
//    table.getReflectCols().stream().map((c)->{
//      ColName cname = new ColName(c.getKey());
//
//    });
        return table.getReflectCols();
    }

    @Override
    public ExecuteResult getProcessDataResults(IExecChainContext execChainContext, ISingleTableDumpFactory singleTableDumpFactory
            , IDataProcessFeedback dataProcessFeedback, ITaskPhaseInfo taskPhaseInfo) throws Exception {
        // 复杂数据导出

        DumpPhaseStatus dumpPhaseStatus = taskPhaseInfo.getPhaseStatus(execChainContext, FullbuildPhase.FullDump);
        DataflowTask tabDump = null;

        DependencyNode dump = new DependencyNode();
        dump.setId(db.getName() + "." + tabName);
        dump.setName(tabName);
        dump.setDbName(db.getName());
        dump.setTabid(String.valueOf(tabId));
        dump.setDbid(String.valueOf(db.getId()));

        //for (DependencyNode dump : topology.getDumpNodes()) {
        tabDump = singleTableDumpFactory.createSingleTableDump(dump, false, /* isHasValidTableDump */
                "tableDump.getPt()", execChainContext.getZkClient(), execChainContext, dumpPhaseStatus);

        tabDump.run();

        return ExecuteResult.SUCCESS;
    }

    @Override
    public IPrimaryTabFinder getPrimaryTabFinder() {
        return new DataFlowAppSource.DftTabFinder();
    }

    @Override
    public EntityName getTargetEntity() {
        return EntityName.parse(db.getName() + "." + this.tabName);
    }

    @Override
    public boolean triggerFullIndexSwapeValidate(IMessageHandler msgHandler, Context ctx) {
        return true;
    }


    @Override
    public Map<IEntityNameGetter, List<IValChain>> getTabTriggerLinker() {
        return Collections.emptyMap();
    }

    @Override
    public Map<DBNode, List<String>> getDependencyTables(IDBTableNamesGetter dbTableNamesGetter) {
        return Collections.emptyMap();
    }

    @Override
    public IERRules getERRule() {
        return new SingleTableErRule();
    }

    private class SingleTableErRule implements IERRules {
        @Override
        public List<PrimaryTableMeta> getPrimaryTabs() {
            TabExtraMeta tabExtraMeta = new TabExtraMeta();
            tabExtraMeta.setPrimaryIndexTab(true);

            PrimaryTableMeta tableMeta = new PrimaryTableMeta(tabName, tabExtraMeta);

            return Collections.singletonList(tableMeta);
        }

        @Override
        public boolean isTriggerIgnore(EntityName entityName) {
            return false;
        }

        @Override
        public List<TableRelation> getAllParent(EntityName entityName) {
            return Collections.emptyList();
        }

        @Override
        public List<TableRelation> getChildTabReference(EntityName entityName) {
            return Collections.emptyList();
        }

        @Override
        public Optional<TableMeta> getPrimaryTab(IDumpTable entityName) {
            return Optional.empty();
        }

        @Override
        public boolean hasSetTimestampVerColumn(EntityName entityName) {
            return false;
        }

        @Override
        public TimeCharacteristic getTimeCharacteristic() {
            return TimeCharacteristic.ProcessTime;
        }

        @Override
        public boolean isTimestampVerColumn(EntityName entityName, String name) {
            return false;
        }

        @Override
        public String getTimestampVerColumn(EntityName entityName) {
            return null;
        }

        @Override
        public List<TabFieldProcessor> getTabFieldProcessors() {
            return Collections.emptyList();
        }

        @Override
        public Optional<TableRelation> getFirstParent(String tabName) {
            return Optional.empty();
        }

        @Override
        public Optional<PrimaryTableMeta> isPrimaryTable(String tabName) {
            return Optional.empty();
        }
    }
}
