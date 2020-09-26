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
package com.qlangtech.tis.exec.lifecycle.hook.impl;

import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PropertyPlaceholderHelper;
import com.qlangtech.tis.manage.common.PropertyPlaceholderHelper.PlaceholderResolver;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 构建完成触发curl命令
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BuildSuccessCurlHook extends AdapterIndexBuildLifeCycleHook {

    private static final Logger logger = LoggerFactory.getLogger(BuildSuccessCurlHook.class);

    private String url;

    @Override
    public void init(Map<String, String> params) {
        this.url = params.get("url");
        if (StringUtils.isEmpty(this.url)) {
            throw new IllegalArgumentException("param url can not be null");
        }
    }

    // http://localhost:8080/trigger?component.start=indexBackflow&ps=20160623001000&appname=search4_fat_instance
    @Override
    public void buildSuccess(IParamContext ctx) {
        if (RunEnvironment.getSysRuntime() != RunEnvironment.ONLINE) {
            logger.info("runtime:" + RunEnvironment.getSysRuntime() + " will skip this step");
            return;
        }
        URL applyUrl = null;
        try {
            applyUrl = new URL(PropertyPlaceholderHelper.replace(url, new PlaceholderResolver() {

                @Override
                public String resolvePlaceholder(String placeholderName) {
                    if (DefaultChainContext.KEY_PARTITION.equals(placeholderName)) {
                        return ctx.getPartitionTimestamp();
                    }
                    throw new IllegalStateException("placeholderName:" + placeholderName + " is not illegal,shall be '" + DefaultChainContext.KEY_PARTITION + "'");
                }
            }));
            logger.info("send buildSuccess hook apply url:" + applyUrl);
            HttpUtils.processContent(applyUrl, new StreamProcess<Void>() {

                @Override
                public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    return null;
                }
            });
        } catch (MalformedURLException e) {
            logger.error(String.valueOf(applyUrl), e);
        }
    }
}
