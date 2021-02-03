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
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.offline.module.manager.impl.OfflineManager;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.util.*;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginAction extends BasicModule {

  private OfflineManager offlineManager;

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
    Descriptor.PluginValidateResult validateResult = null;
    List<Descriptor.PluginValidateResult> items = null;
    for (int pluginIndex = 0; pluginIndex < plugins.size(); pluginIndex++) {
      items = Lists.newArrayList();
      pluginMeta = plugins.get(pluginIndex);
      JSONArray itemsArray = pluginArray.getJSONArray(pluginIndex);
      hEnum = pluginMeta.getHeteroEnum();
      //context.put(KEY_VALIDATE_PLUGIN_INDEX, new Integer(pluginIndex));
      pluginItems = new PluginItems(this, pluginMeta);
      List<AttrValMap> describableAttrValMapList = AttrValMap.describableAttrValMapList(this, itemsArray);
      if (pluginMeta.isRequired() && describableAttrValMapList.size() < 1) {
        this.addErrorMessage(context, "请设置'" + hEnum.caption + "'表单内容");
      }
      pluginItems.items = describableAttrValMapList;
      categoryPlugins.add(pluginItems);
      AttrValMap attrValMap = null;
      for (int itemIndex = 0; itemIndex < describableAttrValMapList.size(); itemIndex++) {
        attrValMap = describableAttrValMapList.get(itemIndex);
//        context.put(KEY_VALIDATE_ITEM_INDEX, new Integer(itemIndex));
//        context.put(KEY_VALIDATE_PLUGIN_INDEX, new Integer(pluginIndex));
        Descriptor.PluginValidateResult.setValidateItemPos(context, pluginIndex, itemIndex);
        if (!(validateResult = attrValMap.validate(context)).isValid()) {
          faild = true;
        } else {
          validateResult.setDescriptor(attrValMap.descriptor);
          items.add(validateResult);
        }
      }

      /**===============================================
       * 校验Item字段的identity字段不能重复，不然就报错
       ===============================================*/
      Map<String, Descriptor.PluginValidateResult> identityUniqueMap = Maps.newHashMap();
      Descriptor.PluginValidateResult previous = null;
      if (!faild && hEnum.isIdentityUnique()
        && hEnum.selectable == Selectable.Multi
        && items.size() > 1) {
        for (Descriptor.PluginValidateResult i : items) {
          if ((previous = identityUniqueMap.put(i.getIdentityFieldValue(), i)) != null) {
            previous.addIdentityFieldValueDuplicateError(this, context);
            i.addIdentityFieldValueDuplicateError(this, context);
            return;
          }
        }
      }

    }
    if (this.hasErrors(context) || this.getBoolean("verify")) {
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

  /**
   * description: 添加一个 数据源库 date: 2:30 PM 4/28/2017
   */
  @Override
  public final void addDb(String dbName, Context context, boolean shallUpdateDB) {
    createDatabase(this, dbName, context, shallUpdateDB, this.offlineManager);
  }

  public static void createDatabase(BasicModule module, String dbName, Context context
    , boolean shallUpdateDB, OfflineManager offlineManager) {
    DatasourceDb datasourceDb = null;
    if (shallUpdateDB) {
      datasourceDb = new DatasourceDb();
      datasourceDb.setName(dbName);
      datasourceDb.setSyncOnline(new Byte("0"));
      datasourceDb.setCreateTime(new Date());
      datasourceDb.setOpTime(new Date());
      DatasourceDbCriteria criteria = new DatasourceDbCriteria();
      criteria.createCriteria().andNameEqualTo(dbName);
      int exist = module.getWorkflowDAOFacade().getDatasourceDbDAO().countByExample(criteria);
      if (exist > 0) {
        module.addErrorMessage(context, "已经有了同名(" + dbName + ")的数据库");
        return;
      }
      /**
       * 校验数据库连接是否正常
       */
      int dbId = module.getWorkflowDAOFacade().getDatasourceDbDAO().insertSelective(datasourceDb);
      datasourceDb.setId(dbId);
      // this.setBizResult(context, datasourceDb);
    } else {
      // 更新状态
      DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
      dbCriteria.createCriteria().andNameEqualTo(dbName);
      for (DatasourceDb db : module.getWorkflowDAOFacade().getDatasourceDbDAO().selectByExample(dbCriteria)) {
        datasourceDb = db;
        break;
      }
      Objects.requireNonNull(datasourceDb, "datasourceDb can not be null");
    }

    module.setBizResult(context, offlineManager.getDbConfig(module, datasourceDb));
  }


  @Autowired
  public void setOfflineManager(OfflineManager offlineManager) {
    this.offlineManager = offlineManager;
  }

}
