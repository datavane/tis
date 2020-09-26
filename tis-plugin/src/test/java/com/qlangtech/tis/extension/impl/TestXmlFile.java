/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.util.XStream2;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-26 14:13
 */
public class TestXmlFile extends TestCase {

    /**
     * 测试序列化
     */
    public void testMashall() throws Exception {
        File testFile = new File("/tmp/test_file.xml");
        XmlFile xmlFile = new XmlFile(testFile);
        List<TestBean> plugins = Lists.newArrayList();
        plugins.add(new TestBean("baisui"));
        plugins.add(new TestBean("dabao"));
        plugins.add(new TestBean("xiaobao"));
        Set<XStream2.PluginMeta> pluginsMeta = Sets.newHashSet();
        pluginsMeta.addAll(XStream2.PluginMeta.parse("test1@1.1"));
        pluginsMeta.addAll(XStream2.PluginMeta.parse("mock2@1.2"));
        xmlFile.write(plugins, pluginsMeta);
        List<IRepositoryResource> resources = Lists.newArrayList();
        resources.add(new FileRepositoryResource(testFile));
        ComponentMeta componentMeta = new ComponentMeta(resources);
        Set<XStream2.PluginMeta> pluginMetaSet = componentMeta.loadPluginMeta();
        assertEquals(2, pluginMetaSet.size());
        for (XStream2.PluginMeta pm : pluginsMeta) {
            assertTrue(pm.toString(), pluginMetaSet.contains(pm));
        }
        FileUtils.deleteQuietly(testFile);
    }

    private static class FileRepositoryResource implements IRepositoryResource {

        private final File f;

        public FileRepositoryResource(File f) {
            this.f = f;
        }

        @Override
        public void copyConfigFromRemote() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getTargetFile() {
            return f;
        }
    }

    public static class TestBean {

        private final String name;

        public TestBean(String name) {
            this.name = name;
        }
    }
}
