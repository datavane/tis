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
package com.qlangtech.tis.plugin.workshop.widget.groovy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.util.CustomerGroovyClassLoader;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.TisUTF8;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Groovy 脚本加载桥接器：读取 .groovy 文件，评估并提取 Widget 渲染配置 & 表单字段定义。
 * <p>
 * 脚本格式约定：
 * <pre>
 * displayName = "用户详情 Widget"
 * fields = [
 *   [name: "userName", type: "INPUTTEXT", label: "用户名", required: true],
 *   [name: "maxCount", type: "INT_NUMBER", label: "最大数量", defaultValue: 100]
 * ]
 * </pre>
 * <p>
 * 使用 {@link CustomerGroovyClassLoader} 加载并编译脚本，支持按路径缓存、3 秒执行超时。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/10
 */
public class GroovyFieldScriptBridge {

    private static final Logger logger = LoggerFactory.getLogger(GroovyFieldScriptBridge.class);

    /**
     * 脚本执行超时（毫秒）
     */
    private static final long SCRIPT_TIMEOUT_MS = 3000L;

    private static final ExecutorService scriptExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "groovy-script-eval");
        t.setDaemon(true);
        return t;
    });

    /**
     * 按 scriptPath 缓存的 ScriptConfig
     */
    private static final LoadingCache<String, ScriptConfig> scriptConfigCache =
            CacheBuilder.newBuilder()
                    .maximumSize(128)
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, ScriptConfig>() {
                        @Override
                        public ScriptConfig load(String scriptPath) throws Exception {
                            return doLoad(scriptPath);
                        }
                    });

    private GroovyFieldScriptBridge() {
    }

    /**
     * 加载并解析指定路径的 .groovy 脚本，返回渲染配置
     *
     * @param scriptPath classpath 相对路径，如 "com/example/MyWidget.groovy"
     * @return 脚本渲染配置，解析失败返回带默认值的空配置
     */
    public static ScriptConfig load(String scriptPath) {
        try {
            return scriptConfigCache.get(scriptPath);
        } catch (ExecutionException e) {
            logger.error("Failed to load groovy script: {}", scriptPath, e);
            return ScriptConfig.empty();
        }
    }

    /**
     * 将脚本中声明的字段列表转为 TIS 标准 {@link PluginExtraProps.Props} 列表，
     * 供 Descriptor 动态注入表单字段定义。
     *
     * @param descriptor 当前 Widget 的 Descriptor 实例
     * @return 字段额外属性列表，可为空列表
     */
    public static List<PluginExtraProps.Props> toPropertyTypes(Descriptor<?> descriptor) {
        // 通过 ThreadLocal 获取当前正在渲染的 GroovyWorkshopWidget 实例
        GroovyWorkshopWidget host = resolveHostWidget(descriptor);
        if (host == null || host.scriptPath == null) {
            return java.util.Collections.emptyList();
        }
        ScriptConfig config = load(host.scriptPath);
        if (config == null || config.fields == null || config.fields.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<PluginExtraProps.Props> result = new ArrayList<>(config.fields.size());
        for (ScriptField sf : config.fields) {
            Map<String, Object> propsMap = new HashMap<>();
            propsMap.put(PluginExtraProps.KEY_LABEL_PROP, sf.label);
            if (sf.defaultValue != null) {
                propsMap.put(PluginExtraProps.KEY_DFTVAL_PROP, sf.defaultValue);
            }
            if (sf.required) {
                propsMap.put("required", true);
            }
            PluginExtraProps.Props props = new PluginExtraProps.Props(propsMap);
            // 将 Props 的 key 设为字段名 —— PluginExtraProps.Props 在构造后可通过 put 设置
            props.put("type", sf.type);
            result.add(props);
        }
        return result;
    }

    // ========================================================================
    // 内部方法
    // ========================================================================

    /**
     * 从 ThreadLocal 或 Descriptor 上下文中获取当前 Widget 宿主实例
     */
    private static GroovyWorkshopWidget resolveHostWidget(Descriptor<?> descriptor) {
        if (descriptor instanceof GroovyWidgetDescriptor) {
            GroovyWidgetDescriptor gd = (GroovyWidgetDescriptor) descriptor;
            if (gd.getHost() != null) {
                return gd.getHost();
            }
        }
        // 回退：从 GroovyShellUtil 的 ThreadLocal pluginThreadLocal 中查找
        java.util.Map<Class<? extends Descriptor>, com.qlangtech.tis.extension.Describable> map =
                com.qlangtech.tis.extension.util.GroovyShellUtil.pluginThreadLocal.get();
        if (map != null) {
            com.qlangtech.tis.extension.Describable plugin = map.get(descriptor.getClass());
            if (plugin instanceof GroovyWorkshopWidget) {
                return (GroovyWorkshopWidget) plugin;
            }
        }
        return null;
    }

    /**
     * 实际执行脚本加载与评估
     */
    @SuppressWarnings("unchecked")
    private static ScriptConfig doLoad(String scriptPath) throws Exception {
        // 1. 从 classpath 读取脚本内容
        String scriptContent;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptPath)) {
            if (in == null) {
                logger.warn("Groovy script not found: {}", scriptPath);
                return ScriptConfig.empty();
            }
            scriptContent = IOUtils.toString(in, TisUTF8.get());
        }

        // 2. 在独立线程中执行，施加 3 秒超时
        Future<Map<String, Object>> future = scriptExecutor.submit(() -> {
            ClassLoader originalCtxLoader = Thread.currentThread().getContextClassLoader();
            try {
                CustomerGroovyClassLoader gcl = new CustomerGroovyClassLoader(
                        GroovyFieldScriptBridge.class.getClassLoader());
                Thread.currentThread().setContextClassLoader(gcl);
                Binding binding = new Binding();
                GroovyShell shell = new GroovyShell(gcl, binding);
                shell.evaluate(scriptContent);

                ScriptConfig config = new ScriptConfig();
                Object displayNameObj = binding.getVariable("displayName");
                config.displayName = displayNameObj != null ? String.valueOf(displayNameObj) : null;

                Object fieldsObj = binding.getVariable("fields");
                if (fieldsObj instanceof List) {
                    List<Map<String, Object>> rawFields = (List<Map<String, Object>>) fieldsObj;
                    config.fields = rawFields.stream()
                            .map(GroovyFieldScriptBridge::toScriptField)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                return config.toMap();
            } finally {
                Thread.currentThread().setContextClassLoader(originalCtxLoader);
            }
        });

        try {
            Map<String, Object> configMap = future.get(SCRIPT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return ScriptConfig.fromMap(configMap);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Groovy script {} execution timed out after {}ms", scriptPath, SCRIPT_TIMEOUT_MS);
            return ScriptConfig.empty();
        }
    }

    /**
     * 将原始 Map 转换为 ScriptField
     */
    private static ScriptField toScriptField(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        ScriptField sf = new ScriptField();
        sf.name = safeString(raw.get("name"));
        sf.type = safeString(raw.get("type"));
        sf.label = safeString(raw.get("label"));
        sf.required = Boolean.TRUE.equals(raw.get("required"));
        sf.defaultValue = raw.get("defaultValue");
        // name 和 type 是必需的
        if (sf.name == null || sf.type == null) {
            return null;
        }
        return sf;
    }

    private static String safeString(Object val) {
        return val != null ? String.valueOf(val) : null;
    }

    // ========================================================================
    // 内部模型
    // ========================================================================

    /**
     * 脚本渲染配置：Widget 在画布上的展示信息及表单字段定义
     */
    public static class ScriptConfig {
        public String displayName;
        public List<ScriptField> fields;

        public static ScriptConfig empty() {
            ScriptConfig cfg = new ScriptConfig();
            cfg.fields = java.util.Collections.emptyList();
            return cfg;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("displayName", displayName);
            if (fields != null) {
                List<Map<String, Object>> fieldMaps = fields.stream()
                        .map(ScriptField::toMap)
                        .collect(Collectors.toList());
                map.put("fields", fieldMaps);
            }
            return map;
        }

        @SuppressWarnings("unchecked")
        static ScriptConfig fromMap(Map<String, Object> map) {
            if (map == null) {
                return empty();
            }
            ScriptConfig cfg = new ScriptConfig();
            cfg.displayName = safeString(map.get("displayName"));
            Object fieldsObj = map.get("fields");
            if (fieldsObj instanceof List) {
                cfg.fields = ((List<Map<String, Object>>) fieldsObj).stream()
                        .map(GroovyFieldScriptBridge::toScriptField)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            } else {
                cfg.fields = java.util.Collections.emptyList();
            }
            return cfg;
        }
    }

    /**
     * 脚本声明的单个表单字段定义
     */
    public static class ScriptField {
        public String name;
        public String type;
        public String label;
        public boolean required;
        public Object defaultValue;

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("type", type);
            map.put("label", label);
            map.put("required", required);
            if (defaultValue != null) {
                map.put("defaultValue", defaultValue);
            }
            return map;
        }
    }
}