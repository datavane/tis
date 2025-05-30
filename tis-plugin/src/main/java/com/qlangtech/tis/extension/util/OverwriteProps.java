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

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.Option;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-06-08 13:22
 **/
public class OverwriteProps {

    public static List<Option> ENUM_BOOLEAN
            = Lists.newArrayList(new Option("是", true), new Option("否", false));

    public static OverwriteProps dft(Object dftVal) {
        return (new OverwriteProps()).setDftVal(dftVal);
    }

    public static OverwriteProps placeholder(Object placeholder) {
        return (new OverwriteProps()).setPlaceholder(placeholder);
    }

    public static OverwriteProps label(String label) {
        return label((l) -> label);
    }

    public static OverwriteProps label(Function<String, String> labelRewrite) {
        return (new OverwriteProps()).setLabelRewrite(labelRewrite);
    }

    public static OverwriteProps withAppendHelper(String appendHelper) {
        return (new OverwriteProps()).setAppendHelper(appendHelper);
    }

    public static OverwriteProps createBooleanEnums() {
        OverwriteProps opts = new OverwriteProps();
        opts.opts = Optional.of(ENUM_BOOLEAN);
        opts.dftValConvert = (val) -> {
            if (val instanceof String) {
                return Boolean.valueOf((String) val);
            }
            return val;
        };
        return opts;
    }

    public Object processDftVal(Object dftVal) {
        return dftVal != null ? dftValConvert.apply(dftVal) : (this.dftVal != null ? this.dftVal : null);
    }

    public Object getPlaceholder() {
        return this.placeholder;
    }

    public Optional<String> appendHelper = Optional.empty();
    private Object dftVal;
    private Object placeholder;
    private Boolean disabled;
    public Function<String, String> labelRewrite = (label) -> label;


    public Optional<List<Option>> opts = Optional.empty();
    public Function<Object, Object> dftValConvert = (val) -> val;

    public OverwriteProps setAppendHelper(String appendHelper) {
        this.appendHelper = Optional.of(appendHelper);
        return this;
    }

    public OverwriteProps setLabelRewrite(Function<String, String> labelRewrite) {
        this.labelRewrite = labelRewrite;
        return this;
    }

    public boolean getDisabled() {
        return disabled != null && disabled;
    }

    public OverwriteProps setEnumOpts(List<Option> optsOp) {
        this.opts = Optional.of(optsOp);
        return this;
    }

    public OverwriteProps setDisabled() {
        this.disabled = true;
        return this;
    }

    public OverwriteProps setDftVal(Object dftVal) {
        this.dftVal = dftVal;
        this.dftValConvert = (val) -> dftVal;
        return this;
    }

    public OverwriteProps setPlaceholder(Object placeholder) {
        this.placeholder = placeholder;
        return this;
    }

}
