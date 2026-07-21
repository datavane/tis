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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.mcp.tools.ChatBITool;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyDomain;
import com.qlangtech.tis.plugin.ontology.OntologyDomainManipulate;
import com.qlangtech.tis.plugin.ontology.OntologyGlossary;
import com.qlangtech.tis.plugin.ontology.OntologyLinker;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.OntologySharedProperty;
import com.qlangtech.tis.plugin.ontology.OntologyValueType;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIConstants;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIResult;
import com.qlangtech.tis.plugin.ontology.chatbi.ChatBIService;
import com.qlangtech.tis.plugin.ontology.chatbi.QueryResult;
import com.qlangtech.tis.plugin.ontology.chatbi.TraceStep;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.linker.LinkResources;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding;
import com.qlangtech.tis.plugin.ontology.impl.synonyms.SynonymsElement;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.qlangtech.tis.plugin.ontology.Ontology.KEY_CREATE_TIME;
import static com.qlangtech.tis.plugin.ontology.Ontology.KEY_DESCRIPTION;
import static com.qlangtech.tis.plugin.ontology.OntologyDomain.NAME_ONTOLOGY_DOMAIN;
import static com.qlangtech.tis.plugin.ontology.OntologyObjectType.KEY_OBJECT_TYPE;
import static com.qlangtech.tis.util.DescriptorsJSON.KEY_DISPLAY_NAME;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/15
 */
@SuppressWarnings("all")
public class OntologyAction extends BasicModule {

  private Pager pager;

  public Pager getPager() {
    if (pager == null) {
      pager = this.createPager();
    }
    return pager;
  }

