/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.util.*;

import java.util.List;

import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX;
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginAction extends BasicModule {


  /**
   * @param context
   * @throws Exception
   */
  public void doSwitchExtensionPointShow(Context context) throws Exception {
    boolean open = this.getBoolean("switch");
    TIS tis = TIS.get();
    tis.saveComponent(tis.loadGlobalComponent().setShowExtensionDetail(open));
  }

  public void doGetExtensionPointShow(Context context) throws Exception {
    TIS tis = TIS.get();
    this.setBizResult(context, tis.loadGlobalComponent().isShowExtensionDetail());
  }

  public void doGetPluginConfigInfo(Context context) throws Exception {
    //Thread.sleep(1000);

    List<UploadPluginMeta> plugins = getPluginMeta();

    // final String[] plugins = this.getStringArray("plugin");
    if (plugins == null || plugins.size() < 1) {
      throw new IllegalArgumentException("param plugin is not illegal");
    }
    org.json.JSONObject pluginDetail = new org.json.JSONObject();
    org.json.JSONArray hlist = new org.json.JSONArray();
    pluginDetail.put("showExtensionPoint", TIS.get().loadGlobalComponent().isShowExtensionDetail());
    for (UploadPluginMeta p : plugins) {
      HeteroEnum hEnum = p.getHeteroEnum();
      HeteroList<?> hList = new HeteroList<>();
      hList.setCaption(hEnum.caption);
      hList.setExtensionPoint(hEnum.extensionPoint);
      hList.setItems(hEnum.getPlugins(this, p));
      hList.setDescriptors(hEnum.descriptors());
      hList.setSelectable(hEnum.selectable);
      hlist.put(hList.toJSON());
    }
    pluginDetail.put("plugins", hlist);
    this.setBizResult(context, pluginDetail);
  }

  /**
   * 保存blugin配置
   *
   * @param context
   */
  public void doSavePluginConfig(Context context) throws Exception {
    if (this.getBoolean("errors_page_show")) {
      this.errorsPageShow(context);
    }
    List<UploadPluginMeta> plugins = getPluginMeta();
    JSONArray pluginArray = parseJsonArrayPost();
    UploadPluginMeta pluginMeta = null;
    JSONObject itemObj = null;
    boolean faild = false;
    List<PluginItems> categoryPlugins = Lists.newArrayList();
    PluginItems pluginItems = null;
    HeteroEnum hEnum = null;
    for (int pluginIndex = 0; pluginIndex < plugins.size(); pluginIndex++) {
      pluginMeta = plugins.get(pluginIndex);
      JSONArray itemsArray = pluginArray.getJSONArray(pluginIndex);
      hEnum = pluginMeta.getHeteroEnum();
      context.put(KEY_VALIDATE_PLUGIN_INDEX, new Integer(pluginIndex));
      pluginItems = new PluginItems(this, pluginMeta);
      List<AttrValMap> describableAttrValMapList = AttrValMap.describableAttrValMapList(this, itemsArray);
      if (pluginMeta.isRequired() && describableAttrValMapList.size() < 1) {
        this.addErrorMessage(context, "请设置'" + hEnum.caption + "'表单内容");
      }
      pluginItems.items = describableAttrValMapList;
      categoryPlugins.add(pluginItems);
      AttrValMap attrValMap = null;
      for (int i = 0; i < describableAttrValMapList.size(); i++) {
        attrValMap = describableAttrValMapList.get(i);
        context.put(KEY_VALIDATE_ITEM_INDEX, new Integer(i));
        if (!attrValMap.validate(context)) {
          faild = true;
        }
      }
    }
    if (this.hasErrors(context)) {
      return;
    }
    if (faild) {
      // 判断提交的plugin表单是否有错误？错误则退出
      this.addErrorMessage(context, "提交表单内容有错误");
      return;
    }
    for (PluginItems pi : categoryPlugins) {
      pi.save(context);
    }
    addActionMessage(context, "配置保存成功");
  }

  private List<UploadPluginMeta> getPluginMeta() {
    return UploadPluginMeta.parse(this.getStringArray("plugin"));
  }

  /**
   * 是否是和数据源相关的流程处理
   *
   * @return
   */
  @Override
  public boolean isDataSourceAware() {
    //return super.isDataSourceAware();
    List<UploadPluginMeta> pluginMeta = getPluginMeta();
    return pluginMeta.size() == 1 && pluginMeta.stream().findFirst().get().getHeteroEnum() == HeteroEnum.DATASOURCE;
  }


}
