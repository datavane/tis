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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.IndexBuildHook;

/*
 * 适配一下下
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AdapterIndexBuildLifeCycleHook implements IIndexBuildLifeCycleHook {

    private static final Logger logger = LoggerFactory.getLogger(AdapterIndexBuildLifeCycleHook.class);

    public static IIndexBuildLifeCycleHook create(ParseResult schemaParse) {
        try {
            List<IIndexBuildLifeCycleHook> indexBuildLifeCycleHooks = new ArrayList<>();
            List<IndexBuildHook> indexBuildHooks = schemaParse.getIndexBuildHooks();
            for (IndexBuildHook buildHook : indexBuildHooks) {
                Class<?> clazz = Class.forName(buildHook.getFullClassName());
                IIndexBuildLifeCycleHook indexBuildHook = (IIndexBuildLifeCycleHook) clazz.newInstance();
                if (indexBuildHook instanceof AdapterIndexBuildLifeCycleHook) {
                    ((AdapterIndexBuildLifeCycleHook) indexBuildHook).init(buildHook.getParams());
                }
                indexBuildLifeCycleHooks.add(indexBuildHook);
            }
            return new IIndexBuildLifeCycleHook() {

                @Override
                public void start(IParamContext ctx) {
                    try {
                        indexBuildLifeCycleHooks.forEach((e) -> {
                            e.start(ctx);
                        });
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                @Override
                public void buildFaild(IParamContext ctx) {
                    try {
                        indexBuildLifeCycleHooks.forEach((e) -> {
                            e.buildFaild(ctx);
                        });
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                @Override
                public void buildSuccess(IParamContext ctx) {
                    try {
                        indexBuildLifeCycleHooks.forEach((e) -> {
                            e.buildSuccess(ctx);
                        });
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AdapterIndexBuildLifeCycleHook() {
        super();
    }

    public void init(Map<String, String> params) {
    }

    @Override
    public void start(IParamContext ctx) {
    }

    @Override
    public void buildFaild(IParamContext ctx) {
    }

    @Override
    public void buildSuccess(IParamContext ctx) {
    }
}
