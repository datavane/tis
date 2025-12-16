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

package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import junit.framework.TestCase;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: 百岁(baisui@qlangtech.com)
 * @create: 2025-12-15
 */
public class TriggerBuildResultTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Clean up any existing mock connections
//        if (HttpUtils.mockConnMaker != null) {
//            HttpUtils.mockConnMaker.clearStubs();
//        }
    }

    /**
     * Test successful trigger build with valid response
     */
    public void testTriggerBuildSuccess() throws Exception {
        // Prepare mock response JSON
        JSONObject bizResult = new JSONObject();
        bizResult.put(JobCommon.KEY_TASK_ID, "12345");

        JSONObject mockResponse = new JSONObject();
        mockResponse.put("success", true);
        mockResponse.put("biz", bizResult);

        // Setup mock HTTP connection
//        HttpUtils.addMockApply(0, TriggerBuildResult.TRIGGER_FULL_BUILD_COLLECTION_PATH,
//                new HttpUtils.IClasspathRes() {
//                    @Override
//                    public InputStream getResourceAsStream(java.net.URL url) {
//                        return new ByteArrayInputStream(
//                                mockResponse.toString().getBytes(StandardCharsets.UTF_8)
//                        );
//                    }
//
//                    @Override
//                    public Map<String, List<String>> headerFields() {
//                        return Collections.emptyMap();
//                    }
//                });

        // Prepare test parameters
        List<HttpUtils.PostParam> params = Lists.newArrayList(
                new HttpUtils.PostParam(TriggerBuildResult.KEY_APPNAME, "test_app")
        );

        // Mock control message handler
        IControlMsgHandler mockHandler = IControlMsgHandler.namedContext("test_app");

        // Execute the method under test
        TriggerBuildResult result = TriggerBuildResult.triggerBuild(
                mockHandler,
                null,
                ConfigFileContext.HTTPMethod.POST,
                params,
                Collections.emptyList()
        );

        // Assertions
        Assert.assertNotNull("Trigger result should not be null", result);
        Assert.assertTrue("Trigger should be successful", result.success);
        Assert.assertEquals("Task ID should match", 12345, result.getTaskid());
    }

    @Override
    protected void tearDown() throws Exception {
        // Clean up mock connections
//        if (HttpUtils.mockConnMaker != null) {
//            HttpUtils.mockConnMaker.clearStubs();
//        }
        super.tearDown();
    }
}