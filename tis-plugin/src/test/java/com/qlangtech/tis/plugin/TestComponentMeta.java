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

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.yarn.IYarnConfig;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.offline.FlatTableBuilder;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.TestHeteroList;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-25 11:09
 */
public class TestComponentMeta extends TestCase {


    @Override
    public void setUp() throws Exception {
        super.setUp();
        CenterResource.setFetchFromCenterRepository(false);
        HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();//.clearStubs();

        final String paramsConfig = "com.qlangtech.tis.config.ParamsConfig.xml";
        HttpUtils.addMockApply(paramsConfig, new HttpUtils.LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream(URL url) {
                return TestComponentMeta.class.getResourceAsStream(paramsConfig);
            }
        });

        HttpUtils.addMockGlobalParametersConfig();

        stubTpi("tis-datax-common-plugin.tpi");
        stubTpi("tis-hive-flat-table-builder-plugin.tpi");
        stubTpi("tis-k8s-plugin.tpi");
        // stubTpi("tis-hive-flat-table-builder-plugin");

        String tableDumpFactory = "com.qlangtech.tis.offline.TableDumpFactory.xml";
        HttpUtils.addMockApply(tableDumpFactory, new HttpUtils.LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream(URL url) {
                return TestComponentMeta.class.getResourceAsStream(tableDumpFactory);
            }
        });

        String indexBuilderTriggerFactory = "com.qlangtech.tis.offline.IndexBuilderTriggerFactory.xml";
        HttpUtils.addMockApply(indexBuilderTriggerFactory, new HttpUtils.LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream(URL url) {
                return TestComponentMeta.class.getResourceAsStream(indexBuilderTriggerFactory);
            }
        });

        String flatTableBuilder = "com.qlangtech.tis.offline.FlatTableBuilder.xml";
        HttpUtils.addMockApply(flatTableBuilder, new HttpUtils.LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream(URL url) {
                return TestComponentMeta.class.getResourceAsStream(flatTableBuilder);
            }
        });

        System.clearProperty(Config.KEY_DATA_DIR);
        TIS.clean();
        Config.setTestDataDir();
        TestHeteroList.setTISField();

        // TIS.initialized = false;
    }

    private static void stubTpi(String tpiFileName) {
        HttpUtils.addMockApply(tpiFileName, new HttpUtils.LatestUpdateTimestampClasspathRes() {
            @Override
            public InputStream getResourceAsStream(URL url) {
                String pluginFilePath = "/opt/data/tis/libs/plugins/" + tpiFileName;
                try {
                    return FileUtils.openInputStream(new File(pluginFilePath));
                } catch (IOException e) {
                    throw new RuntimeException(pluginFilePath, e);
                }
            }
        });
    }

    /**
     * Assemble节点启动执行
     */
    public void testAssembleComponent() {

        ComponentMeta assembleComponent = TIS.getAssembleComponent();
        assembleComponent.synchronizePluginsFromRemoteRepository();
        TIS.clean();
        IndexBuilderTriggerFactory builderFactory = HeteroEnum.INDEX_BUILD_CONTAINER.getPlugin();
        assertNotNull("builderFactory can not be null", builderFactory);

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

}
