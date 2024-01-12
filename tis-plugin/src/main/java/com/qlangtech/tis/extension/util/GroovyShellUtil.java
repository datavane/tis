/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-12 09:20
 **/
public class GroovyShellUtil {
    public final static ThreadLocal<Descriptor> descriptorThreadLocal = new ThreadLocal<>();
    public final static ThreadLocal<Map<Class<? extends Descriptor>, Describable>> pluginThreadLocal
            = new ThreadLocal<Map<Class<? extends Descriptor>, Describable>>() {
        @Override
        protected Map<Class<? extends Descriptor>, Describable> initialValue() {
            return new ConcurrentHashMap<>();
        }
    };
    private static CustomerGroovyClassLoader loader;
    private static GroovyShell shell;
    private static LoadingCache<String, Script> scriptCache;

    public static void loadMyClass(String className, String script) {
        try {
            getGroovyLoader().loadMyClass(className, script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static LoadingCache<String, Script> getScriptCache() {
        if (scriptCache == null) {
            synchronized (GroovyShellUtil.class) {
                if (scriptCache == null) {
                    scriptCache
                            = CacheBuilder.newBuilder().build(new CacheLoader<String, Script>() {
                        @Override
                        public Script load(String key) throws Exception {
                            Script parse = getGroovyShell().parse(key);
                            return parse;
                        }
                    });
                }
            }
        }
        return scriptCache;
    }

    private static GroovyShell getGroovyShell() {
        if (shell == null) {
            synchronized (GroovyShellUtil.class) {
                if (shell == null) {
                    shell = new GroovyShell(new ClassLoader(GroovyShellEvaluate.class.getClassLoader()) {
                        @Override
                        protected Class<?> findClass(String name) throws ClassNotFoundException {
                            return TIS.get().getPluginManager().uberClassLoader.findClass(name);
                        }
                    });
                }
            }
        }
        return shell;
    }

    private static CustomerGroovyClassLoader getGroovyLoader() {
        if (loader == null) {
            synchronized (GroovyShellUtil.class) {
                if (loader == null) {
                    loader = new CustomerGroovyClassLoader();
                }
            }
        }
        return loader;
    }

    public static Class<?> loadClass(String pkg, String className) {
        try {
            return getGroovyLoader().loadClass(pkg + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T eval(String javaScript) {
        if (!GroovyShellEvaluate.isInConsoleModule) {
            // 如果不在console中运行则返回空即可
            return null;
        }
        try {
            Script script = getScriptCache().get(javaScript);
            return (T) script.run();
        } catch (Throwable e) {
            throw new RuntimeException(javaScript, e);
        }
    }

}
