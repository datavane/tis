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

package com.qlangtech.tis.manage.common;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.runtime.module.action.IParamGetter;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/7
 */
public class UserProfile extends ParamsConfig implements IPluginStore.BeforePluginSaved {

    public static final String KEY_DISPLAY_NAME = "UserProfile";
    public static final String KEY_FIELD_LLM_NAME = "llm";
    private static final String KEY_FIELD_CONTROL = "control";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.identity,
            Validator.require})
    public String name;


    /**
     * 大模型接口
     */
    @FormField(type = FormFieldType.SELECTABLE, ordinal = 1, validate = {Validator.identity})
    public String llm;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 2, validate = {Validator.email})
    public String email;

    @FormField(ordinal = 3, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String control;

    @FormField(ordinal = 5, desClazz = TestChild.class, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN, validate =
            {Validator.require})
    public List<TestChild> children;

    //    @FormField(ordinal = 1, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    //    public List<RecordTransformer> rules = Lists.newArrayList();

    //    @FormField(type = FormFieldType.ENUM, ordinal = 3, validate = {Validator.require})
    //    public XX kk;

    //    public enum XX implements DescriptorUseableShortComment, IEndTypeGetter {
    //        AA(EndType.Bucket), BB(EndType.Table);
    //        private final EndType endType;
    //
    //        XX(EndType endType) {
    //            this.endType = endType;
    //        }
    //
    //        @Override
    //        public String shortComment() {
    //            return "hello " + this.name();
    //        }
    //
    //        @Override
    //        public EndType getEndType() {
    //            return endType;
    //        }
    //    }

    public static UserProfile load(IPluginContext pluginContext, boolean validateNull) {
        UploadPluginMeta pluginMeta = ParamsConfigPluginStore.createParamsConfigUserIsolation(KEY_DISPLAY_NAME);
        List<ParamsConfig> users = HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.getPlugins(pluginContext, pluginMeta);
        for (ParamsConfig user : users) {
            return (UserProfile) user;
        }
        if (validateNull) {
            throw new IllegalStateException("can not find userProfile instance with user:" + pluginContext.getLoginUser().getName());
        }
        return null;
    }

    /**
     * 执行当前登录用户的属性更新逻辑
     *
     * @param pluginContext
     * @param profile
     */
    public static void update(IPluginContext pluginContext, UserProfile profile) {
        UploadPluginMeta pluginMeta = ParamsConfigPluginStore.createParamsConfigUserIsolation(KEY_DISPLAY_NAME);
        IPluginStore pluginStore = HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.getPluginStore(pluginContext, pluginMeta);
        pluginStore.setPlugins(pluginContext, Optional.empty(),
                Collections.singletonList(new Descriptor.ParseDescribable(profile)));
    }

    public LLMProvider getLlmProvider() {
        return LLMProvider.load(Objects.requireNonNull(IPluginContext.getThreadLocalInstance()), llm);
    }


    @Override
    public UserProfile createConfigInstance() {
        return this;
    }

    @Override
    public String identityValue() {
        return this.name;
    }

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.name = pluginContext.getLoginUser().getName();
    }


    @TISExtension
    public static final class DftDescriptor extends ParamsConfig.BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
            this.registerSelectOptions(KEY_FIELD_LLM_NAME, LLMProvider::getExistProviders);
            this.registerSelectOptions(KEY_FIELD_CONTROL, () -> {
                return Lists.newArrayList(IdentityName.create("a"), IdentityName.create("b"));
            });
            this.valueChangePipe(KEY_FIELD_CONTROL, "children") //
                    .render(new BiFunction<UploadPluginMeta, IParamGetter, List<? extends IdentityName>>() {
                        @Override
                        public List<? extends IdentityName> apply(UploadPluginMeta pluginMeta, IParamGetter param) {
                            String control = param.getString(KEY_FIELD_CONTROL);
                            return getChildren(control);
                        }
                    });
        }


        @Override
        public String getDisplayName() {
            return KEY_DISPLAY_NAME;
        }

        @Override
        public EndType getEndType() {
            return EndType.UserProfile;
        }
    }

    public static List<TestChild> getChildren(String control) {
        List<TestChild> children = new ArrayList<>();
        TestChild child = null;
        switch (control) {
            case "a": {
                child = new TestChild();
                child.name = "a";
                child.age = 11;
                child.sex = TestChild.Sex.male;
                children.add(child);
                break;
            }
            case "b": {
                child = new TestChild();
                child.name = "b";
                child.age = 12;
                child.sex = TestChild.Sex.male;
                children.add(child);
                child = new TestChild();
                child.name = "bb";
                child.age = 14;
                child.sex = TestChild.Sex.female;
                children.add(child);
                break;
            }
            default:
                throw new IllegalStateException(control);
        }
        return children;
    }

}
