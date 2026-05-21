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
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyDomain;
import com.qlangtech.tis.plugin.ontology.OntologyGlossary;
import com.qlangtech.tis.plugin.ontology.OntologyLinker;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologySharedProperty;
import com.qlangtech.tis.plugin.ontology.OntologyValueType;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.linker.LinkResources;
import com.qlangtech.tis.plugin.ontology.impl.synonyms.SynonymsElement;
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.qlangtech.tis.plugin.ontology.OntologyDomain.NAME_ONTOLOGY_DOMAIN;
import static com.qlangtech.tis.plugin.ontology.OntologyObjectType.KEY_OBJECT_TYPE;
import static com.qlangtech.tis.util.DescriptorsJSON.KEY_DISPLAY_NAME;

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
    //final String ds = this.getString(OntologyObjectType.KEY_DATASOURCE_NAME);
    // OntologyObjectType.
    JSONObject biz = new JSONObject();

    OntologyPluginMeta pluginMeta = OntologyPluginMeta.create(Ontology.OntologyEnum.ObjectType, ontologyName);
    //  context.put(UploadPluginMeta.KEY_PLUGIN_META, pluginMeta.getDelegate());
    UploadPluginMeta.putPluginMeta(context, pluginMeta.getDelegate());
    OntologyObjectType objectType = OntologyObjectType.loadDetail(ontologyName, objType);
    biz.put(KEY_OBJECT_TYPE, Objects.requireNonNull(objectType, "objectType can not be null"));
    List<OntologyLinker> linkers = OntologyLinker.loadAll(ontologyName);
    LinkResources linkResourcesStep = null;
    List<LinkResources.ObjectLinkInfo> links = null;
    // List<LinkResources.ObjectLinkInfo> matchedLinks = null;
    JSONArray linkTypes = new JSONArray();
    for (OntologyLinker linker : linkers) {
      linkResourcesStep = linker.getLinkResourcesStep();
      links = linkResourcesStep.getLinks().stream().filter((linkInfo) -> linkInfo.contain(objType)).toList();
      if (CollectionUtils.isNotEmpty(links)) {
        JSONObject l = new JSONObject();
        l.put("name", linker.name);
        l.put("links", links);
        linkTypes.add(l);
      }
    }
    biz.put(OntologyLinker.KEY_LINK_TYPES, linkTypes);
    this.setBizResult(context, biz);
  }

  /**
   * 获取本体详情：Object Types, Link Types, Value Types
   */
  @SuppressWarnings("all")
  public void doGetOntologyDetail(Context context) {
    final String ontologyName = getDomain();
    UploadPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta(UploadPluginMeta.create(Ontology.ONTOLOGY))
      .getDelegate().putExtraParams(NAME_ONTOLOGY_DOMAIN, ontologyName);

    //  context.put(UploadPluginMeta.KEY_PLUGIN_META, pluginMeta);
    UploadPluginMeta.putPluginMeta(context, pluginMeta);

    JSONObject result = new JSONObject();

    result.put("ontologyDescs",
      new DefaultDescriptorsJSON<>(pluginMeta.getHeteroEnum().descriptors()).getDescriptorsJSON());
    result.put("name", ontologyName);

    // Object Types: 读取该 ontology 下的所有 domain 实例
    //    UploadPluginMeta domainMeta = UploadPluginMeta.parse(
    //      OntologyDomain.ONTOLOGY_DOMAIN.getIdentity() + ":require,"
    //        + OntologyDomain.NAME_ONTOLOGY_DOMAIN + "_" + ontologyName);

    List<OntologyObjectType> objTypes = OntologyObjectType.loadAll(ontologyName);

    // IPluginStore<OntologyDomain> domainStore = OntologyDomain.ONTOLOGY_DOMAIN.getPluginStore(null, domainMeta);
    // List<OntologyDomain> domains = domainStore.getPlugins();
    JSONArray objectTypes = new JSONArray();
    for (OntologyObjectType ot : objTypes) {
      JSONObject obj = new JSONObject();
      obj.put("name", ot.getName());
      obj.put("bound",
        Objects.requireNonNull(ot.getProfile().binding, "binding can not be null").getObjectTypeBindingInfo());
      obj.put("colSize", ot.getCols().size());
      objectTypes.add(obj);
    }
    result.put("objectTypes", objectTypes);

    // Link Types
    //    UploadPluginMeta linkerMeta = UploadPluginMeta.parse(
    //      HeteroEnum.ONTOLOGY_LINKER.getIdentity() + ":require,"
    //        + OntologyDomain.NAME_ONTOLOGY_DOMAIN + "_" + ontologyName);
    //    IPluginStore<OntologyLinker> linkerStore = HeteroEnum.ONTOLOGY_LINKER.getPluginStore(null, linkerMeta);
    List<OntologyLinker> linkers = OntologyLinker.loadAll(ontologyName); //linkerStore.getPlugins();
    JSONArray linkTypes = new JSONArray();
    for (OntologyLinker linker : linkers) {
      //      JSONObject obj = new JSONObject();
      //      obj.put("name", linker.name);
      //      obj.put("type", linker.getLinkTypeEnd().getVal());
      //      obj.put("createTime", linker.getCreate());
      //      obj.put("sourceType", linker.sourceType);
      //      obj.put("targetType", linker.targetType);
      //      obj.put("description", linker.description);
      linkTypes.add(linker.serializeJSON());
    }
    result.put("linkTypes", linkTypes);


    List<OntologyValueType> valueTypes = OntologyValueType.load(ontologyName);
    JSONArray vtArray = new JSONArray();
    for (OntologyValueType vt : valueTypes) {
      JSONObject obj = new JSONObject();
      obj.put("name", vt.identityValue());
      obj.put("description", vt.getDescription());
      vtArray.add(obj);
    }
    result.put("valueTypes", vtArray);


    List<OntologySharedProperty> sharedProperties
      = OntologySharedProperty.loadAll(OntologyPluginMeta.create(Ontology.OntologyEnum.SharedProperty, ontologyName));
    JSONArray sharedPropArray = new JSONArray();
    for (OntologySharedProperty sharedProp : sharedProperties) {
      JSONObject obj = new JSONObject();
      obj.put("name", sharedProp.name);
      obj.put("alias", sharedProp.alias);
      obj.put("type", sharedProp.getOntologyType());
      obj.put("description", sharedProp.description);
      obj.put("createTime", sharedProp.getCreate());
      vtArray.add(obj);
      sharedPropArray.add(obj);
    }
    result.put("sharedProperties", sharedPropArray);

    /**
     * Glossary相关
     */
    List<OntologyGlossary> glossaries
      = OntologyGlossary.loadAll(OntologyPluginMeta.create(Ontology.OntologyEnum.Glossary, ontologyName));
    JSONArray glossaryArray = new JSONArray();
    for (OntologyGlossary glossary : glossaries) {
      JSONObject obj = new JSONObject();
      obj.put("term", glossary.term);
      obj.put("description", glossary.description);
      JSONArray synonymArray = new JSONArray();
      for (SynonymsElement syn : glossary.getSynonyms()) {
        synonymArray.add(syn.getEnumVal());
      }
      obj.put("synonyms", synonymArray);
      JSONObject targetJson = new JSONObject();// (JSONObject) JSONObject.toJSON(glossary.target);
      targetJson.put("targetLiteral", glossary.target.getTargetLiteral());
      // targetJson.put("targetType", glossary.target.getClass().getSimpleName());
      targetJson.put(KEY_DISPLAY_NAME,
        Objects.requireNonNull(glossary.target.getDescriptor(), "desc can not be null").getDisplayName());
      obj.put("target", targetJson);
      obj.put("createTime", glossary.getCreate());
      glossaryArray.add(obj);
    }
    result.put("glossaries", glossaryArray);

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
   * 删除一个本体域
   *
   * @param context
   */
  public void doDeleteOntologyDomain(Context context) {
    final String domain = this.getString("domain");
    if (StringUtils.isEmpty(domain)) {
      throw new IllegalArgumentException("param domain can not be empty");
    }
    Pair<OntologyDomain, IPluginStore<OntologyDomain>> pair = OntologyDomain.load(domain);
    IPluginStore<OntologyDomain> store = pair.getValue();
    XmlFile targetFile = store.getTargetFile();
    File domainDir = targetFile.getFile().getParentFile();
    try {
      if (domainDir.exists()) {
        FileUtils.deleteDirectory(domainDir);
      }
    } catch (IOException e) {
      throw new RuntimeException("failed to delete ontology domain dir: " + domainDir.getAbsolutePath(), e);
    }
    this.addActionMessage(context, "本体域 \"" + domain + "\" 已成功删除");
  }

  /**
   * 获取本体域列表
   */
  public void doGetOntologyDomain(Context context) {
    Pager pager = getPager();
    List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> doaminList = OntologyDomain.getDoaminList();
    this.setBizResult(context, new PaginationResult(pager, doaminList.stream().map((p) -> {
      return p.getKey().convertPojo();
    }).toList()));
  }

  // private static final OntologyBindingSwitcher BINDING_SWITCHER = new DefaultBindingSwitcher();

  //  /**
  //   * 校验是否可把 OT 的 binding 切换到指定数据源（典型场景：MySQL 建模 → Doris 落地后切换）。
  //   * 入参：domain (= ontology), objectType, newDsName。
  //   */
  //  public void doValidateBindingSwitch(Context context) {
  //    final String ontologyName = getDomain();
  //    final String objType = this.getString(KEY_OBJECT_TYPE);
  //    final String newDsName = this.getString("newDsName");
  //    OntologyObjectType ot = Objects.requireNonNull(
  //      OntologyObjectType.loadDetail(ontologyName, objType), "objectType can not be null");
  //    BindingSwitchReport report = BINDING_SWITCHER.validate(ot, newDsName);
  //    JSONObject biz = new JSONObject();
  //    biz.put("ok", report.ok());
  //    biz.put("missingColumns", report.missingColumns());
  //    biz.put("extraColumns", report.extraColumns());
  //    biz.put("typeMismatches", report.typeMismatches());
  //    biz.put("error", report.error());
  //    this.setBizResult(context, biz);
  //  }

  /**
   * 执行 OT binding 切换。
   */
  //  public void doSwitchBinding(Context context) {
  //    final String ontologyName = getDomain();
  //    final String objType = this.getString(KEY_OBJECT_TYPE);
  //    final String newDsName = this.getString("newDsName");
  //    OntologyObjectType ot = Objects.requireNonNull(
  //      OntologyObjectType.loadDetail(ontologyName, objType), "objectType can not be null");
  //    BINDING_SWITCHER.switchBinding(ot, newDsName, this);
  //    this.addActionMessage(context, "binding 已切换到 \"" + newDsName + "\"");
  //  }
}