  public void doChatbiQuery(Context context) {
    final String ontologyName = getDomain();


    //    ManipulatePluginCacheRegister.TemplateManipulateStore<OntologyDomainManipulate> manipulateStore =
    //      OntologyDomainManipulate.getManipulateStore(ontologyName, false);
    ChatBIService chatBI = null;
    for (OntologyDomainManipulate m : OntologyDomain.getDomainManiplidateList(ontologyName)) {
      if (m instanceof ChatBIService bi) {
        chatBI = bi;
        break;
      }
    }
    Objects.requireNonNull(chatBI, "instance of " + ChatBIService.class.getSimpleName() + " can not be null");

    final String nlq = this.getString(ChatBITool.KEY_NLQ);
    SSEEventWriter sseWriter = this.getEventStreamWriter();

    ChatBIResult result = chatBI.ask(ontologyName, nlq, (TraceStep step) -> {
      JSONObject json = new JSONObject();
      json.put(ChatBIConstants.FIELD_STEP, step.step());
      json.put(ChatBIConstants.FIELD_OK, step.ok());
      json.put(ChatBIConstants.FIELD_MESSAGE, step.message());
      json.put(ChatBIConstants.FIELD_MILLIS, step.millis());
      if (step.data() != null) {
        json.put(ChatBIConstants.FIELD_DATA, createUdfDescs(step.data()));
      }
      sseWriter.writeSSEEvent(SSERunnable.SSEEventType.LLM_CHAT_BI_STEP_RECORD, json);
    });

    JSONObject resultJson = new JSONObject();
    resultJson.put(ChatBIConstants.FIELD_SUCCESS, result.isSuccess());
    resultJson.put(ChatBIConstants.FIELD_SQL, result.sql());
    resultJson.put(ChatBIConstants.FIELD_ERROR, result.error());
    QueryResult qr = result.data();
    if (qr != null) {
      resultJson.put(ChatBIConstants.FIELD_COLUMNS, qr.columns());
      resultJson.put(ChatBIConstants.FIELD_ROWS, qr.rows());
      resultJson.put(ChatBIConstants.FIELD_ROW_COUNT, qr.rowCount());
      resultJson.put(ChatBIConstants.FIELD_TRUNCATED, qr.truncated());
      resultJson.put(ChatBIConstants.FIELD_ACTUAL_ROWS, qr.actualRows());
    }
    sseWriter.writeSSEEvent(SSERunnable.SSEEventType.AI_AGNET_DONE, resultJson);
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

    // Ontology.OntologyEnum.Linker
    biz.put("linkerDesc",
      new DefaultDescriptorsJSON<>(pluginMeta.getDelegate().getHeteroEnum().descriptors().stream().filter((desc) -> {
        return ((Ontology.BasicDesc) desc).getOntologyType() == Ontology.OntologyEnum.Linker;
      }).toList()).getDescriptorsJSON());

    biz.put(ObjectTypeBinding.KEY_BOUND_DATASOURCE, objectType.getObjectTypeBindingInfo());
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

    Pair<OntologyDomain, IPluginStore<OntologyDomain>> domainPair = OntologyDomain.load(ontologyName);

    result.put("manipulateMetas",
      Objects.requireNonNull(domainPair, "domainPair can not be null").getKey().convertPojo().getManipulateMetas());

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
      List<String> pks = ot.getCols().stream().filter((col) -> col.isPk()).map((col) -> col.getName()).toList();
      obj.put("pks", pks);
      obj.put(ObjectTypeBinding.KEY_BOUND_DATASOURCE, ot.getObjectTypeBindingInfo());
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
    }).sorted((d1, d2) -> d2.getUpdateTime().compareTo(d1.getUpdateTime())).toList()));
  }

  // ================================================================
  //  ChatBI 状态页 API
  // ================================================================

  /**
   * ChatBI 调用统计：成功/失败总数 + 最近 14 天每日调用量。
   * emethod=get_chatbi_stats&action=ontology_action&ontology={domain}
   */
  public void doGetChatbiStats(Context context) {
    final String domain = getDomain();
    File traceDir = ChatBIConstants.getOntologyDoaminTraceDir(domain);// new File(Config.getDataDir(),
    // "chatbi/trace/" + domain);

    long successCount = 0;
    long failCount = 0;
    // key = "yyyyMMdd"，按日累计
    TreeMap<String, Long> dailyMap = new TreeMap<>();

    // 初始化最近 7 天的 key，确保无数据的日期也出现在结果中
    LocalDate today = LocalDate.now();
    // DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

    for (int i = 6; i >= 0; i--) {
      dailyMap.put(today.minusDays(i).format(TimeFormat.yyyy_MM_dd.timeFormatter), 0L);
    }

    if (traceDir.exists() && traceDir.isDirectory()) {
      File[] files = traceDir.listFiles((d, name) -> name.endsWith(".jsonl"));
      if (files != null) {
        // 文件名格式：yyyyMMddHHmmss-{uuid}.jsonl
        for (File f : files) {
          String name = f.getName();
          if (name.length() < 8)
            continue;
          String dateKey = name.substring(0, 8);

          boolean success = false;
          boolean hasExecute = false;
          boolean hasError = false;
          try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
              if (line.contains("\"step\":\"execute\"") || line.contains("\"step\": \"execute\"")) {
                hasExecute = true;
              }
              if (line.contains("\"step\":\"error\"") || line.contains("\"step\": \"error\"")) {
                hasError = true;
              }
            }
            success = hasExecute && !hasError;
          } catch (IOException e) {
            // 跳过无法读取的文件
            continue;
          }

          if (success) {
            successCount++;
          } else {
            failCount++;
          }
          // 仅统计最近 14 天
          if (dailyMap.containsKey(dateKey)) {
            dailyMap.merge(dateKey, 1L, Long::sum);
          }
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("successCount", successCount);
    result.put("failCount", failCount);
    result.put("totalCount", successCount + failCount);

    JSONArray daily = new JSONArray();
    for (Map.Entry<String, Long> e : dailyMap.entrySet()) {
      JSONObject item = new JSONObject();
      item.put("date", e.getKey());
      item.put("count", e.getValue());
      daily.add(item);
    }
    result.put("daily", daily);
    this.setBizResult(context, result);
  }

  /**
   * Neo4j 图谱同步状态：各类节点数量（Neo4j 实际值 vs XML 配置值）+ 最后同步时间。
   * emethod=get_neo4j_stats&action=ontology_action&ontology={domain}
   */
  public void doGetNeo4jStats(Context context) {
    final String domain = getDomain();

    // XML 侧计数
    int xmlOtCount = OntologyObjectType.loadAll(domain).size();
    int xmlLinkerCount = Ontology.loadAllLinkers(domain).size();
    int xmlGlossaryCount = Ontology.loadAllGlossary(domain).size();
    int xmlSpCount = Ontology.loadAllSharedProperties(domain).size();
    int xmlVtCount = Ontology.loadAllValueTypes(domain).size();

    // 汇总 Property 数（需逐 OT 累加）
    int xmlPropertyCount = OntologyObjectType.loadAll(domain).stream()
      .mapToInt(ot -> ot.getCols().size()).sum();

    JSONObject xmlObj = new JSONObject();
    xmlObj.put("objectTypes", xmlOtCount);
    xmlObj.put("properties", xmlPropertyCount);
    xmlObj.put("linkers", xmlLinkerCount);
    xmlObj.put("glossaries", xmlGlossaryCount);
    xmlObj.put("sharedProperties", xmlSpCount);
    xmlObj.put("valueTypes", xmlVtCount);

    // Neo4j 侧计数（若 Neo4j 未启动则返回全 0）
    JSONObject neo4jObj = new JSONObject();
    Ontology.queryGraphStats(domain).ifPresentOrElse(s -> {
      neo4jObj.put("objectTypes", s.objectTypeCount());
      neo4jObj.put("properties", s.propertyCount());
      neo4jObj.put("linkers", s.linkerCount());
      neo4jObj.put("glossaries", s.glossaryCount());
      neo4jObj.put("sharedProperties", s.sharedPropertyCount());
      neo4jObj.put("valueTypes", s.valueTypeCount());
      neo4jObj.put("lastSyncAt", s.lastSyncAt());
      neo4jObj.put("available", true);
    }, () -> {
      neo4jObj.put("objectTypes", 0);
      neo4jObj.put("properties", 0);
      neo4jObj.put("linkers", 0);
      neo4jObj.put("glossaries", 0);
      neo4jObj.put("sharedProperties", 0);
      neo4jObj.put("valueTypes", 0);
      neo4jObj.put("lastSyncAt", 0);
      neo4jObj.put("available", false);
    });

    JSONObject result = new JSONObject();
    result.put("xml", xmlObj);
    result.put("neo4j", neo4jObj);
    this.setBizResult(context, result);
  }

  /**
   * 获取本体知识图谱数据：返回所有实体实例节点及其关系边，用于前端可视化。
   * emethod=get_ontology_graph&action=ontology_action&ontology={domain}
   */
  public void doGetOntologyGraph(Context context) {
    final String domain = getDomain();

    UploadPluginMeta.putPluginMeta(context,
      OntologyPluginMeta.create(Ontology.OntologyEnum.ObjectType, domain).getDelegate());

    JSONArray nodes = new JSONArray();
    JSONArray edges = new JSONArray();

    // ── 1. ObjectType 节点 ──
    List<OntologyObjectType> objTypes = OntologyObjectType.loadAll(domain);
    for (OntologyObjectType ot : objTypes) {
      JSONObject node = new JSONObject();
      node.put("id", "ot:" + ot.getName());
      node.put("label", ot.getName());
      node.put("nodeType", "ObjectType");
      node.put("colSize", ot.getCols().size());
      nodes.add(node);

      // ── 2. Property 节点 + OT→Property 边 ──
      for (com.qlangtech.tis.plugin.ontology.OntologyProperty prop : ot.getCols()) {
        String propId = "prop:" + ot.getName() + ":" + prop.getName();
        JSONObject pNode = new JSONObject();
        pNode.put("id", propId);
        pNode.put("label", prop.getName());
        pNode.put("nodeType", "Property");
        pNode.put("ownerOT", ot.getName());
        // typeRef: 优先判断 SharedProperty 引用，再判断 ValueType 引用
        com.qlangtech.tis.plugin.ontology.OntologyPropertyTypeRef typeRef = prop.getPropertyTypeRef();
        java.util.Optional<String> sharedRef = typeRef != null
          ? typeRef.getSharedPropertyRef() : java.util.Optional.<String>empty();
        java.util.Optional<String> valueRef = typeRef != null
          ? typeRef.getValueTypeRef() : java.util.Optional.<String>empty();
        if (sharedRef.isPresent()) {
          pNode.put("typeRef", sharedRef.get());
          pNode.put("typeRefKind", "SharedProperty");
        } else if (valueRef.isPresent()) {
          pNode.put("typeRef", valueRef.get());
          pNode.put("typeRefKind", "ValueType");
        }
        if (typeRef != null && typeRef.getOntologyType() != null) {
          pNode.put("ontologyType", typeRef.getOntologyType());
        }
        nodes.add(pNode);

        JSONObject e = new JSONObject();
        e.put("source", "ot:" + ot.getName());
        e.put("target", propId);
        e.put("edgeType", "HAS_PROPERTY");
        edges.add(e);

        // Property → SharedProperty 边
        if (sharedRef.isPresent()) {
          JSONObject spEdge = new JSONObject();
          spEdge.put("source", propId);
          spEdge.put("target", "sp:" + sharedRef.get());
          spEdge.put("edgeType", "TYPE_REF");
          edges.add(spEdge);
        }
        // Property → ValueType 边
        if (valueRef.isPresent()) {
          JSONObject vtEdge = new JSONObject();
          vtEdge.put("source", propId);
          vtEdge.put("target", "vt:" + valueRef.get());
          vtEdge.put("edgeType", "TYPE_REF");
          edges.add(vtEdge);
        }
      }
    }

    // ── 3. Linker 节点 + OT↔OT 关系边 ──
    List<OntologyLinker> linkers = Ontology.loadAllLinkers(domain);
    for (OntologyLinker linker : linkers) {
      try {
        String linkerName = linker.identityValue();
        String linkerId = "lk:" + linkerName;

        JSONObject lNode = new JSONObject();
        lNode.put("id", linkerId);
        lNode.put("label", linkerName);
        lNode.put("nodeType", "Linker");
        lNode.put("endType", linker.getLinkTypeEnd().getVal());
        nodes.add(lNode);

        LinkResources.ObjectLinkerPair pair = linker.getLinkResourcesStep().getLinks();
        LinkResources.ObjectLinkInfo left = pair.left();
        LinkResources.ObjectLinkInfo right = pair.right();

        // source OT → Linker 边
        JSONObject e1 = new JSONObject();
        e1.put("source", "ot:" + left.source());
        e1.put("target", linkerId);
        e1.put("edgeType", "LINK_SOURCE");
        e1.put("sourceField", left.sourceField());
        edges.add(e1);

        // Linker → target OT 边
        JSONObject e2 = new JSONObject();
        e2.put("source", linkerId);
        e2.put("target", "ot:" + right.target());
        e2.put("edgeType", "LINK_TARGET");
        e2.put("targetField", right.targetField());
        edges.add(e2);

      } catch (Exception ex) {
        // 跳过无法解析的 Linker
      }
    }

    // ── 4. SharedProperty 节点 ──
    List<OntologySharedProperty> sharedProperties = Ontology.loadAllSharedProperties(domain);
    for (OntologySharedProperty sp : sharedProperties) {
      JSONObject node = new JSONObject();
      node.put("id", "sp:" + sp.name);
      node.put("label", sp.name);
      node.put("nodeType", "SharedProperty");
      node.put("alias", sp.alias);
      node.put("ontologyType", sp.getOntologyType());
      nodes.add(node);
    }

    // ── 5. ValueType 节点 ──
    List<OntologyValueType> valueTypes = Ontology.loadAllValueTypes(domain);
    for (OntologyValueType vt : valueTypes) {
      JSONObject node = new JSONObject();
      String vtName = vt.identityValue();
      node.put("id", "vt:" + vtName);
      node.put("label", vtName);
      node.put("nodeType", "ValueType");
      OntologyValueType.IMetadataOfValueType meta = vt.getMeta();
      node.put("ontologyType", meta.ontologyType());
      node.put("description", meta.getDescription());
      nodes.add(node);
    }

    // ── 6. Glossary 节点 + Glossary→OT/Property 边 ──
    List<OntologyGlossary> glossaries = Ontology.loadAllGlossary(domain);
    for (OntologyGlossary glossary : glossaries) {
      String glossaryId = "gl:" + glossary.term;
      JSONObject node = new JSONObject();
      node.put("id", glossaryId);
      node.put("label", glossary.term);
      node.put("nodeType", "Glossary");
      node.put("description", glossary.description);
      nodes.add(node);

      // Glossary → target 边
      if (glossary.target != null) {
        String targetLiteral = glossary.target.getTargetLiteral();
        if (StringUtils.isNotEmpty(targetLiteral)) {
          JSONObject gEdge = new JSONObject();
          gEdge.put("source", glossaryId);
          // targetLiteral 格式: "OT名" 或 "OT名.Property名"
          if (targetLiteral.contains(".")) {
            String[] parts = targetLiteral.split("\\.", 2);
            gEdge.put("target", "prop:" + parts[0] + ":" + parts[1]);
          } else {
            gEdge.put("target", "ot:" + targetLiteral);
          }
          gEdge.put("edgeType", "GLOSSARY_TARGET");
          edges.add(gEdge);
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("nodes", nodes);
    result.put("edges", edges);
    this.setBizResult(context, result);
  }

  private static final int TRACE_PAGE_SIZE = 20;

  /**
   * 获取历史查询记录（去重，用于查询输入框的快捷标签）。
   * emethod=get_chatbi_history&action=ontology_action&ontology={domain}&limit=20
   */
  public void doGetChatbiHistory(Context context) {
    final String domain = getDomain();
    int limit = this.getInt("limit", 20);
    if (limit < 1)
      limit = 20;
    if (limit > 100)
      limit = 100;

    File traceDir = ChatBIConstants.getOntologyDoaminTraceDir(domain);//new File(Config.getDataDir(), "chatbi/trace/"
    // + domain);
    List<String> uniqueQueries = new ArrayList<>();
    java.util.Set<String> seen = new java.util.LinkedHashSet<>();

    if (traceDir.exists() && traceDir.isDirectory()) {
      File[] files = traceDir.listFiles((d, name) -> name.endsWith(".jsonl"));
      if (files != null && files.length > 0) {
        // 按文件名倒序（时间倒序）
        Arrays.sort(files, Comparator.comparing(File::getName).reversed());

        for (File f : files) {
          if (seen.size() >= limit)
            break;

          try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
              com.alibaba.fastjson.JSONObject header = com.alibaba.fastjson.JSON.parseObject(headerLine);
              String nlq = header.getString("nlq");
              if (StringUtils.isNotBlank(nlq) && !seen.contains(nlq)) {
                seen.add(nlq);
                uniqueQueries.add(nlq);
              }
            }
          } catch (Exception e) {
            // 跳过无法读取的文件
          }
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("queries", uniqueQueries);
    result.put("total", uniqueQueries.size());
    this.setBizResult(context, result);
  }

  /**
   * 历史 Trace 列表（倒序，分页）。
   * emethod=get_chatbi_traces&action=ontology_action&ontology={domain}&page=1
   */
  public void doGetChatbiTraces(Context context) {
    final String domain = getDomain();
    int page = this.getInt("page", 1);
    if (page < 1)
      page = 1;

    File traceDir = ChatBIConstants.getOntologyDoaminTraceDir(domain);// new File(Config.getDataDir(),
    // "chatbi/trace/" + domain);
    List<JSONObject> items = new ArrayList<>();
    int total = 0;

    if (traceDir.exists() && traceDir.isDirectory()) {
      File[] files = traceDir.listFiles((d, name) -> name.endsWith(".jsonl"));
      if (files != null && files.length > 0) {
        // 文件名字典序倒序 = 时间倒序
        Arrays.sort(files, Comparator.comparing(File::getName).reversed());
        total = files.length;

        int from = (page - 1) * TRACE_PAGE_SIZE;
        int to = Math.min(from + TRACE_PAGE_SIZE, files.length);

        for (int i = from; i < to; i++) {
          File f = files[i];
          JSONObject item = parseTraceSummary(f);
          if (item != null)
            items.add(item);
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("total", total);
    result.put("pageSize", TRACE_PAGE_SIZE);
    result.put("items", items);
    this.setBizResult(context, result);
  }

  /**
   * 获取单条 Trace 的完整步骤明细。
   * emethod=get_chatbi_trace_detail&action=ontology_action&ontology={domain}&reqId={reqId}
   */
  public void doGetChatbiTraceDetail(Context context) {
    final String domain = getDomain();
    final String reqId = this.getString(ChatBIConstants.FIELD_REQ_ID);
    if (StringUtils.isEmpty(reqId)) {
      throw new IllegalArgumentException("param reqId can not be empty");
    }

    File traceFile = new File(ChatBIConstants.getOntologyDoaminTraceDir(domain), reqId + ".jsonl");
    if (!traceFile.exists()) {
      this.addErrorMessage(context, "Trace file not found: " + reqId);
      return;
    }

    JSONObject result = new JSONObject();
    JSONArray steps = new JSONArray();
    String nlq = null;
    long timestamp = 0L;

    try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
      // 首行是 header
      String headerLine = br.readLine();
      if (headerLine != null) {
        com.alibaba.fastjson.JSONObject header = com.alibaba.fastjson.JSON.parseObject(headerLine);
        nlq = header.getString(ChatBIConstants.FIELD_NLQ);
        timestamp = header.getLongValue(ChatBIConstants.FIELD_TIMESTAMP);
      }
      // 后续每行是 TraceStep
      String line;
      while ((line = br.readLine()) != null) {
        if (StringUtils.isBlank(line))
          continue;
        JSONObject row = JSON.parseObject(line);
        JSONObject traceData = null;
        if ((traceData = row.getJSONObject(ChatBIConstants.FIELD_DATA)) != null) {
          row.put(ChatBIConstants.FIELD_DATA, createUdfDescs(traceData));
        }
        steps.add(row);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read trace file: " + reqId, e);
    }

    result.put(ChatBIConstants.FIELD_REQ_ID, reqId);
    result.put(ChatBIConstants.FIELD_NLQ, nlq);
    result.put(ChatBIConstants.FIELD_TIMESTAMP, timestamp);
    result.put(ChatBIConstants.FIELD_STEPS, steps);
    this.setBizResult(context, result);
  }

  private List<UDFDesc> createUdfDescs(JSONObject traceData) {
    List<UDFDesc> literia = Lists.newArrayList();
    for (Map.Entry<String, Object> entry : traceData.entrySet()) {
      Object val = entry.getValue();
      UDFDesc desc = null;
      if (val instanceof JSONObject s) {
        desc = new UDFDesc(entry.getKey(), JsonUtil.toString(s, false));
      } else if (val instanceof JSONArray s) {
        desc = new UDFDesc(entry.getKey(), JsonUtil.toString(s, false));
      } else {
        desc = new UDFDesc(entry.getKey(), String.valueOf(val));
      }
      literia.add(desc);
    }
    return literia;
  }

  /**
   * 解析 trace 文件，提取摘要信息（仅读头行 + 扫 step 类型）。
   */
  private JSONObject parseTraceSummary(File f) {
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String headerLine = br.readLine();
      if (headerLine == null)
        return null;

      com.alibaba.fastjson.JSONObject header = com.alibaba.fastjson.JSON.parseObject(headerLine);
      String reqId = header.getString(ChatBIConstants.FIELD_REQ_ID);
      String nlq = header.getString(ChatBIConstants.FIELD_NLQ);
      long timestamp = header.getLongValue(ChatBIConstants.FIELD_TIMESTAMP);

      boolean hasExecute = false;
      boolean hasError = false;
      int llmCount = 0;
      long llmMs = 0;
      long executeMs = 0;

      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank())
          continue;
        com.alibaba.fastjson.JSONObject step = com.alibaba.fastjson.JSON.parseObject(line);
        String stepName = step.getString(ChatBIConstants.FIELD_STEP);
        if (stepName == null)
          continue;
        switch (stepName) {
          case ChatBIConstants.STEP_LLM -> {
            llmCount++;
            llmMs += step.getLongValue(ChatBIConstants.FIELD_MILLIS);
          }
          case ChatBIConstants.STEP_EXECUTE -> {
            hasExecute = true;
            executeMs = step.getLongValue(ChatBIConstants.FIELD_MILLIS);
          }
          case ChatBIConstants.STEP_ERROR -> hasError = true;
        }
      }

      JSONObject item = new JSONObject();
      item.put(ChatBIConstants.FIELD_REQ_ID, reqId);
      item.put(ChatBIConstants.FIELD_NLQ, nlq);
      item.put(ChatBIConstants.FIELD_TIMESTAMP, timestamp);
      item.put(ChatBIConstants.FIELD_SUCCESS, hasExecute && !hasError);
      item.put(ChatBIConstants.FIELD_RETRY_CNT, Math.max(0, llmCount - 1));
      item.put(ChatBIConstants.FIELD_LLM_MS, llmMs);
      item.put(ChatBIConstants.FIELD_EXECUTE_MS, executeMs);
      return item;
    } catch (Exception e) {
      return null;
    }
  }


}
