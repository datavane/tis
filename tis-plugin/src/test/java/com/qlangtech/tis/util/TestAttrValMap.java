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
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.plugin.TestPluginImpl;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-22 10:27
 **/
public class TestAttrValMap extends TestCase {
    public void testCreateDescribable() {

        IControlMsgHandler fieldErrorHandler = EasyMock.createMock("fieldErrorHandler", IControlMsgHandler.class);
        IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);

        JSONObject jsonObject = IOUtils.loadResourceFromClasspath(TestPluginImpl.class
                , "testPluginImpl-post-content.json", true, (input) -> {
                    return JSON.parseObject(org.apache.commons.io.IOUtils.toString(input, TisUTF8.get()));
                });

        EasyMock.replay(fieldErrorHandler, pluginContext);
        AttrValMap attrValMap = AttrValMap.parseDescribableMap(fieldErrorHandler, Optional.empty(), jsonObject);

        Descriptor.ParseDescribable describable = attrValMap.createDescribable(pluginContext);
        assertNotNull(describable);
        TestPluginImpl testPlugin = (TestPluginImpl) describable.instance;
        assertNotNull(testPlugin);
        // 没有设置值，所以值对象应该为空，不能为0
        assertTrue("testPlugin.connectionsPerHost must be null", testPlugin.connectionsPerHost == null);
        assertEquals(12, (int) testPlugin.maxPendingPerConnection);

        EasyMock.verify(fieldErrorHandler, pluginContext);
    }
}
