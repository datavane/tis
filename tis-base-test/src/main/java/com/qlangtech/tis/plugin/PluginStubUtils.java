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

import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.ExtensionFinder;
import com.qlangtech.tis.extension.impl.ClassicPluginStrategy;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-20 15:49
 **/
public class PluginStubUtils {

    /**
     * 单元测试用于stub请求仓库中配置资源
     */
    public static void stubPluginConfig() {

        //  http://192.168.28.200:8080/tjs/config/stream_script_repo.action?path=cfg_repo%2Ftis_plugin_config%2Fcom.qlangtech.tis.offline.FileSystemFactory.xml

        //  final String paramsConfig = "com.qlangtech.tis.config.ParamsConfig.xml";
        HttpUtils.addMockApply("/tjs/config/stream_script_repo.action", new HttpUtils.LatestUpdateTimestampClasspathRes() {
            boolean targetDir = false;
            File targetPath;

            @Override
            public InputStream getResourceAsStream(URL url) {
                try {
                    System.out.println(url);
                    String path = URLDecoder.decode(StringUtils.substringAfter(String.valueOf(url), "path="), TisUTF8.getName());
                    targetPath = new File(Config.DEFAULT_DATA_DIR, path);
                    if (!targetPath.exists()) {
                        throw new IllegalStateException("path is not exist:" + targetPath.getAbsolutePath());
                    }
                    if (targetPath.isDirectory()) {
                        targetDir = true;
                        return new ByteArrayInputStream(new byte[]{});
                    } else {
                        targetDir = false;
                        return FileUtils.openInputStream(targetPath);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(String.valueOf(url), e);
                }
            }

            @Override
            public Map<String, List<String>> headerFields() {
                Map<String, List<String>> result = Maps.newHashMap(super.headerFields());
                if (targetDir) {
                    Objects.requireNonNull(targetPath, "targetPath can not be null");
                    result.put(ConfigFileContext.KEY_HEAD_FILES
                            , Arrays.stream(targetPath.list()).map((path) -> path + ":f").collect(Collectors.toList()));
                }
                return result;
            }
        });
    }

    public static void setDataDir(String path) throws Exception {
        System.clearProperty(Config.KEY_DATA_DIR);
        Config.setDataDir(path);
        TIS.clean();
        PluginStubUtils.setTISField();
    }


    public static void setTISField() throws Exception {
        Field pluginCfgRootField = TIS.class.getField("pluginCfgRoot");
        setFinalStatic(pluginCfgRootField, new File(Config.getMetaCfgDir(), TIS.KEY_TIS_PLUGIN_CONFIG));

        Field pluginDirRootField = TIS.class.getDeclaredField("pluginDirRoot");
        setFinalStatic(pluginDirRootField, new File(Config.getLibDir(), TIS.KEY_TIS_PLUGIN_ROOT));

        Field finders = ClassicPluginStrategy.class.getField("finders");
        setFinalStatic(finders, Collections.singletonList(new ExtensionFinder.Sezpoz()));
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
