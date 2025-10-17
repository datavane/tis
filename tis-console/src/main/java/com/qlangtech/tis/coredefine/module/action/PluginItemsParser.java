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
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.IPluginContext;
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
    return parsePluginItems(module, module, pluginMeta, context, pluginIndex, itemsArray, verify, propValRewrite);
  }

  /**
   *
   * @param module
   * @param pluginMeta
   * @param context
   * @param pluginIndex
   * @param itemsArray     example:   <pre>[ {
   *                                                                                           "impl" : "com.qlangtech.tis.config.spark.impl.DefaultSparkConnGetter",
   *                                                                                           "vals" : {
   *                                                                                             "connStrategy" : {
   *                                                                                               "descVal" : {
   *                                                                                                 "impl" : "com.qlangtech.tis.config.spark.impl.YarnConnStrategy",
   *                                                                                                 "vals" : {
   *                                                                                                   "yarnSite" : {
   *                                                                                                     "_primaryVal" : "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<configuration>\n <!-- Site specific YARN configuration properties -->\n  <!--RM的主机名 -->\n  <property>\n    <name>yarn.resourcemanager.hostname</name>\n    <value>192.168.28.200</value>\n  </property>\n\n  <!--RM对客户端暴露的地址,客户端通过该地址向RM提交应用程序、杀死应用程序等-->\n  <property>\n    <name>yarn.resourcemanager.address</name>\n    <value>${yarn.resourcemanager.hostname}:8032</value>\n  </property>\n\n  <!--RM对AM暴露的访问地址,AM通过该地址向RM申请资源、释放资源等-->\n  <property>\n    <name>yarn.resourcemanager.scheduler.address</name>\n    <value>${yarn.resourcemanager.hostname}:8030</value>\n  </property>\n\n  <!--RM对外暴露的web http地址,用户可通过该地址在浏览器中查看集群信息-->\n  <property>\n    <name>yarn.resourcemanager.webapp.address</name>\n    <value>${yarn.resourcemanager.hostname}:8088</value>\n  </property>\n\n  <!--RM对NM暴露地址,NM通过该地址向RM汇报心跳、领取任务等-->\n  <property>\n    <name>yarn.resourcemanager.resource-tracker.address</name>\n    <value>${yarn.resourcemanager.hostname}:8031</value>\n  </property>\n\n  <!--RM对管理员暴露的访问地址,管理员通过该地址向RM发送管理命令等-->\n  <property>\n    <name>yarn.resourcemanager.admin.address</name>\n    <value>${yarn.resourcemanager.hostname}:8033</value>\n  </property>\n</configuration>"
   *                                                                                                   }
   *                                                                                                 }
   *                                                                                               }
   *                                                                                             },
   *                                                                                             "name" : {
   *                                                                                               "_primaryVal" : "spark_yarn"
   *                                                                                             }
   *                                                                                           }
   *                                                                                         } ]</pre>
   * @param verify
   * @param propValRewrite
   * @return
   */
  public static PluginItemsParser parsePluginItems( //
    IPluginContext module, IControlMsgHandler msgHandler, UploadPluginMeta pluginMeta, Context context,
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
            msgHandler, context, AttrValMap.IAttrVals.rootForm(Collections.emptyMap())), pluginIndex, newAddItemsCount++);
          r.setDescriptor(desc);
          identityUniqueMap.put(p.identityValue(), r);
        }
      }

      for (PluginValidateResult i : parseResult.items) {
        if ((previous = identityUniqueMap.put(i.getIdentityFieldValue(), i)) != null) {
          previous.addIdentityFieldValueDuplicateError(msgHandler, context);
          i.addIdentityFieldValueDuplicateError(msgHandler, context);
          return parseResult;
        }
      }
    }
    return parseResult;
  }
}
