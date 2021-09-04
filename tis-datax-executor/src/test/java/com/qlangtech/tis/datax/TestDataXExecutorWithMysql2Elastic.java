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

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.qlangtech.tis.manage.common.Config;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-03 16:34
 **/
public class TestDataXExecutorWithMysql2Elastic extends BasicDataXExecutorTestCase {

    private boolean hasExecuteStartEngine;
    final Integer jobId = 123;
    final String jobName = "instancedetail_0.json";
    final String dataxName = "mysql_elastic";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.hasExecuteStartEngine = false;
    }

    /**
     * 测试配置文件和plugin是否正确下载
     */
    public void testResourceSync() throws Exception {

        this.executor.exec(jobId, jobName, dataxName);
        assertTrue("hasExecuteStartEngine", hasExecuteStartEngine);
    }

    @Override
    protected void setDataDir() {
        Config.setTestDataDir();

       // Config.setDataDir("/tmp/tis");
    }

    @Override
    protected boolean isNotFetchFromCenterRepository() {
        return false;
    }

    protected DataxExecutor createExecutor() {
        return new DataxExecutor(statusRpc, DataXJobSubmit.InstanceType.LOCAL, 300) {
            @Override
            protected void startEngine(Configuration configuration, Integer jobId, String jobName) {
                //  make skip the ex

                int jobSleepIntervalInMillSec = configuration.getInt(
                        CoreConstant.DATAX_CORE_CONTAINER_JOB_SLEEPINTERVAL, 10000);
                assertEquals(3000, jobSleepIntervalInMillSec);
                hasExecuteStartEngine = true;
            }
        };
    }

}
