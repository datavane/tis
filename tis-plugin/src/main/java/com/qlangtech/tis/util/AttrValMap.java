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
package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.INotebookable;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.impl.AttrVals;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AttrValMap {

    public static final String PLUGIN_EXTENSION_IMPL = "impl";
    public static final String PLUGIN_EXTENSION_VALS = "vals";

    private static final ThreadLocal<Descriptor> currentRootPluginValidator = new ThreadLocal<>();

    public static void setCurrentRootPluginValidator(Descriptor descriptor) {
        currentRootPluginValidator.set(descriptor);
    }

    public static Descriptor getCurrentRootPluginValidator() {
        return Objects.requireNonNull(currentRootPluginValidator.get()
                , "currentRootPluginValidator must be present");
    }

    public static void removeCurrentRootPluginValidator() {
        currentRootPluginValidator.remove();
    }

    private final AttrVals attrValMap;

    public final Descriptor descriptor;

    //private IControlMsgHandler msgHandler;
    private final Optional<SubFormFilter> subFormFilter;
    private final PropValRewrite propValRewrite;

    public static List<AttrValMap> describableAttrValMapList(JSONArray itemsArray,
                                                             Optional<SubFormFilter> subFormFilter) {
        return describableAttrValMapList(itemsArray, subFormFilter, ((propType, val) -> val));
    }

    public static List<AttrValMap> describableAttrValMapList(JSONArray itemsArray,
                                                             Optional<SubFormFilter> subFormFilter, PropValRewrite propValRewrite) {
        List<AttrValMap> describableAttrValMapList = Lists.newArrayList();
        AttrValMap describableAttrValMap = null;
        JSONObject itemObj = null;
        for (int i = 0; i < itemsArray.size(); i++) {
            itemObj = itemsArray.getJSONObject(i);
            describableAttrValMap = parseDescribableMap(subFormFilter, itemObj, propValRewrite);
            describableAttrValMapList.add(describableAttrValMap);
        }
        return describableAttrValMapList;
    }

    public static AttrValMap parseDescribableMap(Optional<SubFormFilter> subFormFilter,
                                                 com.alibaba.fastjson.JSONObject jsonObject) {
        return parseDescribableMap(subFormFilter, jsonObject, ((propType, val) -> val));
    }


    public static AttrValMap parseDescribableMap(Optional<SubFormFilter> subFormFilter,
                                                 com.alibaba.fastjson.JSONObject jsonObject, PropValRewrite propValRewrite) {
        String impl = null;
        Descriptor descriptor;
        impl = jsonObject.getString(PLUGIN_EXTENSION_IMPL);
        descriptor = TIS.get().getDescriptor(impl);
        if (descriptor == null) {
            throw new IllegalStateException("impl:" + impl + " can not find relevant ");
        }
        Object vals = jsonObject.get(PLUGIN_EXTENSION_VALS);
        AttrVals attrValMap = Descriptor.parseAttrValMap(vals);
        return new AttrValMap(attrValMap, subFormFilter, descriptor, propValRewrite);
    }

    public AttrValMap(//IControlMsgHandler msgHandler,
                      AttrVals attrValMap, Optional<SubFormFilter> subFormFilter, Descriptor descriptor, PropValRewrite propValRewrite) {
        this.attrValMap = attrValMap;
        this.descriptor = descriptor;
        //  this.msgHandler = msgHandler;
        this.subFormFilter = subFormFilter;
        this.propValRewrite = propValRewrite;
    }


    public Descriptor.PluginValidateResult validate(IControlMsgHandler msgHandler, Context context, boolean verify) {
        // return this.descriptor.verify(msgHandler, context, verify, attrValMap, subFormFilter);
        return this.validate(msgHandler, context, Optional.empty(), verify);
    }

    /**
     * 校验表单输入内容
     *
     * @param context
     * @param verify  是否进行业务逻辑校验
     * @return true：校验没有错误 false：校验有错误
     */
    public Descriptor.PluginValidateResult validate(
            IControlMsgHandler msgHandler, Context context, Optional<PluginFormProperties> propertyTypes, boolean verify) {
        // if (this.descriptor == propertyTypes.getDescriptor()) {
        return this.descriptor.verify(msgHandler, context, verify, attrValMap, propertyTypes, subFormFilter, this.propValRewrite);
//        } else {
//            return this.descriptor.verify(msgHandler, context, verify, attrValMap, subFormFilter);
//        }

    }

    public String createOrGetNotebook(IControlMsgHandler msgHandler, Context context) throws Exception {

        if (!(this.descriptor instanceof INotebookable)) {
            throw new IllegalStateException("desc:" + this.descriptor.getClass().getName() + " must be instance of " + INotebookable.class.getSimpleName());
        }
        INotebookable notebook = (INotebookable) this.descriptor;
        return notebook.createOrGetNotebook((new Descriptor.PostFormVals(this.descriptor, msgHandler, context,
                this.attrValMap)).newInstance());
    }

    public Descriptor.ParseDescribable createDescribable(IControlMsgHandler pluginContext, Context context) {
        return this.createDescribable(pluginContext, context, Optional.empty());
    }

    /**
     * 创建插件实例对象
     *
     * @return
     */
    public Descriptor.ParseDescribable createDescribable(IControlMsgHandler pluginContext, Context context, Optional<PluginFormProperties> formProperties) {
        return this.descriptor.parseDescribable(pluginContext, context, this.attrValMap, (formProperties), this.subFormFilter, this.propValRewrite);
    }

    public int size() {
        return this.attrValMap.size();
    }


    /**
     * @author: 百岁（baisui@qlangtech.com）
     * @create: 2022-08-12 21:54
     **/
    public interface IAttrVals {

        public static IAttrVals rootForm(Map<String, JSONObject> sform) {
            return new IAttrVals() {
                @Override
                public Map<String, JSONObject> asRootFormVals() {
                    return sform;
                }

                @Override
                public int size() {
                    return sform.size();
                }
            };
        }

        default Map<String, JSONObject> asRootFormVals() {
            throw new UnsupportedOperationException();
        }

        default Map<String, JSONArray> asSubFormDetails() {
            throw new UnsupportedOperationException();
        }

        int size();
    }
}
