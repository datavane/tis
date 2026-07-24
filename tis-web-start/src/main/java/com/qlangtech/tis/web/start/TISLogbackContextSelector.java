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
package com.qlangtech.tis.web.start;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logback ContextSelector that maps each webapp's ClassLoader to its own LoggerContext.
 *
 * Replaces the JNDI-based ContextJNDISelector which stopped working in Jetty 12 because
 * Jetty 12 requires explicit JNDI configuration that was previously automatic.
 *
 * Usage:
 *   1. Register as global selector before logback initializes:
 *      System.setProperty("logback.ContextSelector",
 *          "com.qlangtech.tis.web.start.TISLogbackContextSelector")
 *   2. After creating each webapp's ClassLoader, call registerContext(classLoader, loggerContext).
 *   3. Logback dispatches logging calls to the context associated with the thread's
 *      ClassLoader (walking the parent chain until a match is found).
 */
public class TISLogbackContextSelector implements ContextSelector {

    private final LoggerContext defaultContext;

    // IdentityHashMap so ClassLoader identity (not equality) is the key
    private final Map<ClassLoader, LoggerContext> contextMap = new IdentityHashMap<>();

    public TISLogbackContextSelector(LoggerContext context) {
        this.defaultContext = context;
    }

    public synchronized void registerContext(ClassLoader classLoader, LoggerContext context) {
        contextMap.put(classLoader, context);
    }

    @Override
    public LoggerContext getLoggerContext() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            LoggerContext lc;
            synchronized (this) {
                lc = contextMap.get(cl);
            }
            if (lc != null) {
                return lc;
            }
            cl = cl.getParent();
        }
        return defaultContext;
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultContext;
    }

    @Override
    public LoggerContext detachLoggerContext(String loggerContextName) {
        synchronized (this) {
            for (Map.Entry<ClassLoader, LoggerContext> entry : contextMap.entrySet()) {
                if (loggerContextName.equals(entry.getValue().getName())) {
                    contextMap.remove(entry.getKey());
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getContextNames() {
        List<String> names = new ArrayList<>();
        synchronized (this) {
            for (LoggerContext lc : contextMap.values()) {
                names.add(lc.getName());
            }
        }
        names.add(defaultContext.getName());
        return names;
    }

    @Override
    public LoggerContext getLoggerContext(String name) {
        if (defaultContext.getName().equals(name)) {
            return defaultContext;
        }
        synchronized (this) {
            for (LoggerContext lc : contextMap.values()) {
                if (name.equals(lc.getName())) {
                    return lc;
                }
            }
        }
        return null;
    }
}