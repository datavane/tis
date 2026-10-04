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

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link UserProfile#children} 的子表单行配置，由 @SubForm(desClazz = TestChild.class) 驱动渲染。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/16
 */
public class TestChild implements Describable<TestChild>, IPluginStore.MultiDescribleElement {

    /**
     * 姓名，作为子表单行的唯一标识（框架要求子表单元素必须声明 identity 属性）
     */
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String name;

    /**
     * 年龄
     */
    @FormField(ordinal = 1, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer age;

    /**
     * 性别，取值固定，使用 ENUM
     */
    @FormField(ordinal = 2, type = FormFieldType.ENUM, validate = {Validator.require})
    public Sex sex;

    @Override
    public String identityValue() {
        return this.name;
    }


    public enum Sex {
        male("男"), female("女");
        public final String label;

        Sex(String label) {
            this.label = label;
        }
    }

    @TISExtension
    public static class DftDescriptor extends IPluginStore.MultiDescribleElementDescriptor<TestChild> {
        @Override
        public EndType getEndType() {
            return EndType.Copy;
        }

        @Override
        protected List<Pair<Option, IPluginStore.MultiDescribleElement>> getEnumableCandidateSet(ParseDescribable<?> describable) {
            List<Pair<Option, IPluginStore.MultiDescribleElement>> target = Lists.newArrayList();
            UserProfile userProfile = describable.getInstance();
            // userProfile.control;
            // userProfile.children;
            Set<String> selected =
                    userProfile.children.stream().map(TestChild::identityValue).collect(Collectors.toSet());
            List<TestChild> children = UserProfile.getChildren(userProfile.control);
            for (TestChild c : children) {
                target.add(Pair.of(new Option(c.identityValue()).setChecked(selected.contains(c.identityValue())), c));
            }
            return target;
        }

        @Override
        public String shortComment() {
            return "测试子表单属性";
        }

        @Override
        public boolean isEnumableSet() {
            return true;
        }

        @Override
        public ViewStyle viewStyle() {
            return ViewStyle.Table;
        }

        @Override
        public List<ColConfig> colsConfig() {
            // Table 视图要求至少有一列被标记为 clickable：点击该列可打开该条记录的编辑对话框
            return Lists.newArrayList(new ColConfig("name", 12).setClickable(), new ColConfig("age", 12),
                    new ColConfig("sex"));
        }
    }
}
