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
package com.qlangtech.tis;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.util.XStream2;
import junit.framework.TestCase;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestTIS extends TestCase {

    private static final String collection = "search4totalpay";

    static {
        CenterResource.setNotFetchFromCenterRepository();
        HttpUtils.addMockGlobalParametersConfig();
        Config.setDataDir("./");
    }


    public void testReadPluginInfo() throws Exception {
        final String collectionRelativePath = TIS.KEY_TIS_PLUGIN_CONFIG + "/" + collection;
        List<String> subFiles = CenterResource.getSubFiles(collectionRelativePath, false, true);
        List<File> subs = Lists.newArrayList();
        for (String f : subFiles) {
            subs.add(CenterResource.copyFromRemote2Local(CenterResource.getPath(collectionRelativePath, f), true));
        }
        Set<XStream2.PluginMeta> pluginMetas = TIS.loadIncrComponentUsedPlugin(collection, subs, true);

        assertEquals(2, pluginMetas.size());
        for (XStream2.PluginMeta pluginName : pluginMetas) {
            System.out.println("used plugin:" + pluginName);
        }
    }
}
