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

import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.IndexBuildHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
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
