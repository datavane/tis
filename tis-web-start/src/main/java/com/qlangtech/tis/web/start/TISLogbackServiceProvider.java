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
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import ch.qos.logback.core.ContextBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom SLF4J 2.x service provider for TIS that routes log calls to per-webapp
 * LoggerContexts based on the calling thread's ClassLoader.
 * <p>
 * Root cause why this is needed:
 * logback 1.5.x (LogbackServiceProvider.initialize()) no longer calls
 * ContextSelectorStaticBinder.init(), so the old "logback.ContextSelector"
 * system-property mechanism is dead. Additionally, SLF4J 2.x LoggerFactory
 * never consults ContextSelectorStaticBinder in its hot path — it always
 * delegates directly to the registered SLF4JServiceProvider.
 * <p>
 * Activation (must be set BEFORE any LoggerFactory call):
 * System.setProperty("slf4j.provider",
 * "com.qlangtech.tis.web.start.TISLogbackServiceProvider");
 * (SLF4J 2.x reads this in LoggerFactory.loadExplicitlySpecified() and loads
 * it directly, bypassing ServiceLoader ordering entirely.)
 * <p>
 * Usage:
 * After creating each webapp's ClassLoader and loading its logback-{name}.xml
 * into a new LoggerContext, call TISLogbackServiceProvider.getInstance()
 * .registerContext(classLoader, loggerContext) to register it.
 * From that point on, any thread running with that ClassLoader (or a child of
 * it) as the TCL will have its log calls routed to that LoggerContext.
 */
public class TISLogbackServiceProvider implements SLF4JServiceProvider {

    private static volatile TISLogbackServiceProvider INSTANCE;

    private final LogbackServiceProvider delegate = new LogbackServiceProvider();
    private volatile TISRoutingLoggerFactory routingFactory;

    @Override
    public void initialize() {
        delegate.initialize();
        routingFactory = new TISRoutingLoggerFactory((LoggerContext) delegate.getLoggerFactory());
        INSTANCE = this;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        TISRoutingLoggerFactory f = routingFactory;
        return f != null ? f : delegate.getLoggerFactory();
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return delegate.getMarkerFactory();
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return delegate.getMDCAdapter();
    }

    @Override
    public String getRequestedApiVersion() {
        return delegate.getRequestedApiVersion();
    }

    public static TISLogbackServiceProvider getInstance() {
        return INSTANCE;
    }

    public void registerContext(ClassLoader classLoader, LoggerContext context) {
        TISRoutingLoggerFactory f = routingFactory;
        if (f != null) {
            f.registerContext(classLoader, context);
        }
    }

    public LoggerContext getLoggerContext(String logbackContextName) {
        TISRoutingLoggerFactory f = routingFactory;

        for (Map.Entry<ClassLoader, LoggerContext> entry : f.contextMap.entrySet()) {
            if (StringUtils.equals(entry.getValue().getName(), logbackContextName)) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("can not find contextName:"
                + logbackContextName + " relevant " + LoggerContext.class.getSimpleName() + " in context set:"
                + f.contextMap.values().stream().map(ContextBase::getName).collect(Collectors.joining(",")));
    }

    /**
     * ILoggerFactory that routes getLogger() to per-webapp LoggerContexts
     * based on the calling thread's context ClassLoader.
     */
    static class TISRoutingLoggerFactory implements ILoggerFactory {

        private final LoggerContext defaultContext;
        private final Map<ClassLoader, LoggerContext> contextMap = new IdentityHashMap<>();

        TISRoutingLoggerFactory(LoggerContext defaultContext) {
            this.defaultContext = defaultContext;
        }

        @Override
        public org.slf4j.Logger getLogger(String name) {
            return resolveContext().getLogger(name);
        }

        private LoggerContext resolveContext() {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            while (cl != null) {
                LoggerContext ctx;
                synchronized (this) {
                    ctx = contextMap.get(cl);
                }
                if (ctx != null)
                    return ctx;
                cl = cl.getParent();
            }
            return defaultContext;
        }

        synchronized void registerContext(ClassLoader cl, LoggerContext ctx) {
            contextMap.put(cl, ctx);
        }
    }
}
