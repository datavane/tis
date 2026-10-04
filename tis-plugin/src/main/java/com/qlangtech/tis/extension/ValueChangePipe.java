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

package com.qlangtech.tis.extension;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.runtime.module.action.IParamGetter;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 前后端字段联动管道：fromField 值变化时，服务端渲染 toField 的候选内容。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/2
 */
@SuppressWarnings("all")
public class ValueChangePipe {
    private final String fromField;

    /**
     * 联动目标字段：去重且保持首次声明顺序。
     * <p>
     * 一个 fromField 只对应一个 pipe，多次 {@code valueChangePipe(同一 fromField, ...)} 会合并到这里
     * （见 {@link Descriptor#valueChangePipe(String, String...)}），而不是静默覆盖前一次的声明。
     */
    private final Set<String> toFields = new LinkedHashSet<>();

    /**
     * 广播渲染：未单独登记逐字段渲染的 toField 共用本次结果。
     */
    private BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>> serverSideRender;

    /**
     * key = toField；该目标字段的专属渲染，优先于 {@link #serverSideRender}。
     */
    private final Map<String, BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>>> fieldRenders =
            Maps.newHashMap();

    public static JSONArray renderValueSerialize2Json(List<? extends IdentityName> value) {
        Optional<?> first = value.stream().filter((v) -> {
            return v instanceof IPluginStore.MultiDescribleElement;
        }).findFirst();

        if (first.isPresent()) {
            final Descriptor desc = ((Describable) first.get()).getDescriptor();
            JSONArray result = new JSONArray();
            value.forEach((v) -> {
                try {
                    result.add(new DescribableJSON((Describable) v, desc).getItemJson());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            return result;
        } else {
            return Option.toJson(value);
        }
    }

    static String createMapperKey(String fromField) {
        if (StringUtils.isEmpty(fromField)) {
            throw new IllegalArgumentException("param fromField can not be empty");
        }
        return fromField + "_";
    }

    /**
     * 构建前端field联动管道，当fromField值发生onChange事件，toField显示内容需要联动
     *
     * @param fromField
     * @param toField
     */
    public ValueChangePipe(String fromField, String... toField) {
        if (StringUtils.isEmpty(fromField)) {
            throw new IllegalArgumentException("param fromField can not be empty");
        }
        this.fromField = fromField;
        this.addToFields(toField);
    }

    /**
     * 合并式追加联动目标字段：重复字段去重，顺序保持首次出现的次序。
     *
     * @see Descriptor#valueChangePipe(String, String...)
     */
    public void addToFields(String... toField) {
        if (toField == null) {
            return;
        }
        for (String field : toField) {
            if (StringUtils.isEmpty(field)) {
                throw new IllegalArgumentException("param toField element can not be empty");
            }
            this.toFields.add(field);
        }
    }

    public String getFromField() {
        return fromField;
    }

    public String[] getToField() {
        return this.toFields.toArray(new String[0]);
    }

    /**
     * 广播渲染：未单独登记逐字段渲染的 toField 共用本次结果。
     * <p>
     * 注意：整个 pipe 只会调用该函数一次，各 toField 复用同一结果列表。
     */
    public ValueChangePipe render(BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>> function) {
        this.serverSideRender = Objects.requireNonNull(function, "function can not be null");
        return this;
    }

    /**
     * 逐字段渲染：仅对指定的 toField 生效，并覆盖广播结果。
     * <p>
     * 用于同一个 fromField 需要驱动<b>内容不同</b>的多个目标字段的场景——例如
     * {@code ObjectListWidget} 的 {@code titleProperty} 要的是属性下拉选项，而
     * {@code cardFields} 要的是列配置行，二者同源于 {@code objectSetVar} 却形态不同。
     *
     * @param toField 必须已在本 pipe 的目标字段列表中登记。前端只会请求已登记的字段，
     *                给未登记的字段挂渲染是死代码，此处快速失败优于留下一个「永不联动」的哑谜。
     */
    public ValueChangePipe render(String toField
            , BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>> function) {
        Objects.requireNonNull(toField, "toField can not be null");
        if (!this.toFields.contains(toField)) {
            throw new IllegalArgumentException("toField '" + toField + "' is not registered on pipe fromField '"
                    + this.fromField + "', registered:" + this.toFields);
        }
        this.fieldRenders.put(toField, Objects.requireNonNull(function, "function can not be null"));
        return this;
    }

    public Map<String, List<? extends IdentityName>> render(UploadPluginMeta pluginMeta, IParamGetter htmlParam) {
        Objects.requireNonNull(pluginMeta, "pluginMeta can not be null");
        Objects.requireNonNull(htmlParam, "param can not be null");

        Map<String, List<? extends IdentityName>> cascadeVals = Maps.newHashMap();
        boolean sharedResolved = false;
        List<? extends IdentityName> sharedVals = null;
        for (String cascadeField : this.toFields) {
            BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>> fieldRender =
                    this.fieldRenders.get(cascadeField);
            List<? extends IdentityName> opts;
            if (fieldRender != null) {
                opts = fieldRender.apply(pluginMeta, htmlParam);
            } else {
                if (this.serverSideRender == null) {
                    throw new IllegalStateException("pipe fromField '" + this.fromField
                            + "' has neither a broadcast render nor a per-field render for toField '"
                            + cascadeField + "'");
                }
                if (!sharedResolved) {
                    // 保持既有语义：广播函数对整个 pipe 只调用一次，各 toField 复用同一结果
                    sharedVals = this.serverSideRender.apply(pluginMeta, htmlParam);
                    sharedResolved = true;
                }
                opts = sharedVals;
            }
            // 不允许为 null：调用方（PluginAction）用 Collectors.toMap 汇总，null value 会 NPE
            cascadeVals.put(cascadeField, Objects.requireNonNull(opts,
                    "render result of toField '" + cascadeField + "' can not be null"));
        }
        return cascadeVals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof ValueChangePipe that))
            return false;

        return new EqualsBuilder().append(fromField, that.fromField).append(getToField(), that.getToField()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(fromField).append(getToField()).toHashCode();
    }
}
