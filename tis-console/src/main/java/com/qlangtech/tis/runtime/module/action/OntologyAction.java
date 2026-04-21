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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.ontology.OntologyDomain;
import com.qlangtech.tis.plugin.ontology.OntologyLinker;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyValueType;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.qlangtech.tis.plugin.ontology.OntologyObjectType.KEY_OBJECT_TYPE;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/15
 */
public class OntologyAction extends BasicModule {

  private Pager pager;

  public Pager getPager() {
    if (pager == null) {
      pager = this.createPager();
    }
    return pager;
  }

  /**
   * 获取本体对象详细
   *
   * @param context
   */
  public void doGetObjectType(Context context) {
    final String ontologyName = getDomain();
    final String objType = this.getString(KEY_OBJECT_TYPE);
    final String ds = this.getString(OntologyObjectType.KEY_DATASOURCE_NAME);
    // OntologyObjectType.
    JSONObject biz = new JSONObject();
    OntologyObjectType objectType = OntologyObjectType.loadDetail(ontologyName, ds, objType);
    biz.put(KEY_OBJECT_TYPE, Objects.requireNonNull(objectType, "objectType can not be null"));
    biz.put(OntologyLinker.KEY_LINK_TYPES, Lists.newArrayList());
    this.setBizResult(context, biz);
  }

  /**
   * 获取本体详情：Object Types, Link Types, Value Types
   */
  public void doGetOntologyDetail(Context context) {
    final String ontologyName = getDomain();

    JSONObject result = new JSONObject();
    result.put("name", ontologyName);

    // Object Types: 读取该 ontology 下的所有 domain 实例
    //    UploadPluginMeta domainMeta = UploadPluginMeta.parse(
    //      OntologyDomain.ONTOLOGY_DOMAIN.getIdentity() + ":require,"
    //        + OntologyDomain.NAME_ONTOLOGY_DOMAIN + "_" + ontologyName);

    List<OntologyObjectType> objTypes = OntologyObjectType.load(ontologyName);

    // IPluginStore<OntologyDomain> domainStore = OntologyDomain.ONTOLOGY_DOMAIN.getPluginStore(null, domainMeta);
    // List<OntologyDomain> domains = domainStore.getPlugins();
    JSONArray objectTypes = new JSONArray();
    for (OntologyObjectType ot : objTypes) {
      JSONObject obj = new JSONObject();
      obj.put("name", ot.name);
      obj.put(OntologyObjectType.KEY_DATASOURCE_NAME, ot.getDataSourceName());
      obj.put("colSize", ot.getCols().size());
      objectTypes.add(obj);
    }
    result.put("objectTypes", objectTypes);

    // Link Types
    UploadPluginMeta linkerMeta = UploadPluginMeta.parse(
      HeteroEnum.ONTOLOGY_LINKER.getIdentity() + ":require,"
        + OntologyDomain.NAME_ONTOLOGY_DOMAIN + "_" + ontologyName);
    IPluginStore<OntologyLinker> linkerStore = HeteroEnum.ONTOLOGY_LINKER.getPluginStore(null, linkerMeta);
    List<OntologyLinker> linkers = linkerStore.getPlugins();
    JSONArray linkTypes = new JSONArray();
    for (OntologyLinker linker : linkers) {
      JSONObject obj = new JSONObject();
      obj.put("name", linker.name);
      obj.put("sourceType", linker.sourceType);
      obj.put("targetType", linker.targetType);
      obj.put("description", linker.description);
      linkTypes.add(obj);
    }
    result.put("linkTypes", linkTypes);



    List<OntologyValueType> valueTypes = OntologyValueType.load(ontologyName);
    JSONArray vtArray = new JSONArray();
    for (OntologyValueType vt : valueTypes) {
      JSONObject obj = new JSONObject();
      obj.put("name", vt.name);
      obj.put("description", vt.getDescription());
      vtArray.add(obj);
    }
    result.put("valueTypes", vtArray);

    this.setBizResult(context, result);
  }

  private String getDomain() {
    final String ontologyName = this.getString("ontology");
    if (StringUtils.isEmpty(ontologyName)) {
      throw new IllegalArgumentException("param ontology can not be empty");
    }
    return ontologyName;
  }

  /**
   * 获取本体域列表
   */
  public void doGetOntologyDomain(Context context) {
    Pager pager = getPager();
    this.setBizResult(context, new PaginationResult(pager, OntologyDomain.getDoaminList()));
  }
}
