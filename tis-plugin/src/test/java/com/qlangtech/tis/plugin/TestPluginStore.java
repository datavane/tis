/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.plugin;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.util.XStream2;
import junit.framework.TestCase;

import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-03 09:04
 */
public class TestPluginStore extends TestCase {

    private static final String VALUE_PROP_1 = "prop1-1";

    private static final String VALUE_PROP_2 = "prop2-1";

    @Override
    protected void setUp() throws Exception {
        CenterResource.setNotFetchFromCenterRepository();
        HttpUtils.addMockGlobalParametersConfig();
    }

    public void testTableDumpFactory() {

       // assertFalse(TIS.initialized);
        PluginStore<TestPlugin> pstore = new PluginStore<>(TestPlugin.class);
        TestPlugin p = new TestPlugin();
        p.prop1 = VALUE_PROP_1;
        p.prop2 = VALUE_PROP_2;
        List<Descriptor.ParseDescribable<TestPlugin>> dlist = Lists.newArrayList();
        Descriptor.ParseDescribable parseDescribable = new Descriptor.ParseDescribable(p);
        parseDescribable.extraPluginMetas.add(new XStream2.PluginMeta("testmeta", "1.0.0"));
        dlist.add(parseDescribable);
        pstore.setPlugins(null, Optional.empty(), dlist);
        pstore.cleanPlugins();
        List<TestPlugin> plugins = pstore.getPlugins();
        assertEquals(1, plugins.size());
        TestPlugin plugin = pstore.getPlugin();
        assertNotNull(plugin);
        assertEquals(VALUE_PROP_1, plugin.prop1);
        assertEquals(VALUE_PROP_2, plugin.prop2);
    }
}
