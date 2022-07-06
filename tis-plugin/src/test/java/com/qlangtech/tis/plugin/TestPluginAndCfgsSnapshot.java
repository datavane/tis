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

import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.incr.TISSinkFactory;
import com.qlangtech.tis.util.PluginMeta;
import junit.framework.TestCase;

import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-04-05 07:04
 **/
public class TestPluginAndCfgsSnapshot extends TestCase {
    TargetResName datax = new TargetResName("hudi");

    @Override
    protected void setUp() throws Exception {
        // super.setUp();
        CenterResource.setNotFetchFromCenterRepository();
    }

    public void testGetLocalPluginAndCfgsSnapshot() {

        PluginMeta flinkPluginMeta
                = new PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + datax.getName()
                , Config.getMetaProps().getVersion(), Optional.empty(), null);
        PluginAndCfgsSnapshot snapshot = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, Optional.empty(), flinkPluginMeta);
        Assert.assertNotNull(snapshot);

        Assert.assertEquals(datax.getName(), snapshot.getAppName().getName());

        Set<String> addedPlugins = Sets.newHashSet();
        Set<PluginMeta> pluginMetas = snapshot.pluginMetas;
        for (PluginMeta meta : pluginMetas) {

            if (!meta.getPluginName().startsWith(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH)) {
                Assert.assertTrue("meta(" + meta.toString() + ").getLastModifyTimeStamp:" + meta.getLastModifyTimeStamp() + " must large than 1"
                        , meta.getLastModifyTimeStamp() > 0);
            }

            System.out.println(meta.toString());
            if (!addedPlugins.add(meta.getPluginName())) {
                Assert.fail("plugin:" + meta.getPluginName() + " has been add twice");
            }
        }

        Manifest manifest = new Manifest();

        snapshot.attachPluginCfgSnapshot2Manifest(manifest);
        Attributes metas = manifest.getAttributes(Config.KEY_PLUGIN_METAS);

        String metaVal = metas.getValue(new Attributes.Name(KeyedPluginStore.PluginMetas.KEY_PLUGIN_META));
        System.out.println("------------------------");
        System.out.println(metaVal);

        snapshot.getPluginNames();

        PluginAndCfgsSnapshot sn = PluginAndCfgsSnapshot.deserializePluginAndCfgsSnapshot(datax, manifest);
        assertNotNull(sn);
    }

    public void testShallBeUpdateTpis() {
        PluginMeta flinkPluginMeta
                = new PluginMeta(TISSinkFactory.KEY_PLUGIN_TPI_CHILD_PATH + datax.getName()
                , Config.getMetaProps().getVersion(), Optional.empty());
        PluginAndCfgsSnapshot remote = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, Optional.empty(), flinkPluginMeta);
        Assert.assertNotNull(remote);

        Config.setTestDataDir();
        TIS.clean();

        PluginAndCfgsSnapshot local = PluginAndCfgsSnapshot.getLocalPluginAndCfgsSnapshot(datax, Optional.empty(), flinkPluginMeta);
        Assert.assertNotNull(local);

//        Set<XStream2.PluginMeta> pluginMetas = remote.shallBeUpdateTpis(local);
//        Assert.assertTrue(CollectionUtils.isNotEmpty(pluginMetas));
    }

}
