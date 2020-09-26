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
package com.qlangtech.tis.plugin;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.yarn.IYarnConfig;
import com.qlangtech.tis.manage.common.Config;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-25 11:09
 */
public class TestComponentMeta extends TestCase {

    static {
        try {
            File tmp = new File("/tmp/tis");
            FileUtils.cleanDirectory(tmp);
            Config.setDataDir(tmp.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        assertEquals("yarn1", yarn1.getName());
    }
}
