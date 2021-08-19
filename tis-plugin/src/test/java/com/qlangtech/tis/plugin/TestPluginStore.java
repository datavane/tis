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
