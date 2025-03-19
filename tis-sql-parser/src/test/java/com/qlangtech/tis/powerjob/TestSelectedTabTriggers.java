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

package com.qlangtech.tis.powerjob;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.CuratorDataXTaskMessage;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPostTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.ds.DefaultTab;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.test.TISEasyMock;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/12
 */
public class TestSelectedTabTriggers extends TestCase implements TISEasyMock {

    public void testDeserialize() {
        JSONObject dataXJobCfg = IOUtils.loadResourceFromClasspath(
                TestSelectedTabTriggers.class, "datax-job-cfg.json", true, (input) -> {
                    return JSON.parseObject(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
                });

        SelectedTabTriggersConfig
                triggersCfg = SelectedTabTriggers.deserialize(dataXJobCfg);

        Assert.assertEquals("mysql_aliyun_hive", triggersCfg.getDataXName());
        Assert.assertEquals("payinfo", triggersCfg.getTabName());


        Assert.assertEquals("prep_payinfo", triggersCfg.getPreTrigger());
        Assert.assertEquals("hive_payinfo_bind", triggersCfg.getPostTrigger());
        List<CuratorDataXTaskMessage> splitTabsCfg = triggersCfg.getSplitTabsCfg();
        Assert.assertEquals(1, splitTabsCfg.size());

        for (CuratorDataXTaskMessage msg : splitTabsCfg) {
            
            Assert.assertEquals("{\"allRowsApproximately\":-1,\"dataXName\":\"mysql_aliyun_hive\",\"execEpochMilli\":0,\"jobId\":-1,\"jobName\":\"payinfo_0.json/order2/payinfo\",\"resType\":\"DataApp\",\"taskSerializeNum\":1}",
                    CuratorDataXTaskMessage.serialize(msg));
        }

    }

    public void testSerialize() {
        String tableName = "payinfo";
        String dataXName = "mysql_aliyun_hive";
        ISelectedTab entry = new DefaultTab(tableName);
        IDataxProcessor appSource = mock("appSource", IDataxProcessor.class);
        EasyMock.expect(appSource.identityValue()).andReturn(dataXName);

        IRemoteTaskPreviousTrigger preTrigger = mock("preTrigger", IRemoteTaskPreviousTrigger.class);
        EasyMock.expect(preTrigger.getTaskName()).andReturn("prep_payinfo");
        IRemoteTaskPostTrigger postTrigger = mock("postTrigger", IRemoteTaskPostTrigger.class);
        EasyMock.expect(postTrigger.getTaskName()).andReturn("hive_payinfo_bind");
        SelectedTabTriggers tabTriggers = new SelectedTabTriggers(entry, appSource);
        tabTriggers.setPreTrigger(preTrigger);
        tabTriggers.setPostTrigger(postTrigger);

        DataXJobInfo dataXJobInfo = DataXJobInfo.parse("payinfo_0.json/order2/payinfo");
        CuratorDataXTaskMessage tskMsg = CuratorDataXTaskMessage.deserialize("{\n" +
                "\t\t\"jobId\":-1,\n" +
                "\t\t\"execEpochMilli\":0,\n" +
                "\t\t\"resType\":\"DataApp\",\n" +
                "\t\t\"dataXName\":\"mysql_aliyun_hive\",\n" +
                "\t\t\"taskSerializeNum\":1,\n" +
                "\t\t\"allRowsApproximately\":-1\n" +
                "\t}");

        SelectedTabTriggers.PowerJobRemoteTaskTrigger splitTabTrigger
                = new SelectedTabTriggers.PowerJobRemoteTaskTrigger(dataXJobInfo, tskMsg);

        tabTriggers.setSplitTabTriggers(Collections.singletonList(splitTabTrigger));

        replay();
        //  System.out.println(JsonUtil.toString(tabTriggers.createMRParams()));

        JsonUtil.assertJSONEqual(TestSelectedTabTriggers.class, "datax-job-cfg-assert.json", tabTriggers.createMRParams()
                , (message, expected, actual) -> {
                    Assert.assertEquals(message, expected, actual);
                });

        verifyAll();
    }

}
