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

package com.qlangtech.tis.plugin.datax.doris;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.thoughtworks.xstream.core.MapBackedDataHolder;
import junit.framework.TestCase;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/2
 */
public class TestDorisSelectedTab extends TestCase {

    public void testMultiChildrenInstanceLoad() {

        PluginsCollector pluginsCollector = loadSelectedTab("OnSeqKey");

        Assert.assertNotNull(pluginsCollector);
        Assert.assertEquals(1, pluginsCollector.getPlugins().size());
        SelectedTab tab = pluginsCollector.getPlugins().get(0);

        TIS.clean();

        pluginsCollector = loadSelectedTab("OffSeqKey");
        Assert.assertNotNull(pluginsCollector);
        Assert.assertEquals(1, pluginsCollector.getPlugins().size());
        tab = pluginsCollector.getPlugins().get(0);

    }

    private static PluginsCollector loadSelectedTab(String resType) {

        MapBackedDataHolder holder = new MapBackedDataHolder();

        PluginsCollector pluginsCollector = IOUtils.loadResourceFromClasspath(TestDorisSelectedTab.class //
                , "TestDorisSelectedTab_" + resType + ".xml", true //
                , (input) -> {
                    Object obj = XmlFile.DEFAULT_XSTREAM.unmarshal( //
                            XmlFile.DEFAULT_DRIVER.createReader(input), new PluginsCollector(), holder);
                    return (PluginsCollector) obj;
                });
        PluginStore.processError(holder);
        return pluginsCollector;
    }
}
