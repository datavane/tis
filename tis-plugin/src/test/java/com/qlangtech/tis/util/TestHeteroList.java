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
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.StoreResourceTypeConstants;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.ExtensionFinder;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 20:33
 */
public class TestHeteroList {

    public static final String DATAX_INSTANCE_NAME = "baisuitestTestcase";

    @Before public void setUp() throws Exception {

        Config.setDataDir(Config.DEFAULT_DATA_DIR);
        TIS.clean();
        CenterResource.setNotFetchFromCenterRepository();

        setTISField();


//        public static final File pluginCfgRoot = new File(Config.getMetaCfgDir(), KEY_TIS_PLUGIN_CONFIG);
//        public static final File pluginDirRoot = new File(Config.getLibDir(), KEY_TIS_PLUGIN_ROOT);
    }

    public static void setTISField() throws Exception {
        Field pluginCfgRootField = TIS.class.getField("pluginCfgRoot");
        setFinalStatic(pluginCfgRootField, new File(Config.getMetaCfgDir(), StoreResourceTypeConstants.KEY_TIS_PLUGIN_CONFIG));

        Field pluginDirRootField = TIS.class.getDeclaredField("pluginDirRoot");
        setFinalStatic(pluginDirRootField, new File(Config.getLibDir(), Config.KEY_TIS_PLUGIN_ROOT));

        Field finders = ClassicPluginStrategy.class.getField("finders");
        setFinalStatic(finders, Collections.singletonList(new ExtensionFinder.Sezpoz()));
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    @Test
    public void testSubFormFieldToJson() throws Exception {

        HeteroEnum dataxReader = HeteroEnum.DATAX_READER;
        String pluginMeta = dataxReader.identity + ":require," + UploadPluginMeta.PLUGIN_META_TARGET_DESCRIPTOR_NAME
                + "_MySQL," + SubFormFilter.PLUGIN_META_SUB_FORM_FIELD + "_selectedTabs," + StoreResourceType.DATAX_NAME + "_" + DATAX_INSTANCE_NAME;

        UploadPluginMeta meta = UploadPluginMeta.parse(pluginMeta);
        IPluginContext pluginContext = Mockito.mock(IPluginContext.class, Mockito.withSettings().name("pluginContext"));
//        Mockito.when(pluginContext.isCollectionAware()).thenReturn(false);
//        Mockito.when(pluginContext.isDataSourceAware()).thenReturn(false);
        Mockito.when(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).thenReturn("/x/" + DATAX_INSTANCE_NAME + "/update");

        HeteroList<?> hlist = meta.getHeteroList(pluginContext);

        JSONObject j = hlist.toJSON();
        assertNotNull(j);
        //System.out.println();

        JsonUtil.assertJSONEqual(TestHeteroList.class, "dataxReader.assert.json"
                , JsonUtil.toString(j), (msg, expect, actual) -> {
                    assertEquals(msg, expect, actual);
                });


        Mockito.verify(pluginContext, Mockito.times(2)).getRequestHeader(DataxReader.HEAD_KEY_REFERER);
    }
}
