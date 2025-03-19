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

package com.qlangtech.tis.exec;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.plugin.PluginAndCfgsSnapshot;
import junit.framework.TestCase;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-16 15:36
 **/
public class TestIExecChainContext extends TestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
        CenterResource.setNotFetchFromCenterRepository();
    }

    public void testCreateInstanceParams() {
        int taskId = 999;
        String pipeline = "mysql5";
        IDataxProcessor processor = DataxProcessor.load(null, pipeline);
        JSONObject instanceParams = IExecChainContext.createInstanceParams(taskId, processor, false, Optional.empty());
        Assert.assertNotNull(instanceParams);

        PluginAndCfgsSnapshot snapshot = AbstractExecContext.resolveCfgsSnapshotConsumer(instanceParams);
        Assert.assertNotNull(snapshot);
    }
}
