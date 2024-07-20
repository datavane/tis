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

package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.PluginValidateResult;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.PluginItems;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-20 10:15
 **/
public class PluginItemsParser {

  public boolean faild = false;
  public PluginItems pluginItems;
  public final List<PluginValidateResult> items;

  public PluginItemsParser(List<PluginValidateResult> items) {
    this.items = items;
  }

  public static PluginItemsParser parsePluginItems(BasicModule module, UploadPluginMeta pluginMeta, Context context,
                                                   int pluginIndex, JSONArray itemsArray, boolean verify, PropValRewrite propValRewrite) {
    context.put(UploadPluginMeta.KEY_PLUGIN_META, pluginMeta);
    Optional<SubFormFilter> subFormFilter = pluginMeta.getSubFormFilter();

    IPluginEnum hEnum = pluginMeta.getHeteroEnum();
    PluginItems pluginItems = new PluginItems(module, context, pluginMeta);
    List<AttrValMap> describableAttrValMapList = AttrValMap.describableAttrValMapList(itemsArray, subFormFilter, propValRewrite);
    if (pluginMeta.isRequired() && describableAttrValMapList.size() < 1) {
      module.addErrorMessage(context, "请设置'" + hEnum.getCaption() + "'表单内容");
    }

    pluginItems.items = describableAttrValMapList;

    PluginItemsParser parseResult = pluginItems.validate(module, context, pluginIndex, verify);

    int newAddItemsCount = parseResult.items.size();
    /**===============================================
     * 校验Item字段的identity字段不能重复，不然就报错
     ===============================================*/
    Map<String, PluginValidateResult> identityUniqueMap = Maps.newHashMap();

    PluginValidateResult previous = null;
    if (!parseResult.faild
      && hEnum.isIdentityUnique()
      && hEnum.getSelectable() == Selectable.Multi
      && (parseResult.items.size() > 1 || pluginMeta.isAppend())) {

      Descriptor desc = null;
      if (pluginMeta.isAppend()) {
        // 已经存在的添加到identityUniqueMap
        List<IdentityName> plugins = hEnum.getPlugins(module, pluginMeta);
        for (IdentityName p : plugins) {
          desc = ((Describable) p).getDescriptor();
          PluginValidateResult r = new PluginValidateResult(new Descriptor.PostFormVals(desc,
            module, context, AttrValMap.IAttrVals.rootForm(Collections.emptyMap())), pluginIndex, newAddItemsCount++);
          r.setDescriptor(desc);
          identityUniqueMap.put(p.identityValue(), r);
        }
      }

      for (PluginValidateResult i : parseResult.items) {
        if ((previous = identityUniqueMap.put(i.getIdentityFieldValue(), i)) != null) {
          previous.addIdentityFieldValueDuplicateError(module, context);
          i.addIdentityFieldValueDuplicateError(module, context);
          return parseResult;
        }
      }
    }
    return parseResult;
  }
}
