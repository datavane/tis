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
package com.qlangtech.tis.extension.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class TestPluginExtraProps extends TestCase {
    public void testLode() throws Exception {
        Optional<PluginExtraProps> ep = PluginExtraProps.load(TestPluginExtraProps.class);
        assertNotNull(ep);
        assertTrue(ep.isPresent());
        PluginExtraProps extraProps = ep.get();
        PluginExtraProps.Props prop = extraProps.getProp("dbName");
        assertNotNull(prop);
        assertNotNull("数据库名", prop.getLable());

        prop = extraProps.getProp("userName");
        assertNotNull(prop);
        assertNotNull("用户名", prop.getLable());
        assertTrue("isAsynHelp must be true", prop.isAsynHelp());


        PluginExtraProps.Props encode = extraProps.getProp("encode");
        JSONObject props = encode.getProps();
        JSONObject creator = props.getJSONObject("creator");
        assertNotNull(creator);
        assertEquals("部门管理", creator.getString("label"));
        assertEquals("/base/departmentlist", creator.getString("routerLink"));
    }

    public void testCreatorWithError() throws Exception {

        try {
            Optional<PluginExtraProps> ep = PluginExtraProps.load(WithCreatorError.class);
            fail("must have faild");
        } catch (Exception e) {
            assertEquals("propKey:dbName,package:com.qlangtech.tis.extension.util,propKey:WithCreatorError.json", e.getMessage());
        }
    }

    public void testCreatorWithMerge() throws Exception {
        Optional<PluginExtraProps> ep = PluginExtraProps.load(WithCreatorErrorOk.class);
        assertTrue(ep.isPresent());
        PluginExtraProps extraProps = ep.get();
        for (Map.Entry<String, PluginExtraProps.Props> e : extraProps.entrySet()) {
            System.out.println("key:" + e.getKey());
            System.out.println("value:" + JsonUtil.toString(e.getValue()));
            System.out.println("==============================================");
        }

        PluginExtraProps.Props dbName = extraProps.getProp("dbName");
        assertNotNull(dbName);

        JSONObject props = dbName.getProps();
        JSONObject creator = props.getJSONObject(PluginExtraProps.KEY_CREATOR);
        assertNotNull(creator);

        assertEquals("/base/departmentlist", creator.getString(PluginExtraProps.KEY_ROUTER_LINK));
        assertEquals("部门管理", creator.getString(PluginExtraProps.KEY_LABEL));

        JSONArray plugins = creator.getJSONArray("plugin");
        assertEquals(1, plugins.size());
        JSONObject pmeta = plugins.getJSONObject(0);
        assertNotNull(pmeta);

        JsonUtil.assertJSONEqual(TestPluginExtraProps.class, "pluginMeta.json", creator, (m, e, a) -> {
            assertEquals(m, e, a);
        });

//        {
//            "hetero": "params-cfg",
//                "descName": "DataX-global",
//                "extraParam": "append_true"
//        }

    }
}
