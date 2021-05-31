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
package com.qlangtech.tis.extension.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-06 13:27
 */
public class GroovyShellEvaluate {
    static final boolean isInConsoleModule;

    static {
        boolean loaded = false;
        try {
            loaded = (null != Class.forName("com.qlangtech.tis.runtime.module.action.BasicModule"));
        } catch (ClassNotFoundException e) { }
        isInConsoleModule = loaded;
    }

    public final static ThreadLocal<Descriptor> descriptorThreadLocal = new ThreadLocal<>();

    final static GroovyShell shell = new GroovyShell(new ClassLoader() {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // return super.findClass(name);
            return TIS.get().getPluginManager().uberClassLoader.findClass(name);
        }
    });
    private static final LoadingCache<String, Script> scriptCache
            = CacheBuilder.newBuilder().build(new CacheLoader<String, Script>() {
        @Override
        public Script load(String key) throws Exception {
            Script parse = shell.parse(key);
            return parse;
        }
    });

    private GroovyShellEvaluate() {
    }

    public static <T> T eval(String javaScript) {
        if (!isInConsoleModule) {
            // 如果不在console中运行则返回空即可
            return null;
        }
        try {
            Script script = scriptCache.get(javaScript);
            return (T) script.run();
        } catch (Throwable e) {
            throw new RuntimeException(javaScript, e);
        }
    }

}
