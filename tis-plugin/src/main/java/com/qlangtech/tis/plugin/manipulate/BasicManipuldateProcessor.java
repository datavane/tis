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

package com.qlangtech.tis.plugin.manipulate;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.manipulate.ManipulateItemsProcessor;
import com.qlangtech.tis.plugin.ds.manipulate.ManipuldateUtils;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 伴生对象持久化操作基类，封装 manipuldateProcess 的通用校验、判重、存储、删除逻辑。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @see com.qlangtech.tis.datax.DefaultDataXProcessorManipulate
 * @see com.qlangtech.tis.plugin.ontology.OntologyDomainManipulate
 */
@SuppressWarnings("all")
public abstract class BasicManipuldateProcessor<T extends BasicManipuldateProcessor<T>>
        implements Describable<T>, IPluginStore.ManipuldateProcessor, IdentityName {

    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT,
            validate = {Validator.require, Validator.identity})
    public String name;

    @Override
    public String identityValue() {
        return this.name;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        BasicManipuldateProcessor<?> that = (BasicManipuldateProcessor<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(name);
    }

    private transient IDescribableManipulate _accompanyingOwnerPluginDesc;

    /**
     * 子类提供对应的 IPluginStore，用于持久化操作。
     * itemsProcessor 中含有 pluginMeta，可从中提取 domain / pipelineName 等上下文。
     */
    protected abstract IPluginStore<T> loadPluginStore(IPluginContext pluginContext,
                                                    ManipulateItemsProcessor itemsProcessor);// {
//        try {
//            if (this._accompanyingOwnerPluginDesc == null) {
//                Descriptor descriptor = TIS.get().getDescriptor(getAccompanyingPluginClass());
//                if (descriptor instanceof IDescribableManipulate describableManipulate) {
//                    this._accompanyingOwnerPluginDesc = describableManipulate;
//                } else {
//                    throw new IllegalStateException("descriptor:" + descriptor.getClass().getName() + " must be type "
//                            + "of " + IDescribableManipulate.class.getName());
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return (IPluginStore<T>) _accompanyingOwnerPluginDesc.getManipulateStore().orElseThrow();
//    }

//    /**
//     * 获取伴生宿主插件类
//     *
//     * @param <T>
//     * @return
//     */
//    protected abstract <T extends Describable<?>> Class<T> getAccompanyingPluginClass();

    /**
     * 持久化完成后的后续操作（如触发同步任务）。默认空实现，子类按需覆盖。
     */
    protected void afterManipuldateProcess(IPluginContext pluginContext, Optional<Context> context,
                                           ManipulateItemsProcessor itemsProcessor) {

    }

    /**
     * 子类可覆盖以提供新的 identity 名称（用于克隆/重命名场景）。
     */
    protected String getNewIdentityName() {
        return null;
    }

    @Override
    public Descriptor<T> getDescriptor() {
        return Describable.super.getDescriptor();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                         Optional<Context> context) {
        ManipulateItemsProcessor itemsProcessor = ManipuldateUtils.instance(
                pluginContext, context.orElseThrow(), this.getNewIdentityName(), (meta) -> {
                });
        if (itemsProcessor == null) {
            return;
        }

        final BasicDesc<T> desc = (BasicDesc<T>) this.getDescriptor();
        IPluginStore<T> store = this.loadPluginStore(pluginContext, itemsProcessor);
        T self = (T) this;

        if (itemsProcessor.isDeleteProcess()) {
            deleteFromStore(pluginContext, context, store, self);
            afterManipuldateProcess(pluginContext, context, itemsProcessor);
            return;
        }

        if (desc.isManipulateStorable() && !itemsProcessor.isUpdateProcess()) {
            List<T> existPlugins = getTargetInstancePlugins(store);
            if (CollectionUtils.isNotEmpty(existPlugins)) {
                for (T i : existPlugins) {
                    pluginContext.addErrorMessage(context.get(), "实例'" + i.identityValue() + "'已经配置，不能再创建新实例");
                }
                return;
            }
        }

        if (desc.isManipulateStorable()) {
            replaceInStore(pluginContext, context, store, self);
        }
        afterManipuldateProcess(pluginContext, context, itemsProcessor);
    }

    @SuppressWarnings("unchecked")
    private List<T> getTargetInstancePlugins(IPluginStore<T> store) {
        Class<T> clazz = (Class<T>) this.getClass();
        return store.getPlugins().stream()
                .filter(p -> clazz.isAssignableFrom(p.getClass()))
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    private void replaceInStore(IPluginContext pluginContext, Optional<Context> context,
                                IPluginStore<T> store, T beReplace) {
        List<T> all = store.getPlugins();
        List<Descriptor.ParseDescribable<T>> dlist = all.stream()
                .filter(p -> !StringUtils.equals(
                        Objects.requireNonNull(beReplace, "beReplace can not be null").identityValue(),
                        p.identityValue()))
                .map(Descriptor.ParseDescribable::new)
                .collect(Collectors.toList());
        dlist.add(new Descriptor.ParseDescribable<>(beReplace));
        store.setPlugins(pluginContext, context, dlist, true);
    }

    private void deleteFromStore(IPluginContext pluginContext, Optional<Context> context,
                                 IPluginStore<T> store, IdentityName id) {
        List<T> all = store.getPlugins();
        List<Descriptor.ParseDescribable<T>> dlist = all.stream()
                .filter(p -> !StringUtils.equals(
                        Objects.requireNonNull(id, "id can not be null").identityValue(),
                        p.identityValue()))
                .map(Descriptor.ParseDescribable::new)
                .collect(Collectors.toList());
        store.setPlugins(pluginContext, context, dlist, true);
    }

    protected static class BasicDesc<T extends BasicManipuldateProcessor<T>>
            extends Descriptor<T> implements IDescribableManipulate.IManipulateStorable {
        public BasicDesc() {
            super();
        }
    }
}
