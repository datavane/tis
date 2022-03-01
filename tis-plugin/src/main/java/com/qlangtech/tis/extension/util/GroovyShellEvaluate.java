/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.extension.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
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

    public final static ThreadLocal<Describable> pluginThreadLocal = new ThreadLocal<>();

    final static GroovyShell shell = new GroovyShell(new ClassLoader(GroovyShellEvaluate.class.getClassLoader()) {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return TIS.get().getPluginManager().uberClassLoader.findClass(name);
        }
    });


//    final static GroovyShell shell = new GroovyShell(new ClassLoader(GroovyShellEvaluate.class.getClassLoader()) {
//        @Override
//        protected Class<?> findClass(String name) throws ClassNotFoundException {
//            // return super.findClass(name);
//            return TIS.get().getPluginManager().uberClassLoader.findClass(name);
//        }
//    });

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
