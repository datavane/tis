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

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-20 13:21
 */
public class TestUploadPluginMeta extends TestCase {

    public void testPluginMetaParse() {
        //  String pluginName = "dsname_yuqing_zj2_bak";
        String pluginName = "test_plugin";
        String[] plugins = new String[]{pluginName + ":require"};
        List<UploadPluginMeta> pluginMetas = UploadPluginMeta.parse(plugins);
        assertEquals(1, pluginMetas.size());
        UploadPluginMeta meta = pluginMetas.get(0);
        assertNotNull(meta);
        assertEquals(pluginName, meta.getName());
        assertTrue(meta.isRequired());
        // ===============================================
        plugins = new String[]{pluginName};
        pluginMetas = UploadPluginMeta.parse(plugins);
        assertEquals(1, pluginMetas.size());
        meta = pluginMetas.get(0);
        assertNotNull(meta);
        assertEquals(pluginName, meta.getName());
        assertFalse(meta.isRequired());
        // ==============================================
        plugins = new String[]{pluginName + ":xxxx"};
        try {
            pluginMetas = UploadPluginMeta.parse(plugins);
            fail("shall be faild,but not");
        } catch (Exception e) {
        }
        String dsnameKey = "dsname";
        final String dbName = "yuqing_zj2_bak";
        plugins = new String[]{pluginName + ":" + dsnameKey + "_" + dbName + ",require"};

        pluginMetas = UploadPluginMeta.parse(plugins);

        assertEquals(1, pluginMetas.size());
        meta = pluginMetas.get(0);
        assertNotNull(meta);
        assertEquals(pluginName, meta.getName());
        assertTrue(meta.isRequired());
        assertEquals(dbName, meta.getExtraParam(dsnameKey));


        //=======================================================================
        final String targetDescriptor = "MySQLDataxReader";
        final String subFieldName = "subFieldName";

        plugins = new String[]{pluginName + ":" + IPropertyType.SubFormFilter.PLUGIN_META_TARGET_DESCRIPTOR_NAME
                + "_" + targetDescriptor + "," + IPropertyType.SubFormFilter.PLUGIN_META_SUB_FORM_FIELD + "_" + subFieldName + ",require"};

        pluginMetas = UploadPluginMeta.parse(plugins);

        assertEquals(1, pluginMetas.size());
        meta = pluginMetas.get(0);
        assertTrue(meta.isRequired());
        Optional<IPropertyType.SubFormFilter> subFormFilter = meta.getSubFormFilter();
        assertTrue(subFormFilter.isPresent());
        IPropertyType.SubFormFilter filter = subFormFilter.get();

        assertEquals(subFieldName, filter.subFieldName);
        Descriptor descriptor = EasyMock.createMock("descriptor", Descriptor.class);
        EasyMock.expect(descriptor.getDisplayName()).andReturn(targetDescriptor);
        EasyMock.expect(descriptor.getDisplayName()).andReturn("dddd");

        EasyMock.replay(descriptor);
        assertTrue(filter.match(descriptor));
        assertFalse(filter.match(descriptor));
        EasyMock.verify(descriptor);

    }
}
