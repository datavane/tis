/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.exec.datax;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.DataXCfgFile;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.IDataxGlobalCfg;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.datax.DBDataXChildTask;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.TransformerInfo;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteDumpTaskTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableInDB;
import com.qlangtech.tis.test.TISTestCase;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-04-21 13:06
 **/
public abstract class BasicDataXExecuteInterceptor extends TISTestCase {

    protected static final String AP_NAME = "testDataxProcessor";
    protected static File dataxCfgDir;
    protected static final String dbName = "order2";
    protected static final String tableName = "customer_order_relation";
    protected static final String dataCfgTaskName = tableName + "_1";
    protected static final String dataCfgFileName
            = dataCfgTaskName + DataXCfgFile.DATAX_CREATE_DATAX_CFG_FILE_NAME_SUFFIX;
    static final int testTaskId = 999;


    protected DataxReader mockDataXReader() {
        //testDataxProcessorDataXReader
        DataxReader dataxReader = mock(AP_NAME + "DataXReader", DataxReader.class);
        TableInDB tabInDB = TableInDB.create(DBIdentity.parseId(dbName));
        Mockito.when(dataxReader.getTablesInDB()).thenReturn(tabInDB);
        ISelectedTab tab = new TestSelectedTab(tableName);
        Mockito.when(dataxReader.getSelectedTabs()).thenReturn(Collections.singletonList(tab));

        DataxReader.dataxReaderGetter = (name) -> {
            return dataxReader;
        };
        return dataxReader;
    }

    protected IRemoteDumpTaskTrigger mockDataXExecTaskTrigger() {
        IRemoteDumpTaskTrigger jobTrigger = mock("remoteJobTrigger", IRemoteDumpTaskTrigger.class);
        Mockito.when(jobTrigger.getTaskName()).thenReturn(dataCfgFileName);

        //
        Mockito.when(jobTrigger.isAsyn()).thenReturn(false);
        Mockito.doThrow(new RuntimeException("throw a exception")).when(jobTrigger).run();
        return jobTrigger;
    }


    protected IExecChainContext mockExecChainContext(IDataxProcessor dataxProcessor) {
        IExecChainContext execChainContext = mock("execChainContext", IExecChainContext.class);
       // execChainContext.rebindLoggingMDCParams();
        Mockito.when(execChainContext.getPartitionTimestampWithMillis()).thenReturn(TimeFormat.getCurrentTimeStamp());
        Mockito.when(execChainContext.hasIndexName()).thenReturn(true);
        Mockito.when(execChainContext.getIndexName()).thenReturn(AP_NAME);
      //  Mockito.when(execChainContext.loadPhaseStatusFromLatest()).thenReturn(null);
        Mockito.when(execChainContext.getTaskId()).thenReturn(testTaskId);
        //  getTaskId

        Mockito.when(execChainContext.isDryRun()).thenReturn(false);

        Mockito.when(execChainContext.getProcessor()).thenReturn(dataxProcessor);

        Mockito.when(execChainContext.getAttribute(Mockito.eq(DataXJobSubmit.KEY_DATAX_READERS), Mockito.any()))
                .thenReturn(dataxProcessor.getReaders(null));

        return execChainContext;
    }

    protected DataXCfgGenerator.GenerateCfgs mockGenerateCfgs(File dataxCfgDir) throws IOException {

        DataXCfgGenerator.GenerateCfgs genCfg
                = new DataXCfgGenerator.GenerateCfgs(new MockDataxProcessor(), IPluginContext.namedContext(AP_NAME), dataxCfgDir);
        genCfg.setGenTime(System.currentTimeMillis());
        Map<String, List<DBDataXChildTask>> groupedChildTask = Maps.newHashMap();
        groupedChildTask.put(tableName, Lists.newArrayList(
                new DBDataXChildTask(DataXJobSubmit.TableDataXEntity.TEST_JDBC_URL, dbName, dataCfgTaskName)));
        genCfg.setGroupedChildTask(groupedChildTask);
        genCfg.write2GenFile(dataxCfgDir);

        try (InputStream res = TestDataXExecuteInterceptor.class.getResourceAsStream(dataCfgFileName)) {
            Objects.requireNonNull(res, dataCfgFileName + " can not be null");
            FileUtils.copyInputStreamToFile(res, new File(dataxCfgDir, dbName + "/" + dataCfgFileName));
        }
        return genCfg;
    }

    protected static class MockDataxProcessor extends DataxProcessor {
        @Override
        public Application buildApp() {
            return null;
        }

        @Override
        public String identityValue() {
            return TestDataXExecuteInterceptor.AP_NAME;
        }

        @Override
        public StoreResourceType getResType() {
            // throw new UnsupportedOperationException();
            return StoreResourceType.DataApp;
        }

        @Override
        public Set<TransformerInfo> getTransformerInfo(
                IPluginContext pluginCtx, Map<String, List<DBDataXChildTask>> groupedChildTask) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pair<List<RecordTransformerRules>, IPluginStore> getRecordTransformerRulesAndPluginStore(IPluginContext pluginCtx, String tableName) {
           throw new UnsupportedOperationException();
        }

        @Override
        public IDataxReader getReader(IPluginContext pluginContext, ISelectedTab tab) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IDataxGlobalCfg getDataXGlobalCfg() {
            return null;
        }
    }
}
