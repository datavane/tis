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

package com.qlangtech.tis.plugin.datax.transformer;

import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.util.impl.DefaultGroovyShellFactory;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.datax.transformer.impl.TestCopyValUDF;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Assert;

import java.util.Optional;

import static com.qlangtech.tis.util.HeteroEnum.TRANSFORMER_RULES;

/**
 *
 */
public class TestRecordTransformerRules extends TestCase {

    public static <T extends Describable> void assertDesc(Class<T> clazz, String assertFileName) {
        try {
            T plugin = clazz.newInstance();// RecordTransformerRules.class.newInstance();
// DescriptorsJSON descJson = new DescriptorsJSON(plugin.getDescriptor());
            JsonUtil.assertJSONEqual(clazz, assertFileName
                    , JsonUtil.toString(DescriptorsJSON.desc(plugin.getDescriptor())), (m, e, a) -> {
                        Assert.assertEquals(m, e, a);
                    });
            //return plugin;
        } catch (Exception e) {
            throw new RuntimeException(assertFileName, e);
        }
    }

    public void testCleanPluginStoreCache() {
        DataXName dataX = DataXName.createDataXPipeline("test");
        IPluginContext pluginContext = IPluginContext.namedContext(dataX);

        UploadPluginMeta pluginMeta
                = UploadPluginMeta.parse("transformer:require,dataxName_" + dataX.getPipelineName() + ",id_orderdetail");

        IPluginStore pluginStore
                = TRANSFORMER_RULES.getPluginStore(pluginContext, pluginMeta);
        TestCopyValUDF cpUDF = TestCopyValUDF.create();
        RecordTransformerRules rules = RecordTransformerRules.create(cpUDF);
        pluginStore.setPlugins(pluginContext, Optional.empty(), Lists.newArrayList(new ParseDescribable(rules)));

        int clearCount = RecordTransformerRules.cleanPluginStoreCache(pluginContext, dataX);
        Assert.assertEquals(1, clearCount);
    }


    public void testDescJsonGen() {
        DefaultGroovyShellFactory.setInConsoleModule();
        assertDesc(RecordTransformerRules.class, "record-transformer-rules-descriptor.json");
    }


}
