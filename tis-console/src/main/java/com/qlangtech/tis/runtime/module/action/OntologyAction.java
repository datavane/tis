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
//----------------------------------------------------
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
//----------------------------------------------------
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.qlangtech.tis.plugin.ontology.Ontology.KEY_CREATE_TIME;
import static com.qlangtech.tis.plugin.ontology.Ontology.KEY_DESCRIPTION;
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
    OntologyObjectType objectType = Ontology.loadObjectTypeDetail(ontologyName, objType);

    biz.put(KEY_OBJECT_TYPE, Map.of("cols",
      Objects.requireNonNull(objectType, "objectType can not be null").getCols()));

    List<OntologyLinker> linkers = Ontology.loadAllLinkers(ontologyName);
    LinkResources linkResourcesStep = null;
    List<LinkResources.ObjectLinkInfo> links = null;
    // List<LinkResources.ObjectLinkInfo> matchedLinks = null;
    JSONArray linkTypes = new JSONArray();
    for (OntologyLinker linker : linkers) {
      linkResourcesStep = linker.getLinkResourcesStep();
      links = linkResourcesStep.getLinks().stream().filter((linkInfo) -> linkInfo.contain(objType)).toList();
      if (CollectionUtils.isNotEmpty(links)) {
        JSONObject l = new JSONObject();
        l.put("name", linker.identityValue());
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
      obj.put("bound", ot.getObjectTypeBindingInfo());
      obj.put("colSize", ot.getCols().size());
      obj.put(Ontology.KEY_CREATE_TIME, ot.getCreate());
      objectTypes.add(obj);
    }
    result.put("objectTypes", objectTypes);


    List<OntologyLinker> linkers = Ontology.loadAllLinkers(ontologyName);
    JSONArray linkTypes = new JSONArray();
    for (OntologyLinker linker : linkers) {
      linkTypes.add(linker.serializeJSON());
    }
    result.put("linkTypes", linkTypes);


    List<OntologyValueType> valueTypes = Ontology.loadAllValueTypes(ontologyName);
    JSONArray vtArray = new JSONArray();
    for (OntologyValueType vt : valueTypes) {
      OntologyValueType.IMetadataOfValueType meta = vt.getMeta();
      JSONObject obj = new JSONObject();
      obj.put("name", vt.identityValue());
      obj.put(KEY_DESCRIPTION, meta.getDescription());
      obj.put(Ontology.KEY_TYPE, meta.ontologyType());
      obj.put(KEY_CREATE_TIME, vt.getCreate());
      vtArray.add(obj);
    }
    result.put("valueTypes", vtArray);


    List<OntologySharedProperty> sharedProperties
      = Ontology.loadAllSharedProperties(ontologyName);
    JSONArray sharedPropArray = new JSONArray();
    for (OntologySharedProperty sharedProp : sharedProperties) {
      JSONObject obj = new JSONObject();
      obj.put("name", sharedProp.name);
      obj.put("alias", sharedProp.alias);
      obj.put(Ontology.KEY_TYPE, sharedProp.getOntologyType());
      obj.put(KEY_DESCRIPTION, sharedProp.description);
      obj.put(Ontology.KEY_CREATE_TIME, sharedProp.getCreate());
      //  vtArray.add(obj);
      sharedPropArray.add(obj);
    }
    result.put("sharedProperties", sharedPropArray);

    /**
     * Glossary相关
     */
    List<OntologyGlossary> glossaries
      = Ontology.loadAllGlossary(ontologyName);
    JSONArray glossaryArray = new JSONArray();
    for (OntologyGlossary glossary : glossaries) {
      JSONObject obj = new JSONObject();
      obj.put("term", glossary.term);
      obj.put(KEY_DESCRIPTION, glossary.description);
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
      obj.put(Ontology.KEY_CREATE_TIME, glossary.getCreate());
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


}
