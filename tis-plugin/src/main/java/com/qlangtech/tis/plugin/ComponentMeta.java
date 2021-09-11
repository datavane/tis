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

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.util.RobustReflectionConverter;
import com.qlangtech.tis.util.XStream2;
import com.qlangtech.tis.util.XStream2PluginInfoReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组件元数据信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-24 16:24
 */
public class ComponentMeta {
    private static final Logger logger = LoggerFactory.getLogger(ComponentMeta.class);
    public final List<IRepositoryResource> resources;


    public ComponentMeta(List<IRepositoryResource> resources) {
        this.resources = resources;
    }

    public ComponentMeta(IRepositoryResource resource) {
        this(Collections.singletonList(resource));
    }

    public void addResource(IRepositoryResource rr) {
        this.resources.add(rr);
    }

    /**
     * 下载配置文件
     */
    public void downloaConfig() {
        resources.forEach((r) -> {
            r.copyConfigFromRemote();
        });
    }

    /**
     * 取得元数据信息
     *
     * @return
     */
    public Set<XStream2.PluginMeta> loadPluginMeta() {
        synchronized (RobustReflectionConverter.usedPluginInfo) {
            RobustReflectionConverter.usedPluginInfo.remove();
            XStream2PluginInfoReader reader = new XStream2PluginInfoReader(XmlFile.DEFAULT_DRIVER);
            for (IRepositoryResource res : this.resources) {
                File targetFile = res.getTargetFile();
                if (!targetFile.exists()) {
                    //  throw new IllegalStateException("file:" + targetFile.getAbsolutePath() + " is not exist");
                    continue;
                }
                try {
                    XmlFile xmlFile = new XmlFile(reader, targetFile);
                    xmlFile.read();
                } catch (IOException e) {
                    throw new RuntimeException(targetFile.getAbsolutePath(), e);
                }
            }
            return RobustReflectionConverter.usedPluginInfo.get();
        }
    }

    /**
     * 同步插件
     */
    public void synchronizePluginsFromRemoteRepository() {
        try {
            this.downloaConfig();
            this.synchronizePluginsPackageFromRemote();
        } finally {
            TIS.permitInitialize = true;
        }
        if (TIS.initialized) {
            throw new IllegalStateException("make sure TIS plugin have not be initialized");
        }
    }


    /**
     * 同步插件包
     *
     * @return 本地被更新的插件包
     */
    public List<XStream2.PluginMeta> synchronizePluginsPackageFromRemote() {
        List<XStream2.PluginMeta> updateTpiPkgs = Lists.newArrayList();
        Set<XStream2.PluginMeta> pluginMetas = loadPluginMeta();
        for (XStream2.PluginMeta m : pluginMetas) {
            if (m.copyFromRemote()) {
                // 本地包已经被更新
                updateTpiPkgs.add(m);
                m.install();
            }
        }
        logger.info("download plugin from remote repository:"
                + updateTpiPkgs.stream().map((m) -> m.toString()).collect(Collectors.joining(",")));
        return updateTpiPkgs;
    }
}
