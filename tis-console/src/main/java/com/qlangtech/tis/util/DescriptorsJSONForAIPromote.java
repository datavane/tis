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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_CREATOR_HETERO;
import static com.qlangtech.tis.extension.util.PluginExtraProps.KEY_DESC_NAME;

/**
 * 生成的json描述信息提供给大模型使用
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/16
 */
public class DescriptorsJSONForAIPromote<T extends Describable<T>> extends DescriptorsJSON<T> {


  public static Pair<DescriptorsJSONResult, DescriptorsJSONForAIPromote> desc(DescribableImpl pluginImpl) {
    DescriptorsJSONForAIPromote aiPromote = new DescriptorsJSONForAIPromote(pluginImpl.getImplDesc(), pluginImpl);
    return Pair.of(aiPromote.getDescriptorsJSON(), aiPromote);
  }

  /**
   * 进行过程中构建的组件依赖
   */
  private Map<Class<? extends Descriptor>, DescribableImpl> descFieldsRegister = Maps.newHashMap();

  public DescriptorsJSONForAIPromote(Descriptor<T> descriptor, DescribableImpl pluginImpl) {
    super(descriptor);
    this.descFieldsRegister.put(descriptor.getClass(), pluginImpl);
  }

  public DescriptorsJSONForAIPromote(Collection<Descriptor<T>> collection, boolean rootDesc) {
    super(collection, rootDesc);
  }

  public Map<Class<? extends Descriptor>, DescribableImpl> getFieldDescRegister() {
    return this.descFieldsRegister;
  }

  @Override
  protected Pair<JSONObject, Descriptor> createFormPropertyTypes(Optional<SubFormFilter> subFormFilter, Descriptor<?> dd) {
    Pair<JSONObject, Descriptor> pair = createPluginFormPropertyTypes(dd, subFormFilter, true);
    return pair;
  }

  @Override
  public DescriptorsJSONResult getDescriptorsJSON() {
    return super.getDescriptorsJSON();
  }

  @Override
  protected DescriptorsJSON<T> createInnerDescrible(PropertyType val) {
    return new DescriptorsJSONForAIPromote(val.getApplicableDescriptors(), false);
  }

//  @Override
//  protected boolean processExtraProps(Descriptor<?> desc, PropertyType propVal, PropertyType ep) {

//    Optional<PluginExtraProps.FieldRefCreateor> refCreator = ep.getRefCreator();
//    // JSONObject creator = ep.getJSONObject(PluginExtraProps.KEY_CREATOR);
//    String descDisplayName = null;
//    if (refCreator.isPresent()) {
//      PluginExtraProps.FieldRefCreateor creator = refCreator.get();
//      List<PluginExtraProps.CandidatePlugin> supportedPlugins = creator.getCandidatePlugins();
//      if (CollectionUtils.isNotEmpty(supportedPlugins)) {
//        PluginExtraProps.CandidatePlugin supported = null;
//
//        DescribableImpl descImpl =
//          Objects.requireNonNull(descFieldsRegister.get(desc)
//            , "desc:" + desc.getId() + " relevant descImpl can not be null");
//
//        boolean multipOptions = supportedPlugins.size() > 1;
//
//        aa:
//        for (int i = 0; i < supportedPlugins.size(); i++) {
//          // TODO 需要让用户确定使用哪种plugin
//          supported = supportedPlugins.get(i);
//
//
//          IPluginEnum hetero = supported.getHetero();
//          List<Descriptor> descriptors = hetero.descriptors();
//          hetero.getExtensionPoint().getName();
//          descDisplayName = supported.getDisplayName();
//          DescribableImpl describable = new DescribableImpl(hetero.getExtensionPoint());
//
//          for (Descriptor d : descriptors) {
//            if (descDisplayName.equals(d.getDisplayName())) {
//              describable.setImpl(d.clazz.getName());
//              if (multipOptions) {
//                continue aa;
//              }
//            }
//          }
//
//          // descImpl.addFieldImplRef(propVal.f.getName(), describable);
//
//        }
//        // 说明没有找到合适的引用插件，应该插件还没有安装，需要开启安装流程
//
//        return false;
//      }
//    }

//    return true;
//  }

  @Override
  protected JSONObject getFieldExtraProps(PropertyType val) {
    PluginExtraProps.Props extraProp = val.extraProp;
    if (extraProp != null && extraProp.isAsynHelp()) {
      JSONObject props = new JSONObject(val.getExtraProps());
      props.put(PluginExtraProps.Props.KEY_HELP, extraProp.getAsynHelp());
      return props;
    }
    return val.getExtraProps();
  }
}
