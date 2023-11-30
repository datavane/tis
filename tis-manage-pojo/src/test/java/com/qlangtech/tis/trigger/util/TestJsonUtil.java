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

package com.qlangtech.tis.trigger.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.impl.IOUtils;
import junit.framework.TestCase;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/29
 */
public class TestJsonUtil extends TestCase {

    public void testEquals() {

        JSONObject desc1 = JSONObject.parseObject(IOUtils.loadResourceFromClasspath(TestJsonUtil.class, "payinfo-desc1.json"));
        JSONObject desc2 = JSONObject.parseObject(IOUtils.loadResourceFromClasspath(TestJsonUtil.class, "payinfo-desc2.json"));

        Assert.assertFalse(JsonUtil.objEquals(desc1, desc2, Sets.newHashSet()));

        Assert.assertFalse(JsonUtil.objEquals(desc1, desc2, Sets.newHashSet("/exec/taskSerializeNum")));

        Assert.assertFalse(JsonUtil.objEquals(desc1, desc2, Sets.newHashSet("/exec/jobInfo[]/taskSerializeNum")));

        Assert.assertTrue(JsonUtil.objEquals(desc1, desc2, Sets.newHashSet("/exec/taskSerializeNum","/exec/jobInfo[]/taskSerializeNum")));

    }
}
