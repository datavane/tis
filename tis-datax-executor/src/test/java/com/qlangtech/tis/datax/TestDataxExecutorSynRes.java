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

package com.qlangtech.tis.datax;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.PluginStubUtils;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-08 10:23
 **/
public class TestDataxExecutorSynRes extends TestCase implements IExecutorContext {


    @Override
    protected void setUp() throws Exception {
        System.clearProperty(Config.KEY_DATA_DIR);
        Config.setTestDataDir();
        HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();
        HttpUtils.addMockGlobalParametersConfig();
        PluginStubUtils.stubPluginConfig();
        PluginStubUtils.setTISField();
        TIS.clean();
        CenterResource.setFetchFromCenterRepository(false);
    }

    public void testSynchronizeDataXPluginsFromRemoteRepository() {
        DataxExecutor.synchronizeDataXPluginsFromRemoteRepository(dataXName, jobName);
        DataxProcessor dataxProcessor = IAppSource.load(null, dataXName);
        File dataxCfgDir = dataxProcessor.getDataxCfgDir(null);
        assertTrue(dataxCfgDir.getAbsolutePath(), dataxCfgDir.exists());
        File jobCfgFile = new File(dataxCfgDir, jobName);
        assertTrue("jobCfgFile must exist:" + jobCfgFile.getAbsolutePath(), jobCfgFile.exists());
    }

    /**
     * create DDL 下的文件是否同步过来
     */
    public void testSynchronizeCreateDDLFromRemoteRepository() {
        String dataX = "mysql_clickhouse";
        DataxExecutor.synchronizeDataXPluginsFromRemoteRepository(dataX, jobName);
        DataxProcessor dataxProcessor = IAppSource.load(null, dataX);
        File dataxCreateDDLDir = dataxProcessor.getDataxCreateDDLDir(null);
        List<String> synFiles = Lists.newArrayList(dataxCreateDDLDir.list((dir, name) -> !StringUtils.endsWith(name, CenterResource.KEY_LAST_MODIFIED_EXTENDION)));
        assertEquals(1, synFiles.size());
        String synFilesStr = synFiles.stream().collect(Collectors.joining(","));
        assertTrue(synFilesStr, synFiles.contains("customer_order_relation.sql"));
        //assertTrue(synFilesStr, synFiles.contains("instancedetail.sql"));
    }
}
