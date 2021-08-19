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
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.ExtensionFinder;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 20:33
 */
public class TestHeteroList extends TestCase {

    public static final String DATAX_INSTANCE_NAME = "baisuitestTestcase";

    @Override
    public void setUp() throws Exception {

        Config.setDataDir(Config.DEFAULT_DATA_DIR);
        TIS.clean();
        CenterResource.setNotFetchFromCenterRepository();

        setTISField();


//        public static final File pluginCfgRoot = new File(Config.getMetaCfgDir(), KEY_TIS_PLUGIN_CONFIG);
//        public static final File pluginDirRoot = new File(Config.getLibDir(), KEY_TIS_PLUGIN_ROOT);
    }

    public static void setTISField() throws Exception {
        Field pluginCfgRootField = TIS.class.getField("pluginCfgRoot");
        setFinalStatic(pluginCfgRootField, new File(Config.getMetaCfgDir(), TIS.KEY_TIS_PLUGIN_CONFIG));

        Field pluginDirRootField = TIS.class.getDeclaredField("pluginDirRoot");
        setFinalStatic(pluginDirRootField, new File(Config.getLibDir(), TIS.KEY_TIS_PLUGIN_ROOT));

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

    public void testSubFormFieldToJson() throws Exception {

        HeteroEnum dataxReader = HeteroEnum.DATAX_READER;
        String pluginMeta = dataxReader.identity + ":require," + IPropertyType.SubFormFilter.PLUGIN_META_TARGET_DESCRIPTOR_NAME
                + "_MySQL," + IPropertyType.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD + "_selectedTabs," + DataxUtils.DATAX_NAME + "_" + DATAX_INSTANCE_NAME;

        UploadPluginMeta meta = UploadPluginMeta.parse(pluginMeta);
        IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
//        EasyMock.expect(pluginContext.isCollectionAware()).andReturn(false);
//        EasyMock.expect(pluginContext.isDataSourceAware()).andReturn(false);
        EasyMock.expect(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).andReturn("/x/" + DATAX_INSTANCE_NAME + "/update").times(2);


        EasyMock.replay(pluginContext);
        HeteroList<?> hlist = meta.getHeteroList(pluginContext);

        JSONObject j = hlist.toJSON();
        assertNotNull(j);
        //System.out.println();

        JsonUtil.assertJSONEqual(TestHeteroList.class, "dataxReader.assert.json"
                , JsonUtil.toString(j), (msg, expect, actual) -> {
                    assertEquals(msg, expect, actual);
                });


        EasyMock.verify(pluginContext);
    }
}
