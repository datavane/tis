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

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-07 15:54
 **/
public class TestDataXJobConsumer extends TestCase {
    // TISTestCase {

    static {
        HttpUtils.addMockGlobalParametersConfig();
        Config.setTestDataDir();
    }

    /**
     * 需要执行 TestDistributedOverseerDataXJobSubmit.testPushMsgToDistributeQueue() 作为测试配合
     *
     * @throws Exception
     */
    public void testConsumeDistributeMsg() throws Exception {

//        String[] args = new String[]{"192.168.28.200:2181/tis/cloud", "/datax/jobs"};
//        DataXJobConsumer.main(args);

        DataXJobConsumer consumer = DataXJobConsumer.getDataXJobConsumer("/datax/jobs", "192.168.28.200:2181/tis/cloud");
        assertNotNull(consumer);

        // dataXName:ttt,jobid:866,jobName:customer_order_relation_0.json,jobPath:/opt/data/tis/cfg_repo/tis_plugin_config/ap/ttt/dataxCfg/customer_order_relation_0.json

        CuratorTaskMessage msg = new CuratorTaskMessage();
        msg.setDataXName("ttt");
        msg.setJobPath("/opt/data/tis/cfg_repo/tis_plugin_config/ap/ttt/dataxCfg/customer_order_relation_0.json");
        msg.setJobName("customer_order_relation_0.json");
        msg.setJobId(866);


        consumer.consumeMessage(msg);
    }
}
