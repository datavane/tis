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
package com.qlangtech.tis.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.yarn.IYarnConfig;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.util.HeteroEnum;
import junit.framework.TestCase;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-25 11:09
 */
public class TestComponentMeta extends TestCase  {

    static {
//
        final String paramsConfig = "com.qlangtech.tis.config.ParamsConfig.xml";
        HttpUtils.addMockApply(paramsConfig, new LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream() {
                return TestComponentMeta.class.getResourceAsStream(paramsConfig);
            }
        });

        String tableDumpFactory = "com.qlangtech.tis.offline.TableDumpFactory.xml";
        HttpUtils.addMockApply(tableDumpFactory, new LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream() {
                return TestComponentMeta.class.getResourceAsStream(tableDumpFactory);
            }
        });

        String indexBuilderTriggerFactory = "com.qlangtech.tis.offline.IndexBuilderTriggerFactory.xml";
        HttpUtils.addMockApply(indexBuilderTriggerFactory, new LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream() {
                return TestComponentMeta.class.getResourceAsStream(indexBuilderTriggerFactory);
            }
        });
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.setTestDataDir();
        TIS.initialized = false;
    }

    /**
     * Assemble节点启动执行
     */
    public void testAssembleComponent() {
        ComponentMeta assembleComponent = TIS.getAssembleComponent();
        assembleComponent.synchronizePluginsFromRemoteRepository();
        IndexBuilderTriggerFactory builderFactory = HeteroEnum.INDEX_BUILD_CONTAINER.getPlugin();
        assertNotNull("builderFactory can not be null", builderFactory);
        //  Objects.requireNonNull(builderFactory, );

        PluginStore<FlatTableBuilder> pluginStore = TIS.getPluginStore(FlatTableBuilder.class);
        assertNotNull("flatTableBuilder can not be null", pluginStore.getPlugin());

        PluginStore<TableDumpFactory> tableDumpFactory = TIS.getPluginStore(TableDumpFactory.class);
        assertNotNull("tableDumpFactory can not be null", tableDumpFactory.getPlugin());
    }

    public void testDumpAndIndexBuilderComponent() {
        ComponentMeta dumpAndIndexBuilderComponent = TIS.getDumpAndIndexBuilderComponent();
        dumpAndIndexBuilderComponent.synchronizePluginsFromRemoteRepository();
        assertEquals(3, dumpAndIndexBuilderComponent.resources.size());
        for (IRepositoryResource res : dumpAndIndexBuilderComponent.resources) {
            File targetFile = res.getTargetFile();
            assertTrue(targetFile.getAbsolutePath(), targetFile.exists());
        }
        IYarnConfig yarn1 = ParamsConfig.getItem("yarn1", IYarnConfig.class);
        assertNotNull(yarn1);
        assertEquals("yarn1", yarn1.identityValue());
    }

    protected static abstract class LatestUpdateTimestampClasspathRes implements HttpUtils.IClasspathRes {
        @Override
        public Map<String, List<String>> headerFields() {
            return ImmutableMap.of(ConfigFileContext.KEY_HEAD_LAST_UPDATE, Lists.newArrayList(String.valueOf(System.currentTimeMillis())));
        }
    }
}
