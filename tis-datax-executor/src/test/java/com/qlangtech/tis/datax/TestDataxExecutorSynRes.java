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
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-08 10:23
 **/
public class TestDataxExecutorSynRes extends TestCase implements IExecutorContext {
    static {
        Config.setTestDataDir();
        HttpUtils.addMockGlobalParametersConfig();
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
        List<String> synFiles = Lists.newArrayList(dataxCreateDDLDir.list());
        assertTrue(synFiles.contains("customer_order_relation.sql"));
        assertTrue(synFiles.contains("instancedetail.sql"));
    }
}
