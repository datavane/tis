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

package com.qlangtech.tis.extension;

import com.qlangtech.tis.extension.util.GroovyShellEvaluate;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSON;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-11 09:52
 **/
public class TestDescriptor extends TestCase {

    public void testGetPluginFormPropertyTypes() {

        GroovyShellEvaluate.eval("com.qlangtech.tis.extension.DefaultPlugin.getColsDefaultVal()");

        DefaultPlugin dftPlugin = new DefaultPlugin();
        DescriptorsJSON descJson = new DescriptorsJSON(dftPlugin.getDescriptor());
        //descJson.getDescriptorsJSON();

        JsonUtil.assertJSONEqual(DefaultPlugin.class, "default-plugin-descriptor-turn-1.json"
                , descJson.getDescriptorsJSON(), (m, e, a) -> {
                    assertEquals(m, e, a);
                });


        JsonUtil.assertJSONEqual(DefaultPlugin.class, "default-plugin-descriptor-turn-2.json"
                , descJson.getDescriptorsJSON(), (m, e, a) -> {
                    assertEquals(m, e, a);
                });
    }
}
