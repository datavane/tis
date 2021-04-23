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

import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;

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
}
