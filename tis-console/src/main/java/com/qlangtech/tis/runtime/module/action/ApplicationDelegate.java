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

package com.qlangtech.tis.runtime.module.action;

import com.alibaba.fastjson.JSONArray;
import com.qlangtech.tis.datax.DefaultDataXProcessorManipulate;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.biz.dal.pojo.AppType;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Date;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/22
 */
public class ApplicationDelegate {
  private final Application app;

  public ApplicationDelegate(Application app) {
    this.app = app;

  }

  /**
   * 取得manipuldate 插件 元数据信息
   *
   * @return
   */
  public JSONArray getManipulateMetas() {
    DefaultDataXProcessorManipulate.DataXProcessorTemplateManipulateStore manipulateStore = DefaultDataXProcessorManipulate.getManipulateStore(app.getProjectName());
    Collection<DefaultDataXProcessorManipulate> manipulates = manipulateStore.getManipulates();
    if (CollectionUtils.isEmpty(manipulates)) {
      return null;
    }
    return Descriptor.getManipulateMetas(false, manipulates);
  }

  public Date getLastProcessTime() {
    return app.getLastProcessTime();
  }

  public void setLastProcessTime(Date lastProcessTime) {
    app.setLastProcessTime(lastProcessTime);
  }

  public String identityValue() {
    return app.identityValue();
  }

  public Integer getAppId() {
    return app.getAppId();
  }

  public String getFullBuildCronTime() {
    return app.getFullBuildCronTime();
  }

  public void setUpdateTime(Date updateTime) {
    app.setUpdateTime(updateTime);
  }

  public Integer getWorkFlowId() {
    return app.getWorkFlowId();
  }

  public void setFullBuildCronTime(String fullBuildCronTime) {
    app.setFullBuildCronTime(fullBuildCronTime);
  }

  public Integer getAppType() {
    return app.getAppType();
  }

  public String getDptName() {
    return app.getDptName();
  }

  public void setIsDeleted(String isDeleted) {
    app.setIsDeleted(isDeleted);
  }

  public Boolean getIsAutoDeploy() {
    return app.getIsAutoDeploy();
  }

  public Date getCreateTime() {
    return app.getCreateTime();
  }

  public void setCreateTime(Date createTime) {
    app.setCreateTime(createTime);
  }

  public void setDptName(String dptName) {
    app.setDptName(dptName);
  }

  public AppType parseAppType() {
    return app.parseAppType();
  }

  public void setDptId(Integer dptId) {
    app.setDptId(dptId);
  }

  public Integer getDptId() {
    return app.getDptId();
  }

  public void setManager(String manager) {
    app.setManager(manager);
  }

  public void setIsAutoDeploy(Boolean isAutoDeploy) {
    app.setIsAutoDeploy(isAutoDeploy);
  }

  public void setAppType(Integer appType) {
    app.setAppType(appType);
  }

  public Date getUpdateTime() {
    return app.getUpdateTime();
  }

  public String getIsDeleted() {
    return app.getIsDeleted();
  }

  public void setRecept(String recept) {
    app.setRecept(recept);
  }

  public void setAppId(Integer appId) {
    app.setAppId(appId);
  }

  public String getProjectName() {
    return app.getProjectName();
  }

  public String getManager() {
    return app.getManager();
  }

  public void setProjectName(String projectName) {
    app.setProjectName(projectName);
  }

  public String getRecept() {
    return app.getRecept();
  }


}
