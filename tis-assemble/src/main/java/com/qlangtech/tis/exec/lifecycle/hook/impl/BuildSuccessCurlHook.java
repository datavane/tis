/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.exec.lifecycle.hook.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PropertyPlaceholderHelper;
import com.qlangtech.tis.manage.common.PropertyPlaceholderHelper.PlaceholderResolver;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
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
                public Void p(int status, InputStream stream, String md5) {
                    return null;
                }
            });
        } catch (MalformedURLException e) {
            logger.error(String.valueOf(applyUrl), e);
        }
    }
}
