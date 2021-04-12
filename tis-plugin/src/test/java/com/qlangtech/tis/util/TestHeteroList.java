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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 20:33
 */
public class TestHeteroList extends TestCase {
    static {
        CenterResource.setNotFetchFromCenterRepository();
    }

    public void testToJson() throws Exception {


        HeteroEnum dataxReader = HeteroEnum.DATAX_READER;
        String pluginMeta = dataxReader.identity + ":require," + IPropertyType.SubFormFilter.PLUGIN_META_TARGET_DESCRIPTOR_NAME
                + "_MySQL," + IPropertyType.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD + "_selectedTabs," + DataxUtils.DATAX_NAME + "_baisuitest";

        UploadPluginMeta meta = UploadPluginMeta.parse(pluginMeta);
        IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
        EasyMock.expect(pluginContext.isCollectionAware()).andReturn(false);
        EasyMock.expect(pluginContext.isDataSourceAware()).andReturn(false);
        EasyMock.replay(pluginContext);
        HeteroList<?> hlist = meta.getHeteroList(pluginContext);

        JSONObject j = hlist.toJSON();
        assertNotNull(j);
        //System.out.println();

        assertEquals( //
                JsonUtil.toString(JSON.parseObject(IOUtils.loadResourceFromClasspath(TestHeteroList.class, "dataxReader.assert.json")))
                , JsonUtil.toString(j));
        EasyMock.verify(pluginContext);
    }
}
