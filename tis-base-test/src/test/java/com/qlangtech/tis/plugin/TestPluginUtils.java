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

import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import junit.framework.TestCase;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-20 15:59
 **/
public class TestPluginUtils extends TestCase {

    public void testStubParamsConfig() throws Exception {
        PluginStubUtils.stubPluginConfig();
        URL url = new URL("http://192.168.28.200:8080/tjs/config/stream_script_repo.action?path=cfg_repo%2Ftis_plugin_config%2Fcom.qlangtech.tis.config.ParamsConfig.xml");
        HttpUtils.get(url, new ConfigFileContext.StreamProcess<Void>() {
            @Override
            public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                assertNotNull(stream);
                return null;
            }
        });


    }
}
