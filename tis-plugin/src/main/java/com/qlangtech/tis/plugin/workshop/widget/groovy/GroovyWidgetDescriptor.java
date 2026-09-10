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

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.workshop.widget.IWorkshopWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * GroovyWorkshopWidget 的 Descriptor，支持脚本驱动的动态字段与显示名称。
 * <p>
 * 通过 {@link GroovyFieldScriptBridge} 加载目标 .groovy 脚本的 render config，
 * 动态提供 {@link #getDisplayName()} 和表单字段定义。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/10
 */
public class GroovyWidgetDescriptor extends Descriptor<IWorkshopWidget> {

    /**
     * 宿主 Widget 实例，用于获取 scriptPath 并从脚本中动态加载字段
     */
    private GroovyWorkshopWidget host;

    /**
     * 无参构造器（供 TIS 扩展机制使用，子类 DftDescriptor 通过 {@code super()} 调用）
     */
    public GroovyWidgetDescriptor() {
        super();
    }

    /**
     * 带宿主 Widget 实例的构造器，用于直接指定脚本来源。
     *
     * @param host 宿主 GroovyWorkshopWidget 实例
     */
    public GroovyWidgetDescriptor(GroovyWorkshopWidget host) {
        this.host = host;
    }

    /**
     * 返回宿主 Widget 实例
     */
    public GroovyWorkshopWidget getHost() {
        return this.host;
    }

    @Override
    public String getDisplayName() {
        // 优先从宿主 Widget 的脚本中加载 displayName
        if (host != null && host.scriptPath != null) {
            GroovyFieldScriptBridge.ScriptConfig cfg = GroovyFieldScriptBridge.load(host.scriptPath);
            if (cfg != null && cfg.displayName != null) {
                return cfg.displayName;
            }
        }
        // 回退：通过 ThreadLocal 获取当前 Widget 实例
        GroovyWorkshopWidget instance = resolveCurrentWidget();
        if (instance != null && instance.scriptPath != null) {
            GroovyFieldScriptBridge.ScriptConfig cfg = GroovyFieldScriptBridge.load(instance.scriptPath);
            if (cfg != null && cfg.displayName != null) {
                return cfg.displayName;
            }
        }
        return "Groovy Widget";
    }

    @Override
    public Map<String, Object> getExtractProps(boolean forAIPromote) {
        Map<String, Object> props = new HashMap<>();
        props.put("icon", "code");
        props.put("category", "custom");
        return props;
    }

    // ========================================================================
    // 内部辅助方法
    // ========================================================================

    /**
     * 从 {@link com.qlangtech.tis.extension.util.GroovyShellUtil#pluginThreadLocal}
     * 中解析当前正在渲染的 {@link GroovyWorkshopWidget} 实例。
     */
    private GroovyWorkshopWidget resolveCurrentWidget() {
        java.util.Map<Class<? extends Descriptor>, com.qlangtech.tis.extension.Describable> map =
                com.qlangtech.tis.extension.util.GroovyShellUtil.pluginThreadLocal.get();
        if (map != null) {
            com.qlangtech.tis.extension.Describable plugin = map.get(this.getClass());
            if (plugin instanceof GroovyWorkshopWidget) {
                return (GroovyWorkshopWidget) plugin;
            }
        }
        return null;
    }
}