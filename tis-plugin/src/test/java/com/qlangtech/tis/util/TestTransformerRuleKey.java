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

package com.qlangtech.tis.util;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.plugin.KeyedPluginStore.Key;
import com.qlangtech.tis.datax.StoreResourceType;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-04-08 15:02
 **/
public class TestTransformerRuleKey extends TestCase {

    public void testCreateStoreKey() {

        Map<Key, String> key2TableMapper = new HashMap<>();
        IPluginContext pluginContext = IPluginContext.namedContext("test");
        String tabOrderDetail = "orderdetail";
        String tabTotalpay = "totalpay";
        DataXName dataX = pluginContext.getCollectionName();
        Key storeKeyOrderDetail = TransformerRuleKey.createStoreKey(
                pluginContext, dataX.getType(), dataX.getPipelineName(), tabOrderDetail);
        Key storeKeyTotalpay = TransformerRuleKey.createStoreKey(
                pluginContext, dataX.getType(), dataX.getPipelineName(), tabTotalpay);

        key2TableMapper.put(storeKeyOrderDetail, tabOrderDetail);
        key2TableMapper.put(storeKeyTotalpay, tabTotalpay);

        Assert.assertEquals("test/transformer", storeKeyOrderDetail.keyVal.getKeyVal());
        Assert.assertEquals("test/transformer", storeKeyTotalpay.keyVal.getKeyVal());

        Assert.assertEquals(tabOrderDetail, key2TableMapper.get(storeKeyOrderDetail));
        Assert.assertEquals(tabTotalpay, key2TableMapper.get(storeKeyTotalpay));
    }
}