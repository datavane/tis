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

package com.qlangtech.tis.plugin;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.XStream2;
import junit.framework.TestCase;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-04-05 07:04
 **/
public class TestPluginAndCfgsSnapshot extends TestCase {
    TargetResName datax = new TargetResName("hudi");

    public void testGetLocalPluginAndCfgsSnapshot() {

        XStream2.PluginMeta flinkPluginMeta
                = new XStream2.PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + datax.getName()
                , Config.getMetaProps().getVersion());
        PluginAndCfgsSnapshot snapshot = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, flinkPluginMeta);
        Assert.assertNotNull(snapshot);

        Assert.assertEquals(datax.getName(), snapshot.getAppName().getName());


        Set<XStream2.PluginMeta> pluginMetas = snapshot.pluginMetas;
        for (XStream2.PluginMeta meta : pluginMetas) {
            meta.getLastModifyTimeStamp();
            System.out.println(meta.toString());

        }
    }

    public void testShallBeUpdateTpis() {
        XStream2.PluginMeta flinkPluginMeta
                = new XStream2.PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + datax.getName()
                , Config.getMetaProps().getVersion());
        PluginAndCfgsSnapshot remote = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, flinkPluginMeta);
        Assert.assertNotNull(remote);

        Config.setTestDataDir();
        TIS.clean();

        PluginAndCfgsSnapshot local = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, flinkPluginMeta);
        Assert.assertNotNull(local);

        Set<XStream2.PluginMeta> pluginMetas = remote.shallBeUpdateTpis(local);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pluginMetas));
    }

}
